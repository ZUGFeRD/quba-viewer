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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.openindex.zugferd.manager.model.BankDetails
import de.openindex.zugferd.manager.model.Contact
import de.openindex.zugferd.manager.model.TradeParty
import de.openindex.zugferd.manager.utils.stringResource
import de.openindex.zugferd.manager.utils.title
import de.openindex.zugferd.manager.utils.translate
import de.openindex.zugferd.manager.theme.LocalQubaColors
import de.openindex.zugferd.manager.gui.QubaButton
import de.openindex.zugferd.manager.gui.QubaButtonVariant
import de.openindex.zugferd.quba.generated.resources.AppTradePartyDialogAccount
import de.openindex.zugferd.quba.generated.resources.AppTradePartyDialogAccountBIC
import de.openindex.zugferd.quba.generated.resources.AppTradePartyDialogAccountCreditorReferenceId
import de.openindex.zugferd.quba.generated.resources.AppTradePartyDialogAccountDirectDebitMandateId
import de.openindex.zugferd.quba.generated.resources.AppTradePartyDialogAccountIBAN
import de.openindex.zugferd.quba.generated.resources.AppTradePartyDialogAccountName
import de.openindex.zugferd.quba.generated.resources.AppTradePartyDialogAccountPreferredPaymentMethod
import de.openindex.zugferd.quba.generated.resources.AppTradePartyDialogCancel
import de.openindex.zugferd.quba.generated.resources.AppTradePartyDialogContact
import de.openindex.zugferd.quba.generated.resources.AppTradePartyDialogContactCountry
import de.openindex.zugferd.quba.generated.resources.AppTradePartyDialogContactEmail
import de.openindex.zugferd.quba.generated.resources.AppTradePartyDialogContactFax
import de.openindex.zugferd.quba.generated.resources.AppTradePartyDialogContactLocation
import de.openindex.zugferd.quba.generated.resources.AppTradePartyDialogContactName
import de.openindex.zugferd.quba.generated.resources.AppTradePartyDialogContactPhone
import de.openindex.zugferd.quba.generated.resources.AppTradePartyDialogContactStreet
import de.openindex.zugferd.quba.generated.resources.AppTradePartyDialogContactZip
import de.openindex.zugferd.quba.generated.resources.AppTradePartyDialogGeneral
import de.openindex.zugferd.quba.generated.resources.AppTradePartyDialogGeneralAdditionalAddress
import de.openindex.zugferd.quba.generated.resources.AppTradePartyDialogGeneralTaxHint
import de.openindex.zugferd.quba.generated.resources.AppTradePartyDialogGeneralCountry
import de.openindex.zugferd.quba.generated.resources.AppTradePartyDialogGeneralCustomerId
import de.openindex.zugferd.quba.generated.resources.AppTradePartyDialogGeneralLocation
import de.openindex.zugferd.quba.generated.resources.AppTradePartyDialogGeneralName
import de.openindex.zugferd.quba.generated.resources.AppTradePartyDialogGeneralRegisterNr
import de.openindex.zugferd.quba.generated.resources.AppTradePartyDialogGeneralStreet
import de.openindex.zugferd.quba.generated.resources.AppTradePartyDialogGeneralTaxId
import de.openindex.zugferd.quba.generated.resources.AppTradePartyDialogGeneralVatId
import de.openindex.zugferd.quba.generated.resources.AppTradePartyDialogGeneralZip
import de.openindex.zugferd.quba.generated.resources.AppTradePartyDialogNotes
import de.openindex.zugferd.quba.generated.resources.AppTradePartyDialogNotesDescription
import de.openindex.zugferd.quba.generated.resources.AppTradePartyDialogNotesImprint
import de.openindex.zugferd.quba.generated.resources.AppTradePartyDialogNotesImprintInfo
import de.openindex.zugferd.quba.generated.resources.AppTradePartyDialogSavePermanently
import de.openindex.zugferd.quba.generated.resources.AppTradePartyDialogSubmit
import de.openindex.zugferd.quba.generated.resources.Res
import org.jetbrains.compose.resources.Resource
import org.jetbrains.compose.resources.StringResource

private enum class TradePartyForm {
    GENERAL,
    CONTACT,
    ACCOUNT,
    NOTES;

