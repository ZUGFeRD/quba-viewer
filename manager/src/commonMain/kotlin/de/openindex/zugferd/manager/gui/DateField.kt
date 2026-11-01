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

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import de.openindex.zugferd.manager.utils.formatAsMediumDate
import de.openindex.zugferd.manager.utils.pluralStringResource
import de.openindex.zugferd.manager.utils.stringResource
import de.openindex.zugferd.manager.utils.title
import de.openindex.zugferd.quba.generated.resources.AppDateSelectionFieldClear
import de.openindex.zugferd.quba.generated.resources.AppDateSelectionFieldSelect
import de.openindex.zugferd.quba.generated.resources.Res
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.StringResource

@Composable
fun DateField(
    label: String,
    value: LocalDate?,
    clearable: Boolean = false,
    requiredIndicator: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null,
    onValueChange: (LocalDate?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDialog by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Open dialog when user clicks anywhere on the text field.
    LaunchedEffect(isPressed) {
        if (isPressed) showDialog = true
    }

    Column(modifier = modifier) {
        FieldLabel(
            text = label.title(),
            requiredIndicator = requiredIndicator,
            modifier = Modifier.padding(bottom = 4.dp),
        )
        OutlinedTextField(
            value = value?.formatAsMediumDate() ?: "",
            onValueChange = { },
            supportingText = supportingText,
            interactionSource = interactionSource,
            trailingIcon = {
                Row {
                    // Button for date selection via dialog.
                    IconButton(
                        onClick = { showDialog = true },
                        modifier = Modifier.pointerHoverIcon(PointerIcon.Default, true),
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = stringResource(Res.string.AppDateSelectionFieldSelect),
                        )
                    }

                    // Button to clear input.
                    if (clearable && value != null) {
                        IconButton(
                            onClick = { onValueChange(null) },
                            modifier = Modifier.pointerHoverIcon(PointerIcon.Default, true),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = stringResource(Res.string.AppDateSelectionFieldClear),
                            )
                        }
                    }
                }
            },
            readOnly = true,
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }

    // Show dialog, if requested.
    if (showDialog) {
        DateDialog(
            value = value,
            onValueChange = {
                onValueChange(it)
                showDialog = false
            },
            onDismiss = {
                showDialog = false
            },
        )
    }
}

@Composable
fun DateField(
    label: StringResource,
    value: LocalDate?,
    clearable: Boolean = false,
    requiredIndicator: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null,
    onValueChange: (LocalDate?) -> Unit,
    modifier: Modifier = Modifier,
) = DateField(
    label = stringResource(label).title(),
    value = value,
    clearable = clearable,
    requiredIndicator = requiredIndicator,
    supportingText = supportingText,
    onValueChange = onValueChange,
    modifier = modifier,
)

@Composable
@Suppress("unused")
fun DateField(
    label: PluralStringResource,
    labelQuantity: Int = 1,
    value: LocalDate?,
    clearable: Boolean = false,
    requiredIndicator: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null,
    onValueChange: (LocalDate?) -> Unit,
    modifier: Modifier = Modifier,
) = DateField(
    label = pluralStringResource(label, labelQuantity).title(),
    value = value,
    clearable = clearable,
    requiredIndicator = requiredIndicator,
    supportingText = supportingText,
    onValueChange = onValueChange,
    modifier = modifier,
)
