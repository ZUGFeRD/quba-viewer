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

package de.openindex.zugferd.manager.sections

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipPlacement
import androidx.compose.foundation.clickable
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import de.openindex.zugferd.manager.theme.LocalQubaColors
import de.openindex.zugferd.manager.theme.LocalQubaTypography
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import de.openindex.zugferd.manager.LocalAppState
import de.openindex.zugferd.manager.gui.ActionButtonWithTooltip
import de.openindex.zugferd.manager.gui.CurrencyField
import de.openindex.zugferd.manager.gui.DateField
import de.openindex.zugferd.manager.gui.DecimalField
import de.openindex.zugferd.manager.gui.Label
import de.openindex.zugferd.manager.gui.NotificationBar
import de.openindex.zugferd.manager.gui.PaymentMethodField
import de.openindex.zugferd.manager.gui.PdfViewer
import de.openindex.zugferd.manager.gui.ProductFieldWithAdd
import de.openindex.zugferd.manager.gui.QubaButton
import de.openindex.zugferd.manager.gui.QubaButtonVariant
import de.openindex.zugferd.manager.gui.QubaCard
import de.openindex.zugferd.manager.gui.QubaSectionHeader
import de.openindex.zugferd.manager.gui.SectionTitle
import de.openindex.zugferd.manager.gui.TextField
import de.openindex.zugferd.manager.gui.Tooltip
import de.openindex.zugferd.manager.gui.TradePartyFieldWithAdd
import de.openindex.zugferd.manager.gui.VerticalScrollBox
import de.openindex.zugferd.manager.gui.XmlViewer
import de.openindex.zugferd.manager.model.Item
import de.openindex.zugferd.manager.model.UnitOfMeasurement
import de.openindex.zugferd.manager.utils.FALLBACK_CURRENCY
import de.openindex.zugferd.manager.utils.MAX_PDF_ARCHIVE_VERSION
import de.openindex.zugferd.manager.utils.createDragAndDropTarget
import de.openindex.zugferd.manager.utils.formatAsPercentage
import de.openindex.zugferd.manager.utils.formatPrice
import de.openindex.zugferd.manager.utils.pluralStringResource
import de.openindex.zugferd.manager.utils.stringResource
import de.openindex.zugferd.manager.utils.title
import de.openindex.zugferd.quba.generated.resources.AppCreate
import de.openindex.zugferd.quba.generated.resources.AppCreateConvert
import de.openindex.zugferd.quba.generated.resources.AppCreateConvertExperimental
import de.openindex.zugferd.quba.generated.resources.AppCreateConvertInfo
import de.openindex.zugferd.quba.generated.resources.AppCreateConvertWarning
import de.openindex.zugferd.quba.generated.resources.AppCreateDetailsPdf
import de.openindex.zugferd.quba.generated.resources.AppCreateDetailsXml
import de.openindex.zugferd.quba.generated.resources.AppCreateErrorConversion
import de.openindex.zugferd.quba.generated.resources.AppCreateErrorIncompatible
import de.openindex.zugferd.quba.generated.resources.AppCreateErrorInvalid
import de.openindex.zugferd.quba.generated.resources.AppCreateErrorCurrencyMissing
import de.openindex.zugferd.quba.generated.resources.AppCreateErrorInvalidFields
import de.openindex.zugferd.quba.generated.resources.AppCreateErrorItemsMissing
import de.openindex.zugferd.quba.generated.resources.AppCreateErrorSenderTaxMissing
import de.openindex.zugferd.quba.generated.resources.AppCreateGeneral
import de.openindex.zugferd.quba.generated.resources.AppCreateGeneralDeliveryDate
import de.openindex.zugferd.quba.generated.resources.AppCreateGeneralDeliveryDateEnd
import de.openindex.zugferd.quba.generated.resources.AppCreateGeneralDeliveryDateInfo
import de.openindex.zugferd.quba.generated.resources.AppCreateGeneralDeliveryDateStart
import de.openindex.zugferd.quba.generated.resources.AppCreateGeneralDueDate
import de.openindex.zugferd.quba.generated.resources.AppCreateGeneralInvoiceNumber
import de.openindex.zugferd.quba.generated.resources.AppCreateGeneralIssueDate
import de.openindex.zugferd.quba.generated.resources.AppCreateGeneralRecipient
import de.openindex.zugferd.quba.generated.resources.AppCreateGeneralRecipientAdd
import de.openindex.zugferd.quba.generated.resources.AppCreateGeneralRecipientEdit
import de.openindex.zugferd.quba.generated.resources.AppCreateGeneralSender
import de.openindex.zugferd.quba.generated.resources.AppCreateGeneralSenderAdd
import de.openindex.zugferd.quba.generated.resources.AppCreateGeneralSenderEdit
import de.openindex.zugferd.quba.generated.resources.AppCreateGenerate
import de.openindex.zugferd.quba.generated.resources.AppCreateGenerateInfo
import de.openindex.zugferd.quba.generated.resources.AppCreateItems
import de.openindex.zugferd.quba.generated.resources.AppCreateItemsAdd
import de.openindex.zugferd.quba.generated.resources.AppCreateItemsEmpty
import de.openindex.zugferd.quba.generated.resources.AppCreateItemsItem
import de.openindex.zugferd.quba.generated.resources.AppCreateItemsItemAdd
import de.openindex.zugferd.quba.generated.resources.AppCreateItemsItemDescription
import de.openindex.zugferd.quba.generated.resources.AppCreateItemsItemEdit
import de.openindex.zugferd.quba.generated.resources.AppCreateItemsItemPrice
import de.openindex.zugferd.quba.generated.resources.AppCreateItemsItemQuantity
import de.openindex.zugferd.quba.generated.resources.AppCreateItemsItemRemove
import de.openindex.zugferd.quba.generated.resources.AppCreateItemsItemSummaryGross
import de.openindex.zugferd.quba.generated.resources.AppCreateItemsItemSummaryNet
import de.openindex.zugferd.quba.generated.resources.AppCreateItemsItemSummaryTax
import de.openindex.zugferd.quba.generated.resources.AppCreateEmptyHint
import de.openindex.zugferd.quba.generated.resources.AppCreateEmptyTitle
import de.openindex.zugferd.quba.generated.resources.AppCreateSelect
import de.openindex.zugferd.quba.generated.resources.AppCreateSelectInfo
import de.openindex.zugferd.quba.generated.resources.AppCreateSelectMessage
import de.openindex.zugferd.quba.generated.resources.AppCreateSummary
import de.openindex.zugferd.quba.generated.resources.AppCreateSummaryGross
import de.openindex.zugferd.quba.generated.resources.AppCreateSummaryNet
import de.openindex.zugferd.quba.generated.resources.AppCreateSummaryTax
import de.openindex.zugferd.quba.generated.resources.Res
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Main view of the create section.
 */
