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

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.openindex.zugferd.manager.theme.LocalQubaColors
import de.openindex.zugferd.manager.theme.LocalQubaTypography
import de.openindex.zugferd.manager.utils.pluralStringResource
import de.openindex.zugferd.manager.utils.stringResource
import de.openindex.zugferd.manager.utils.title
import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.StringResource

private const val REQUIRED_FIELD_INDICATOR = "*"

@Composable
fun Label(
    text: String,
    requiredIndicator: Boolean = false,
    softWrap: Boolean = false,
    modifier: Modifier = Modifier,
) = Text(
    text = if (requiredIndicator)
        "${text}${REQUIRED_FIELD_INDICATOR}"
    else
        text,
    softWrap = softWrap,
    modifier = modifier,
)

/**
 * Label displayed above an input field (label-above pattern).
 * Renders with Quba label typography and text3 color.
 */
@Composable
fun FieldLabel(
    text: String,
    requiredIndicator: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val colors = LocalQubaColors.current
    val typo = LocalQubaTypography.current
    Text(
        text = if (requiredIndicator) "$text$REQUIRED_FIELD_INDICATOR" else text,
        style = typo.label.copy(color = colors.text3),
        softWrap = false,
        modifier = modifier,
    )
}

@Composable
fun Label(
    text: StringResource,
    requiredIndicator: Boolean = false,
    softWrap: Boolean = false,
    modifier: Modifier = Modifier,
) = Label(
    text = stringResource(text).title(),
    requiredIndicator = requiredIndicator,
    softWrap = softWrap,
    modifier = modifier,
)

@Composable
@Suppress("unused")
fun Label(
    text: PluralStringResource,
    quantity: Int = 1,
    requiredIndicator: Boolean = false,
    softWrap: Boolean = false,
    modifier: Modifier = Modifier,
) = Label(
    text = pluralStringResource(text, quantity).title(),
    requiredIndicator = requiredIndicator,
    softWrap = softWrap,
    modifier = modifier,
)
