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
import de.openindex.zugferd.manager.APP_TITLE_FULL
import de.openindex.zugferd.manager.APP_VERSION
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDDocumentCatalog
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary
import org.apache.pdfbox.pdmodel.PDEmbeddedFilesNameTreeNode
import org.apache.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification
import org.apache.xmpbox.xml.DomXmpParser
import org.apache.xmpbox.xml.XmpParsingException
import org.mustangproject.ZUGFeRD.ZUGFeRDExporterFromA3
import org.mustangproject.ZUGFeRD.ZUGFeRDExporterFromPDFA
import org.mustangproject.ZUGFeRD.ZUGFeRDImporter
import org.mustangproject.ZUGFeRD.ZUGFeRDVisualizer
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.pathString
import kotlin.io.path.writer
@Suppress("SpellCheckingInspection")
private val CUSTOM_VISUALIZATION_CSS = """
    body > form {
        position: fixed !important;
        left: 0 !important;
        right: 0 !important;
        z-index: 1 !important;
    }

    .inhalt {
        padding-top: 75px !important;    
    }
    
    .menue > .innen {
        text-align: center !important;
    }
    
    .menue > .innen > button {
        font-family: sans-serif !important;
        font-size: 14px !important;
    }
    
""".trimIndent()

actual suspend fun getPdfArchiveVersion(pdfFile: PlatformFile): Int {
    return try {
        getPDFAVersion(pdfFile.file)
    } catch (e: Exception) {
        APP_LOGGER.error("Can't extract PDF/A version!", e)
        -1
    }
}

/**
 * taken from ZUGFeRDExporterFromPDFA
 * @see ZUGFeRDExporterFromPDFA.getPDFAVersion
 */
private fun getPDFAVersion(pdfFile: File): Int {
    val document = Loader.loadPDF(pdfFile)
    val catalog: PDDocumentCatalog = document.getDocumentCatalog()
    val metadata = catalog.metadata

    // the PDF version we could get through the document, but we want the PDF-A version,
    // which is different (and can probably base on different PDF versions)
    if (metadata != null) {
        try {
            val xmpParser = DomXmpParser()
            val xmp = xmpParser.parse(metadata.createInputStream())

            val pdfASchema = xmp.pdfaIdentificationSchema
            if (pdfASchema != null) {
                return pdfASchema.part
            }
        } catch (e: XmpParsingException) {
            APP_LOGGER.error("XmpParsingException", e)
        } finally {
            document.close()
        }
    }
    return 0
}



@Throws(IOException::class)
fun PDDocument.removeEmbeddedFiles(): PDDocument {
    val names = PDDocumentNameDictionary(documentCatalog)
    names.embeddedFiles = PDEmbeddedFilesNameTreeNode()
    documentCatalog.names = names
    return this
}

actual fun getAttachmentsFromPdf(pdf: PlatformFile): List<Pair<String, ByteArray>> {
    return try {
        val doc = Loader.loadPDF(pdf.file)
        val result = mutableListOf<Pair<String, ByteArray>>()
        try {
            val nameTree = PDDocumentNameDictionary(doc.documentCatalog).embeddedFiles ?: return emptyList()
            for ((name, spec) in (nameTree.names ?: return emptyList())) {
                val ef = (spec as? PDComplexFileSpecification)?.let {
                    it.embeddedFile ?: it.embeddedFileUnicode
                } ?: continue
                result += name to ef.createInputStream().readBytes()
            }
        } finally {
            doc.close()
        }
        result
    } catch (e: Exception) {
        APP_LOGGER.error("Anhänge konnten nicht aus PDF gelesen werden.", e)
        emptyList()
    }
}

