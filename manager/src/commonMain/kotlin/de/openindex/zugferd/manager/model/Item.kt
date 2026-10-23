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

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Item(
    @Transient
    val _uid: Long = Clock.System.now().toEpochMilliseconds(),

    val product: Product? = null,
    val quantity: Double = 1.0,
    val price: Double = 0.0,
    val notes: String? = null,
) {
    val totalNetPrice: Double
        get() = quantity * price

    val totalGrossPrice: Double
        get() = totalNetPrice + tax

    val tax: Double
        get() = ((product?.vatPercent ?: 0.0) / 100.0) * totalNetPrice
}
