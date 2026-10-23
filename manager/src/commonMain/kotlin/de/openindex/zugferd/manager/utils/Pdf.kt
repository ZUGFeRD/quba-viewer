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

import io.github.vinceglb.filekit.core.PlatformFile
import java.nio.file.Path

const val MAX_PDF_ARCHIVE_VERSION = 3

fun isPdfArchive(version: Int): Boolean =
    version > 0

fun isSupportedPdfArchiveVersion(version: Int): Boolean =
    version == 1 || version == MAX_PDF_ARCHIVE_VERSION

@Suppress("unused")
suspend fun isPdfArchive(pdfFile: PlatformFile): Boolean =
    isPdfArchive(getPdfArchiveVersion(pdfFile))

expect suspend fun getPdfArchiveVersion(pdfFile: PlatformFile): Int



expect fun getXmlFromPdf(pdf: PlatformFile): String?

expect fun getAttachmentsFromPdf(pdf: PlatformFile): List<Pair<String, ByteArray>>

expect fun getAttachmentsFromXml(xmlContent: String): List<Pair<String, ByteArray>>

expect fun postProcessHtmlForAttachments(html: String, attachments: List<Pair<String, ByteArray>>): String

expect suspend fun getHtmlVisualizationFromPdf(pdf: PlatformFile): String?

expect suspend fun getHtmlVisualizationFromXML(xml: Path): String?

expect suspend fun getHtmlVisualizationFromXML(xml: PlatformFile): String?

suspend fun getHtmlWithAttachments(pdf: PlatformFile): String? {
    val attachments = getAttachmentsFromPdf(pdf)
    val rawHtml = getHtmlVisualizationFromPdf(pdf) ?: return null
    return postProcessHtmlForAttachments(rawHtml, attachments)
}

suspend fun getHtmlWithAttachmentsFromXml(xmlPath: Path, xmlContent: String): String? {
    val attachments = getAttachmentsFromXml(xmlContent)
    val rawHtml = getHtmlVisualizationFromXML(xmlPath) ?: return null
    if (attachments.isEmpty()) return rawHtml
    return postProcessHtmlForAttachments(rawHtml, attachments)
}
