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

package de.openindex.zugferd.manager.model

import de.openindex.zugferd.manager.APP_LOGGER
import de.openindex.zugferd.manager.APP_TITLE_FULL
import de.openindex.zugferd.manager.APP_VERSION
import de.openindex.zugferd.manager.utils.getString
import de.openindex.zugferd.manager.utils.removeEmbeddedFiles
import de.openindex.zugferd.manager.utils.toJavaDate
import de.openindex.zugferd.manager.utils.trimToNull
import de.openindex.zugferd.quba.generated.resources.InvoicePaymentTermDescriptionSepaCreditTransfer
import de.openindex.zugferd.quba.generated.resources.InvoicePaymentTermDescriptionSepaDirectDebit
import de.openindex.zugferd.quba.generated.resources.Res
import io.github.vinceglb.filekit.core.PlatformFile
import org.apache.pdfbox.Loader
import kotlinx.coroutines.runBlocking
import org.mustangproject.ZUGFeRD.IExportableTransaction
import org.mustangproject.ZUGFeRD.Profiles
import org.mustangproject.ZUGFeRD.ZUGFeRD2PullProvider
import org.mustangproject.ZUGFeRD.ZUGFeRDExporterFromA1
import org.mustangproject.ZUGFeRD.ZUGFeRDExporterFromA3
import org.mustangproject.ZUGFeRD.ZUGFeRDExporterFromPDFA
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.text.DateFormat
import java.util.Locale
import org.mustangproject.Invoice as _Invoice

const val INVOICE_PROFILE = "EXTENDED"

private val DATE_FORMAT = DateFormat
    .getDateInstance(DateFormat.MEDIUM, Locale.getDefault())

fun Invoice.build(method: PaymentMethod): _Invoice =
    _Invoice()
        .setCurrency(currency)
        .setNumber(number)
        .setIssueDate(issueDate.toJavaDate())
        .setDueDate(dueDate.toJavaDate())
        .setDeliveryDate(deliveryDate?.toJavaDate())
        .setDetailedDeliveryPeriod(deliveryStartDate?.toJavaDate(), deliveryEndDate?.toJavaDate())
        .setPaymentTermDescription(
            runBlocking {
                when (method) {
                    PaymentMethod.SEPA_DIRECT_DEBIT -> getString(
                        Res.string.InvoicePaymentTermDescriptionSepaDirectDebit,
                        DATE_FORMAT.format(dueDate.toJavaDate()),
                    )

                    PaymentMethod.SEPA_CREDIT_TRANSFER -> getString(
                        Res.string.InvoicePaymentTermDescriptionSepaCreditTransfer,
                        DATE_FORMAT.format(dueDate.toJavaDate()),
                    )

                    else -> null
                }
            }
        )
        .setCreditorReferenceID(
            sender?.creditorReferenceId
        )
        .let { invoice ->
            val imprint = sender?.imprint?.trimToNull()
            if (imprint != null) {
                invoice.addRegulatoryNote(imprint)
            }
            invoice
        }
        .let { invoice ->
            @Suppress("LocalVariableName")
            val _sender = if (method == PaymentMethod.SEPA_DIRECT_DEBIT) {
                /**
                 * Direct Debit requires recipients bank account
                 * including mandate reference within senders debit details.
                 * @see org.mustangproject.Invoice#getTradeSettlement()
                 * */
                sender?.copy(
                    bankDetails = listOf(),
                    debitDetails = buildList {
                        recipient?.bankDetails?.firstOrNull()?.let {
                            add(
                                DirectDebit(
                                    iban = it.iban,
                                    mandate = it.directDebitMandateId ?: "",
                                )
                            )
                        }
                    },
                )
            } else {
                sender
            }

            val party = _sender?.build()
            if (party != null) {
                invoice.setSender(party)
            }

            invoice
        }
        .let { invoice ->
            val party = recipient?.build()
            if (party != null) {
                invoice.setRecipient(party)
            }

            invoice
        }
        .let { invoice ->
            items
                .mapNotNull { it.build() }
                .forEach { invoice.addItem(it) }

            invoice
        }

/*actual fun Invoice.isValid(): Boolean {
    return build().isValid
}*/

actual fun Invoice.toXml(method: PaymentMethod): String {
    val transaction = build(
        method = method,
    )

    //val tradeSettlements = transaction.tradeSettlement ?: listOf<IZUGFeRDTradeSettlement>().toTypedArray()
    //APP_LOGGER.debug("TRADE SETTLEMENTS / ${tradeSettlements.size}")
    //if (transaction.sender == null) {
    //    APP_LOGGER.debug("SENDER IS NULL")
    //} else if (transaction.sender.bankDetails.isEmpty() && transaction.sender.debitDetails.isEmpty()) {
    //    APP_LOGGER.debug("SENDER BANKING DETAILS ARE EMPTY")
    //}
    //APP_LOGGER.debug("-".repeat(50))
    //tradeSettlements.forEach {
    //    APP_LOGGER.debug(it.settlementXML)
    //    APP_LOGGER.debug("-".repeat(50))
    //}

    //val zf2p = ZUGFeRD2PullProvider()
    val zf2p = CustomZUGFeRD2PullProvider()
    zf2p.profile = Profiles.getByName(INVOICE_PROFILE)
    zf2p.generateXML(transaction)
    return String(zf2p.xml, StandardCharsets.UTF_8).trim()
}

