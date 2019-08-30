import Vue from "vue";
import App from "./App.vue";
import router from "./router";
import store from "./store";
import "./teamcity-assets";
import {buildDependencies} from "@/main.dependencies";

Vue.config.productionTip = false;

buildDependencies();

new Vue({
    router,
    store,
    render: (h) => h(App),
}).$mount("#app");