    fun title(): StringResource = when (this) {
        GENERAL -> Res.string.AppTradePartyDialogGeneral
        CONTACT -> Res.string.AppTradePartyDialogContact
        ACCOUNT -> Res.string.AppTradePartyDialogAccount
        NOTES -> Res.string.AppTradePartyDialogNotes
    }
}

@Composable
fun TradePartyDialog(
    title: String,
    value: TradeParty,
    isCustomer: Boolean = false,
    permanentSaveOption: Boolean = false,
    onDismissRequest: () -> Unit,
    onSubmitRequest: (tradeParty: TradeParty, savePermanently: Boolean) -> Unit,
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
        TradePartyDialogContent(
            //title = title,
            title = null,
            value = value,
            isCustomer = isCustomer,
            permanentSaveOption = permanentSaveOption,
            onDismissRequest = {
                onDismissRequest()
                //appState.setLocked(false)
            },
            onSubmitRequest = { tradeParty, savePermanently ->
                onSubmitRequest(tradeParty, savePermanently)
                //appState.setLocked(false)
            },
        )
    }

    /*
    Dialog(
        onDismissRequest = onDismissRequest,
    ) {
        TradePartyDialogContent(
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
fun TradePartyDialog(
    title: Resource,
    value: TradeParty,
    isCustomer: Boolean = false,
    permanentSaveOption: Boolean = false,
    onDismissRequest: () -> Unit,
    onSubmitRequest: (tradeParty: TradeParty, savePermanently: Boolean) -> Unit,
) = TradePartyDialog(
    title = title.translate().title(),
    value = value,
    isCustomer = isCustomer,
    permanentSaveOption = permanentSaveOption,
    onDismissRequest = onDismissRequest,
    onSubmitRequest = onSubmitRequest,
)

/**
 * Contents of the trade party dialog.
 */
@Composable
@Suppress("SameParameterValue")
private fun TradePartyDialogContent(
    title: String?,
    value: TradeParty,
    isCustomer: Boolean = false,
    permanentSaveOption: Boolean,
    onDismissRequest: () -> Unit,
    onSubmitRequest: (tradeParty: TradeParty, savePermanently: Boolean) -> Unit,
) {
    var tradeParty by remember { mutableStateOf(value.copy()) }
    var savePermanently by remember { mutableStateOf(false) }

    // Übernehmen is only allowed when name is filled, and for senders also vatID or taxID.
    val isTradePartyValid by remember(tradeParty, isCustomer) {
        mutableStateOf(
            tradeParty.name.isNotBlank() &&
                (isCustomer || !tradeParty.vatID.isNullOrBlank() || !tradeParty.taxID.isNullOrBlank())
        )
    }

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
                    val colors = LocalQubaColors.current
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(color = colors.accentSoft)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    ) {
                        Text(
                            text = title,
                            softWrap = false,
                            color = colors.accent,
                            style = MaterialTheme.typography.titleLarge,
                        )
                    }
                }

                // Tab state lives here so the TabRow stays outside the scroll area.
                var formState by remember { mutableStateOf(0) }

                // Tab bar — always visible, not inside the scroll box.
                TabRow(selectedTabIndex = formState) {
                    TradePartyForm.entries.forEachIndexed { index, form ->
                        Tab(
                            selected = formState == index,
                            onClick = { formState = index },
                            text = { Label(text = form.title()) },
                        )
                    }
                }

                // Trade party form — scrollable so content never hides the bottom buttons.
                VerticalScrollBox(modifier = Modifier.weight(1f)) {
                    TradePartyForm(
                        value = tradeParty,
                        isCustomer = isCustomer,
                        state = formState,
                        onUpdate = { tradeParty = it },
                    )
                }

                // Bottom row — always visible (not inside scroll).
                androidx.compose.material3.HorizontalDivider(
                    color = LocalQubaColors.current.border,
                    thickness = 1.dp,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.Start),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                ) {
                    // Submit button — disabled when required fields are missing.
                    QubaButton(
                        label = stringResource(Res.string.AppTradePartyDialogSubmit).title(),
                        onClick = { onSubmitRequest(tradeParty, savePermanently) },
                        enabled = isTradePartyValid,
                        variant = QubaButtonVariant.Primary,
                    )

                    // Save permanently toggle.
                    if (permanentSaveOption) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                ) { savePermanently = !savePermanently },
                        ) {
                            val colors = LocalQubaColors.current
                            Switch(
                                checked = savePermanently,
                                onCheckedChange = { savePermanently = it },
                                colors = SwitchDefaults.colors(
                                    checkedTrackColor    = colors.accent,
                                    checkedThumbColor    = colors.surface,
                                    uncheckedTrackColor  = colors.border,
                                    uncheckedBorderColor = colors.text3,
                                    uncheckedThumbColor  = colors.text3,
                                ),
                            )
                            Label(
                                text = stringResource(Res.string.AppTradePartyDialogSavePermanently),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f, true))

                    // Cancel button.
                    QubaButton(
                        label = stringResource(Res.string.AppTradePartyDialogCancel).title(),
                        onClick = { onDismissRequest() },
                        variant = QubaButtonVariant.Ghost,
                    )
                }
            }
        }
    }
}