/**
 * https://www.mustangproject.org/invoice-class/?lang=de
 */
actual suspend fun Invoice.export(
    sourceFile: PlatformFile,
    targetFile: PlatformFile,
    method: PaymentMethod,
    removeEmbeddedFiles: Boolean,
): Boolean {
    val exporter = try {
        //sourceFile.file.inputStream().use { input ->
        //    //ZUGFeRDExporterFromPDFA()
        //    CustomZUGFeRDExporterFromPDFA()
        //        .load(input)
        //}
        Loader.loadPDF(sourceFile.file).use { doc ->
            if (removeEmbeddedFiles) {
                println("removeEmbeddedFiles")
                doc.removeEmbeddedFiles()
            }

            val byteArrayOutputStream = ByteArrayOutputStream()
            doc.save(byteArrayOutputStream)

            //ZUGFeRDExporterFromPDFA()
            CustomZUGFeRDExporterFromPDFA()
                .load(byteArrayOutputStream.toByteArray())
        }
            .setProducer("$APP_TITLE_FULL $APP_VERSION")
            .setCreator("$APP_TITLE_FULL $APP_VERSION")
            .setProfile(Profiles.getByName(INVOICE_PROFILE))
            .setTransaction(
                build(
                    method = method,
                )
            )
    } catch (e: Exception) {
        APP_LOGGER.error("Can't init ZUGFeRD exporter!", e)
        return false
    }

    try {
        targetFile.file.outputStream().use { output ->
            exporter.export(output)
        }
    } catch (e: Exception) {
        APP_LOGGER.error("Can't export ZUGFeRD file!", e)
        return false
    }

    return true
}

/**
 * Custom ZUGFeRD exporter.
 */
private class CustomZUGFeRDExporterFromPDFA : ZUGFeRDExporterFromPDFA() {
    override fun determineAndSetExporter(pdfAVersion: Int) {
        super.determineAndSetExporter(pdfAVersion)
        if (theExporter == null) {
            return
        }

        if (theExporter is ZUGFeRDExporterFromA3) {
            theExporter = object : ZUGFeRDExporterFromA3() {
                init {
                    setXMLProvider(CustomZUGFeRD2PullProvider())
                }
            }
            return
        }

        if (theExporter is ZUGFeRDExporterFromA1) {
            theExporter = object : ZUGFeRDExporterFromA1() {
                init {
                    setXMLProvider(CustomZUGFeRD2PullProvider())
                }
            }
            return
        }
    }
}

/**
 * Custom ZUGFeRD XML generator.
 */
private class CustomZUGFeRD2PullProvider : ZUGFeRD2PullProvider() {
    override fun generateXML(trans: IExportableTransaction?) {
        super.generateXML(trans)

        //val doc = DocumentHelper.parseText(
        //    zugferdData.toString(Charsets.UTF_8)
        //)

        //
        // This workaround should not be necessary anymore, as the issue was fixed with Mustang 2.16.0.
        // see https://github.com/ZUGFeRD/mustangproject/issues/565
        // see https://github.com/ZUGFeRD/mustangproject/releases/tag/core-2.16.0
        //
        //fixSpecifiedTradePaymentTerms(doc)

        //zugferdData = XMLTools.removeBOM(
        //    doc.asXML().toByteArray(charset = Charsets.UTF_8)
        //)
    }

    /*private fun fixSpecifiedTradePaymentTerms(doc: Document) {
        val elementOrderForSpecifiedTradePaymentTerms = listOf(
            "ID",
            "FromEventCode",
            "SettlementPeriodMeasure",
            "Description",
            "DueDateDateTime",
            "TypeCode",
            "InstructionTypeCode",
            "DirectDebitMandateID",
            "PartialPaymentPercent",
            "PaymentMeansID",
            "PartialPaymentAmount",
            "ApplicableTradePaymentPenaltyTerms",
            "ApplicableTradePaymentDiscountTerms",
            "PayeeTradeParty",
        )

        @Suppress("SpellCheckingInspection")
        val namespaceMap = mapOf(
            "ram" to "urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:100"
        )

        fun fixElement(element: Element) {
            val childElements = buildList {
                element.elements().forEach { child ->
                    element.remove(child)
                    add(child)
                }
            }

            elementOrderForSpecifiedTradePaymentTerms.forEach { childName ->
                childElements
                    .filter { it.name == childName }
                    .forEach { child ->
                        element.add(child)
                    }
            }
        }

        val xpath = doc.createXPath("//ram:SpecifiedTradePaymentTerms")
        xpath.setNamespaceURIs(namespaceMap)
        xpath.selectNodes(doc)
            .forEach { node -> fixElement(node as Element) }
    }*/
}
