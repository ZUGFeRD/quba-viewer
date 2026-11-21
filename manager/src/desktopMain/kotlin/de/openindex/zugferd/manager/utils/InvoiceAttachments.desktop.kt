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
import de.openindex.zugferd.quba.generated.resources.AppVisualisationAttachmentEmbeddedFiles
import de.openindex.zugferd.quba.generated.resources.AppVisualisationAttachmentFile
import de.openindex.zugferd.quba.generated.resources.AppVisualisationAttachmentMissing
import de.openindex.zugferd.quba.generated.resources.AppVisualisationAttachmentOpen
import de.openindex.zugferd.quba.generated.resources.Res
import kotlinx.coroutines.runBlocking
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.net.URLEncoder
import java.util.Base64

private val DOWNLOAD_TARGET_ID = Regex("""downloadData\s*\(\s*['"]([^'"]+)['"]""")
private val CAMEL_CASE_BOUNDARY = Regex("(?<=[a-zäöüß])(?=[A-ZÄÖÜ])")
private val NAME_SEPARATORS = Regex("[^A-Za-z0-9ÄÖÜäöüß]+")


/** File extension → MIME type for the formats that occur as invoice attachments. */
private val MIME_BY_EXTENSION = mapOf(
    "pdf" to "application/pdf",
    "png" to "image/png",
    "jpg" to "image/jpeg",
    "jpeg" to "image/jpeg",
    "gif" to "image/gif",
    "bmp" to "image/bmp",
    "webp" to "image/webp",
    "svg" to "image/svg+xml",
    "tif" to "image/tiff",
    "tiff" to "image/tiff",
    "txt" to "text/plain",
    "csv" to "text/csv",
    "xml" to "application/xml",
    "htm" to "text/html",
    "html" to "text/html",
    "json" to "application/json",
    "xlsx" to "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    "xls" to "application/vnd.ms-excel",
    "ods" to "application/vnd.oasis.opendocument.spreadsheet",
    "docx" to "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    "doc" to "application/msword",
    "odt" to "application/vnd.oasis.opendocument.text",
    "pptx" to "application/vnd.openxmlformats-officedocument.presentationml.presentation",
    "zip" to "application/zip",
    "eml" to "message/rfc822",
)

private val EXTENSION_BY_MIME: Map<String, String> = buildMap {
    MIME_BY_EXTENSION.forEach { (ext, mime) -> putIfAbsent(mime, ext) }
    put("image/jpeg", "jpg")
    put("text/xml", "xml")
}

/** Lower-case extension of [name], or null if it has none. */
internal fun extensionOf(name: String): String? =
    name.substringAfterLast('/').substringAfterLast('\\')
        .let { if ('.' in it) it.substringAfterLast('.').lowercase().ifEmpty { null } else null }

/** The declared MIME type if there is one, otherwise derived from the file extension. */
internal fun mimeTypeFor(name: String, declared: String?): String =
    declared?.trim()?.takeIf { it.isNotEmpty() && it != "application/octet-stream" }
        ?: extensionOf(name)?.let { MIME_BY_EXTENSION[it] }
        ?: "application/octet-stream"

/** Appends an extension from [mimeType] if [name] has none, so viewers and the OS recognise the file. */
internal fun withExtension(name: String, mimeType: String): String {
    if (extensionOf(name) != null) return name
    val ext = EXTENSION_BY_MIME[mimeType.substringBefore(';').trim().lowercase()] ?: return name
    return "$name.$ext"
}

private class AttachmentLink(
    val index: Int,
    val div: Element,
    val link: Element,
    /** BT-125 file name from the data div, may be empty. */
    val declaredName: String,
    /** File name from a BT-124 `#ef=` reference (a file embedded in the PDF), if any. */
    val reference: String?,
    val declaredMime: String,
) {
    var bytes: ByteArray? = null
    var matchedName: String? = null

    /** Names this link is looking for, most specific first. */
    val wantedNames: List<String> = listOfNotNull(declaredName.ifEmpty { null }, reference)

    val wantedExtension: String?
        get() = wantedNames.firstNotNullOfOrNull { extensionOf(it) }
            ?: declaredMime.takeIf { it.isNotBlank() }?.let { EXTENSION_BY_MIME[it.lowercase()] }

    val displayName: String
        get() = withExtension(
            declaredName.ifEmpty { matchedName ?: reference ?: "Anhang_${index + 1}" },
            mimeTypeFor(matchedName ?: declaredName, declaredMime),
        )
}

private class EmbeddedFile(val name: String, val bytes: ByteArray) {
    var used = false
}

