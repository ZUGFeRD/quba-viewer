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
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.TooltipPlacement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import de.openindex.zugferd.manager.utils.pluralStringResource
import de.openindex.zugferd.manager.utils.stringResource
import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.StringResource

const val DEFAULT_DELAY_MILLIS = 150

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun Tooltip(
    text: String,
    delayMillis: Int = DEFAULT_DELAY_MILLIS,
    modifier: Modifier = Modifier,
    tooltipPlacement: TooltipPlacement = TooltipPlacement.CursorPoint(
        offset = DpOffset(0.dp, 16.dp)
    ),
    content: @Composable () -> Unit
) = TooltipArea(
    tooltip = {
        Surface(
            color = MaterialTheme.colorScheme.tertiaryContainer,
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier
                .shadow(4.dp),
        ) {
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .padding(10.dp)
            )
        }
    },
    delayMillis = delayMillis,
    tooltipPlacement = tooltipPlacement,
    //tooltipPlacement = TooltipPlacement.CursorPoint(
    //    alignment = Alignment.BottomCenter,
    //    //offset = DpOffset.Zero,
    //),
    modifier = modifier,
) {
    content()
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun Tooltip(
    text: StringResource,
    delayMillis: Int = DEFAULT_DELAY_MILLIS,
    modifier: Modifier = Modifier,
    tooltipPlacement: TooltipPlacement = TooltipPlacement.CursorPoint(
        offset = DpOffset(0.dp, 16.dp)
    ),
    content: @Composable () -> Unit
) = Tooltip(
    text = stringResource(text),
    delayMillis = delayMillis,
    modifier = modifier,
    tooltipPlacement = tooltipPlacement,
    content = content,
)

@Composable
@Suppress("unused")
@OptIn(ExperimentalFoundationApi::class)
fun Tooltip(
    text: PluralStringResource,
    textQuantity: Int = 1,
    delayMillis: Int = DEFAULT_DELAY_MILLIS,
    modifier: Modifier = Modifier,
    tooltipPlacement: TooltipPlacement = TooltipPlacement.CursorPoint(
        offset = DpOffset(0.dp, 16.dp)
    ),
    content: @Composable () -> Unit
) = Tooltip(
    text = pluralStringResource(text, textQuantity),
    delayMillis = delayMillis,
    modifier = modifier,
    tooltipPlacement = tooltipPlacement,
    content = content,
)
