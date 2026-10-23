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

import de.openindex.zugferd.manager.APP_LOGGER
import java.text.NumberFormat
import java.text.ParseException
import java.util.Currency
import java.util.Locale

actual fun Number.format(
    minPrecision: Int,
    maxPrecision: Int,
    grouped: Boolean,
): String = NumberFormat
    .getInstance(Locale.getDefault())
    .apply {
        isGroupingUsed = grouped
        if (maxPrecision >= 0) {
            maximumFractionDigits = maxPrecision
        }
        if (minPrecision >= 0) {
            minimumFractionDigits = minPrecision
        }
    }
    .format(this)

actual fun Number.formatPrice(
    currencyCode: String,
    grouped: Boolean
): String =
    NumberFormat
        .getCurrencyInstance(Locale.getDefault())
        .apply {
            isGroupingUsed = grouped
            try {
                currency = Currency.getInstance(currencyCode)
                minimumFractionDigits = currency.defaultFractionDigits
                maximumFractionDigits = currency.defaultFractionDigits
            } catch (e: IllegalArgumentException) {
                APP_LOGGER.warn("Can't load currency (${currencyCode})!", e)
            }
        }
        .format(this)

actual fun String.parseNumber(
    minPrecision: Int,
    maxPrecision: Int,
    grouped: Boolean
): Number? = try {
    NumberFormat
        .getInstance(Locale.getDefault())
        .apply {
            isGroupingUsed = grouped
            if (maxPrecision >= 0) {
                maximumFractionDigits = maxPrecision
            }
            if (minPrecision >= 0) {
                minimumFractionDigits = minPrecision
            }
        }
        .parse(this.trim())
} catch (e: ParseException) {
    null
}
