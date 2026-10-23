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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.openindex.zugferd.manager.LocalAppState
import de.openindex.zugferd.manager.model.Product
import de.openindex.zugferd.manager.model.TaxCategory
import de.openindex.zugferd.manager.model.UnitOfMeasurement
import de.openindex.zugferd.manager.utils.getString
import de.openindex.zugferd.manager.utils.stringResource
import de.openindex.zugferd.manager.utils.title
import de.openindex.zugferd.manager.utils.translate
import de.openindex.zugferd.quba.generated.resources.AppProductDialogCancel
import de.openindex.zugferd.quba.generated.resources.AppProductDialogGeneral
import de.openindex.zugferd.quba.generated.resources.AppProductDialogGeneralName
import de.openindex.zugferd.quba.generated.resources.AppProductDialogGeneralPricePerUnit
import de.openindex.zugferd.quba.generated.resources.AppProductDialogGeneralTax
import de.openindex.zugferd.quba.generated.resources.AppProductDialogGeneralTaxExemptionReason
import de.openindex.zugferd.quba.generated.resources.AppProductDialogNotes
import de.openindex.zugferd.quba.generated.resources.AppProductDialogNotesDescription
import de.openindex.zugferd.quba.generated.resources.AppProductDialogSavePermanently
import de.openindex.zugferd.quba.generated.resources.AppProductDialogSubmit
import de.openindex.zugferd.quba.generated.resources.Res
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.Resource
import org.jetbrains.compose.resources.StringResource

private enum class ProductForm {
    GENERAL,
    NOTES;

    fun title(): StringResource = when (this) {
        GENERAL -> Res.string.AppProductDialogGeneral
        NOTES -> Res.string.AppProductDialogNotes
    }
}

@Composable
fun ProductDialog(
    title: String,
    value: Product,
    permanentSaveOption: Boolean = false,
    onDismissRequest: () -> Unit,
    onSubmitRequest: (product: Product, savePermanently: Boolean) -> Unit,
) {
    //val appState = LocalAppState.current
    //appState.setLocked(true)

    DialogWindow(
        onCloseRequest = {
            onDismissRequest()
            //appState.setLocked(false)
        },
        title = title,
        width = 700.dp,
        height = 600.dp,
    ) {
        ProductDialogContent(
            //title = title,
            title = null,
            value = value,
            permanentSaveOption = permanentSaveOption,
            onDismissRequest = {
                onDismissRequest()
                //appState.setLocked(false)
            },
            onSubmitRequest = { product, savePermanently ->
                onSubmitRequest(product, savePermanently)
                //appState.setLocked(false)
            },
        )
    }

    /*
    Dialog(
        onDismissRequest = onDismissRequest,
    ) {
        ProductDialogContent(
            title = title,
            value = value,
            onDismissRequest = onDismissRequest,
            onSubmitRequest = onSubmitRequest,
        )
    }
    */
}

@Composable
@Suppress("unused")
fun ProductDialog(
    title: Resource,
    value: Product,
    permanentSaveOption: Boolean = false,
    onDismissRequest: () -> Unit,
    onSubmitRequest: (product: Product, savePermanently: Boolean) -> Unit,
) = ProductDialog(
    title = title.translate().title(),
    value = value,
    permanentSaveOption = permanentSaveOption,
    onDismissRequest = onDismissRequest,
    onSubmitRequest = onSubmitRequest,
)

/**
 * Contents of the product dialog.
 */