actual fun getAttachmentsFromXml(xmlContent: String): List<Pair<String, ByteArray>> {
    return try {
        val document = org.dom4j.DocumentHelper.parseText(xmlContent)
        val result = mutableListOf<Pair<String, ByteArray>>()
        // XPath mit local-name() findet Elemente unabhaengig von Namespace-Praefix und Verschachtelungstiefe
        val nodes = document.selectNodes(
            "//*[local-name()='AttachmentBinaryObject' or local-name()='EmbeddedDocumentBinaryObject']"
        )
        for (node in nodes) {
            if (node !is org.dom4j.Element) continue
            val filename = node.attributeValue("filename") ?: continue
            if (filename.isBlank()) continue
            val base64Content = node.textTrim.replace("\\s".toRegex(), "")
            if (base64Content.isBlank()) continue
            try {
                val bytes = java.util.Base64.getDecoder().decode(base64Content)
                result += filename to bytes
                APP_LOGGER.info("XML-Anhang gefunden: '$filename' (${bytes.size} Bytes)")
            } catch (e: IllegalArgumentException) {
                APP_LOGGER.warn("Base64-Dekodierung fehlgeschlagen fuer Anhang '$filename': ${e.message}")
            }
        }
        result
    } catch (e: Exception) {
        APP_LOGGER.error("Anhaenge konnten nicht aus XML gelesen werden.", e)
        emptyList()
    }
}

actual fun postProcessHtmlForAttachments(html: String, attachments: List<Pair<String, ByteArray>>): String {
    if (html.isBlank()) return html
    APP_LOGGER.info("Post-processing HTML for attachments. Found ${attachments.size} attachments in PDF.")

    // Keep bytes in memory — no temp files needed.
    val attachmentData = mutableMapOf<String, ByteArray>()        // original name → bytes
    val attachmentContentMap = mutableMapOf<String, String>()     // base64 content → original name

    for ((name, bytes) in attachments) {
        attachmentData[name] = bytes
        attachmentContentMap[java.util.Base64.getEncoder().encodeToString(bytes)] = name
        APP_LOGGER.info("Registered attachment '$name' (${bytes.size} bytes)")
    }

    val doc = Jsoup.parse(html)
    val dataDivs = doc.select("div[filename], div[data-filename], div[mimetype]")
    APP_LOGGER.info("Found ${dataDivs.size} data divs.")

    for ((index, div) in dataDivs.withIndex()) {
        val id = div.id()
        if (id.isEmpty()) continue

        var filename = div.attr("filename").ifEmpty { div.attr("data-filename") }
        APP_LOGGER.info("Processing data div with ID: $id, Filename: '$filename'")

        val link = doc.select("a[onclick*='$id']").first() ?: run {
            APP_LOGGER.info("No link found for data div ID: $id")
            null
        } ?: continue

        // Resolve filename from BT-124 #ef= reference (grandparent = boxtabelle container).
        if (filename.isEmpty()) {
            val efHref = div.parent()?.parent()?.selectFirst("a[href^='#ef=']")?.attr("href") ?: ""
            if (efHref.startsWith("#ef=")) {
                val efFilename = efHref.removePrefix("#ef=")
                filename = attachmentData.keys.find { it.endsWith(efFilename, ignoreCase = true) } ?: efFilename
                APP_LOGGER.info("Resolved filename via #ef= reference: '$efFilename' → '$filename'")
            }
        }

        // Strategy 1: exact match
        var resolvedName: String? = attachmentData.keys.find { it == filename }
        // Strategy 1b: case-insensitive
        if (resolvedName == null && filename.isNotEmpty())
            resolvedName = attachmentData.keys.find { it.equals(filename, ignoreCase = true) }
        // Strategy 1c: endsWith (e.g. "Aufmass.png" matches "EN16931_Elektron_Aufmass.png")
        if (resolvedName == null && filename.isNotEmpty())
            resolvedName = attachmentData.keys.find { it.endsWith(filename, ignoreCase = true) }
        // Strategy 2: index-based
        if (resolvedName == null && index < attachments.size) {
            resolvedName = attachments[index].first
            APP_LOGGER.info("Matched attachment by index ($index): $resolvedName")
            if (filename.isEmpty()) filename = resolvedName
        }
        // Strategy 3: base64 content match
        if (resolvedName == null) {
            val divContent = div.text().replace("\\s".toRegex(), "")
            if (divContent.isNotEmpty()) {
                resolvedName = attachmentContentMap[divContent]
                if (resolvedName != null) {
                    APP_LOGGER.info("Matched attachment by content for div ID: $id")
                    if (filename.isEmpty()) filename = resolvedName
                }
            }
        }
        // Strategy 4: single attachment fallback
        if (resolvedName == null && attachmentData.size == 1) {
            resolvedName = attachmentData.keys.first()
            APP_LOGGER.info("Fallback: single attachment used for '$filename'")
            if (filename.isEmpty()) filename = resolvedName
        }

        if (resolvedName != null) {
            val bytes = attachmentData[resolvedName]!!
            val mimeType = div.attr("mimetype").ifEmpty { div.attr("type") }.ifEmpty { "application/octet-stream" }

            // Register bytes in global map — temp file written on demand when user clicks.
            globalAttachmentData[filename] = Pair(bytes, mimeType)

            val encodedFilename = java.net.URLEncoder.encode(filename, "UTF-8")
            link.attr("href", "zugferd-attachment://$encodedFilename")
            link.removeAttr("onclick")
            link.removeAttr("download")
            link.text("Anhang öffnen")
            APP_LOGGER.info("Replaced link for '$filename' with in-memory attachment reference (${bytes.size} bytes)")
        } else {
            APP_LOGGER.warn("No matching attachment found for filename '$filename'")
        }
    }

    return doc.outerHtml()
}

