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

import de.openindex.zugferd.quba.generated.resources.Res
import de.openindex.zugferd.quba.generated.resources.allStringResources
import org.jetbrains.compose.resources.ExperimentalResourceApi

expect fun getCountryCodes(): List<String>

expect fun getCountryName(code: String): String

expect fun getSystemCountryCode(): String

expect fun isValidCountryCode(code: String): Boolean

fun getCountries(): Map<String, String> =
    mapOf(
        *getCountryCodes().map { code ->
            Pair(code, getCountryName(code))
        }.toTypedArray()
    )

@OptIn(ExperimentalResourceApi::class)
suspend fun getCountryDefaultTax(code: String): Double? {
    val resource = Res.allStringResources["DefaultTax_${code.trim().uppercase()}"] ?: return null
    return getString(resource)
        .toDoubleOrNull()
        ?.takeIf { it > 0 }
}

expect fun getCountryDefaultCurrency(code: String): String?
