import { createApp } from "vue";
import App from "./App.vue";
import English from "./translation/English.json";
import Deutsch from "./translation/Deutsch.json";
import French from "./translation/French.json";
import { createI18n } from "vue-i18n";

const i18n = createI18n({
  messages: {
    English,
    Deutsch,
    French
  },
  fallbackLocale: "English",
});

const app =  createApp(App)
app.use(i18n);
app.mount("#app");
