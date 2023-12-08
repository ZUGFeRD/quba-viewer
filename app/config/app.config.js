module.exports = {
    platform: process.platform,
    port: process.env.PORT ? process.env.PORT : 3000,
    languages: ['en', 'de','fr', 'de.ids'],
    fallbackLng: 'en',
    namespace: 'translation'
  };