/**
 * Wires every attachment of the HTML visualization to an in-memory copy.
 *
 * Mustang renders each attachment as a hidden `<div id=… filename=… mimeType=…>BASE64</div>`
 * plus a link calling `downloadData(id)`. That script opens a popup, which Quba blocks, so
 * a link that isn't rewired only jumps to the top of the page.
 *
 * 1. If the div carries Base64 data (attachment inside the XML), that data is the attachment.
 * 2. Otherwise the attachment is a file embedded in the PDF, usually referenced as
 *    `#ef=<name>` (BT-124). The referenced name often differs from the real file name
 *    (`ElektronRapport_neu-red.pdf` vs. `EN16931_Elektron_ElektronRapport.pdf`), so all links
 *    are matched together against all embedded files, in passes from strict to loose, and
 *    each file is used at most once (see [matchEmbeddedFiles]).
 * 3. A link that still has no data shows a short note instead of a dead link.
 * 4. Embedded files that no link points to get their own list in the "Anhänge" tab.
 *
 * Base64 payloads are removed from the page once rewired, which keeps the HTML small.
 */
internal fun linkAttachments(doc: Document, pdfAttachments: List<Pair<String, ByteArray>>) {
    val embedded = pdfAttachments
        .filterNot { (name, _) -> name.lowercase() in INVOICE_XML_NAMES }
        .map { (name, bytes) -> EmbeddedFile(name, bytes) }

    val linksById = HashMap<String, Element>()
    for (link in doc.select("a[onclick]")) {
        val id = DOWNLOAD_TARGET_ID.find(link.attr("onclick"))?.groupValues?.get(1) ?: continue
        linksById.putIfAbsent(id, link)
    }

    val links = ArrayList<AttachmentLink>()
    for ((index, div) in doc.select("div[filename], div[data-filename], div[mimetype]").withIndex()) {
        val id = div.id()
        if (id.isEmpty()) continue
        val link = linksById[id] ?: continue
        val reference = div.parent()?.parent()
            ?.selectFirst("a[href^='#ef=']")?.attr("href")
            ?.removePrefix("#ef=")?.trim()?.ifEmpty { null }

        links += AttachmentLink(
            index = index,
            div = div,
            link = link,
            declaredName = div.attr("filename").ifEmpty { div.attr("data-filename") }.trim(),
            reference = reference,
            declaredMime = div.attr("mimetype").ifEmpty { div.attr("type") }.trim(),
        ).also { it.bytes = decodeBase64OrNull(div.wholeText()) }
    }

    matchEmbeddedFiles(links.filter { it.bytes == null }, embedded)

    val linkedContent = HashSet<String>()
    for (attachment in links) {
        val bytes = attachment.bytes
        if (bytes == null) {
            APP_LOGGER.warn("Kein passender Anhang gefunden für '${attachment.wantedNames.joinToString(" / ")}'")
            markMissing(attachment.link)
            continue
        }
        val name = attachment.displayName
        val key = registerAttachment(name, bytes, mimeTypeFor(name, attachment.declaredMime))
        attachment.link.attr("href", attachmentUrl(key))
        attachment.link.removeAttr("onclick")
        attachment.link.removeAttr("download")
        attachment.link.text(runBlocking { getString(Res.string.AppVisualisationAttachmentOpen) })
        attachment.div.empty()
        linkedContent += contentId(bytes)
    }

    // Download links whose data div couldn't be identified at all.
    for (link in doc.select("a[onclick*=downloadData]")) markMissing(link)

    val unlinked = embedded.filter { !it.used && contentId(it.bytes) !in linkedContent }
    if (unlinked.isNotEmpty()) appendEmbeddedFiles(doc, unlinked)
}

/**
 * Assigns embedded PDF files to links without own data. Each pass only looks at links and
 * files that are still free, so a strict match is never taken away by a looser one.
 */
