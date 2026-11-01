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

import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlinx.serialization.Serializable

//private val DEFAULT_PAYMENT_METHOD = PaymentMethod.SEPA_CREDIT_TRANSFER

@Serializable
data class Invoice(
    @Suppress("PropertyName")
    val _paymentMethod: PaymentMethod = PaymentMethod.SEPA_CREDIT_TRANSFER,

    val number: String = "",
    val issueDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    val dueDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()).plus(DatePeriod(days = 14)),
    val deliveryDate: LocalDate? = null,
    val deliveryStartDate: LocalDate? = null,
    val deliveryEndDate: LocalDate? = null,
    val sender: TradeParty? = null,
    val recipient: TradeParty? = null,
    val currency: String? = null,
    val items: List<Item> = listOf(),
)

//expect fun Invoice.isValid(): Boolean

fun Invoice.isValid(): Boolean {
    return number.isNotBlank()
            //&& issueDate != null
            && currency != null
            && sender != null
            && sender.name.isNotBlank()
            && (sender.vatID != null || sender.taxID != null)
            && recipient != null
            && recipient.name.isNotBlank()
            && (deliveryDate != null || (deliveryStartDate != null && deliveryEndDate != null))
}

expect fun Invoice.toXml(
    method: PaymentMethod = PaymentMethod.SEPA_CREDIT_TRANSFER,
): String

expect suspend fun Invoice.export(
    sourceFile: PlatformFile,
    targetFile: PlatformFile,
    method: PaymentMethod = PaymentMethod.SEPA_CREDIT_TRANSFER,
    removeEmbeddedFiles: Boolean = false,
): Boolean
