const config = require("./app.config");
const path = require("path");
const Store = require('electron-store');
const {app} = require("electron");
const store = new Store();
let eligibleSysLanguage=null;

if ((app.getLocale()=="de")||(app.getLocale()=="en")||(app.getLocale()=="fr")) {
  eligibleSysLanguage = app.getLocale();
}
let currentLanguage = store.get("language") || eligibleSysLanguage || config.fallbackLng;

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

