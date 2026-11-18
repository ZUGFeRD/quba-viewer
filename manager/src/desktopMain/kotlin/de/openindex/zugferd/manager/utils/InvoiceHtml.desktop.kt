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
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.mustangproject.XMLTools
import org.mustangproject.ZUGFeRD.ZUGFeRDVisualizer
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.StringReader
import java.net.URLEncoder
import java.util.Base64
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamConstants
import javax.xml.transform.Templates
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.URIResolver
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

/**
 * Same XSLT pipeline as Mustang's [ZUGFeRDVisualizer.visualize] (German HTML), but:
 *
 * - every stylesheet is compiled once per app run and then reused. Mustang compiles three
 *   stylesheets for every new visualizer instance, and even a reused instance recompiles the
 *   HTML stylesheet on every call (eager `putIfAbsent` argument).
 * - the root element is detected with a streaming reader instead of a full DOM parse.
 * - no temp files, no extra copy thread; everything stays in memory.
 * - UBL credit notes always use the credit-note stylesheet (Mustang shares one cached
 *   template between UBL invoices and credit notes, so whichever came first wins).
 *
 * [Templates] are thread-safe, so several documents can be rendered at the same time.
 */
internal object InvoiceHtmlRenderer {
    private val classLoader: ClassLoader = ZUGFeRDVisualizer::class.java.classLoader

    private val factory: TransformerFactory by lazy {
        XMLTools.getTransformerFactory().apply {
            uriResolver = URIResolver { href, _ ->
                StreamSource(classLoader.getResourceAsStream("stylesheets/$href"))
            }
        }
    }

    private val ciiToXr by lazy { compile("cii-xr.xsl", patchSourcePaths = true) }
    private val ublInvoiceToXr by lazy { compile("ubl-invoice-xr.xsl", patchSourcePaths = true) }
    private val ublCreditNoteToXr by lazy { compile("ubl-creditnote-xr.xsl", patchSourcePaths = true) }
    private val cioToXr by lazy { compile("cio-xr.xsl", patchSourcePaths = true) }
    private val xrToHtml by lazy { compile("xrechnung-html.de.xsl") }
    private val zf1ToHtml by lazy { compile("ZUGFeRD_1p0_c1p0_s1p0.xslt") }

    private fun compile(name: String, patchSourcePaths: Boolean = false): Templates {
        val started = System.currentTimeMillis()
        val original = classLoader.getResourceAsStream("stylesheets/$name")
            ?.use { it.readBytes().toString(Charsets.UTF_8) }
            ?: throw IllegalStateException("Stylesheet '$name' nicht gefunden")

        val patched = if (patchSourcePaths) XrSourcePathPatch.apply(original) else null
        val templates = if (patched != null) {
            try {
                newTemplates(patched)
            } catch (e: TransformerException) {
                APP_LOGGER.warn("Optimiertes XSLT '$name' nicht kompilierbar, nutze Original: ${e.message}")
                newTemplates(original)
            }
        } else {
            if (patchSourcePaths) APP_LOGGER.warn("XSLT '$name': Optimierung nicht anwendbar, nutze Original.")
            newTemplates(original)
        }

        APP_LOGGER.info("XSLT '$name' kompiliert in ${System.currentTimeMillis() - started} ms (optimiert: ${patched != null})")
        return templates
    }

    private fun newTemplates(xslt: String): Templates = synchronized(factory) {
        factory.newTemplates(StreamSource(StringReader(xslt)))
    }

    /** Compiles the stylesheets used by almost every invoice (CII → XR → HTML) ahead of time. */
    fun warmUp() {
        try {
            ciiToXr
            xrToHtml
        } catch (e: Exception) {
            APP_LOGGER.warn("XSLT-Vorkompilierung fehlgeschlagen: ${e.message}")
        }
    }

    fun render(xml: ByteArray): String {
        val toXr = when (val root = rootElementName(xml)) {
            "CrossIndustryDocument" -> return transform(zf1ToHtml, xml).toString(Charsets.UTF_8)
            "CrossIndustryInvoice" -> ciiToXr
            "Invoice" -> ublInvoiceToXr
            "CreditNote" -> ublCreditNoteToXr
            "SCRDMCCBDACIOMessageStructure" -> cioToXr
            else -> throw IllegalArgumentException("Kein unterstütztes Rechnungsformat (Wurzelelement: $root)")
        }
        val xr = transform(toXr, xml)
        return transform(xrToHtml, xr).toString(Charsets.UTF_8)
    }

    private fun transform(templates: Templates, input: ByteArray): ByteArray {
        val output = ByteArrayOutputStream(input.size)
        templates.newTransformer().transform(
            StreamSource(ByteArrayInputStream(input)),
            StreamResult(output),
        )
        return output.toByteArray()
    }

