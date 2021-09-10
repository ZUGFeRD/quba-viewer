const config = require("./app.config");
const path = require("path");
const Store = require('electron-store');
const store = new Store();
let currentLanguage = store.get("language") || config.fallbackLng;

const i18nextOptions = {
  debug :false,
  lng : currentLanguage,
  lngs : config.languages,
  supportedLngs : config.languages,
  ns : "translation",
  defaultsNS : "translation",
  fallbackLng : config.fallbackLng,
  saveMissing: true,
  backend: {
    loadPath: path.join(__dirname, '..',  'locales', '{{lng}}','{{ns}}.json'),
    addPath: path.join(__dirname, '..',  'locales', '{{lng}}','{{ns}}.missing.json'),
  },
};

module.exports = i18nextOptions;