@Composable
@Suppress("SameParameterValue", "DuplicatedCode")
private fun ProductDialogContent(
    title: String?,
    value: Product,
    permanentSaveOption: Boolean,
    onDismissRequest: () -> Unit,
    onSubmitRequest: (product: Product, savePermanently: Boolean) -> Unit,
) {
    var product by remember { mutableStateOf(value.copy()) }
    var savePermanently by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .border(border = BorderStroke(Dp.Hairline, MaterialTheme.colorScheme.primaryContainer))
            .shadow(elevation = 12.dp)
            //.fillMaxWidth(),
            .fillMaxWidth(),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
            ) {
                // Dialog title, if available.
                if (title != null) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(color = MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    ) {
                        Text(
                            text = title,
                            softWrap = false,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.titleLarge,
                        )
                    }
                }

                // Product form.
                ProductForm(
                    value = product,
                    onUpdate = { product = it },
                )

                Spacer(Modifier.weight(1f, true))

                // Bottom row.
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    // Submit button.
                    Button(
                        onClick = {
                            onSubmitRequest(
                                product,
                                savePermanently,
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    ) {
                        Label(
                            text = Res.string.AppProductDialogSubmit,
                        )
                    }

                    // Save permanently toggle.
                    if (permanentSaveOption) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .clickable { savePermanently = !savePermanently },
                        ) {
                            Switch(
                                checked = savePermanently,
                                onCheckedChange = { savePermanently = it },
                            )
                            Label(
                                // Evaluate translation string immediately,
                                // to avoid title conversion within the label component.
                                text = stringResource(Res.string.AppProductDialogSavePermanently),
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier
                            .weight(1f, true),
                    )

                    // Cancel button.
                    Button(
                        onClick = { onDismissRequest() },
                    ) {
                        Label(
                            text = Res.string.AppProductDialogCancel,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Product form within the dialog.
 */
@Composable
private fun ProductForm(
    value: Product,
    onUpdate: (product: Product) -> Unit,
) {
    var state by remember { mutableStateOf(0) }

    // Available tabs.
    TabRow(
        selectedTabIndex = state,
    ) {
        ProductForm.entries.forEachIndexed { index, form ->
            Tab(
                selected = state == index,
                onClick = { state = index },
                text = {
                    Label(
                        text = form.title(),
                    )
                },
            )
        }
    }

    // Contents for each tab.
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .fillMaxWidth()
    ) {
        // General form.
        AnimatedVisibility(visible = state == 0) {
            ProductFormGeneral(
                value = value,
                onUpdate = onUpdate,
            )
        }

        // Notes form.
        AnimatedVisibility(visible = state == 1) {
            ProductFormNotes(
                value = value,
                onUpdate = onUpdate,
            )
        }
    }
}

/**
 * Product general form.
 */
@Composable
private fun ProductFormGeneral(
    value: Product,
    onUpdate: (product: Product) -> Unit,
) {
    val scope = rememberCoroutineScope()

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            // Product name field.
            TextField(
                label = Res.string.AppProductDialogGeneralName,
                value = value.name,
                requiredIndicator = true,
                onValueChange = { newName ->
                    onUpdate(
                        value.copy(name = newName)
                    )
                },
                modifier = Modifier
                    .weight(1f, fill = true),
            )

            // Unit of measurement field.
            UnitOfMeasurementField(
                value = UnitOfMeasurement.getByCode(value.unit) ?: UnitOfMeasurement.UNIT,
                requiredIndicator = true,
                onSelect = { newUnit ->
                    onUpdate(
                        value.copy(unit = newUnit.code)
                    )
                },
                modifier = Modifier
                    .width(150.dp),
            )

            // Price per unit field.
            DecimalField(
                label = Res.string.AppProductDialogGeneralPricePerUnit,
                value = value._defaultPricePerUnit,
                requiredIndicator = true,
                minPrecision = 2,
                maxPrecision = 2,
                onValueChange = { newPrice ->
                    if (newPrice != null) {
                        onUpdate(
                            value.copy(_defaultPricePerUnit = newPrice)
                        )
                    }
                },
                modifier = Modifier
                    .width(150.dp),
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            val preferences = LocalAppState.current.preferences
            val defaultTaxPercentage = preferences.vatPercentage

            // Tax category field.
            TaxCategoryField(
                value = TaxCategory.getByCode(value.taxCategoryCode) ?: TaxCategory.NORMAL_TAX,
                requiredIndicator = true,
                onSelect = { newTax ->
                    scope.launch {
                        val taxExemptionReason = if (newTax.defaultExemptionReason != null)
                            getString(newTax.defaultExemptionReason)
                        else
                            null

                        onUpdate(
                            value.copy(
                                taxCategoryCode = newTax.code,
                                vatPercent = defaultTaxPercentage.takeUnless { newTax.isZeroTax } ?: 0.toDouble(),
                                taxExemptionReason = taxExemptionReason,
                            )
                        )
                    }
                },
                modifier = Modifier
                    .weight(1f, fill = true),
            )

            // Tax percentage field.
            DecimalField(
                label = Res.string.AppProductDialogGeneralTax,
                value = value.vatPercent,
                requiredIndicator = true,
                maxPrecision = 1,
                minPrecision = 0,
                onValueChange = { newVat ->
                    if (newVat != null) {
                        onUpdate(
                            value.copy(
                                vatPercent = newVat,
                                taxExemptionReason = if (newVat > 0) "" else value.taxExemptionReason,
                            )
                        )
                    }
                },
                modifier = Modifier
                    .width(150.dp),
            )
        }

        // Tax exemption reason field.
        AnimatedVisibility(visible = value.vatPercent <= 0.0) {
            TextField(
                label = Res.string.AppProductDialogGeneralTaxExemptionReason,
                value = value.taxExemptionReason ?: "",
                requiredIndicator = true,
                singleLine = false,
                onValueChange = { newReason ->
                    onUpdate(
                        value.copy(taxExemptionReason = newReason)
                    )
                },
                modifier = Modifier
                    .heightIn(min = 150.dp, max = 300.dp)
                    .fillMaxWidth(),
            )
        }
    }
}

/**
 * Product notes form.
 */
@Composable
private fun ProductFormNotes(
    value: Product,
    onUpdate: (product: Product) -> Unit,
) {
    // Product description field.
    TextField(
        label = Res.string.AppProductDialogNotesDescription,
        value = value.description ?: "",
        singleLine = false,
        onValueChange = { newDescription ->
            onUpdate(
                value.copy(
                    description = newDescription
                )
            )
        },
        modifier = Modifier
            .heightIn(min = 150.dp, max = 300.dp)
            .fillMaxWidth(),
    )
}
