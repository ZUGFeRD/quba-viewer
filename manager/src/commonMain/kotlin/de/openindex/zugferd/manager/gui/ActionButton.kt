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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.openindex.zugferd.manager.utils.pluralStringResource
import de.openindex.zugferd.manager.utils.stringResource
import de.openindex.zugferd.manager.utils.title
import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.StringResource

@Composable
fun ActionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) = QubaButton(
    label = label,
    onClick = onClick,
    variant = QubaButtonVariant.Default,
    modifier = modifier,
)

@Composable
@Suppress("unused")
fun ActionButton(
    label: StringResource,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) = ActionButton(
    label = stringResource(label).title(),
    onClick = onClick,
    modifier = modifier,
)

@Composable
@Suppress("unused")
fun ActionButton(
    label: PluralStringResource,
    labelQuantity: Int = 1,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) = ActionButton(
    label = pluralStringResource(label, labelQuantity).title(),
    onClick = onClick,
    modifier = modifier,
)

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun ActionButtonWithTooltip(
    label: String,
    tooltip: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) = Tooltip(
    text = tooltip,
) {
    ActionButton(
        label = label,
        onClick = onClick,
        modifier = modifier,
    )
}

@Composable
fun ActionButtonWithTooltip(
    label: StringResource,
    tooltip: StringResource,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) = ActionButtonWithTooltip(
    label = stringResource(label).title(),
    tooltip = stringResource(tooltip),
    onClick = onClick,
    modifier = modifier,
)

@Composable
@Suppress("unused")
fun ActionButtonWithTooltip(
    label: PluralStringResource,
    labelQuantity: Int = 1,
    tooltip: PluralStringResource,
    tooltipQuantity: Int = 1,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) = ActionButtonWithTooltip(
    label = pluralStringResource(label, labelQuantity).title(),
    tooltip = pluralStringResource(tooltip, tooltipQuantity),
    onClick = onClick,
    modifier = modifier,
)