private fun matchEmbeddedFiles(open: List<AttachmentLink>, files: List<EmbeddedFile>) {
    if (open.isEmpty() || files.isEmpty()) return

    fun assign(link: AttachmentLink, file: EmbeddedFile) {
        link.bytes = file.bytes
        link.matchedName = file.name
        file.used = true
    }

    fun free() = files.filter { !it.used }
    fun unmatched() = open.filter { it.bytes == null }

    // 1–3: same name, same name ignoring case, one name ends with the other
    // ("Aufmass.png" ↔ "EN16931_Elektron_Aufmass.png").
    val namePasses: List<(String, String) -> Boolean> = listOf(
        { wanted, actual -> actual == wanted },
        { wanted, actual -> actual.equals(wanted, ignoreCase = true) },
        { wanted, actual -> actual.endsWith(wanted, ignoreCase = true) || wanted.endsWith(actual, ignoreCase = true) },
    )
    for (matches in namePasses) {
        for (link in unmatched()) {
            free().firstOrNull { file -> link.wantedNames.any { matches(it, file.name) } }?.let { assign(link, it) }
        }
    }

    // 4: same file type and the most shared name parts; ties stay open.
    for (link in unmatched()) {
        if (link.wantedNames.isEmpty()) continue
        val ext = link.wantedExtension
        val scored = free()
            .filter { ext == null || extensionOf(it.name) == ext }
            .map { file -> file to link.wantedNames.maxOf { sharedNameParts(it, file.name) } }
            .filter { it.second > 0 }
        val best = scored.maxByOrNull { it.second } ?: continue
        if (scored.count { it.second == best.second } == 1) assign(link, best.first)
    }

    // 5: exactly one free file of the wanted type.
    for (link in unmatched()) {
        val ext = link.wantedExtension ?: continue
        free().filter { extensionOf(it.name) == ext }.singleOrNull()?.let { assign(link, it) }
    }

    // 6: as many open links as free files, with compatible types: pair them in order.
    val stillOpen = unmatched()
    val remaining = free()
    if (stillOpen.isNotEmpty() && stillOpen.size == remaining.size) {
        val pairs = stillOpen.zip(remaining)
        val compatible = pairs.all { (link, file) ->
            val wanted = link.wantedExtension
            val actual = extensionOf(file.name)
            wanted == null || actual == null || wanted == actual
        }
        if (compatible) pairs.forEach { (link, file) -> assign(link, file) }
    }
}

/** Number of name parts (≥ 3 characters, split at separators and camelCase) both names share. */
private fun sharedNameParts(a: String, b: String): Int = (nameParts(a) intersect nameParts(b)).size

private fun nameParts(name: String): Set<String> {
    val stem = name.substringAfterLast('/').substringAfterLast('\\').substringBeforeLast('.')
    val parts = stem.split(NAME_SEPARATORS).filter { it.isNotEmpty() }
    return (parts + parts.flatMap { it.split(CAMEL_CASE_BOUNDARY) })
        .map { it.lowercase() }
        .filter { it.length >= 3 }
        .toSet()
}

private fun markMissing(link: Element) {
    link.replaceWith(
        Element("span")
            .attr("style", "color:#888;font-style:italic")
            .text(runBlocking { getString(Res.string.AppVisualisationAttachmentMissing) })
    )
}

private fun appendEmbeddedFiles(doc: Document, files: List<EmbeddedFile>) {
    val container = doc.getElementById("anlagen") ?: doc.body()
    val box = container.appendElement("div").attr("class", "boxtabelle boxabstandtop")
        .appendElement("div").attr("class", "boxzeile")
        .appendElement("div").attr("class", "box")
    box.appendElement("div").attr("class", "boxtitel")
        .text(runBlocking { getString(Res.string.AppVisualisationAttachmentEmbeddedFiles) })
    val fileLabel = runBlocking { getString(Res.string.AppVisualisationAttachmentFile) }
    val table = box.appendElement("div").attr("class", "boxtabelle boxinhalt borderSpacing")

    for (file in files) {
        val mimeType = mimeTypeFor(file.name, null)
        val name = withExtension(file.name, mimeType)
        val key = registerAttachment(name, file.bytes, mimeType)
        val row = table.appendElement("div").attr("class", "boxzeile")
        row.appendElement("div").attr("class", "boxdaten legende").text(fileLabel)
        row.appendElement("div").attr("class", "boxdaten wert")
            .appendElement("a").attr("href", attachmentUrl(key)).text(name)
            .parent()!!.appendText(" (${formatSize(file.bytes.size)})")
    }
}

private fun attachmentUrl(key: String): String =
    "zugferd-attachment://" + URLEncoder.encode(key, Charsets.UTF_8)

private fun contentId(bytes: ByteArray): String = "${bytes.size}-${bytes.contentHashCode()}"

private fun formatSize(bytes: Int): String = when {
    bytes >= 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
    bytes >= 1024 -> "${bytes / 1024} KB"
    else -> "$bytes Bytes"
}

/** Strict Base64 decode that ignores whitespace; null for empty or invalid content. */
private fun decodeBase64OrNull(text: String): ByteArray? {
    if (text.isBlank()) return null
    val compact = StringBuilder(text.length)
    for (c in text) if (!c.isWhitespace()) compact.append(c)
    return try {
        Base64.getDecoder().decode(compact.toString()).takeIf { it.isNotEmpty() }
    } catch (e: IllegalArgumentException) {
        APP_LOGGER.warn("Base64-Daten eines Anhangs sind ungültig: ${e.message}")
        null
    }
}
