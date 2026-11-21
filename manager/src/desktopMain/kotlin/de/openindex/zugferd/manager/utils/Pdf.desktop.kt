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
import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary
import org.apache.pdfbox.pdmodel.PDEmbeddedFilesNameTreeNode
import org.apache.xmpbox.xml.DomXmpParser
import org.apache.xmpbox.xml.XmpParsingException
import org.mustangproject.ZUGFeRD.ZUGFeRDExporterFromA3
import org.mustangproject.ZUGFeRD.ZUGFeRDExporterFromPDFA
import org.mustangproject.ZUGFeRD.ZUGFeRDVisualizer
import java.io.File
import java.io.IOException
import java.io.OutputStream

actual suspend fun getPdfArchiveVersion(pdfFile: PlatformFile): Int = withContext(Dispatchers.IO) {
    try {
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
    Loader.loadPDF(pdfFile).use { document ->
        // the PDF version we could get through the document, but we want the PDF-A version,
        // which is different (and can probably base on different PDF versions)
        val metadata = document.documentCatalog.metadata ?: return 0
        try {
            val xmp = metadata.createInputStream().use { DomXmpParser().parse(it) }
            xmp.pdfaIdentificationSchema?.let { return it.part }
        } catch (e: XmpParsingException) {
            APP_LOGGER.error("XmpParsingException", e)
        }
        return 0
    }
}

@Throws(IOException::class)
fun PDDocument.removeEmbeddedFiles(): PDDocument {
    val names = PDDocumentNameDictionary(documentCatalog)
    names.embeddedFiles = PDEmbeddedFilesNameTreeNode()
    documentCatalog.names = names
    return this
}

/**
 * Maps the application's current language to the language of Mustang's visualization templates.
 */
private fun getVisualizerLanguage(): ZUGFeRDVisualizer.Language =
    when (getCurrentLanguage()) {
        Language.DE -> ZUGFeRDVisualizer.Language.DE
        Language.EN -> ZUGFeRDVisualizer.Language.EN
        Language.FR -> ZUGFeRDVisualizer.Language.FR
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
 * The PDF is rendered in the app's current language (German, English or French).
 */
suspend fun exportVisualizationAsPdf(xml: String, target: PlatformFile) {
    val language = getVisualizerLanguage()
    withContext(Dispatchers.IO) {
        val tempXml = File.createTempFile("quba_visual_export_", ".xml")
        val tempPdf = File.createTempFile("quba_visual_export_", ".pdf")
        try {
            tempXml.writeText(xml, Charsets.UTF_8)
            ZUGFeRDVisualizer().toPDF(tempXml.absolutePath, tempPdf.absolutePath, language)
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