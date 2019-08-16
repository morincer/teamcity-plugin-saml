import Vue from 'vue';
import Router from 'vue-router';
import AdminDemo from './views/AdminDemo.vue';

Vue.use(Router);

export default new Router({
    routes: [
        {
            path: '/demo',
            name: 'AdminPage',
            component: AdminDemo,
        },
    ],
});