    private fun rootElementName(xml: ByteArray): String? {
        val inputFactory = XMLInputFactory.newFactory().apply {
            setProperty(XMLInputFactory.SUPPORT_DTD, false)
            setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false)
            setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, true)
        }
        val reader = inputFactory.createXMLStreamReader(ByteArrayInputStream(xml))
        try {
            while (reader.hasNext()) {
                if (reader.next() == XMLStreamConstants.START_ELEMENT) return reader.localName
            }
        } finally {
            reader.close()
        }
        return null
    }
}

/**
 * Removes the quadratic hot spot of the `*-xr.xsl` stylesheets (CII/UBL/CIO → XR).
 *
 * For each of the ~200 mapped fields they build a source path via `xr:src-path`, which counts,
 * on every ancestor level, all preceding siblings with the same name. Inside line item #2000
 * that means scanning 2000 line items for every field, so large invoices took seconds to
 * minutes (1,236 items ≈ 6 s, 2,472 items ≈ 22 s).
 *
 * The patch computes all sibling positions once in a single pass (an XPath 3.1 map keyed by
 * `generate-id()`) and looks them up instead. The generated paths stay exactly the same.
 * If the template text in a future Mustang version doesn't match, nothing is changed.
 */
internal object XrSourcePathPatch {
    private val SIBLING_INDEX = Regex(
        """<xsl:if\s+test="preceding-sibling::\*\[name\(\.\) = name\(current\(\)\)\] or following-sibling::\*\[name\(\.\) = name\(current\(\)\)\]"\s*>\s*""" +
            """<xsl:text>\[</xsl:text>\s*""" +
            """<xsl:value-of\s+select="count\(preceding-sibling::\*\[name\(\.\) = name\(current\(\)\)\]\) \+ 1"\s*/>\s*""" +
            """<xsl:text>\]</xsl:text>\s*</xsl:if>"""
    )

    private const val LOOKUP = """<xsl:variable name="quba-sibling-pos" select="${'$'}quba-sibling-positions(generate-id(.))"/>""" +
        """<xsl:if test="exists(${'$'}quba-sibling-pos)"><xsl:text>[</xsl:text><xsl:value-of select="${'$'}quba-sibling-pos"/><xsl:text>]</xsl:text></xsl:if>"""

    private const val POSITIONS = """
    <xsl:variable name="quba-sibling-positions" as="map(*)">
        <xsl:map>
            <xsl:for-each select="//*">
                <xsl:for-each-group select="*" group-by="name()">
                    <xsl:if test="count(current-group()) gt 1">
                        <xsl:for-each select="current-group()">
                            <xsl:map-entry key="generate-id(.)" select="position()"/>
                        </xsl:for-each>
                    </xsl:if>
                </xsl:for-each-group>
            </xsl:for-each>
        </xsl:map>
    </xsl:variable>
"""

    /** Patched stylesheet, or null if the expected template wasn't found exactly once. */
    fun apply(xslt: String): String? {
        if (SIBLING_INDEX.findAll(xslt).count() != 1) return null
        val end = xslt.lastIndexOf("</xsl:stylesheet>")
        if (end < 0) return null
        return xslt.substring(0, end).replace(SIBLING_INDEX) { LOOKUP } + POSITIONS + xslt.substring(end)
    }
}

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

private val DOWNLOAD_TARGET_ID = Regex("""downloadData\s*\(\s*['"]([^'"]+)['"]""")

/**
 * One Jsoup pass over the rendered HTML (previously two full parse/serialize rounds):
 * custom CSS, BT ids in labels, and attachment links rewired to in-memory data.
 */
internal fun postProcessInvoiceHtml(rawHtml: String, pdfAttachments: List<Pair<String, ByteArray>>): String {
    val doc = Jsoup.parse(rawHtml)
    doc.head().appendElement("style").appendText(CUSTOM_VISUALIZATION_CSS)
    addBusinessTermIds(doc)
    linkAttachments(doc, pdfAttachments)
    return doc.outerHtml()
}

private fun addBusinessTermIds(doc: Document) {
    // 1. BT ids in boxzeile
    for (row in doc.select("div.boxzeile")) {
        val legend = row.selectFirst("div.boxdaten.legende") ?: continue
        val value = row.selectFirst("div.boxdaten.wert[id]") ?: continue
        val btId = value.id()
        val legendText = legend.text()
        if (!legendText.contains(btId)) {
            legend.text("${legendText.removeSuffix(":").trim()} [$btId]")
        }
    }

    // 2. BT ids in rechnungsZeile → rechnungSp1 gets the id of rechnungSp3
    for (row in doc.select("div.rechnungsZeile")) {
        val label = row.selectFirst("div.rechnungSp1") ?: continue
        val idSource = row.selectFirst("div.rechnungSp3[id]") ?: continue
        val btId = idSource.id()
        val labelText = label.text()
        if (!labelText.contains(btId)) {
            label.text("${labelText.removeSuffix(":").trim()} [$btId]")
        }
    }
}

