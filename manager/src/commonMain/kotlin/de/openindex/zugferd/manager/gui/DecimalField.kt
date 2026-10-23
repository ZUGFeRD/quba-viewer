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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.openindex.zugferd.manager.utils.format
import de.openindex.zugferd.manager.utils.parseDouble
import de.openindex.zugferd.manager.utils.pluralStringResource
import de.openindex.zugferd.manager.utils.stringResource
import de.openindex.zugferd.manager.utils.title
import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.StringResource
import kotlin.math.max

@Composable
fun DecimalField(
    label: String,
    value: Double? = null,
    minPrecision: Int = 0,
    maxPrecision: Int = 2,
    onValueChange: (Double?) -> Unit,
    requiredIndicator: Boolean = false,
    modifier: Modifier = Modifier,
) = Column(modifier = modifier) {
    FieldLabel(
        text = label,
        requiredIndicator = requiredIndicator,
        modifier = Modifier.padding(bottom = 4.dp),
    )
    OutlinedTextField(
        value = value
            ?.format(
                minPrecision = if (minPrecision <= 0 && maxPrecision > 0) 1 else minPrecision,
                maxPrecision = maxPrecision,
                grouped = false,
            )
            ?: "",
        onValueChange = { newValue: String ->
            onValueChange(newValue.parseDouble())
        },
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Decimal
                .takeIf { max(minPrecision, maxPrecision) > 0 }
                ?: KeyboardType.Number,
        ),
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

@Composable
fun DecimalField(
    label: StringResource,
    value: Double? = null,
    minPrecision: Int = 0,
    maxPrecision: Int = 2,
    onValueChange: (Double?) -> Unit,
    requiredIndicator: Boolean = false,
    modifier: Modifier = Modifier,
) = DecimalField(
    label = stringResource(label).title(),
    value = value,
    minPrecision = minPrecision,
    maxPrecision = maxPrecision,
    onValueChange = onValueChange,
    requiredIndicator = requiredIndicator,
    modifier = modifier,
)

@Composable
@Suppress("unused")
fun DecimalField(
    label: PluralStringResource,
    labelQuantity: Int = 1,
    value: Double? = null,
    minPrecision: Int = 0,
    maxPrecision: Int = 2,
    onValueChange: (Double?) -> Unit,
    requiredIndicator: Boolean = false,
    modifier: Modifier = Modifier,
) = DecimalField(
    label = pluralStringResource(label, labelQuantity).title(),
    value = value,
    minPrecision = minPrecision,
    maxPrecision = maxPrecision,
    onValueChange = onValueChange,
    requiredIndicator = requiredIndicator,
    modifier = modifier,
)
