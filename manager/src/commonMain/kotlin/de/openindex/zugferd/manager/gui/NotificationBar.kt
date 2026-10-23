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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.openindex.zugferd.manager.theme.LocalQubaColors
import de.openindex.zugferd.manager.theme.LocalQubaTypography
import de.openindex.zugferd.manager.utils.pluralStringResource
import de.openindex.zugferd.manager.utils.stringResource
import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.StringResource

// Warn-soft strip per Quba design spec:
// "keep the 'We accept no liability…' warning but as a warn-soft strip, not a yellow rectangle"

@Composable
fun NotificationBar(
    text: String,
    action: @Composable () -> Unit = {},
) {
    val colors = LocalQubaColors.current
    val typo = LocalQubaTypography.current
    val shape = RoundedCornerShape(8.dp)

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(color = colors.warnSoft, shape = shape)
            .border(width = 1.dp, color = colors.warn.copy(alpha = 0.18f), shape = shape)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        action()

        Text(
            text = text,
            color = colors.warn,
            style = typo.small,
            softWrap = true,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
fun NotificationBar(
    text: StringResource,
    action: @Composable () -> Unit = {},
) = NotificationBar(
    text = stringResource(text),
    action = action,
)

@Composable
@Suppress("unused")
fun NotificationBar(
    text: PluralStringResource,
    quantity: Int = 1,
    action: @Composable () -> Unit = {},
) = NotificationBar(
    text = pluralStringResource(text, quantity),
    action = action,
)