actual fun getXmlFromPdf(pdf: PlatformFile): String? {
    return try {
        pdf.file
            .inputStream().use { input ->
                ZUGFeRDImporter(input)
            }
            .utF8
            ?.trimToNull()
    } catch (e: Exception) {
        APP_LOGGER.error("PDF read error.", e)
        null
    }
}

actual suspend fun getHtmlVisualizationFromXML(xml: Path): String? {
    return try {
        // ZUGFeRDVisualizer auf IO-Dispatcher ausführen (blockierende XSLT-Transformation)
        // Timeout verhindert endloses Hängen bei beschädigten oder sehr großen Dateien
        val rawHtml = withContext(Dispatchers.IO) {
            withTimeoutOrNull(30_000L) {
                ZUGFeRDVisualizer().visualize(xml.pathString, ZUGFeRDVisualizer.Language.DE)
            }
        }

        if (rawHtml.isNullOrBlank() || rawHtml.length < 200) {
            APP_LOGGER.warn("ZUGFeRDVisualizer lieferte leeres HTML – Fallback wird verwendet.")
            return generateFallbackHtml(xml)
        }

        val doc: Document = Jsoup.parse(rawHtml)

        // Benutzerdefiniertes CSS einfügen
        doc.head().appendElement("style").appendText(CUSTOM_VISUALIZATION_CSS)

        // 1. BT-IDs in boxzeile
        val boxRows = doc.select("div.boxzeile")
        for (row in boxRows) {
            val legend = row.selectFirst("div.boxdaten.legende")
            val value = row.selectFirst("div.boxdaten.wert[id]")

            if (legend != null && value != null) {
                val btId = value.id()
                if (!legend.text().contains(btId)) {
                    val labelText = legend.text().removeSuffix(":").trim()
                    legend.text("$labelText [$btId]")
                }
            }
        }

        //  2. BT-IDs in rechnungsZeile → rechnungSp1 bekommt die ID von rechnungSp3
        val invoiceRows = doc.select("div.rechnungsZeile")
        for (row in invoiceRows) {
            val label = row.selectFirst("div.rechnungSp1")
            val idSource = row.selectFirst("div.rechnungSp3[id]")

            if (label != null && idSource != null) {
                val btId = idSource.id()
                if (!label.text().contains(btId)) {
                    val text = label.text().removeSuffix(":").trim()
                    label.text("$text [$btId]")
                }
            }
        }

        doc.outerHtml()
    } catch (e: Exception) {
        APP_LOGGER.error("HTML-Visualisierung fehlgeschlagen.", e)
        generateFallbackHtml(xml)
    }
}

