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

package de.openindex.zugferd.manager.utils

import de.openindex.zugferd.manager.APP_LOGGER
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary
import org.apache.pdfbox.pdmodel.common.PDNameTreeNode
import org.apache.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification
import java.io.File
import java.io.IOException

/**
 * File names under which ZUGFeRD, Factur-X, XRechnung, Order-X and CIDA embed the invoice XML
 * into a PDF (same list Mustang uses), compared case-insensitively.
 */
internal val INVOICE_XML_NAMES = setOf(
    "zugferd-invoice.xml",
    "factur-x.xml",
    "xrechnung.xml",
    "order-x.xml",
    "cida.xml",
)

/** Markers that identify a stray `.xml` attachment as an e-invoice, if no standard name matched. */
private val INVOICE_XML_MARKERS = listOf(
    "CrossIndustryInvoice",
    "CrossIndustryDocument",
    "SCRDMCCBDACIOMessageStructure",
    "urn:oasis:names:specification:ubl:schema:xsd:Invoice",
    "urn:oasis:names:specification:ubl:schema:xsd:CreditNote",
)

private const val READ_ATTEMPTS = 5
private const val READ_INITIAL_DELAY_MS = 100L

actual fun PlatformFile.contentKey(): String =
    file.absoluteFile.let { "${it.path}|${it.length()}|${it.lastModified()}" }

/**
 * Reads [file] exactly once: a PDF is opened a single time with PDFBox (lazily, without
 * pulling the whole file onto the heap), which yields both the invoice XML and all embedded
 * attachments. Unlike Mustang's ZUGFeRDImporter, the invoice is not parsed into Java objects;
 * the viewer only needs the raw XML.
 */
actual suspend fun readInvoiceSource(file: PlatformFile): InvoiceSource = withContext(Dispatchers.IO) {
    val javaFile = file.file
    val isPdf = retryOnIoError { hasPdfHeader(javaFile) }

    if (isPdf) {
        val (xmlBytes, attachments) = retryOnIoError { extractFromPdf(javaFile) }
        val xml = xmlBytes?.let(::decodeXml)?.trimToNull()
        InvoiceSource(
            isPdf = true,
            xml = xml,
            prettyXml = xml?.let(::prettyPrintIfNeeded),
            attachments = attachments,
        )
    } else {
        val xml = decodeXml(retryOnIoError { javaFile.readBytes() }).trimToNull()
        InvoiceSource(
            isPdf = false,
            xml = xml,
            prettyXml = xml?.let(::prettyPrintIfNeeded),
            attachments = emptyList(),
        )
    }
}

actual suspend fun renderInvoiceHtml(source: InvoiceSource, language: Language): String? = withContext(Dispatchers.IO) {
    val xml = source.xml ?: return@withContext null
    val rawHtml = try {
        InvoiceHtmlRenderer.render(xml.toByteArray(Charsets.UTF_8), language)
    } catch (e: Exception) {
        APP_LOGGER.error("HTML-Visualisierung fehlgeschlagen.", e)
        null
    }

    if (rawHtml.isNullOrBlank() || rawHtml.length < 200) {
        APP_LOGGER.warn("Visualisierung lieferte leeres HTML – Fallback wird verwendet.")
        return@withContext generateFallbackHtml(xml)
    }

    try {
        postProcessInvoiceHtml(rawHtml, source.attachments)
    } catch (e: Exception) {
        APP_LOGGER.error("HTML-Nachbearbeitung fehlgeschlagen.", e)
        rawHtml
    }
}

/**
 * Retries [block] on [IOException] with exponential backoff. Right after a drag & drop or a
 * save from another program, the OS may still hold a lock on the file for a moment.
 */
private suspend fun <T> retryOnIoError(block: () -> T): T {
    var wait = READ_INITIAL_DELAY_MS
    repeat(READ_ATTEMPTS - 1) {
        try {
            return block()
        } catch (e: IOException) {
            APP_LOGGER.warn("Datei nicht lesbar, neuer Versuch in $wait ms: ${e.message}")
            delay(wait)
            wait *= 2
        }
    }
    return block()
}

