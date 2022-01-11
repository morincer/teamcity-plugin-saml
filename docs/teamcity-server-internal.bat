@echo off
:: This script should not be called directly, use teamcity-server.bat instead

:: Handles almost all server commands except 'start' one

if not defined TEAMCITY_SERVER_SCRIPT (
  echo This script should not be called directly, use teamcity-server.bat instead
  exit /b 2
)

SET TEAMCITY_SERVER_CURRENT_DIR=%CD%
cd /d %~dp0

if not exist teamcity-server.bat goto errCwd

if exist "%TEAMCITY_LOGS_PATH%" goto has_logs
mkdir "%TEAMCITY_LOGS_PATH%"
:has_logs

:: Unset _RUNJDB and _RUNJAVA
:: The path values in these variables are set in setclasspath.bat inside quotes
:: This leads to an error in setclasspath.bat in if statements that check if the values are empty
:: The issue is observed only when the batch file is called inside its child process (i.e. in TeamCity Server during upgrade/restart) and any of the path values contains a space
SET _RUNJDB=
SET _RUNJAVA=

SET QUIET=0

:command
if ""%1"" == ""start"" goto run
if ""%1"" == ""run"" goto run
if ""%1"" == ""stop"" goto run
if ""%1"" == ""configure"" goto run
if ""%1"" == ""status"" goto pre_run_status
if ""%1"" == ""service"" goto service
if ""%1"" == ""usage"" goto usage
goto usage_error

:pre_run_status
if not ""%2"" == ""short"" goto run
SET QUIET=1
goto run

:: service section should not be removed to preserve compatibility with teamcity-server.bat from previous versions
:service
shift
SET TEAMCITY_SERVICE_COMMAND=%1
IF NOT "%1"=="install" IF NOT "%1"=="delete" goto usage_error
shift
TeamCityService.exe %TEAMCITY_SERVICE_COMMAND% /settings=..\conf\teamcity-server-service.xml "/logfile=%TEAMCITY_LOGS_PATH%\teamcity-winservice.log" %*
IF NOT ERRORLEVEL 1 GOTO service_succeeded
:: service failed to complete here
IF "%TEAMCITY_SERVICE_COMMAND%" == "install" ECHO Call teamcity-server.bat usage for usage details
IF "%TEAMCITY_SERVICE_COMMAND%" == "install" ECHO Service logs are in %TEAMCITY_LOGS_PATH%\teamcity-winservice.log
SET TEAMCITY_SERVICE_COMMAND=
goto done_failed

:: service command call succeeded
:service_succeeded
IF "%TEAMCITY_SERVICE_COMMAND%" == "install" ECHO Service logs are in %TEAMCITY_LOGS_PATH%\teamcity-winservice.log
SET TEAMCITY_SERVICE_COMMAND=
goto done_succeeded

:run
IF NOT "%QUIET%"=="1" ECHO Looking for installed Java...
if exist ..\jre SET JRE_HOME=%cd%\..\jre
set "FJ_MIN_UNSUPPORTED_JAVA_VERSION=12"
set "FJ_LOOK_FOR_SERVER_JAVA=1"
:: Uncomment next line to search only for x64 JDK
::set "FJ_LOOK_FOR_X64_JAVA=1"
CALL "%cd%\findJava.bat" "1.8" "%TEAMCITY_JRE%" "%cd%\..\jre"
IF ERRORLEVEL 0 GOTO java_search_done
IF NOT "%QUIET%"=="1" ECHO Java not found. Cannot start TeamCity server. Please ensure JDK or JRE is installed and JAVA_HOME environment variable points to it.
GOTO done_failed
:java_search_done
IF NOT "%QUIET%"=="1" ECHO Java executable is found: '%FJ_JAVA_EXEC%'

:: obsolete init. Use TEAMCITY_PREPARE_SCRIPT environment variable instead
if not exist "%cd%\teamcity-init.bat" goto no_teamcity_init
CALL "%cd%\teamcity-init.bat"
:no_teamcity_init

if not "%TEAMCITY_SERVER_MEM_OPTS%" == "" goto server_mem_opts_set
:: Default options suitable for product evaluation
SET TEAMCITY_SERVER_MEM_OPTS_ACTUAL=-Xmx4096m

goto server_mem_opts_done

:server_mem_opts_set
SET TEAMCITY_SERVER_MEM_OPTS_ACTUAL=%TEAMCITY_SERVER_MEM_OPTS%

:server_mem_opts_done
SET CATALINA_OPTS=%TEAMCITY_SERVER_OPTS%
SET CATALINA_OPTS=%CATALINA_OPTS% -server
IF ""%2"" == ""service"" SET CATALINA_OPTS=%CATALINA_OPTS% -Xrs
SET CATALINA_OPTS=%CATALINA_OPTS% %TEAMCITY_SERVER_MEM_OPTS_ACTUAL% -Dteamcity.configuration.path=../conf/teamcity-startup.properties -Dlog4j.configuration=file:../conf/teamcity-server-log4j.xml "-Dteamcity_logs=%TEAMCITY_LOGS_PATH%"
SET CATALINA_HOME=%cd%\..
SET CATALINA_BASE=%cd%\..

:: Add the Java 9 specific parameter, required by some TeamCity inner functionality
SET JDK_JAVA_OPTIONS=%JDK_JAVA_OPTIONS% --add-opens jdk.management/com.sun.management.internal=ALL-UNNAMED -XX:+IgnoreUnrecognizedVMOptions

:: console window title for catalina
set TITLE=TeamCity Server

