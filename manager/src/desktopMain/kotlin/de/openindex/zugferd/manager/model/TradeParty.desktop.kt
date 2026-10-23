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
import org.mustangproject.LegalOrganisation
import org.mustangproject.SchemedID
import org.mustangproject.TradeParty as _TradeParty

fun TradeParty.build(): _TradeParty? =
    _TradeParty()
        .setName(name.trim())
        .setZIP(zip?.trimToNull())
        .setStreet(street?.trimToNull())
        .setLocation(location?.trimToNull())
        .setCountry(country?.trimToNull())
        .setLegalOrganisation(
            LegalOrganisation()
                .also { org ->
                    org.setTradingBusinessName(name.trim())

                    //
                    // Using the register nr (Handelsregister-Nr) for identification without a scheme (BT-30),
                    // if no VAT-ID is present for the trade party.
                    //
                    // Otherwise without VAT-ID and any other identification, a BR-CO-26 validation error might happen,
                    // as discussed here: https://github.com/OpenIndex/ZUGFeRD-Manager/issues/17
                    //

                    val tradeRegisterNr = registerNr?.trimToNull()
                    if (tradeRegisterNr != null) {
                        org.setSchemedID(
                            SchemedID(
                                null,
                                tradeRegisterNr
                            )
                        )
                    }
                }
        )

        //
        // Avoid validation errors, when tax ID and VAT ID is used together.
        //
        // Currently, we should only provide a VAT ID
        // and only use the tax ID, if the VAT ID is not present.
        //
        //.setTaxID(taxID?.trimToNull())
        .setTaxID(taxID?.trimToNull()?.takeIf { vatID?.trimToNull() == null })

        .setVATID(vatID?.trimToNull())
        .setID(id?.trimToNull())
        .setDescription(description?.trimToNull())
        .setAdditionalAddress(additionalAddress?.trimToNull())
        .setAdditionalAddressExtension(additionalAddressExtension?.trimToNull())
        .setContact(contact?.build())
        .let { party ->
            bankDetails
                .mapNotNull { it.build() }
                .forEach { bankDetail ->
                    bankDetail.setAccountName(
                        bankDetail.accountName?.trimToNull() ?: name.trim()
                    )
                    party.addBankDetails(bankDetail)
                }
            party
        }
        .let { party ->
            debitDetails
                .mapNotNull { it.build() }
                .forEach { party.addDebitDetails(it) }
            party
        }
        .takeIf { it.name.isNotBlank() }