private fun generateFallbackHtml(xml: Path): String {
    val xmlContent = try {
        xml.toFile().readText(Charsets.UTF_8)
    } catch (e: Exception) {
        "<!-- Fehler beim Lesen der XML-Datei: ${e.message} -->"
    }
    val escaped = xmlContent
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

actual suspend fun getHtmlVisualizationFromXML(xml: PlatformFile): String? {
    return getHtmlVisualizationFromXML(xml.file.toPath())
}


actual suspend fun getHtmlVisualizationFromPdf(pdf: PlatformFile): String? {
    val xmlData = getXmlFromPdf(pdf) ?: return null
    val tempXmlFile = withContext(Dispatchers.IO) {
        val tempXmlFile = Files.createTempFile("zugferd-", ".xml")
        tempXmlFile.writer().use { writer ->
            writer.write(xmlData)
        }
        tempXmlFile
    }
    return getHtmlVisualizationFromXML(tempXmlFile)
}

/**
 * Renders [xml] to [target] via Mustang's own ZUGFeRDVisualizer PDF template — a paginated
 * invoice document combining all sections (overview, line items, attachments, history, ...)
 * with no interactive tab concept, unlike the app's HTML preview. The invoice XML is then
 * embedded into that freshly rendered PDF (not any original source PDF) — unless the XML
 * isn't embeddable this way (see [isEmbeddableAsZugferdXml]), in which case the plain
 * rendered PDF is used as-is.
 *
 * The whole result is built in memory before writing to [target], so a failure never leaves
 * behind a truncated/empty file at the target location.
 *
 * Note: the current Mustang version (2.21.0) has no language parameter for this PDF template —
 * the output is always in German, regardless of the app's current language.
 */
suspend fun exportVisualizationAsPdf(xml: String, target: PlatformFile) {
    withContext(Dispatchers.IO) {
        val tempXml = File.createTempFile("quba_visual_export_", ".xml")
        val tempPdf = File.createTempFile("quba_visual_export_", ".pdf")
        try {
            tempXml.writeText(xml, Charsets.UTF_8)
            ZUGFeRDVisualizer().toPDF(tempXml.absolutePath, tempPdf.absolutePath)
            val plainPdfBytes = tempPdf.readBytes()

            val resultBytes = if (isEmbeddableAsZugferdXml(xml)) {
                val buffer = java.io.ByteArrayOutputStream()
                embedZugferdXmlIntoPdf(plainPdfBytes, xml, buffer)
                buffer.toByteArray()
            } else {
                // ZUGFeRD/Factur-X XML embedding is only defined for CII-based XML (ZUGFeRD,
                // Factur-X, XRechnung-CII, Order-X). UBL invoices use a different, PDF-independent
                // e-invoice format that can't be embedded this way — Mustang's own XML provider
                // would reject it with a RuntimeException — so we just keep the plain PDF.
                APP_LOGGER.info("XML ist kein CII/ZUGFeRD-Format — PDF wird ohne eingebettete XML-Daten exportiert.")
                plainPdfBytes
            }

            target.file.writeBytes(resultBytes)
        } finally {
            tempXml.delete()
            tempPdf.delete()
        }
    }
}

/**
 * Whether [xml] can be embedded into a PDF via [ZUGFeRDExporterFromA3.setXML] — mirrors the
 * exact check Mustang's own `CustomXMLProvider.setXML()` performs internally, so this never
 * drifts out of sync with what would actually be accepted.
 */
private fun isEmbeddableAsZugferdXml(xml: String): Boolean =
    xml.contains("rsm:CrossIndustry") ||
        xml.contains("rsm:SCRDMCCBDACIOMessageStructure") ||
        xml.contains("SCRDMCCBDACIDAMessageStructure")

/**
 * Embeds raw ZUGFeRD/Factur-X XML into an arbitrary (not necessarily PDF/A) PDF.
 * Uses [ZUGFeRDExporterFromA3] directly rather than [ZUGFeRDExporterFromPDFA],
 * since the latter requires pre-existing PDF/A XMP metadata to detect the conformance
 * level — which a freshly rendered PDF does not have.
 */
private fun embedZugferdXmlIntoPdf(pdfBytes: ByteArray, xml: String, output: OutputStream) {
    ZUGFeRDExporterFromA3()
        .load(pdfBytes)
        .setProducer("$APP_TITLE_FULL $APP_VERSION")
        .setCreator("$APP_TITLE_FULL $APP_VERSION")
        .setXML(xml.toByteArray(Charsets.UTF_8))
        .export(output)
}