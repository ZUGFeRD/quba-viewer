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

package de.openindex.zugferd.manager.gui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import de.openindex.zugferd.manager.LocalAppState
import de.openindex.zugferd.manager.utils.getCurrenciesByName
import de.openindex.zugferd.manager.utils.getCurrenciesBySymbol
import de.openindex.zugferd.manager.utils.translate
import de.openindex.zugferd.quba.generated.resources.Currency
import de.openindex.zugferd.quba.generated.resources.Res
import org.jetbrains.compose.resources.Resource

@Composable
fun CurrencyField(
    label: Resource = Res.plurals.Currency,
    currency: String? = null,
    short: Boolean = false,
    requiredIndicator: Boolean = false,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val language = LocalAppState.current.preferences.language

    AutoCompleteField(
        label = label.translate(),
        entry = currency,
        entries = remember(language) {
            (if (short) getCurrenciesBySymbol() else getCurrenciesByName())
                .toList()
                .sortedBy { (_, value) -> value }
                .toMap()
        },
        requiredIndicator = requiredIndicator,
        onSelect = onSelect,
        modifier = modifier,
    )
}

/*
@Composable
fun CurrencyField(
    label: Resource = Res.plurals.Currency,
    currency: String? = null,
    requiredIndicator: Boolean = false,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier,
) = AutoCompleteField(
    label = label.translate(),
    entry = currency,
    entries = remember { getCurrencies() },
    requiredIndicator = requiredIndicator,
    onSelect = onSelect,
    modifier = modifier,
)
*/