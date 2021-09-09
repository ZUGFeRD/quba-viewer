import { createApp } from "vue";
import App from "./App.vue";
import en from "./translation/en.json";
import de from "./translation/de.json";
import { createI18n } from "vue-i18n";

const i18n = createI18n({
  messages: {
    en,
    de,
  },
  fallbackLocale: "en",
});

const app =  createApp(App)
app.use(i18n);
app.mount("#app");