/**
 * Trade party form within the dialog.
 */
@Composable
private fun TradePartyForm(
    value: TradeParty,
    isCustomer: Boolean,
    state: Int,
    onUpdate: (tradeParty: TradeParty) -> Unit,
) {
    // Contents for each tab.
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .fillMaxWidth()
    ) {
        // General form.
        AnimatedVisibility(visible = state == 0) {
            TradePartyFormGeneral(
                value = value,
                isCustomer = isCustomer,
                onUpdate = onUpdate,
            )
        }

        // Contact form.
        AnimatedVisibility(visible = state == 1) {
            TradePartyFormContact(
                value = value,
                isCustomer = isCustomer,
                onUpdate = onUpdate,
            )
        }

        // Account form.
        AnimatedVisibility(visible = state == 2) {
            TradePartyFormAccount(
                value = value,
                isCustomer = isCustomer,
                onUpdate = onUpdate,
            )
        }

        // Notes form.
        AnimatedVisibility(visible = state == 3) {
            TradePartyFormNotes(
                value = value,
                isCustomer = isCustomer,
                onUpdate = onUpdate,
            )
        }
    }
}

/**
 * Trade party general form.
 */
@Composable
private fun TradePartyFormGeneral(
    value: TradeParty,
    isCustomer: Boolean,
    onUpdate: (tradeParty: TradeParty) -> Unit,
) {
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
            // Trade party name field.
            TextField(
                label = Res.string.AppTradePartyDialogGeneralName,
                value = value.name,
                requiredIndicator = true,
                onValueChange = { newName ->
                    onUpdate(
                        value.copy(name = newName)
                    )
                },
                modifier = Modifier
                    .weight(0.67f),
            )

            if (isCustomer) {
                // Trade party customer ID field.
                TextField(
                    label = Res.string.AppTradePartyDialogGeneralCustomerId,
                    value = value.id ?: "",
                    onValueChange = { newID ->
                        onUpdate(
                            value.copy(id = newID)
                        )
                    },
                    modifier = Modifier
                        .weight(0.33f),
                )
            }
        }

        // Trade party street field.
        TextField(
            label = Res.string.AppTradePartyDialogGeneralStreet,
            value = value.street ?: "",
            onValueChange = { newStreet ->
                onUpdate(
                    value.copy(street = newStreet)
                )
            },
            modifier = Modifier
                .fillMaxWidth(),
        )

        // Trade party additional address field.
        TextField(
            label = Res.string.AppTradePartyDialogGeneralAdditionalAddress,
            value = value.additionalAddress ?: "",
            onValueChange = { newAdditionalAddress ->
                onUpdate(
                    value.copy(additionalAddress = newAdditionalAddress)
                )
            },
            modifier = Modifier
                .fillMaxWidth(),
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            // Trade party zip field.
            TextField(
                label = Res.string.AppTradePartyDialogGeneralZip,
                value = value.zip ?: "",
                onValueChange = { newZip ->
                    onUpdate(
                        value.copy(zip = newZip)
                    )
                },
                modifier = Modifier
                    .weight(0.3f),
            )

            // Trade party location field.
            TextField(
                label = Res.string.AppTradePartyDialogGeneralLocation,
                value = value.location ?: "",
                onValueChange = { newLocation ->
                    onUpdate(
                        value.copy(location = newLocation)
                    )
                },
                modifier = Modifier
                    .weight(0.7f),
            )
        }

        // Trade party country field.
        CountryField(
            label = Res.string.AppTradePartyDialogGeneralCountry,
            country = value.country,
            onSelect = { newCountry ->
                onUpdate(
                    value.copy(country = newCountry)
                )
            },
            modifier = Modifier
                .fillMaxWidth(),
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            // Trade party VAT ID field.
            TextField(
                label = Res.string.AppTradePartyDialogGeneralVatId,
                value = value.vatID ?: "",
                requiredIndicator = !isCustomer,
                onValueChange = { newVatID ->
                    onUpdate(
                        value.copy(vatID = newVatID)
                    )
                },
                modifier = Modifier
                    .weight(0.33f),
            )

            // Trade party tax ID field.
            TextField(
                label = Res.string.AppTradePartyDialogGeneralTaxId,
                value = value.taxID ?: "",
                requiredIndicator = !isCustomer,
                onValueChange = { newTaxID ->
                    onUpdate(
                        value.copy(taxID = newTaxID)
                    )
                },
                modifier = Modifier
                    .weight(0.33f),
            )

            // Trade party register nr field.
            TextField(
                label = Res.string.AppTradePartyDialogGeneralRegisterNr,
                value = value.registerNr ?: "",
                onValueChange = { newRegisterNr ->
                    onUpdate(
                        value.copy(registerNr = newRegisterNr)
                    )
                },
                modifier = Modifier
                    .weight(0.33f),
            )
        }

        // Hint that at least one of VAT ID / Tax ID is required for the sender.
        if (!isCustomer) {
            Text(
                text = stringResource(Res.string.AppTradePartyDialogGeneralTaxHint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp),
            )
        }
    }
}

