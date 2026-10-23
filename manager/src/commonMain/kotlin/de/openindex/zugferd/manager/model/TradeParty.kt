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

import de.openindex.zugferd.manager.utils.trimToNull
import kotlinx.serialization.Serializable

@Serializable
data class TradeParty(
    @Suppress("PropertyName")
    val _key: UInt? = null,
    @Suppress("PropertyName")
    val _defaultPaymentMethod: PaymentMethod = PaymentMethod.SEPA_CREDIT_TRANSFER,

    val name: String = "",
    val zip: String? = null,
    val street: String? = null,
    val location: String? = null,
    val country: String? = null,
    val id: String? = null,
    val taxID: String? = null,
    val vatID: String? = null,
    val registerNr: String? = null,
    val creditorReferenceId: String? = null,
    val imprint: String? = null,
    val description: String? = null,
    val additionalAddress: String? = null,
    val additionalAddressExtension: String? = null,
    val bankDetails: List<BankDetails> = listOf(),
    val debitDetails: List<DirectDebit> = listOf(),
    val contact: Contact? = null,
    val legalOrg: LegalOrganisation? = null,
    val globalId: SchemedID? = null,
    val uriUniversalCommunicationId: SchemedID? = null,
) {
    val isSaved: Boolean
        get() = _key != null

    @Suppress("DuplicatedCode")
    val summary: String
        get() = buildList {
            add(name.trim().takeIf { it.isNotBlank() } ?: "???")

            val address = buildList {
                if (!zip.isNullOrBlank()) {
                    add(zip.trim())
                }
                if (!location.isNullOrBlank()) {
                    add(location.trim())
                }
            }.joinToString(" ")

            if (address.trim().isNotBlank()) {
                add(address)
            }

            if (id?.trimToNull() != null) {
                add("#${id.trim()}")
            }
        }.joinToString(" | ")

    @Suppress("DuplicatedCode")
    val summaryShort: String
        get() = buildList {
            add(name.trim().takeIf { it.isNotBlank() } ?: "???")

            if (id?.trimToNull() != null) {
                add("#${id.trim()}")
            } else {
                val address = buildList {
                    if (!zip.isNullOrBlank()) {
                        add(zip.trim())
                    }
                    if (!location.isNullOrBlank()) {
                        add(location.trim())
                    }
                }.joinToString(" ")

                if (address.trim().isNotBlank()) {
                    add(address)
                }
            }

        }.joinToString(" | ")
}