@Composable
@OptIn(ExperimentalFoundationApi::class)
fun CreateSection(state: CreateSectionState) {
    val scope = rememberCoroutineScope()
    val appState = LocalAppState.current
    val isValid = state.invoiceValid
    val selectedPdf = state.selectedPdf
    val selectedPdfArchiveVersion = state.selectedPdfArchiveVersion
    val selectedPdfArchiveError = state.selectedPdfArchiveError
    val isSelectedPdfArchiveUsable = state.isSelectedPdfArchiveUsable

    val dragAndDropCallback = remember {
        createDragAndDropTarget(
            onDrop = { pdfFile ->
                scope.launch {
                    state.selectPdf(
                        pdf = pdfFile,
                        appState = appState,
                    )
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
    // Show an empty view, if no PDF file was selected.
    if (selectedPdf == null) {
        EmptyView(state)
    }

    // Show two column layout, if a PDF file was selected.
    else {
        Row(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            // Left column with e-invoice creation form.
            Column(
                modifier = Modifier
                    .weight(0.6f, fill = true),
            ) {
                // Show form to create an e-invoice from the selected PDF file.
                VerticalScrollBox(
                    modifier = Modifier
                        .weight(1f, fill = true),
                ) {
                    CreateView(state)
                }

                // Show validation error for invalid e-invoice inputs.
                AnimatedVisibility(visible = (!isValid || state.invoiceItems.isEmpty()) && isSelectedPdfArchiveUsable) {
                    val senderLabel = stringResource(Res.string.AppCreateGeneralSender)
                    val recipientLabel = stringResource(Res.string.AppCreateGeneralRecipient)
                    val numberLabel = stringResource(Res.string.AppCreateGeneralInvoiceNumber)
                    val deliveryLabel = stringResource(Res.string.AppCreateGeneralDeliveryDate)
                    val senderTaxLabel = stringResource(Res.string.AppCreateErrorSenderTaxMissing)
                    val currencyLabel = stringResource(Res.string.AppCreateErrorCurrencyMissing)
                    val itemsLabel = stringResource(Res.string.AppCreateErrorItemsMissing)
                    val missingFields = buildList {
                        if (state.invoiceSender == null) add(senderLabel)
                        if (state.invoiceRecipient == null) add(recipientLabel)
                        if (state.invoiceNumber.isBlank()) add(numberLabel)
                        if (state.invoiceCurrency == null) add(currencyLabel)
                        if (state.invoiceItems.isEmpty()) add(itemsLabel)
                        if (state.deliveryDate == null && (state.deliveryStartDate == null || state.deliveryEndDate == null)) {
                            add(deliveryLabel)
                        }
                        if (state.invoiceSender != null &&
                            state.invoiceSender?.vatID.isNullOrBlank() &&
                            state.invoiceSender?.taxID.isNullOrBlank()
                        ) {
                            add(senderTaxLabel)
                        }
                    }
                    val baseText = stringResource(Res.string.AppCreateErrorInvalid)
                    NotificationBar(
                        text = if (missingFields.isNotEmpty())
                            "$baseText " + stringResource(Res.string.AppCreateErrorInvalidFields, missingFields.joinToString(", "))
                        else baseText,
                    )
                }

                // Show error message, if selected PDF has an unsupported / unusable PDF/A version.
                AnimatedVisibility(visible = !isSelectedPdfArchiveUsable && selectedPdfArchiveVersion > MAX_PDF_ARCHIVE_VERSION) {
                    NotificationBar(
                        text = stringResource(
                            Res.string.AppCreateErrorIncompatible,
                            "PDF/A-${selectedPdfArchiveVersion}",
                        ),
                    )
                }



                // Show warning message, if selected PDF has an unsupported PDF/A version but maybe convertable to PDF/A-3.
                AnimatedVisibility(visible = !isSelectedPdfArchiveUsable && selectedPdfArchiveVersion < MAX_PDF_ARCHIVE_VERSION && selectedPdfArchiveError == null) {
                    NotificationBar(
                        text = Res.string.AppCreateConvertWarning,
                    )
                }
            }

            // Right column with further details about the selected PDF.
            Column(
                modifier = Modifier
                    .weight(0.4f, fill = true),
            ) {
                DetailsView(state)
            }
        }
    }
    } // end Column wrapper

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (state.selectedPdf == null)
                    Modifier.clickable { scope.launch(Dispatchers.IO) { state.selectPdf(appState) } }
                else Modifier
            )
            .dragAndDropTarget(
                target = dragAndDropCallback,
                shouldStartDragAndDrop = { true },
            ),
    )
}

/**
 * Action buttons of the create section, shown on the top right.
 */
@Composable
fun CreateSectionActions(state: CreateSectionState) {
    val scope = rememberCoroutineScope()
    val appState = LocalAppState.current
    val preferences = appState.preferences
    val selectedPdf = state.selectedPdf
    val isSelectedPdfArchiveUsable = state.isSelectedPdfArchiveUsable
    val isValid = state.invoiceValid

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .padding(end = 8.dp),
    ) {
        // Datei-Auswahl-Button ausgeblendet — Drag & Drop und Klick auf leere Fläche genügen.
        // ActionButtonWithTooltip(
        //     label = Res.string.AppCreateSelect,
        //     tooltip = Res.string.AppCreateSelectInfo,
        //     onClick = {
        //         scope.launch(Dispatchers.IO) {
        //             state.selectPdf(appState = appState)
        //         }
        //     },
        // )

        // Add button to create an e-invoice from the selected PDF with the user-provided inputs.
        AnimatedVisibility(visible = selectedPdf != null && isSelectedPdfArchiveUsable && isValid) {
            ActionButtonWithTooltip(
                label = Res.string.AppCreateGenerate,
                tooltip = Res.string.AppCreateGenerateInfo,
                onClick = {
                    scope.launch(Dispatchers.IO) {
                        state.exportPdf(
                            preferences = preferences,
                        )
                    }
                },
            )
        }
    }
}

