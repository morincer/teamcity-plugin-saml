var samlAdminSettingsFormHandler = {
    formElement: null,
    actionUrl: ""
};

samlAdminSettingsFormHandler.bind = function (formElement, actionUrl) {
    samlAdminSettingsFormHandler.formElement = formElement.get(0);
    samlAdminSettingsFormHandler.actionUrl = $(formElement).prop("action");

    formElement
        .find('input[id]')
        .each(function (index, el) {
            var id = el.id;
            var upperCasedName = id.charAt(0).toUpperCase() + id.substring(1);
            var funcName = "on" + upperCasedName + "Error";
            samlAdminSettingsFormHandler.listener[funcName] = function (elem) {
                var errorElementId = "error" + upperCasedName;
                var fieldElement = jQuery('#' + id);
                var errorElement = jQuery('#' + errorElementId);

                if (errorElement.length === 0) {
                    errorElement = jQuery('<span class="error" id="' + errorElementId + '"></span>');
                    fieldElement.after(errorElement);
                }

                errorElement.html(elem.firstChild.nodeValue);
                samlAdminSettingsFormHandler.form.highlightErrorField(fieldElement.get(0));
            }
        });
};

samlAdminSettingsFormHandler.form = OO.extend(BS.AbstractWebForm, {
    formElement: function () {
        return samlAdminSettingsFormHandler.formElement;
    }
});

samlAdminSettingsFormHandler.listener = OO.extend(BS.ErrorsAwareListener);

samlAdminSettingsFormHandler.saver = OO.extend(BS.FormSaver);

samlAdminSettingsFormHandler.save = function () {
    BS.FormSaver.save(
        samlAdminSettingsFormHandler.form,
        samlAdminSettingsFormHandler.actionUrl,
        samlAdminSettingsFormHandler.listener,
        false
    );
};