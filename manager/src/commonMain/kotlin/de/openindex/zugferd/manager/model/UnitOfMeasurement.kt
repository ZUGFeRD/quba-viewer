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

import de.openindex.zugferd.manager.utils.getPluralString
import de.openindex.zugferd.manager.utils.getString
import de.openindex.zugferd.quba.generated.resources.Res
import de.openindex.zugferd.quba.generated.resources.UnitOfMeasurement_ANN
import de.openindex.zugferd.quba.generated.resources.UnitOfMeasurement_ANN_Value
import de.openindex.zugferd.quba.generated.resources.UnitOfMeasurement_C62
import de.openindex.zugferd.quba.generated.resources.UnitOfMeasurement_C62_Value
import de.openindex.zugferd.quba.generated.resources.UnitOfMeasurement_DAY
import de.openindex.zugferd.quba.generated.resources.UnitOfMeasurement_DAY_Value
import de.openindex.zugferd.quba.generated.resources.UnitOfMeasurement_H87
import de.openindex.zugferd.quba.generated.resources.UnitOfMeasurement_H87_Value
import de.openindex.zugferd.quba.generated.resources.UnitOfMeasurement_HAR
import de.openindex.zugferd.quba.generated.resources.UnitOfMeasurement_HAR_Value
import de.openindex.zugferd.quba.generated.resources.UnitOfMeasurement_HUR
import de.openindex.zugferd.quba.generated.resources.UnitOfMeasurement_HUR_Value
import de.openindex.zugferd.quba.generated.resources.UnitOfMeasurement_KGM
import de.openindex.zugferd.quba.generated.resources.UnitOfMeasurement_KGM_Value
import de.openindex.zugferd.quba.generated.resources.UnitOfMeasurement_KMT
import de.openindex.zugferd.quba.generated.resources.UnitOfMeasurement_KMT_Value
import de.openindex.zugferd.quba.generated.resources.UnitOfMeasurement_KWH
import de.openindex.zugferd.quba.generated.resources.UnitOfMeasurement_KWH_Value
import de.openindex.zugferd.quba.generated.resources.UnitOfMeasurement_LS
import de.openindex.zugferd.quba.generated.resources.UnitOfMeasurement_LS_Value
import de.openindex.zugferd.quba.generated.resources.UnitOfMeasurement_LTR
import de.openindex.zugferd.quba.generated.resources.UnitOfMeasurement_LTR_Value
import de.openindex.zugferd.quba.generated.resources.UnitOfMeasurement_MIN
import de.openindex.zugferd.quba.generated.resources.UnitOfMeasurement_MIN_Value
import de.openindex.zugferd.quba.generated.resources.UnitOfMeasurement_MMK
import de.openindex.zugferd.quba.generated.resources.UnitOfMeasurement_MMK_Value
import de.openindex.zugferd.quba.generated.resources.UnitOfMeasurement_MMT
import de.openindex.zugferd.quba.generated.resources.UnitOfMeasurement_MMT_Value
import de.openindex.zugferd.quba.generated.resources.UnitOfMeasurement_MON
import de.openindex.zugferd.quba.generated.resources.UnitOfMeasurement_MON_Value
import de.openindex.zugferd.quba.generated.resources.UnitOfMeasurement_MTK
import de.openindex.zugferd.quba.generated.resources.UnitOfMeasurement_MTK_Value
import de.openindex.zugferd.quba.generated.resources.UnitOfMeasurement_MTQ
import de.openindex.zugferd.quba.generated.resources.UnitOfMeasurement_MTQ_Value
import de.openindex.zugferd.quba.generated.resources.UnitOfMeasurement_MTR
import de.openindex.zugferd.quba.generated.resources.UnitOfMeasurement_MTR_Value
import de.openindex.zugferd.quba.generated.resources.UnitOfMeasurement_NAR
import de.openindex.zugferd.quba.generated.resources.UnitOfMeasurement_NAR_Value
import de.openindex.zugferd.quba.generated.resources.UnitOfMeasurement_NPR
import de.openindex.zugferd.quba.generated.resources.UnitOfMeasurement_NPR_Value
import de.openindex.zugferd.quba.generated.resources.UnitOfMeasurement_P1
import de.openindex.zugferd.quba.generated.resources.UnitOfMeasurement_P1_Value
import de.openindex.zugferd.quba.generated.resources.UnitOfMeasurement_SEC
import de.openindex.zugferd.quba.generated.resources.UnitOfMeasurement_SEC_Value
import de.openindex.zugferd.quba.generated.resources.UnitOfMeasurement_SET
import de.openindex.zugferd.quba.generated.resources.UnitOfMeasurement_SET_Value
import de.openindex.zugferd.quba.generated.resources.UnitOfMeasurement_SMI
import de.openindex.zugferd.quba.generated.resources.UnitOfMeasurement_SMI_Value
import de.openindex.zugferd.quba.generated.resources.UnitOfMeasurement_TNE
import de.openindex.zugferd.quba.generated.resources.UnitOfMeasurement_TNE_Value
import de.openindex.zugferd.quba.generated.resources.UnitOfMeasurement_WEE
import de.openindex.zugferd.quba.generated.resources.UnitOfMeasurement_WEE_Value
import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.StringResource