/**
 * Empty view of the create section.
 * This is shown, if no PDF file was selected by the user.
 */
@Composable
@Suppress("UNUSED_PARAMETER")
private fun EmptyView(state: CreateSectionState) {
    val colors = LocalQubaColors.current
    val typo = LocalQubaTypography.current

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(104.dp)
                    .drawBehind {
                        drawRoundRect(
                            color = colors.border,
                            style = Stroke(
                                width = 1.5.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f)),
                            ),
                            cornerRadius = CornerRadius(20.dp.toPx()),
                        )
                    },
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(48.dp),
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = stringResource(Res.string.AppCreateEmptyTitle),
                    style = typo.bodyMed.copy(color = colors.text2),
                )
                Text(
                    text = stringResource(Res.string.AppCreateEmptyHint),
                    style = typo.small.copy(color = colors.text4),
                )
            }
        }
    }
}

/**
 * Left side view of the create section.
 * This provides the form for invoice data.
 */
@Composable
private fun CreateView(state: CreateSectionState) {
    val selectedPdfName = state.originalSelectedPdf?.name ?: "???"

    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 20.dp)
    ) {
        // Section title.
        SectionTitle(
            text = stringResource(Res.string.AppCreate, selectedPdfName).title(),
        )

        // Subsection with form for general information.
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
        ) {
            QubaSectionHeader(
                number = "01",
                title = stringResource(Res.string.AppCreateGeneral).title(),
            )

            GeneralForm(state)
        }

        // Subsection with form for line items.
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
        ) {
            QubaSectionHeader(
                number = "02",
                title = stringResource(Res.string.AppCreateItems).title(),
            ) {
                QubaButton(
                    label = stringResource(Res.string.AppCreateItemsAdd).title(),
                    onClick = { state.invoiceItems = state.invoiceItems.plus(Item()) },
                    variant = QubaButtonVariant.Primary,
                )
            }

            ItemsForm(state)
        }

        // Subsection with calculated summary of line items.
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
        ) {

            QubaSectionHeader(
                number = "03",
                title = stringResource(Res.string.AppCreateSummary).title(),
            )

            AmountSummary(state)
        }
    }
}

