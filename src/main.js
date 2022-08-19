import { createApp } from "vue";
import App from "./App.vue";
import en from "./translation/en.json";
import de from "./translation/de.json";
import fr from "./translation/fr.json";
import { createI18n } from "vue-i18n";
import VueSweetalert2 from 'vue-sweetalert2';
import 'sweetalert2/dist/sweetalert2.min.css';

const i18n = createI18n({
  messages: {
    en,
    de,
    fr
  },
  fallbackLocale: "en",
});

const app =  createApp(App)
app.use(i18n);
app.use(VueSweetalert2);
app.mount("#app");
