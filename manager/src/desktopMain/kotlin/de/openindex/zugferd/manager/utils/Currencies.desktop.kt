/*
 * Copyright (c) 2024-2025 Andreas Rudolph <andy@openindex.de>.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package de.openindex.zugferd.manager.utils

import org.apache.commons.lang3.LocaleUtils
import java.util.Currency
import java.util.Locale

private val CURRENCY_CODES by lazy {
    // This approach also returns historic currencies.
    //Currency
    //    .getAvailableCurrencies()
    //    .map { it.currencyCode }
    //    .sorted()

    // We only need active currencies for available countries.
    Locale
        .getISOCountries()
        .mapNotNull { LocaleUtils.languagesByCountry(it).firstOrNull() }
        .mapNotNull { Currency.getInstance(it)?.currencyCode }
        .distinct()
        .sorted()
}

actual fun getCurrencyCodes(): List<String> =
    CURRENCY_CODES

actual fun getCurrencySymbol(currency: String): String? =
    Currency.getInstance(currency)
        ?.getSymbol(Locale.getDefault())
        ?.takeIf { !it.equals(currency, true) }

actual fun getCurrencyName(currency: String): String? =
    Currency.getInstance(currency)
        ?.getDisplayName(Locale.getDefault())
        ?.takeIf { !it.equals(currency, true) }

@Suppress("unused")
actual fun getSystemCurrency(): String =
    try {
        Currency.getInstance(Locale.getDefault())
            ?.currencyCode
            ?: FALLBACK_CURRENCY
    } catch (e: IllegalArgumentException) {
        FALLBACK_CURRENCY
    }

actual fun isValidCurrencyCode(code: String): Boolean =
    try {
        Currency.getInstance(code) != null
    } catch (e: IllegalArgumentException) {
        false
    }