/**
 * Trade party contact form.
 */
@Composable
@Suppress("UNUSED_PARAMETER")
private fun TradePartyFormContact(
    value: TradeParty,
    isCustomer: Boolean,
    onUpdate: (tradeParty: TradeParty) -> Unit,
) {
    val contact by remember(value.contact) {
        mutableStateOf(value.contact ?: Contact())
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        // Contact name field.
        TextField(
            label = Res.string.AppTradePartyDialogContactName,
            value = contact.name,
            onValueChange = { newName ->
                onUpdate(
                    value.copy(contact = contact.copy(name = newName))
                )
            },
            modifier = Modifier
                .fillMaxWidth(),
        )

        // Contact street field.
        TextField(
            label = Res.string.AppTradePartyDialogContactStreet,
            value = contact.street ?: "",
            onValueChange = { newStreet ->
                onUpdate(
                    value.copy(contact = contact.copy(street = newStreet))
                )
            },
            modifier = Modifier
                .fillMaxWidth(),
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            // Contact zip field.
            TextField(
                label = Res.string.AppTradePartyDialogContactZip,
                value = contact.zip ?: "",
                onValueChange = { newZip ->
                    onUpdate(
                        value.copy(contact = contact.copy(zip = newZip))
                    )
                },
                modifier = Modifier
                    .weight(0.3f),
            )

            // Contact location field.
            TextField(
                label = Res.string.AppTradePartyDialogContactLocation,
                value = contact.location ?: "",
                onValueChange = { newLocation ->
                    onUpdate(
                        value.copy(contact = contact.copy(location = newLocation))
                    )
                },
                modifier = Modifier
                    .weight(0.7f),
            )
        }

        // Contact country field.
        CountryField(
            label = Res.string.AppTradePartyDialogContactCountry,
            country = contact.country,
            onSelect = { newCountry ->
                onUpdate(
                    value.copy(contact = contact.copy(country = newCountry))
                )
            },
            modifier = Modifier
                .fillMaxWidth(),
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            // Contact email field.
            TextField(
                label = Res.string.AppTradePartyDialogContactEmail,
                value = contact.email ?: "",
                onValueChange = { newEmail ->
                    onUpdate(
                        value.copy(contact = contact.copy(email = newEmail))
                    )
                },
                modifier = Modifier
                    .weight(0.33f),
            )

            // Contact phone field.
            TextField(
                label = Res.string.AppTradePartyDialogContactPhone,
                value = contact.phone ?: "",
                onValueChange = { newPhone ->
                    onUpdate(
                        value.copy(contact = contact.copy(phone = newPhone))
                    )
                },
                modifier = Modifier
                    .weight(0.33f),
            )

            // Contact fax field.
            TextField(
                label = Res.string.AppTradePartyDialogContactFax,
                value = contact.fax ?: "",
                onValueChange = { newFax ->
                    onUpdate(
                        value.copy(contact = contact.copy(fax = newFax))
                    )
                },
                modifier = Modifier
                    .weight(0.33f),
            )
        }
    }
}