/**
 * BT-130
 */
enum class UnitOfMeasurement(
    val code: String,
    val symbol: String? = null,
    val title: StringResource,
    val value: PluralStringResource,
    val minPrecision: Int = 0,
    val maxPrecision: Int = 2,
) {
    /** The lump sum. */
    LUMP_SUM(
        code = "LS",
        title = Res.string.UnitOfMeasurement_LS,
        value = Res.plurals.UnitOfMeasurement_LS_Value,
        maxPrecision = 0,
        //description = "Pauschale",
        //pluralDescription = "Pauschalen",
    ),

    /** The Percent (%). */
    PERCENT(
        code = "P1",
        symbol = "%",
        title = Res.string.UnitOfMeasurement_P1,
        value = Res.plurals.UnitOfMeasurement_P1_Value,
        //description = "Prozent",
        //pluralDescription = "Prozent",
    ),


    /** A unit of count defining the number of pieces */
    UNIT(
        code = "C62",
        title = Res.string.UnitOfMeasurement_C62,
        value = Res.plurals.UnitOfMeasurement_C62_Value,
        //description = "Einheit",
        //pluralDescription = "Einheiten",
    ),

    /** piece */
    PIECE(
        code = "H87",
        title = Res.string.UnitOfMeasurement_H87,
        value = Res.plurals.UnitOfMeasurement_H87_Value,
        //description = "Stück",
        //pluralDescription = "Stücke",
    ),

    /** A unit of count defining the number of articles (items). */
    ARTICLE(
        code = "NAR",
        title = Res.string.UnitOfMeasurement_NAR,
        value = Res.plurals.UnitOfMeasurement_NAR_Value,
        //description = "Artikel",
        //pluralDescription = "Artikel",
    ),

    /** The number of pairs. */
    PAIR(
        code = "NPR",
        title = Res.string.UnitOfMeasurement_NPR,
        value = Res.plurals.UnitOfMeasurement_NPR_Value,
        //description = "Paar",
        //pluralDescription = "Paare",
    ),

    /** A number of objects grouped together to a set */
    SET(
        code = "SET",
        title = Res.string.UnitOfMeasurement_SET,
        value = Res.plurals.UnitOfMeasurement_SET_Value,
        //description = "Set",
        //pluralDescription = "Sets",
    ),


    /** The Year. */
    YEAR(
        code = "ANN",
        title = Res.string.UnitOfMeasurement_ANN,
        value = Res.plurals.UnitOfMeasurement_ANN_Value,
        //description = "Jahr",
        //pluralDescription = "Jahre",
    ),

    /** The Month. */
    MONTH(
        code = "MON",
        title = Res.string.UnitOfMeasurement_MON,
        value = Res.plurals.UnitOfMeasurement_MON_Value,
        //description = "Monat",
        //pluralDescription = "Monate",
    ),

    /** The week. */
    WEEK(
        code = "WEE",
        title = Res.string.UnitOfMeasurement_WEE,
        value = Res.plurals.UnitOfMeasurement_WEE_Value,
        //description = "Woche",
        //pluralDescription = "Wochen",
    ),

    /** The day count */
    DAY(
        code = "DAY",
        title = Res.string.UnitOfMeasurement_DAY,
        value = Res.plurals.UnitOfMeasurement_DAY_Value,
        //description = "Tag",
        //pluralDescription = "Tage",
    ),

    /** The Hour (h). */
    HOUR(
        code = "HUR",
        symbol = "h",
        title = Res.string.UnitOfMeasurement_HUR,
        value = Res.plurals.UnitOfMeasurement_HUR_Value,
        //description = "Stunde",
        //pluralDescription = "Stunden",
    ),

    /** The Minute (min). */
    MINUTE(
        code = "MIN",
        symbol = "min",
        title = Res.string.UnitOfMeasurement_MIN,
        value = Res.plurals.UnitOfMeasurement_MIN_Value,
        //description = "Minute",
        //pluralDescription = "Minuten",
    ),

    /** The Second (s). */
    SECOND(
        code = "SEC",
        symbol = "s",
        title = Res.string.UnitOfMeasurement_SEC,
        value = Res.plurals.UnitOfMeasurement_SEC_Value,
        //description = "Sekunde",
        //pluralDescription = "Sekunden",
    ),


    /** The Hectare (ha). */
    HECTARE(
        code = "HAR",
        symbol = "ha",
        title = Res.string.UnitOfMeasurement_HAR,
        value = Res.plurals.UnitOfMeasurement_HAR_Value,
        maxPrecision = 5,
        //description = "Hektar",
        //pluralDescription = "Hektar",
    ),

    /** The square meter (m2). */
    METER_SQUARE(
        code = "MTK",
        symbol = "m²",
        title = Res.string.UnitOfMeasurement_MTK,
        value = Res.plurals.UnitOfMeasurement_MTK_Value,
        maxPrecision = 5,
        //description = "Quadratmeter",
        //pluralDescription = "Quadratmeter",
    ),

    /** The square millimeter (mm2). */
    MILLIMETER_SQUARE(
        code = "MMK",
        symbol = "mm²",
        title = Res.string.UnitOfMeasurement_MMK,
        value = Res.plurals.UnitOfMeasurement_MMK_Value,
        maxPrecision = 5,
        //description = "Quadratmillimeter",
        //pluralDescription = "Quadratmillimeter",
    ),


    /** The Mile. 1609,344 m */
    MILE(
        code = "SMI",
        title = Res.string.UnitOfMeasurement_SMI,
        value = Res.plurals.UnitOfMeasurement_SMI_Value,
        maxPrecision = 5,
        //description = "Meile",
        //pluralDescription = "Meilen",
    ),

    /** The Kilometer (km). */
    KILOMETER(
        code = "KMT",
        symbol = "km",
        title = Res.string.UnitOfMeasurement_KMT,
        value = Res.plurals.UnitOfMeasurement_KMT_Value,
        maxPrecision = 5,
        //description = "Kilometer",
        //pluralDescription = "Kilometer",
    ),

    /** The Meter (m). */
    METER(
        code = "MTR",
        symbol = "m",
        title = Res.string.UnitOfMeasurement_MTR,
        value = Res.plurals.UnitOfMeasurement_MTR_Value,
        maxPrecision = 5,
        //description = "Meter",
        //pluralDescription = "Meter",
    ),

    /** The Millimeter (mm). */
    MILLIMETER(
        code = "MMT",
        symbol = "mm",
        title = Res.string.UnitOfMeasurement_MMT,
        value = Res.plurals.UnitOfMeasurement_MMT_Value,
        maxPrecision = 5,
        //description = "Millimeter",
        //pluralDescription = "Millimeter",
    ),


    /** The cubic meter (m3). */
    METER_CUBIC(
        code = "MTQ",
        symbol = "m³",
        title = Res.string.UnitOfMeasurement_MTQ,
        value = Res.plurals.UnitOfMeasurement_MTQ_Value,
        maxPrecision = 5,
        //description = "Kubikmeter",
        //pluralDescription = "Kubikmeter",
    ),

    /** The Liter (l). */
    LITRE(
        code = "LTR",
        symbol = "l",
        title = Res.string.UnitOfMeasurement_LTR,
        value = Res.plurals.UnitOfMeasurement_LTR_Value,
        maxPrecision = 5,
        //description = "Liter",
        //pluralDescription = "Liter",
    ),


    /** The metric ton (t). */
    TON_METRIC(
        code = "TNE",
        symbol = "t",
        title = Res.string.UnitOfMeasurement_TNE,
        value = Res.plurals.UnitOfMeasurement_TNE_Value,
        maxPrecision = 5,
        //description = "Tonne",
        //pluralDescription = "Tonnen",
    ),

    /** The Kilogram (kg). */
    KILOGRAM(
        code = "KGM",
        symbol = "kg",
        title = Res.string.UnitOfMeasurement_KGM,
        value = Res.plurals.UnitOfMeasurement_KGM_Value,
        maxPrecision = 5,
        //description = "Kilogramm",
        //pluralDescription = "Kilogramm",
    ),


    /** The Kilowatt hour (kWh). */
    KILOWATT_HOUR(
        code = "KWH",
        symbol = "kWh",
        title = Res.string.UnitOfMeasurement_KWH,
        value = Res.plurals.UnitOfMeasurement_KWH_Value,
        maxPrecision = 5,
        //description = "Kilowattstunde",
        //pluralDescription = "Kilowattstunden",
    ),

    ;

    @Suppress("unused")
    suspend fun translateTitle(): String = getString(title)

    @Suppress("unused")
    suspend fun translateValue(quantity: Int = 1): String = getPluralString(value, quantity)

    companion object {
        fun getByCode(code: String?): UnitOfMeasurement? =
            if (code != null)
                entries.firstOrNull { it.code == code }
            else
                null
    }
}