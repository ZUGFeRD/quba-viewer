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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.openindex.zugferd.manager.utils.pluralStringResource
import de.openindex.zugferd.manager.utils.stringResource
import de.openindex.zugferd.manager.utils.title
import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.StringResource
import androidx.compose.material3.OutlinedTextField as MaterialTextField

@Composable
fun TextField(
    label: String,
    value: String = "",
    requiredIndicator: Boolean = false,
    singleLine: Boolean = true,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) = Column(modifier = modifier) {
    FieldLabel(
        text = label,
        requiredIndicator = requiredIndicator,
        modifier = Modifier.padding(bottom = 4.dp),
    )
    MaterialTextField(
        value = value,
        singleLine = singleLine,
        onValueChange = onValueChange,
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

@Composable
fun TextField(
    label: StringResource,
    value: String = "",
    requiredIndicator: Boolean = false,
    singleLine: Boolean = true,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) = TextField(
    label = stringResource(label).title(),
    value = value,
    requiredIndicator = requiredIndicator,
    singleLine = singleLine,
    onValueChange = onValueChange,
    modifier = modifier,
)

@Composable
fun TextField(
    label: PluralStringResource,
    labelQuantity: Int = 1,
    value: String = "",
    requiredIndicator: Boolean = false,
    singleLine: Boolean = true,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) = TextField(
    label = pluralStringResource(label, labelQuantity).title(),
    value = value,
    requiredIndicator = requiredIndicator,
    singleLine = singleLine,
    onValueChange = onValueChange,
    modifier = modifier,
)