/**
 * Trade party account form.
 */
@Composable
private fun TradePartyFormAccount(
    value: TradeParty,
    isCustomer: Boolean,
    onUpdate: (tradeParty: TradeParty) -> Unit,
) {
    val account by remember(value.bankDetails) {
        mutableStateOf(value.bankDetails.firstOrNull() ?: BankDetails())
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        // Account name field.
        TextField(
            label = Res.string.AppTradePartyDialogAccountName,
            value = account.accountName ?: "",
            onValueChange = { newAccountName ->
                onUpdate(
                    value.copy(bankDetails = listOf(account.copy(accountName = newAccountName)))
                )
            },
            modifier = Modifier
                .fillMaxWidth(),
        )

        // Account IBAN field.
        TextField(
            label = Res.string.AppTradePartyDialogAccountIBAN,
            value = account.iban,
            onValueChange = { newIban ->
                onUpdate(
                    value.copy(bankDetails = listOf(account.copy(iban = newIban)))
                )
            },
            modifier = Modifier
                .fillMaxWidth(),
        )

        // Account BIC field.
        TextField(
            label = Res.string.AppTradePartyDialogAccountBIC,
            value = account.bic ?: "",
            onValueChange = { newBic ->
                onUpdate(
                    value.copy(bankDetails = listOf(account.copy(bic = newBic)))
                )
            },
            modifier = Modifier
                .fillMaxWidth(),
        )

        // Following fields are only valid for trade parties, that issue an e-invoice.
        if (!isCustomer) {
            // Account creditor reference ID field.
            TextField(
                label = Res.string.AppTradePartyDialogAccountCreditorReferenceId,
                value = value.creditorReferenceId ?: "",
                onValueChange = { newCreditorReferenceId ->
                    onUpdate(
                        value.copy(creditorReferenceId = newCreditorReferenceId)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth(),
            )
        }

        // Following fields are only valid for trade parties, that receive an e-invoice.
        if (isCustomer) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                // Account preferred payment method field.
                PaymentMethodField(
                    label = Res.string.AppTradePartyDialogAccountPreferredPaymentMethod,
                    value = value._defaultPaymentMethod,
                    onSelect = { newDefaultPaymentMethod ->
                        onUpdate(
                            value.copy(_defaultPaymentMethod = newDefaultPaymentMethod)
                        )
                    },
                    modifier = Modifier
                        .weight(0.5f, fill = true),
                )

                // Account direct debit mandate field.
                TextField(
                    label = Res.string.AppTradePartyDialogAccountDirectDebitMandateId,
                    value = account.directDebitMandateId ?: "",
                    onValueChange = { newDirectDebitMandateId ->
                        onUpdate(
                            value.copy(bankDetails = listOf(account.copy(directDebitMandateId = newDirectDebitMandateId)))
                        )
                    },
                    modifier = Modifier
                        .weight(0.5f, fill = true),
                )
            }
        }
    }
}

/**
 * Trade party notes form.
 */
@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun TradePartyFormNotes(
    value: TradeParty,
    isCustomer: Boolean,
    onUpdate: (tradeParty: TradeParty) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        // Description field.
        TextField(
            label = Res.string.AppTradePartyDialogNotesDescription,
            value = value.description ?: "",
            singleLine = false,
            onValueChange = { newDescription ->
                onUpdate(
                    value.copy(description = newDescription)
                )
            },
            modifier = Modifier
                .heightIn(min = 150.dp, max = 300.dp.takeIf { isCustomer } ?: 200.dp)
                .fillMaxWidth(),
        )

        // Following fields are only valid for trade parties, that issue an e-invoice.
        if (!isCustomer) {
            // Imprint field.
            Tooltip(
                text = Res.string.AppTradePartyDialogNotesImprintInfo,
            ) {
                TextField(
                    label = Res.string.AppTradePartyDialogNotesImprint,
                    value = value.imprint ?: "",
                    singleLine = false,
                    onValueChange = {
                        onUpdate(
                            value.copy(
                                imprint = it
                            )
                        )
                    },
                    modifier = Modifier
                        .heightIn(min = 150.dp, max = 200.dp)
                        .fillMaxWidth(),
                )
            }
        }
    }
}