/**
 * Right side view of the create section.
 * This provides the PDF viewer.
 * Also, the XML viewer is shown, if the selected PDF file contains XML metadata.
 */
@Composable
private fun DetailsView(state: CreateSectionState) {
    val isInvoiceValid = state.invoiceValid
    var tabState by remember { mutableStateOf(0) }
    val isPdfTabSelected by derivedStateOf { !isInvoiceValid || tabState == 0 }
    val isXmlTabSelected by derivedStateOf { isInvoiceValid && tabState == 1 }

    TabRow(
        selectedTabIndex = if (isInvoiceValid) tabState else 0,
    ) {
        // Add tab for PDF viewer.
        Tab(
            selected = isPdfTabSelected,
            onClick = { tabState = 0 },
            text = {
                Label(
                    text = Res.string.AppCreateDetailsPdf,
                )
            },
        )

        // Add tab for XML viewer.
        AnimatedVisibility(visible = isInvoiceValid) {
            Tab(
                selected = isXmlTabSelected,
                enabled = isInvoiceValid,
                onClick = { tabState = 1 },
                text = {
                    Label(
                        text = Res.string.AppCreateDetailsXml,
                    )
                },
            )
        }
    }

    // Show PDF viewer.
    if (isPdfTabSelected) {
        PdfViewer(
            pdf = state.selectedPdf!!,
            modifier = Modifier.fillMaxSize(),
        )
    }

    // Show XML viewer.
    if (isXmlTabSelected) {
        XmlViewer(
            xml = state.invoiceXml,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/**
 * Form for editing general information about the e-invoice.
 */
@Composable
@OptIn(ExperimentalFoundationApi::class)
@Suppress("UnusedReceiverParameter")
private fun ColumnScope.GeneralForm(state: CreateSectionState) {
    val scope = rememberCoroutineScope()
    val preferences = LocalAppState.current.preferences

    val senders = LocalAppState.current.senders
    val sendersList = derivedStateOf {
        if (state.invoiceSender?.isSaved == false) {
            listOf(state.invoiceSender!!, *senders.senders.toTypedArray())
        } else {
            senders.senders
        }
    }

    val recipients = LocalAppState.current.recipients
    val recipientsList = derivedStateOf {
        if (state.invoiceRecipient?.isSaved == false) {
            listOf(state.invoiceRecipient!!, *recipients.recipients.toTypedArray())
        } else {
            recipients.recipients
        }
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        // Left side of the form.
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .weight(1f, fill = true),
        ) {
            // Field for the invoice sender / issuer.
            TradePartyFieldWithAdd(
                label = Res.string.AppCreateGeneralSender,
                addLabel = Res.string.AppCreateGeneralSenderAdd,
                editLabel = Res.string.AppCreateGeneralSenderEdit,
                tradeParty = state.invoiceSender,
                tradeParties = sendersList.value,
                requiredIndicator = true,
                onSelect = { sender, savePermanently ->
                    state.invoiceSender = sender
                    if (sender != null && savePermanently) {
                        scope.launch {
                            senders.put(sender, preferences)
                        }
                    } else if (sender?.isSaved == true) {
                        preferences.setPreviousSenderKey(sender._key)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(),
            )

            // Field for the invoice recipient.
            TradePartyFieldWithAdd(
                label = Res.string.AppCreateGeneralRecipient,
                addLabel = Res.string.AppCreateGeneralRecipientAdd,
                editLabel = Res.string.AppCreateGeneralRecipientEdit,
                tradeParty = state.invoiceRecipient,
                tradeParties = recipientsList.value,
                requiredIndicator = true,
                isCustomer = true,
                onSelect = { recipient, savePermanently ->
                    state.invoiceRecipient = recipient
                    state.invoicePaymentMethod = recipient?._defaultPaymentMethod ?: state.invoicePaymentMethod
                    if (recipient != null && savePermanently) {
                        scope.launch {
                            recipients.put(recipient)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(),
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                // Field for the invoice number.
                TextField(
                    label = Res.string.AppCreateGeneralInvoiceNumber,
                    value = state.invoiceNumber,
                    requiredIndicator = true,
                    onValueChange = { state.invoiceNumber = it },
                    modifier = Modifier
                        .weight(0.5f, true),
                )

                // Field for the payment method.
                PaymentMethodField(
                    value = state.invoicePaymentMethod,
                    requiredIndicator = true,
                    onSelect = { state.invoicePaymentMethod = it },
                    modifier = Modifier
                        .weight(0.5f, true),
                )

                // HACK: Show hidden button to ensure equal width of input fields.
                InvisibleButton()
            }

            Tooltip(
                text = Res.string.AppCreateGeneralDeliveryDateInfo,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth(),
                ) {
                    // Field for the delivery date.
                    DateField(
                        label = stringResource(Res.string.AppCreateGeneralDeliveryDate).title(),
                        value = state.deliveryDate,
                        clearable = true,
                        requiredIndicator = state.deliveryStartDate == null || state.deliveryEndDate == null,
                        onValueChange = { state.deliveryDate = it },
                        modifier = Modifier
                            .weight(0.3f, true),
                    )

                    // Field for the start of delivery period.
                    DateField(
                        label = stringResource(Res.string.AppCreateGeneralDeliveryDateStart).title(),
                        value = state.deliveryStartDate,
                        clearable = true,
                        requiredIndicator = state.deliveryDate == null,
                        onValueChange = { state.deliveryStartDate = it },
                        modifier = Modifier
                            .weight(0.3f, true),
                    )

                    // Field for the end of delivery period.
                    DateField(
                        label = stringResource(Res.string.AppCreateGeneralDeliveryDateEnd).title(),
                        value = state.deliveryEndDate,
                        clearable = true,
                        requiredIndicator = state.deliveryDate == null,
                        onValueChange = { state.deliveryEndDate = it },
                        modifier = Modifier
                            .weight(0.3f, true),
                    )

                    // HACK: Show hidden button to ensure equal width of input fields.
                    InvisibleButton()
                }
            }
        }

        // Right side of the form.
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .width(200.dp),
        ) {
            // Field for the currency.
            CurrencyField(
                currency = state.invoiceCurrency,
                short = true,
                requiredIndicator = true,
                onSelect = { currency ->
                    state.invoiceCurrency = currency
                },
                modifier = Modifier
                    .fillMaxWidth(),
            )

            // Field for the issue date.
            DateField(
                label = Res.string.AppCreateGeneralIssueDate,
                value = state.invoiceIssueDate,
                requiredIndicator = true,
                onValueChange = { state.invoiceIssueDate = it ?: state.invoiceIssueDate },
                modifier = Modifier
                    .fillMaxWidth(),
            )

            // Field for the due date.
            DateField(
                label = Res.string.AppCreateGeneralDueDate,
                value = state.invoiceDueDate,
                requiredIndicator = true,
                onValueChange = { state.invoiceDueDate = it ?: state.invoiceDueDate },
                modifier = Modifier
                    .fillMaxWidth(),
            )
        }
    }

    /*
    if (!state.invoiceValid) {
        Text(text = "Die Rechnungsangaben sind ungültig bzw. unvollständig.")
    } else {
        TextField(
            value = state.invoiceXml,
            label = "XML",
            readOnly = true,
            minLines = 10,
            maxLines = 30,
            onValueChange = {},
            modifier = Modifier
                //.height(50.dp)
                .fillMaxWidth(),
        )
    }
    */
}

/**
 * Form for editing one or more line items about the e-invoice.
 */
@Composable
private fun ColumnScope.ItemsForm(state: CreateSectionState) {
    val preferences = LocalAppState.current.preferences
    val invoiceItems = state.invoiceItems

    // No invoice items selected.
    if (invoiceItems.isEmpty()) {
        Text(
            text = stringResource(Res.string.AppCreateItemsEmpty),
        )
        return
    }

    // Render a form for each available invoice item.
    invoiceItems.forEachIndexed { index, item ->
        if (index > 0) {
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth(),
            )
        }

        ItemForm(
            item = item,
            state = state,
            onUpdate = { updatedItem ->
                state.invoiceItems = state.invoiceItems
                    .filter { it._uid != item._uid }
                    .plus(updatedItem)

                if (index == 0 && updatedItem.product?.isSaved == true) {
                    preferences.setPreviousProductKey(
                        updatedItem.product._key
                    )
                }
            },
            onRemove = {
                state.invoiceItems = state.invoiceItems
                    .filter { it._uid != item._uid }
            },
        )
    }
}

/**
 * Form for editing a single line item about the e-invoice.
 */
@Composable
@OptIn(ExperimentalFoundationApi::class)
@Suppress("UnusedReceiverParameter")
private fun ColumnScope.ItemForm(
    item: Item,
    state: CreateSectionState,
    onUpdate: (item: Item) -> Unit,
    onRemove: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    val products = LocalAppState.current.products
    val productsList = derivedStateOf {
        if (item.product?.isSaved == false) {
            listOf(item.product, *products.products.toTypedArray())
        } else {
            products.products
        }
    }

    val unit = remember(item.product?.unit) {
        UnitOfMeasurement.getByCode(item.product?.unit)
            ?: UnitOfMeasurement.UNIT
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        // Item form on the left side.
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .weight(1f, fill = true),
        ) {
            // Field for the invoice item.
            ProductFieldWithAdd(
                label = Res.string.AppCreateItemsItem,
                addLabel = Res.string.AppCreateItemsItemAdd,
                editLabel = Res.string.AppCreateItemsItemEdit,
                product = item.product,
                products = productsList.value,
                requiredIndicator = true,
                onSelect = { product, savePermanently ->
                    onUpdate(
                        item.copy(
                            product = product,
                            price = product?._defaultPricePerUnit ?: 0.0,
                        )
                    )
                    if (product != null && savePermanently) {
                        scope.launch {
                            products.put(product)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(),
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                // Field for the item price.
                DecimalField(
                    label = stringResource(
                        Res.string.AppCreateItemsItemPrice,
                        //unit.symbol ?: pluralStringResource(unit.value, 1),
                        pluralStringResource(unit.value, 1),
                    ),
                    value = item.price,
                    minPrecision = 2,
                    maxPrecision = 2,
                    requiredIndicator = true,
                    onValueChange = { newPrice ->
                        if (newPrice != null) {
                            onUpdate(
                                item.copy(
                                    price = newPrice,
                                )
                            )
                        }
                    },
                    modifier = Modifier
                        .weight(0.5f, fill = true),
                )

                // Field for the item quantity.
                DecimalField(
                    label = stringResource(
                        Res.string.AppCreateItemsItemQuantity,
                        //unit.symbol ?: stringResource(unit.title),
                        pluralStringResource(unit.value, 2),
                    ),
                    value = item.quantity,
                    minPrecision = unit.minPrecision,
                    maxPrecision = unit.maxPrecision,
                    requiredIndicator = true,
                    onValueChange = { newQuantity ->
                        if (newQuantity != null) {
                            onUpdate(
                                item.copy(
                                    quantity = newQuantity,
                                )
                            )
                        }
                    },
                    modifier = Modifier
                        .weight(0.5f, fill = true),
                )

                // Button for item removal.
                Tooltip(
                    text = Res.string.AppCreateItemsItemRemove,
                ) {
                    IconButton(
                        onClick = { onRemove() },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = stringResource(Res.string.AppCreateItemsItemRemove),
                        )
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                // Field for the item description.
                TextField(
                    label = Res.string.AppCreateItemsItemDescription,
                    value = item.notes ?: "",
                    onValueChange = {
                        onUpdate(
                            item.copy(
                                notes = it,
                            )
                        )
                    },
                    modifier = Modifier
                        .weight(1f, fill = true),
                )

                // HACK: Show hidden button to ensure equal width of input fields.
                InvisibleButton()
            }
        }

        // Item summary on the right side.
        ItemSummary(
            item = item,
            state = state,
            modifier = Modifier
                .width(200.dp),
        )
    }
}

/**
 * Summary of a single line item.
 */
@Composable
@Suppress("UnusedReceiverParameter")
private fun RowScope.ItemSummary(
    item: Item,
    state: CreateSectionState,
    modifier: Modifier = Modifier,
) {
    val currency = remember(state.invoiceCurrency) { state.invoiceCurrency ?: FALLBACK_CURRENCY }

    QubaCard(modifier = modifier) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 14.dp)
        ) {
            // Item net amount.
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontSize = 0.9.em)) {
                        append(stringResource(Res.string.AppCreateItemsItemSummaryNet).title())
                        append("\n")
                    }
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(item.totalNetPrice.formatPrice(currency))
                    }
                },
                style = MaterialTheme.typography.bodyLarge,
            )

            // Calculated item tax.
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontSize = 0.9.em)) {
                        if (item.product != null) {
                            append("${item.product.vatPercent.formatAsPercentage}% ")
                        }
                        append(stringResource(Res.string.AppCreateItemsItemSummaryTax).title())
                        append("\n")
                    }
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(item.tax.formatPrice(currency))
                    }
                },
                style = MaterialTheme.typography.bodyLarge,
            )

            // Calculated item gross amount.
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontSize = 0.9.em)) {
                        append(stringResource(Res.string.AppCreateItemsItemSummaryGross).title())
                        append("\n")
                    }
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(item.totalGrossPrice.formatPrice(currency))
                    }
                },
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

