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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import de.openindex.zugferd.manager.utils.stringResource
import de.openindex.zugferd.manager.utils.title
import de.openindex.zugferd.manager.utils.translate
import org.jetbrains.compose.resources.Resource
import org.jetbrains.compose.resources.StringResource

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun <T> AutoCompleteField(
    label: String,
    entry: T? = null,
    entries: Map<T, String>,
    requiredIndicator: Boolean = false,
    onSelect: (T?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isEntrySelected by remember(entry) { mutableStateOf(entry != null) }

    var textValue by remember(entry) {
        mutableStateOf((if (entry != null) entries[entry] else null) ?: "")
    }

    val compactColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    )

    Column(modifier = modifier) {
        FieldLabel(
            text = label.title(),
            requiredIndicator = requiredIndicator,
            modifier = Modifier.padding(bottom = 4.dp),
        )

        // Just render a regular text field with a clear button,
        // if a value is selected.
        if (isEntrySelected) {
            OutlinedTextField(
                value = textValue,
                onValueChange = { textValue = it },
                readOnly = true,
                singleLine = true,
                trailingIcon = {
                    IconButton(
                        onClick = { onSelect(null) },
                        modifier = Modifier.pointerHoverIcon(PointerIcon.Default, true),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Auswahl leeren",
                        )
                    }
                },
                shape = RoundedCornerShape(8.dp),
                colors = compactColors,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // Render an autocomplete dropdown field,
        // if no value is selected.
        else {
            val filteredOptions = entries
                .filter {
                    val query = textValue.trim()
                    query.isBlank() || it.value.lowercase().contains(query.lowercase())
                }
                .toList()

            val (allowExpanded, setExpanded) = remember { mutableStateOf(false) }
            val expanded = allowExpanded && filteredOptions.isNotEmpty()
            var ignoreFocusChange by remember { mutableStateOf(false) }

            val focusRequester = remember { FocusRequester() }

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = setExpanded,
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = textValue,
                    onValueChange = { textValue = it },
                    singleLine = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = expanded,
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.SecondaryEditable)
                                .pointerHoverIcon(PointerIcon.Default, true),
                        )
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = compactColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { state ->
                            // Reset input field selection to the last valid value,
                            // if the focus on the text field is lost.
                            if (!state.hasFocus && !ignoreFocusChange) {
                                textValue = if (entry != null) entries[entry] ?: "" else ""
                                onSelect(entry)
                            }
                        }
                        .focusRequester(focusRequester)
                        .menuAnchor(MenuAnchorType.PrimaryEditable),
                )

                ExposedDropdownMenu(
                    modifier = Modifier.heightIn(max = 280.dp),
                    expanded = expanded,
                    onDismissRequest = { setExpanded(false) },
                ) {
                    filteredOptions.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = option.second,
                                    style = MaterialTheme.typography.bodyLarge,
                                    softWrap = false,
                                )
                            },
                            onClick = {
                                // Disable focus change handler,
                                // if the user made a selection in the dropdown menu.
                                ignoreFocusChange = true

                                textValue = option.second
                                onSelect(option.first)
                                setExpanded(false)
                                focusRequester.freeFocus()
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                        )
                    }
                }
            }
        }
    }
}

@Composable
@Suppress("unused")
fun <T> AutoCompleteField(
    label: Resource,
    entry: T? = null,
    entries: Map<T, StringResource>,
    requiredIndicator: Boolean = false,
    onSelect: (T?) -> Unit,
    modifier: Modifier = Modifier,
) = AutoCompleteField(
    label = label.translate(),
    entry = entry,
    entries = buildMap<T, String> {
        entries.entries.forEach { e -> put(e.key, stringResource(e.value)) }
    },
    requiredIndicator = requiredIndicator,
    onSelect = onSelect,
    modifier = modifier,
)
