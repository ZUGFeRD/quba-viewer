package de.openindex.zugferd.manager.utils

import de.openindex.zugferd.manager.APP_LOGGER
import org.apache.commons.lang3.LocaleUtils
import java.util.Locale

private fun Locale.isSameLanguage(code: String): Boolean =
    language.equals(code, true) || isO3Language.equals(code, true)

private fun Locale.isSameCountry(code: String): Boolean =
    country.equals(code, true) || isO3Country.equals(code, true)

actual fun setCurrentLanguage(language: Language, country: String?) {
    val currentLocale = Locale.getDefault()

    // No need to switch locales,
    // if the current locales already meets the language and country criteria.
    if (currentLocale.isSameLanguage(language.code)) {
        if (country == null) {
            return
        }
        if (currentLocale.isSameCountry(country)) {
            return
        }
    }

    val locale = Locale.Builder()
        .setLanguage(language.code)
        .setRegion(country ?: "")
        .build()

    if (LocaleUtils.isLanguageUndetermined(locale)) {
        APP_LOGGER.warn(
            "Requested language \"{}\" seems to be invalid.",
            language.code.plus(
                if (country != null)
                    "-".plus(country)
                else
                    ""
            ),
        )
        return
    }

    if (!LocaleUtils.isAvailableLocale(locale)) {
        // If locale is unavailable and a country was specified,
        // try selecting the locale without the country.
        if (country != null) {
            setCurrentLanguage(language, null)
            return
        }

        APP_LOGGER.warn(
            "Requested locale \"{}\" is not available.",
            locale.toLanguageTag(),
        )
        return
    }

    if (locale == currentLocale) {
        //println("setCurrentLanguage: ${language.code} (unchanged)")
        return
    }

    APP_LOGGER.info(
        "Switching language to \"{}\".",
        locale.toLanguageTag(),
    )

    Locale.setDefault(locale)
}

actual fun getCurrentLanguage(defaultLanguage: Language): Language {
    val locale = Locale.getDefault()
    val lang = Language.entries
        .firstOrNull { locale.isSameLanguage(it.code) }

    if (lang != null) {
        return lang
    }

    // Enforce a language switch,
    // if the current locale is not in the list of supported languages.
    APP_LOGGER.info(
        "The system language \"{}\" is not supported by the application. Falling back to \"{}\".",
        locale.toLanguageTag(),
        Locale.of(defaultLanguage.code).toLanguageTag(),
    )
    setCurrentLanguage(defaultLanguage)

    return defaultLanguage
}

actual fun getLanguageName(
    language: Language,
    inLanguage: Language?
): String =
    Locale.of(language.code)
        .getDisplayLanguage(
            if (inLanguage != null)
                Locale.of(inLanguage.code)
            else
                Locale.getDefault(),
        )