/** PDF allows the `%PDF-` header anywhere in the first 1024 bytes. */
private fun hasPdfHeader(file: File): Boolean {
    val head = ByteArray(1024)
    val read = file.inputStream().use { it.readNBytes(head, 0, head.size) }
    return String(head, 0, read, Charsets.ISO_8859_1).contains("%PDF-")
}

private fun extractFromPdf(file: File): Pair<ByteArray?, List<Pair<String, ByteArray>>> {
    Loader.loadPDF(file).use { doc ->
        val tree = PDDocumentNameDictionary(doc.documentCatalog).embeddedFiles
            ?: return null to emptyList()

        val specs = LinkedHashMap<String, PDComplexFileSpecification>()
        collectFileSpecs(tree, specs)

        var invoiceXml: ByteArray? = null
        var fallbackXml: ByteArray? = null
        val attachments = ArrayList<Pair<String, ByteArray>>(specs.size)

        for ((key, spec) in specs) {
            val embedded = spec.embeddedFile ?: spec.embeddedFileUnicode ?: continue
            val bytes = embedded.toByteArray()
            val names = listOfNotNull(key, spec.filename).map { it.lowercase() }

            if (invoiceXml == null && names.any { it in INVOICE_XML_NAMES }) {
                invoiceXml = bytes
            } else if (fallbackXml == null && names.any { it.endsWith(".xml") } && looksLikeInvoiceXml(bytes)) {
                fallbackXml = bytes
            }
            attachments += key to bytes
        }

        if (invoiceXml == null && fallbackXml != null) {
            APP_LOGGER.info("Keine Standard-Rechnungs-XML gefunden, nutze eingebettete XML mit Rechnungsinhalt.")
        }
        return (invoiceXml ?: fallbackXml) to attachments
    }
}

/** Embedded files can sit in nested name-tree nodes; Mustang only looks one level deep. */
private fun collectFileSpecs(
    node: PDNameTreeNode<PDComplexFileSpecification>,
    out: MutableMap<String, PDComplexFileSpecification>,
) {
    node.names?.let { out.putAll(it) }
    node.kids?.forEach { collectFileSpecs(it, out) }
}

private fun looksLikeInvoiceXml(bytes: ByteArray): Boolean {
    val head = String(bytes, 0, minOf(bytes.size, 4096), Charsets.UTF_8)
    return INVOICE_XML_MARKERS.any { head.contains(it) }
}

/** UTF-8 decode without BOM (same as Mustang's getUTF8()). */
private fun decodeXml(bytes: ByteArray): String {
    val hasBom = bytes.size >= 3 &&
        bytes[0] == 0xEF.toByte() && bytes[1] == 0xBB.toByte() && bytes[2] == 0xBF.toByte()
    return if (hasBom) String(bytes, 3, bytes.size - 3, Charsets.UTF_8) else String(bytes, Charsets.UTF_8)
}

/**
 * Pretty-printing a large XML through a DOM costs a lot of time and memory, and most
 * invoices are already indented. Only minified/flat XML gets reformatted.
 */
private fun prettyPrintIfNeeded(xml: String): String =
    if (isAlreadyIndented(xml)) xml else getPrettyPrintedXml(xml).trimToNull() ?: xml

private fun isAlreadyIndented(xml: String): Boolean {
    val probe = minOf(xml.length, 16_384)
    var newlines = 0
    var indented = 0
    for (i in 0 until probe - 1) {
        if (xml[i] == '\n') {
            newlines++
            val next = xml[i + 1]
            if (next == ' ' || next == '\t') indented++
        }
    }
    return newlines >= 5 && indented >= 3
}

private fun generateFallbackHtml(xml: String): String {
    val escaped = xml
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
    return """<!DOCTYPE html>
<html><head><meta charset="UTF-8"><style>
body{font-family:monospace;padding:20px;background:#1e1e1e;color:#d4d4d4}
pre{white-space:pre-wrap;word-break:break-all;font-size:12px;line-height:1.5}
.warn{background:#b45309;color:#fff;padding:10px 16px;border-radius:6px;margin-bottom:16px;font-family:sans-serif;font-size:13px}
</style></head><body>
<div class="warn">&#9888; HTML-Visualisierung nicht verfügbar – XML-Quellcode wird angezeigt.</div>
<pre>$escaped</pre>
</body></html>"""
}