/**
 * Calculated summary of all line items.
 */
@Composable
@Suppress("UnusedReceiverParameter")
private fun ColumnScope.AmountSummary(
    state: CreateSectionState,
) {
    val currency = remember(state.invoiceCurrency) { state.invoiceCurrency ?: FALLBACK_CURRENCY }
    val netSum = derivedStateOf {
        state.invoiceItems.sumOf { it.totalNetPrice }
    }
    val taxSum = derivedStateOf {
        state.invoiceItems.sumOf { it.tax }
    }
    val grossSum = derivedStateOf {
        state.invoiceItems.sumOf { it.totalGrossPrice }
    }

    QubaCard(
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .padding(vertical = 10.dp, horizontal = 14.dp)
                .fillMaxWidth(),
        ) {
            // Calculated total net amount.
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontSize = 0.9.em)) {
                        append(stringResource(Res.string.AppCreateSummaryNet).title())
                        append("\n")
                    }
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(netSum.value.formatPrice(currency))
                    }
                },
                style = MaterialTheme.typography.bodyLarge,
            )

            // Calculated total tax.
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontSize = 0.9.em)) {
                        append(stringResource(Res.string.AppCreateSummaryTax).title())
                        append("\n")
                    }
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(taxSum.value.formatPrice(currency))
                    }
                },
                style = MaterialTheme.typography.bodyLarge,
            )

            // Calculated total gross amount.
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontSize = 0.9.em)) {
                        append(stringResource(Res.string.AppCreateSummaryGross).title())
                        append("\n")
                    }
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(grossSum.value.formatPrice(currency))
                    }
                },
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

/**
 * Invisible button component.
 *
 * It takes the regular space of a button with icon, but has 100% transparency.
 * This method is used to ensure proper layout.
 */
@Composable
private fun InvisibleButton() = IconButton(
    onClick = {},
    modifier = Modifier
        .alpha(0f)
) {
    Icon(
        imageVector = Icons.Default.QuestionMark,
        contentDescription = "",
    )
}
