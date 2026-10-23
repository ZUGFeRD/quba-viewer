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
import java.math.RoundingMode
import org.mustangproject.Product as _Product

fun Product.build(): _Product? =
    _Product()
        .setName(name.trim())
        .setDescription(description?.trimToNull() ?: "")
        .setUnit(unit.trimToNull() ?: UnitOfMeasurement.UNIT.code)
        .setVATPercent(vatPercent.toBigDecimal().setScale(1, RoundingMode.HALF_EVEN))
        .setTaxExemptionReason(taxExemptionReason?.trimToNull())
        .setTaxCategoryCode(taxCategoryCode.trimToNull() ?: TaxCategory.NORMAL_TAX.code)
        //.let { product ->
        //    if (product.taxCategoryCode.equals(TaxCategoryCode.INTRA_COMMUNITY_SUPPLY.code, true)) {
        //        product.setIntraCommunitySupply()
        //    }
        //    product
        //}
        .takeIf { it.name.isNotBlank() && it.unit.isNotBlank() }
