<template>
    <div id="bodyWrapper">
        <div class="ring-header"></div> <!-- Ring placeholder, do not remove -->
        <div id="topWrapper" class="with-border">
            <div class="headBG clearfix">
                <div class="fixedWidth">

                    <a href="/" class="headerLogo"><i class="headerLogoImg"></i></a>

                    <div id="tabsContainer" style="display: block;">
                        <ul class="tabs">
                            <li id="overview_Tab" class="noCounter first">
                                <p>
                                    <a class="tabs"
                                       showdiscardchangesmessage="false"
                                       href="/">Home</a>
                                </p>
                            </li>
                        </ul>
                    </div>
                </div>
                <div id="header-ready"></div>
            </div>

            <div class="fixedWidth clearfix">
                <h1 style="font-weight: 500;">Teamcity Admin Page</h1>
            </div>
        </div>
        <div id="mainContent" class="fixedWidth clearfix" style="padding-bottom: 111px;">
            <div id="content">
                <table id="admin-container">
                    <tbody>
                    <tr>
                        <td class="user-profile admin-sidebar compact">
                            <div class="item active">
                                <a href="/">Home</a>
                            </div>
                            <div class="admin-menu__bg"></div>
                        </td>
                        <td class="admin-content" style="width: 100%;">
                            <div id="adminBreadcrumbsWrapper">
                                <div class="fixedWidth clearfix" id="breadcrumbsWrapper">
                                    <div id="restBreadcrumbs" style="display: block;">
                                        <div class="restPageTitleWrapper">
                                            <div id="restPageTitle" style="display: block;">
                                                <div class="selected"><span class="contentWrapper ">General</span></div>
                                                <div class="breadcrumbHealthIndicators"></div>
                                            </div>
                                        </div>
                                        <div id="restPageDescription"></div>
                                    </div>
                                </div>
                                <div id="tabsContainer4" class="simpleTabs clearfix"></div>
                                <div>
                                    <!-- PUT YOUR CONTENT HERE -->
                                    <div class="settingsBlockContent">
                                        <slot></slot>
                                    </div>
                                </div>
                            </div>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
    import Vue from "vue";
    import Component from "vue-class-component";

    @Component({})
export default class AdminPage extends Vue {
}
</script>

<style scoped>

</style>
