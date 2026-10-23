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
import androidx.compose.ui.Modifier
import de.openindex.zugferd.manager.model.PaymentMethod
import de.openindex.zugferd.quba.generated.resources.PaymentMethod
import de.openindex.zugferd.quba.generated.resources.Res
import org.jetbrains.compose.resources.Resource
import org.jetbrains.compose.resources.StringResource

@Composable
fun PaymentMethodField(
    label: Resource = Res.plurals.PaymentMethod,
    value: PaymentMethod? = null,
    options: Map<PaymentMethod, StringResource> = buildMap {
        PaymentMethod.entries.forEach { e -> put(e, e.title) }
    },
    requiredIndicator: Boolean = false,
    onSelect: (PaymentMethod) -> Unit,
    modifier: Modifier = Modifier,
) = DropDown(
    label = label,
    value = value,
    options = options,
    requiredIndicator = requiredIndicator,
    onSelect = onSelect,
    modifier = modifier,
)
