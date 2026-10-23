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

expect fun Number.format(
    minPrecision: Int = 0,
    maxPrecision: Int = 2,
    grouped: Boolean = false,
): String

expect fun Number.formatPrice(
    currencyCode: String,
    grouped: Boolean = false,
): String

val Number.formatAsPercentage: String
    get() = format(
        minPrecision = 0,
        //minPrecision = 1,
        maxPrecision = 1,
        grouped = false,
    )

//val Number.formatAsPrice: String
//    get() = format(
//        minPrecision = 2,
//        maxPrecision = 2,
//        grouped = false,
//    )

@Suppress("unused")
val Number.formatAsQuantity: String
    get() = format(
        minPrecision = 1,
        maxPrecision = 2,
        grouped = false,
    )

expect fun String.parseNumber(
    minPrecision: Int = -1,
    maxPrecision: Int = -1,
    grouped: Boolean = false,
): Number?

fun String.parseDouble(
    minPrecision: Int = -1,
    maxPrecision: Int = -1,
    grouped: Boolean = false,
): Double? = parseNumber(
    minPrecision = minPrecision,
    maxPrecision = maxPrecision,
    grouped = grouped,
)?.toDouble()

@Suppress("unused")
fun String.parseFloat(
    minPrecision: Int = -1,
    maxPrecision: Int = -1,
    grouped: Boolean = false,
): Float? = parseNumber(
    minPrecision = minPrecision,
    maxPrecision = maxPrecision,
    grouped = grouped,
)?.toFloat()

@Suppress("unused")
fun String.parseLong(
    grouped: Boolean = false,
): Long? = parseNumber(
    minPrecision = 0,
    maxPrecision = 0,
    grouped = grouped,
)?.toLong()

@Suppress("unused")
fun String.parseInt(
    grouped: Boolean = false,
): Int? = parseNumber(
    minPrecision = 0,
    maxPrecision = 0,
    grouped = grouped,
)?.toInt()
