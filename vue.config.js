module.exports = {
  publicPath: process.env.NODE_ENV === "production" ? "./" : "/",
  productionSourceMap: true,
  pluginOptions: {
    electronBuilder: {
      nodeIntegration: true
    },
    i18n: {
      locale: 'en',
      fallbackLocale: 'en',
      localeDir: 'locales',
      enableLegacy: true,
      runtimeOnly: false,
      compositionOnly: true,
      fullInstall: true
    }
  },
};

