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

import de.openindex.zugferd.manager.utils.getString
import de.openindex.zugferd.quba.generated.resources.Res
import de.openindex.zugferd.quba.generated.resources.TaxCategory_AE
import de.openindex.zugferd.quba.generated.resources.TaxCategory_E
import de.openindex.zugferd.quba.generated.resources.TaxCategory_E_DefaultExemptionReason
import de.openindex.zugferd.quba.generated.resources.TaxCategory_G
import de.openindex.zugferd.quba.generated.resources.TaxCategory_G_DefaultExemptionReason
import de.openindex.zugferd.quba.generated.resources.TaxCategory_K
import de.openindex.zugferd.quba.generated.resources.TaxCategory_K_DefaultExemptionReason
import de.openindex.zugferd.quba.generated.resources.TaxCategory_O
import de.openindex.zugferd.quba.generated.resources.TaxCategory_O_DefaultExemptionReason
import de.openindex.zugferd.quba.generated.resources.TaxCategory_S
import de.openindex.zugferd.quba.generated.resources.TaxCategory_Z
import de.openindex.zugferd.quba.generated.resources.TaxCategory_Z_DefaultExemptionReason
import org.jetbrains.compose.resources.StringResource

/**
 * BT-151
 * https://github.com/ZUGFeRD/mustangproject/issues/463
 */
enum class TaxCategory(
    val code: String,
    val title: StringResource,
    val defaultExemptionReason: StringResource? = null,
    val isZeroTax: Boolean = false,
) {
    /**
     * Standard rate / normale Besteuerung
     */
    NORMAL_TAX(
        code = "S",
        title = Res.string.TaxCategory_S,
    ),

    /**
     * Zero rated goods / keine Besteuerung
     */
    ZERO_RATED(
        code = "Z",
        title = Res.string.TaxCategory_Z,
        defaultExemptionReason = Res.string.TaxCategory_Z_DefaultExemptionReason,
        isZeroTax = true,
    ),

    /**
     * Exempt from tax / Steuerbefreit
     */
    SMALL_BUSINESS(
        code = "E",
        title = Res.string.TaxCategory_E,
        defaultExemptionReason = Res.string.TaxCategory_E_DefaultExemptionReason,
        isZeroTax = true,
    ),

    /**
     * VAT Reverse Charge / Umkehrung der Steuerschuldnerschaft.
     */
    REVERSE_TAX(
        code = "AE",
        title = Res.string.TaxCategory_AE,
        isZeroTax = true,
    ),

    /**
     * VAT exempt for EEA intra-community supply of goods and services / innergemeinschaftliche Transaktion
     */
    INTRA_COMMUNITY_SUPPLY(
        code = "K",
        title = Res.string.TaxCategory_K,
        defaultExemptionReason = Res.string.TaxCategory_K_DefaultExemptionReason,
        isZeroTax = true,
    ),

    /**
     * Free export item, tax not charged / Export außerhalb der EU
     */
    EXPORT_OUT_OF_COMMUNITY(
        code = "G",
        title = Res.string.TaxCategory_G,
        defaultExemptionReason = Res.string.TaxCategory_G_DefaultExemptionReason,
        isZeroTax = true,
    ),

    /**
     * Services outside scope of tax / unversteuerte Leistung
     */
    UNTAXED_SERVICE(
        code = "O",
        title = Res.string.TaxCategory_O,
        defaultExemptionReason = Res.string.TaxCategory_O_DefaultExemptionReason,
        isZeroTax = true,
    ),

    /**
     * Canary Islands general indirect tax /
     */
    //CANARY_ISLAND(
    //    code = "L",
    //    title = Res.string.TaxCategory_L,
    //    isZeroTax = true,
    //),

    /**
     * Tax for production, services and importation in Ceuta and Melilla /
     */
    //CEUTA_AND_MELILLA(
    //    code = "M",
    //    title = Res.string.TaxCategory_M,
    //    isZeroTax = true,
    //),

    ;

    @Suppress("unused")
    suspend fun translateTitle(): String = getString(title)

    companion object {
        fun getByCode(code: String?): TaxCategory? =
            if (code != null)
                TaxCategory.entries.firstOrNull { it.code == code }
            else
                null
    }
}
