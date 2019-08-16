import Vue from 'vue';
import Router from 'vue-router';
import AdminDemo from './views/AdminDemo.vue';
import SamlPluginSettings from "@/views/SamlPluginSettings.vue";

Vue.use(Router);

export default new Router({
    routes: [
        {
            path: '/',
            name: "SamlPluginSettings",
            component: SamlPluginSettings
        },
        {
            path: '/demo',
            name: 'AdminPage',
            component: AdminDemo,
        },
    ],
});