/**
 * Each attachment in the HTML is a hidden `<div id=… filename=… mimeType=…>BASE64</div>`
 * plus a link calling `downloadData(id)`. The div's own Base64 content is the attachment, so
 * it's decoded directly; matching against the PDF's embedded files only happens when a div
 * carries no data (e.g. BT-124 references via `#ef=`).
 *
 * Once a link points to the in-memory copy, the Base64 payload is removed from the page.
 * For invoices with large attachments this shrinks the HTML by megabytes, which the
 * browser then no longer has to parse, and the temp file gets smaller too.
 */
private fun linkAttachments(doc: Document, pdfAttachments: List<Pair<String, ByteArray>>) {
    val dataDivs = doc.select("div[filename], div[data-filename], div[mimetype]")
    if (dataDivs.isEmpty()) return

    // The invoice XML itself is never an "attachment" for a link.
    val candidates = pdfAttachments.filterNot { (name, _) ->
        name.lowercase().let { it == "factur-x.xml" || it == "zugferd-invoice.xml" || it == "xrechnung.xml" || it == "order-x.xml" }
    }

    // Index links by their downloadData() target once, instead of one document-wide query per div.
    val linksById = HashMap<String, Element>()
    for (link in doc.select("a[onclick]")) {
        val id = DOWNLOAD_TARGET_ID.find(link.attr("onclick"))?.groupValues?.get(1) ?: continue
        linksById.putIfAbsent(id, link)
    }

    for ((index, div) in dataDivs.withIndex()) {
        val id = div.id()
        if (id.isEmpty()) continue
        val link = linksById[id] ?: doc.selectFirst("a[onclick*='$id']") ?: continue

        var filename = div.attr("filename").ifEmpty { div.attr("data-filename") }

        // Resolve filename from BT-124 #ef= reference (grandparent = boxtabelle container).
        if (filename.isEmpty()) {
            val efHref = div.parent()?.parent()?.selectFirst("a[href^='#ef=']")?.attr("href").orEmpty()
            if (efHref.startsWith("#ef=")) {
                val efFilename = efHref.removePrefix("#ef=")
                filename = candidates.firstOrNull { it.first.endsWith(efFilename, ignoreCase = true) }?.first ?: efFilename
            }
        }

        var bytes = decodeBase64OrNull(div.wholeText())
        if (bytes == null) {
            val match = findAttachment(filename, index, candidates)
            if (match != null) {
                if (filename.isEmpty()) filename = match.first
                bytes = match.second
            }
        }

        if (bytes == null) {
            APP_LOGGER.warn("Kein passender Anhang gefunden für '$filename'")
            continue
        }

        if (filename.isEmpty()) filename = "Anhang_${index + 1}"
        val mimeType = div.attr("mimetype").ifEmpty { div.attr("type") }.ifEmpty { "application/octet-stream" }
        val key = registerAttachment(filename, bytes, mimeType)

        link.attr("href", "zugferd-attachment://" + URLEncoder.encode(key, Charsets.UTF_8))
        link.removeAttr("onclick")
        link.removeAttr("download")
        link.text("Anhang öffnen")
        div.empty()
    }
}

private fun findAttachment(
    filename: String,
    index: Int,
    candidates: List<Pair<String, ByteArray>>,
): Pair<String, ByteArray>? {
    if (candidates.isEmpty()) return null
    if (filename.isNotEmpty()) {
        candidates.firstOrNull { it.first == filename }?.let { return it }
        candidates.firstOrNull { it.first.equals(filename, ignoreCase = true) }?.let { return it }
        // e.g. "Aufmass.png" matches "EN16931_Elektron_Aufmass.png"
        candidates.firstOrNull { it.first.endsWith(filename, ignoreCase = true) }?.let { return it }
    }
    if (index < candidates.size && filename.isEmpty()) return candidates[index]
    if (candidates.size == 1) return candidates.first()
    return null
}

/** Strict Base64 decode that ignores whitespace; null for empty or invalid content. */
private fun decodeBase64OrNull(text: String): ByteArray? {
    if (text.isBlank()) return null
    val compact = StringBuilder(text.length)
    for (c in text) if (!c.isWhitespace()) compact.append(c)
    return try {
        Base64.getDecoder().decode(compact.toString()).takeIf { it.isNotEmpty() }
    } catch (e: IllegalArgumentException) {
        null
    }
}
