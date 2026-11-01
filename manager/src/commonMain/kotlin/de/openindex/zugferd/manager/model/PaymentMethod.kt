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
import de.openindex.zugferd.quba.generated.resources.PaymentMethod_58
import de.openindex.zugferd.quba.generated.resources.PaymentMethod_59
import de.openindex.zugferd.quba.generated.resources.Res
import org.jetbrains.compose.resources.StringResource

/**
 * BT-81
 */
enum class PaymentMethod(
    val code: Int,
    val title: StringResource
) {
    /**
     * In Cash / Barzahlung
     */
    //CASH(
    //    code = 10,
    //    title = Res.string.PaymentMethod_10,
    //),

    /**
     * Credit Transfer / Überweisung
     */
    //CREDIT_TRANSFER(
    //    code = 30,
    //    title = Res.string.PaymentMethod_30,
    //),

    /**
     * Debit Transfer / Bankeinzug
     */
    //DEBIT_TRANSFER(
    //    code = 31,
    //    title = Res.string.PaymentMethod_31,
    //),

    /**
     * Payment to bank account / Einzahlung auf Konto
     */
    //PAYMENT_TO_BANK_ACCOUNT(
    //    code = 42,
    //    title = Res.string.PaymentMethod_42,
    //),

    /**
     * Bank card / Kartenzahlung
     */
    //BANK_CARD(
    //    code = 48,
    //    title = Res.string.PaymentMethod_48,
    //),

    /**
     * Direct Debit / Abbuchung
     */
    //DIRECT_DEBIT(
    //    code = 49,
    //    title = Res.string.PaymentMethod_49,
    //),

    /**
     * Standing agreement / Dauerauftrag
     */
    //STANDING_AGREEMENT(
    //    code = 57,
    //    title = Res.string.PaymentMethod_57,
    //),

    /**
     * SEPA credit transfer / SEPA-Überweisung
     */
    SEPA_CREDIT_TRANSFER(
        code = 58,
        title = Res.string.PaymentMethod_58,
    ),

    /**
     * SEPA direct debit / SEPA-Lastschrift
     */
    SEPA_DIRECT_DEBIT(
        code = 59,
        title = Res.string.PaymentMethod_59,
    ),

    /**
     * Clearing between partners / partnerschaftliche Verrechnung
     */
    //CLEARING_BETWEEN_PARTNERS(
    //    code = 97,
    //    title = Res.string.PaymentMethod_97,
    //),

    ;

    @Suppress("unused")
    suspend fun translateTitle(): String = getString(title)

    companion object {
        @Suppress("unused")
        fun getByCode(code: Int?): PaymentMethod? =
            if (code != null)
                PaymentMethod.entries.firstOrNull { it.code == code }
            else
                null
    }
}
