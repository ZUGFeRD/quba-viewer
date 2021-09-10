const i18n = require("i18next");
const i18nextBackend = require("i18next-node-fs-backend");
const config = require("./app.config");
const Store = require('electron-store');
const path = require("path");

const i18nextOptions = {
  backend: {
    loadPath: path.join(__dirname, '..',  'locales', '{{lng}}','{{ns}}.json'),

    addPath: path.join(__dirname, '..',  'locales', '{{lng}}','{{ns}}.missing.json'),

    jsonIndent: 2,
  },
  interpolation: {
    escapeValue: false,
  },
  saveMissing: true,
  fallbackLng: config.fallbackLng,
  whitelist: config.languages,
  react: {
    wait: false,
  },
};

i18n.use(i18nextBackend);

if (!i18n.isInitialized) {
  i18n.init(i18nextOptions);
}

module.exports = i18n;

