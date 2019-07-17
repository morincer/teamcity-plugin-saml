import Vue from 'vue'
import Router from 'vue-router'
import Settings from "@/views/Settings.vue";

Vue.use(Router);

export default new Router({
  routes: [
    {
      path: '/',
      component: Settings,
    }
  ]
})
