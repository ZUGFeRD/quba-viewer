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

package de.openindex.zugferd.manager.model

import de.openindex.zugferd.manager.utils.FALLBACK_CURRENCY
import de.openindex.zugferd.manager.utils.Preferences
import de.openindex.zugferd.manager.utils.formatPrice
import de.openindex.zugferd.manager.utils.getString
import de.openindex.zugferd.quba.generated.resources.AppPricePerUnit
import de.openindex.zugferd.quba.generated.resources.Res
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable

@Serializable
data class Product(
    @Suppress("PropertyName")
    val _key: UInt? = null,
    @Suppress("PropertyName")
    val _defaultPricePerUnit: Double = 0.0,

    val name: String = "",
    val description: String? = null,
    val unit: String = UnitOfMeasurement.UNIT.code,
    val vatPercent: Double,
    val taxExemptionReason: String? = null,
    val taxCategoryCode: String = TaxCategory.NORMAL_TAX.code,
) {
    val unitOfMeasurement: UnitOfMeasurement?
        get() = UnitOfMeasurement.getByCode(unit)

    val taxCategory: TaxCategory?
        get() = TaxCategory.getByCode(taxCategoryCode)

    val isSaved: Boolean
        get() = _key != null

    fun getSummary(preferences: Preferences): String =
        runBlocking {
            val currency = preferences.currency ?: FALLBACK_CURRENCY

            buildList {
                val priceInfo = if (_defaultPricePerUnit > 0)
                    _defaultPricePerUnit.formatPrice(currency)
                else
                    ""

                val unitInfo = unitOfMeasurement?.symbol
                    ?: unitOfMeasurement?.translateValue(1)
                    ?: unit

                val taxInfo = taxCategory?.translateTitle()
                    ?: taxCategoryCode

                add(name.trim().takeIf { it.isNotBlank() } ?: "???")
                add(getString(Res.string.AppPricePerUnit, priceInfo, unitInfo))
                add(taxInfo)
            }.joinToString(" | ")
        }
}
