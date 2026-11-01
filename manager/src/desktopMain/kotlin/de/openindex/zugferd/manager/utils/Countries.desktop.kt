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

import java.util.Currency
import java.util.Locale

actual fun getCountryCodes(): List<String> =
    Locale
        .getISOCountries()
        .asList()
        .sorted()

actual fun getCountryName(code: String): String =
    Locale
        .of("", code)
        .getDisplayCountry(Locale.getDefault())

actual fun getSystemCountryCode(): String =
    Locale.getDefault().country

actual fun isValidCountryCode(code: String): Boolean =
    Locale.getISOCountries().firstOrNull { it.equals(code, true) } != null

actual fun getCountryDefaultCurrency(code: String): String? =
    try {
        Currency.getInstance(
            Locale.of("", code)
        )?.currencyCode
    } catch (e: IllegalArgumentException) {
        null
    }