if "%TEAMCITY_PREPARE_SCRIPT%" == "" goto skip_prepare
if "%TEAMCITY_START_COMMAND%" == "" set "TEAMCITY_START_COMMAND=%1"
call "%TEAMCITY_PREPARE_SCRIPT%" %TEAMCITY_START_COMMAND% %2 %3 %4 %5 %6 %7 %8 %9
:skip_prepare
set "TEAMCITY_START_COMMAND="

SET QUIET=

if ""%1"" == ""configure"" goto run_configurator
if ""%1"" == ""status"" goto run_configurator
goto run_catalina

:run_configurator
"%FJ_JAVA_EXEC%" %JAVA_OPTS% -jar teamcity-server-configurator.jar %*
goto :check_run_result

:dirname file varName
    setlocal ENABLEEXTENSIONS ENABLEDELAYEDEXPANSION
    SET _dir=%~dp1
    SET _dir=%_dir:~0,-1%
    endlocal & set %2=%_dir%
goto:eof

:run_catalina
call :dirname "%FJ_JAVA_EXEC%" java_bin_dir
call :dirname "%java_bin_dir%" JAVA_HOME
set "FJ_JAVA_EXEC="
set "java_bin_dir="
set "JRE_HOME="
call catalina.bat %1

if ""%1"" == ""stop"" (
  if not "%2" == "" if not "%2" == "service" goto wait_after_stop
)

:check_run_result
IF ERRORLEVEL 1 goto done_failed

goto done_succeeded

:errCwd
echo This command should be run from ^<TeamCity home^>/bin directory.
echo.
goto done_failed

:usage_error
echo Error parsing command line.

:usage
echo.
echo Usage:
echo teamcity-server.bat ^<command^>
echo Supported commands:
echo start    Starts the TeamCity server in a new console.
echo run      Starts the TeamCity server in the current console.
echo stop     Sends the stop command to the TeamCity server without waiting for it to stop.
echo          It may take some time for the process to end.
echo stop n
echo          Sends the stop command to the TeamCity server and waits up to n seconds for the process to end.
echo stop n -force
echo          Sends the stop command to the TeamCity server, waits up to n seconds for the process to end, kills the server process if it is still running.
echo restart
echo          Restarts the running TeamCity server.
echo restart n -force
echo          Restarts the running the TeamCity server after stopping it using the 'stop n -force' command.
echo service  Installs or removes the TeamCity server as a Windows service, see below.
echo.
echo Supported "service" command invocations:
echo service install /runAsSystem
echo   Installs the TeamCity server as a Windows service and uses the LOCAL_SYSTEM account.
echo service install /user=^<username^> [/domain=^<domain^>] /password=^<password^>
echo   Installs the TeamCity server as a Windows service under the specified user account.
echo service delete
echo   Removes a previously installed TeamCity server service.
echo.
goto done_failed

:done_succeeded
cd /d %TEAMCITY_SERVER_CURRENT_DIR%
set "FJ_JAVA_EXEC="
exit /b 0
goto completed

:done_failed
cd /d %TEAMCITY_SERVER_CURRENT_DIR%
set "FJ_JAVA_EXEC="
exit /b 1
goto completed

:wait_after_stop

set WAIT_TIME=%2
set CHECK_DELAY=5
set KILL_ATTEMPTS=2

:load_pid

if not exist "%TEAMCITY_PID_FILE_PATH%" goto pid_file_not_exists
set /p TEAMCITY_PID=<"%TEAMCITY_PID_FILE_PATH%"

echo Waiting for server process to finish...

:check_process_running

tasklist /FI "PID eq %TEAMCITY_PID%" 2>NUL | find /I /N "%TEAMCITY_PID%">NUL
if not "%ERRORLEVEL%"=="0" goto server_process_finished
if %WAIT_TIME% LEQ 0 goto wait_timeout_expired

:: timeout is implemented using ping because standard timeout utility does not work in non interactive mode
ping 127.0.0.1 -n "%CHECK_DELAY%" >NUL
set /a "WAIT_TIME=WAIT_TIME-CHECK_DELAY"

goto check_process_running

:server_process_finished
echo Server process finished
goto cleanup_after_finish

:pid_file_not_exists
echo File "%TEAMCITY_PID_FILE_PATH%" does not exist. Cannot wait for process to finish. Exiting.
goto done_succeeded

:wait_timeout_expired
echo Wait timeout expired, server process is still running
if not ""%3"" == ""-force"" goto done_succeeded
:kill_server
echo Killing server process
taskkill /PID %TEAMCITY_PID% /T /F
if not "%ERRORLEVEL%"=="0" goto cannot_kill_process

:: wait for some time, process can still keep locking some resources/files
:wait_for_exit
tasklist /FI "PID eq %TEAMCITY_PID%" 2>NUL | find /I /N "%TEAMCITY_PID%">NUL
if "%ERRORLEVEL%"=="0" (
  echo Waiting for the server process to exit
  ping 127.0.0.1 -n 3 >NUL
  goto wait_for_exit
)

echo Server process killed
:cleanup_after_finish
del "%TEAMCITY_PID_FILE_PATH%"
goto done_succeeded

:cannot_kill_process
if %KILL_ATTEMPTS% LEQ 1 (
  echo Could not kill process with id "%TEAMCITY_PID%". Exiting.
  goto done_failed
)

set /a "KILL_ATTEMPTS=KILL_ATTEMPTS-1"

echo Could not kill process with id "%TEAMCITY_PID%". Will try again. Attempts left: %KILL_ATTEMPTS%.
ping 127.0.0.1 -n 1 >NUL
goto kill_server

:completed
