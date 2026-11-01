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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.awtEventOrNull
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.dp
import de.openindex.zugferd.manager.LocalAppState
import de.openindex.zugferd.manager.model.Product
import de.openindex.zugferd.manager.model.TradeParty
import de.openindex.zugferd.manager.utils.trimToNull
import de.openindex.zugferd.quba.generated.resources.AppSettingsItemAdd
import de.openindex.zugferd.quba.generated.resources.AppSettingsItemEdit
import de.openindex.zugferd.quba.generated.resources.AppSettingsItemRemove
import de.openindex.zugferd.quba.generated.resources.Res

interface ItemSettingsContext {
    fun closeDialog()
}

/**
 * A generic implementation to manage a pool of items.
 */
@Composable
@OptIn(ExperimentalLayoutApi::class, ExperimentalComposeUiApi::class)
fun <T, K> ItemSettings(
    items: List<T>,
    itemKey: (T) -> K,
    itemText: (T) -> String,
    itemCreate: () -> T,
    onRemove: (T) -> Unit,
    dialog: @Composable ItemSettingsContext.(item: T) -> Unit,
) {
    val selectedItem = remember { mutableStateOf<T?>(null) }
    val editedItem = remember { mutableStateOf<T?>(null) }

    fun getItemKey(item: T?): K? {
        return if (item != null) {
            itemKey(item)
        } else {
            null
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier,
    ) {
        if (items.isNotEmpty()) {
            Card(
                modifier = Modifier,
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(0.dp, 400.dp)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    items.forEach { item ->
                        TextButton(
                            onClick = {
                                //tradePartySelected.value = item
                                //    .takeIf { tradePartySelected.value != item }
                                selectedItem.value = item
                            },
                            shape = MaterialTheme.shapes.medium,
                            colors = if (getItemKey(selectedItem.value) != getItemKey(item))
                                ButtonDefaults.textButtonColors()
                            else
                                ButtonDefaults.textButtonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = Color.White,
                                ),
                            modifier = Modifier
                                .onPointerEvent(PointerEventType.Press) {
                                    if (it.awtEventOrNull?.clickCount != 2) {
                                        return@onPointerEvent
                                    }
                                    editedItem.value = selectedItem.value
                                },
                        ) {
                            Label(
                                text = itemText(item).trimToNull() ?: "???",
                            )
                        }
                    }
                }
            }
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier,
        ) {
            // Button to add a new item.
            Button(
                onClick = {
                    editedItem.value = itemCreate()
                },
            ) {
                Label(
                    text = Res.string.AppSettingsItemAdd,
                )
            }

            // Button to edit the currently selected item.
            AnimatedVisibility(visible = selectedItem.value != null) {
                Button(
                    onClick = {
                        editedItem.value = selectedItem.value
                        //tradePartySelected.value = null
                    },
                ) {
                    Label(
                        text = Res.string.AppSettingsItemEdit,
                    )
                }
            }

            // Button to remove the currently selected item.
            AnimatedVisibility(visible = selectedItem.value != null) {
                Button(
                    onClick = {
                        val tradeParty = selectedItem.value
                        if (tradeParty != null) {
                            onRemove(tradeParty)
                            editedItem.value = null
                            selectedItem.value = null
                        }
                    },
                ) {
                    Label(
                        text = Res.string.AppSettingsItemRemove,
                    )
                }
            }
        }
    }

    if (editedItem.value != null) {
        val item = editedItem.value!!
        val context = object : ItemSettingsContext {
            override fun closeDialog() {
                editedItem.value = null
                selectedItem.value = null
            }
        }
        context.dialog(item)
    }
}

/**
 * Manage a pool of trade party items.
 */
@Composable
fun TradePartyItemSettings(
    tradeParties: List<TradeParty>,
    isCustomer: Boolean = false,
    onSave: (TradeParty) -> Unit,
    onRemove: (TradeParty) -> Unit,
    dialogTitle: @Composable (TradeParty?) -> String
) {
    val preferences = LocalAppState.current.preferences
    val defaultCountry = preferences.country

    ItemSettings(
        items = tradeParties,
        itemKey = { it._key },
        itemText = { it.summaryShort },
        itemCreate = {
            TradeParty(
                country = defaultCountry,
            )
        },
        onRemove = onRemove,
    ) { selectedItem ->
        TradePartyDialog(
            title = dialogTitle(selectedItem),
            value = selectedItem,
            isCustomer = isCustomer,
            onSubmitRequest = { updatedItem, _ ->
                onSave(updatedItem)
                closeDialog()
            },
            onDismissRequest = {
                closeDialog()
            },
        )
    }
}

/**
 * Manage a pool of product items.
 */
@Composable
fun ProductItemSettings(
    products: List<Product>,
    onSave: (Product) -> Unit,
    onRemove: (Product) -> Unit,
    dialogTitle: @Composable (Product?) -> String
) {
    val preferences = LocalAppState.current.preferences
    val defaultTaxPercentage = preferences.vatPercentage ?: 0.toDouble()

    ItemSettings(
        items = products,
        itemKey = { it._key },
        itemText = { it.getSummary(preferences) },
        itemCreate = { Product(vatPercent = defaultTaxPercentage) },
        onRemove = onRemove,
    ) { selectedItem ->
        ProductDialog(
            title = dialogTitle(selectedItem),
            value = selectedItem,
            onSubmitRequest = { updatedItem, _ ->
                onSave(updatedItem)
                closeDialog()
            },
            onDismissRequest = {
                closeDialog()
            },
        )
    }
}
