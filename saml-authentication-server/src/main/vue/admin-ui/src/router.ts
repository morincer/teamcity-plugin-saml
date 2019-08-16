import Vue from 'vue'
import Router from 'vue-router'
import Settings from "@/views/Settings.vue";
import NewSAMLConnectionWizard from "@/components/NewSAMLConnectionWizard.vue";

Vue.use(Router);

export default new Router({
  routes: [
    {
      path: '/',
      component: Settings,
    },
    {
      path: '/new',
      component: NewSAMLConnectionWizard
    }
  ]
})
