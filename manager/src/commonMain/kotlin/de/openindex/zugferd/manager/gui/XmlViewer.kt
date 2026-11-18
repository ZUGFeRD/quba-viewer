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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import de.openindex.zugferd.manager.sections.SearchState
import de.openindex.zugferd.manager.theme.LocalQubaColors
import de.openindex.zugferd.manager.theme.LocalQubaTypography
import de.openindex.zugferd.manager.theme.QubaColorScheme
import de.openindex.zugferd.manager.utils.stringResource
import de.openindex.zugferd.quba.generated.resources.AppXmlViewerCopyAll
import de.openindex.zugferd.quba.generated.resources.Res
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Longer lines (typically Base64 attachments) are split into rows of this length. */
private const val MAX_ROW_CHARS = 2_000

/** Up to this size the line index is built synchronously, so small files show without a blank frame. */
private const val SYNC_INDEX_CHARS = 300_000

/** More search hits than this aren't highlighted; jumping still works for the first ones. */
private const val MAX_SEARCH_HITS = 50_000

/** Tabs are shown as this many spaces (the mono font has no glyph for the tab character). */
private const val TAB_WIDTH = 4

/**
 * Read-only XML view that stays fast for very large invoices.
 *
 * Only the visible lines are laid out and highlighted. The previous single TextField laid out
 * the whole document at once and re-ran its regex highlighting over everything on every click,
 * which froze the UI for large files (and highlighting was switched off above 500 KB).
 */
@Composable
@OptIn(ExperimentalFoundationApi::class)
fun XmlViewer(
    xml: String,
    modifier: Modifier = Modifier,
    search: SearchState? = null,
) {
    val colors = LocalQubaColors.current
    val textStyle = LocalQubaTypography.current.mono.copy(color = colors.text)
    val palette = remember(colors) { XmlPalette.of(colors) }
    val clipboard = LocalClipboardManager.current
    val listState = remember(xml) { LazyListState() }

    val rows by produceState(
        initialValue = if (xml.length <= SYNC_INDEX_CHARS) XmlRows.of(xml) else null,
        key1 = xml,
    ) {
        if (value?.source !== xml) {
            value = if (xml.length <= SYNC_INDEX_CHARS) XmlRows.of(xml)
            else withContext(Dispatchers.Default) { XmlRows.of(xml) }
        }
    }

    val query = search?.query?.takeIf { it.isNotBlank() }
    val hits by produceState(IntArray(0), xml, query) {
        value = if (query == null) IntArray(0)
        else withContext(Dispatchers.Default) { findAll(xml, query) }
    }
    val currentHit = if (hits.isEmpty() || search == null) -1
    else hits[((search.sequence % hits.size) + hits.size) % hits.size]

    LaunchedEffect(currentHit, rows) {
        val index = rows ?: return@LaunchedEffect
        if (currentHit >= 0) {
            listState.scrollToItem((index.rowOf(currentHit) - 3).coerceAtLeast(0))
        }
    }

    // Gutter wide enough for the largest line number.
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val digits = (rows?.lineCount ?: 1).toString().length.coerceAtLeast(2)
    val gutterWidth = remember(digits, textStyle, density) {
        with(density) { measurer.measure("0".repeat(digits), textStyle).size.width.toDp() } + 20.dp
    }

    Box(
        modifier = modifier
            .clipToBounds()
            .background(colors.surfaceInset),
    ) {
        val index = rows
        if (index != null) {
            SelectionContainer {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 8.dp, bottom = 8.dp, end = 14.dp),
                ) {
                    items(count = index.size) { row ->
                        val start = index.starts[row]
                        val end = index.ends[row]
                        val text = remember(index, row, palette, hits, currentHit) {
                            buildRowText(
                                source = index.source,
                                start = start,
                                end = end,
                                commentEnd = index.commentEndAt(start),
                                palette = palette,
                                hits = hits,
                                queryLength = query?.length ?: 0,
                                currentHit = currentHit,
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth()) {
                            DisableSelection {
                                val number = index.lineNumbers[row]
                                Text(
                                    text = if (number > 0) number.toString() else "",
                                    style = textStyle.copy(color = colors.text4, textAlign = TextAlign.End),
                                    softWrap = false,
                                    modifier = Modifier
                                        .width(gutterWidth)
                                        .padding(end = 12.dp),
                                )
                            }
                            Text(
                                text = text,
                                style = textStyle,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }

            VerticalScrollbar(
                adapter = rememberScrollbarAdapter(listState),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight(),
            )

            if (xml.isNotEmpty()) {
                Tooltip(
                    text = stringResource(Res.string.AppXmlViewerCopyAll),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 4.dp, end = 18.dp),
                ) {
                    IconButton(
                        onClick = { clipboard.setText(AnnotatedString(xml)) },
                        modifier = Modifier
                            .size(30.dp)
                            .background(colors.surface.copy(alpha = 0.85f)),
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = stringResource(Res.string.AppXmlViewerCopyAll),
                            tint = colors.text3,
                            modifier = Modifier.size(15.dp),
                        )
                    }
                }
            }
        }
    }
}

/**
 * Display rows of an XML string: one per line, long lines split into [MAX_ROW_CHARS] chunks.
 * Stored as plain int arrays, so even a 50 MB file costs only a few MB of index.
 */
@Immutable
private class XmlRows(
    val source: String,
    val starts: IntArray,
    val ends: IntArray,
    /** Line number for the first row of a line, 0 for continuation rows. */
    val lineNumbers: IntArray,
    val lineCount: Int,
    /** Start/end offsets of all `<!-- -->` comments, so rows inside multi-line comments are styled as comment. */
    private val commentStarts: IntArray,
    private val commentEnds: IntArray,
) {
    val size: Int get() = starts.size

    /** If [offset] lies inside a comment, the offset where that comment ends; otherwise -1. */
    fun commentEndAt(offset: Int): Int {
        if (commentStarts.isEmpty()) return -1
        // Last comment starting before offset.
        var low = 0
        var high = commentStarts.size - 1
        if (commentStarts[0] >= offset) return -1
        while (low < high) {
            val mid = (low + high + 1) ushr 1
            if (commentStarts[mid] < offset) low = mid else high = mid - 1
        }
        return if (commentEnds[low] > offset) commentEnds[low] else -1
    }

    /** Row containing the character at [offset]. */
    fun rowOf(offset: Int): Int {
        var low = 0
        var high = starts.size - 1
        while (low < high) {
            val mid = (low + high + 1) ushr 1
            if (starts[mid] <= offset) low = mid else high = mid - 1
        }
        return low
    }

    companion object {
        fun of(xml: String): XmlRows {
            val estimate = (xml.length / 40).coerceIn(16, 1 shl 20)
            val starts = IntBuffer(estimate)
            val ends = IntBuffer(estimate)
            val numbers = IntBuffer(estimate)
            var line = 0
            var lineStart = 0

            while (true) {
                val newline = xml.indexOf('\n', lineStart)
                val lineEnd = if (newline < 0) xml.length else newline
                val contentEnd = if (lineEnd > lineStart && xml[lineEnd - 1] == '\r') lineEnd - 1 else lineEnd
                line++

                var rowStart = lineStart
                var first = true
                while (true) {
                    var rowEnd = minOf(rowStart + MAX_ROW_CHARS, contentEnd)
                    // Never split a surrogate pair.
                    if (rowEnd < contentEnd && rowEnd - 1 > rowStart && xml[rowEnd - 1].isHighSurrogate()) rowEnd--
                    starts.add(rowStart)
                    ends.add(rowEnd)
                    numbers.add(if (first) line else 0)
                    first = false
                    rowStart = rowEnd
                    if (rowStart >= contentEnd) break
                }

                if (newline < 0) break
                lineStart = newline + 1
            }

            val commentStarts = IntBuffer(16)
            val commentEnds = IntBuffer(16)
            var c = xml.indexOf("<!--")
            while (c >= 0) {
                val close = xml.indexOf("-->", c + 4)
                val end = if (close < 0) xml.length else close + 3
                commentStarts.add(c)
                commentEnds.add(end)
                c = if (close < 0) -1 else xml.indexOf("<!--", end)
            }

            return XmlRows(
                source = xml,
                starts = starts.toArray(),
                ends = ends.toArray(),
                lineNumbers = numbers.toArray(),
                lineCount = line,
                commentStarts = commentStarts.toArray(),
                commentEnds = commentEnds.toArray(),
            )
        }
    }
}

private class IntBuffer(capacity: Int) {
    private var data = IntArray(capacity)
    private var size = 0

    fun add(value: Int) {
        if (size == data.size) data = data.copyOf(size * 2)
        data[size++] = value
    }

    fun toArray(): IntArray = data.copyOf(size)
}

private fun findAll(text: String, query: String): IntArray {
    val hits = IntBuffer(64)
    var count = 0
    var index = text.indexOf(query, ignoreCase = true)
    while (index >= 0 && count < MAX_SEARCH_HITS) {
        hits.add(index)
        count++
        index = text.indexOf(query, index + 1, ignoreCase = true)
    }
    return hits.toArray()
}

@Immutable
private class XmlPalette(
    val tag: SpanStyle,
    val attribute: SpanStyle,
    val value: SpanStyle,
    val text: SpanStyle,
    val comment: SpanStyle,
    val punctuation: SpanStyle,
    val hit: SpanStyle,
    val currentHit: SpanStyle,
) {
    companion object {
        fun of(c: QubaColorScheme) = XmlPalette(
            tag = SpanStyle(color = c.accent),
            attribute = SpanStyle(color = c.warn),
            value = SpanStyle(color = c.success),
            text = SpanStyle(color = c.text, fontWeight = FontWeight.SemiBold),
            comment = SpanStyle(color = c.text3, fontStyle = FontStyle.Italic),
            punctuation = SpanStyle(color = c.text3),
            hit = SpanStyle(background = c.qubaYellow.copy(alpha = 0.28f)),
            currentHit = SpanStyle(background = c.qubaYellow.copy(alpha = 0.75f), color = Color.Black),
        )
    }
}

private fun buildRowText(
    source: String,
    start: Int,
    end: Int,
    commentEnd: Int,
    palette: XmlPalette,
    hits: IntArray,
    queryLength: Int,
    currentHit: Int,
): AnnotatedString {
    val raw = source.substring(start, end)

    // Expand tabs; display offsets shift by (TAB_WIDTH - 1) per preceding tab.
    val tabsBefore: IntArray? = if (raw.indexOf('\t') < 0) null else IntArray(raw.length + 1).also { counts ->
        for (k in raw.indices) counts[k + 1] = counts[k] + if (raw[k] == '\t') 1 else 0
    }
    fun display(rel: Int): Int = if (tabsBefore == null) rel else rel + tabsBefore[rel] * (TAB_WIDTH - 1)
    val text = if (tabsBefore == null) raw else raw.replace("\t", " ".repeat(TAB_WIDTH))

    return buildAnnotatedString {
        append(text)
        val commentEndInRow = if (commentEnd < 0) 0 else display((commentEnd - start).coerceAtMost(raw.length))
        if (commentEndInRow > 0) addStyle(palette.comment, 0, commentEndInRow)
        highlightXml(text, palette, from = commentEndInRow)

        if (queryLength > 0 && hits.isNotEmpty()) {
            // First hit that may still overlap this row.
            var i = lowerBound(hits, start - queryLength + 1)
            while (i < hits.size && hits[i] < end) {
                val from = display((hits[i] - start).coerceAtLeast(0))
                val to = display((hits[i] + queryLength - start).coerceAtMost(raw.length))
                if (from < to) addStyle(if (hits[i] == currentHit) palette.currentHit else palette.hit, from, to)
                i++
            }
        }
    }
}

private fun lowerBound(values: IntArray, target: Int): Int {
    var low = 0
    var high = values.size
    while (low < high) {
        val mid = (low + high) ushr 1
        if (values[mid] < target) low = mid + 1 else high = mid
    }
    return low
}

/** Small XML tokenizer for one row: tags, attributes, values, text content and comments. */
private fun AnnotatedString.Builder.highlightXml(text: String, p: XmlPalette, from: Int = 0) {
    val n = text.length
    var i = from
    while (i < n) {
        if (text[i] != '<') {
            val end = text.indexOf('<', i).let { if (it < 0) n else it }
            if ((i until end).any { !text[it].isWhitespace() }) addStyle(p.text, i, end)
            i = end
            continue
        }

        if (text.startsWith("<!--", i)) {
            val end = text.indexOf("-->", i + 4).let { if (it < 0) n else it + 3 }
            addStyle(p.comment, i, end)
            i = end
            continue
        }

        if (text.startsWith("<![CDATA[", i)) {
            val end = text.indexOf("]]>", i + 9).let { if (it < 0) n else it + 3 }
            addStyle(p.text, i, end)
            i = end
            continue
        }

        var j = i + 1
        if (j < n && (text[j] == '/' || text[j] == '?' || text[j] == '!')) j++
        addStyle(p.punctuation, i, j)

        val nameStart = j
        while (j < n && !text[j].isWhitespace() && text[j] != '>' && text[j] != '/' && text[j] != '?') j++
        if (j > nameStart) addStyle(p.tag, nameStart, j)

        while (j < n && text[j] != '>') {
            val ch = text[j]
            when {
                ch == '"' || ch == '\'' -> {
                    val close = text.indexOf(ch, j + 1)
                    val end = if (close < 0) n else close + 1
                    addStyle(p.value, j, end)
                    j = end
                }

                ch.isWhitespace() || ch == '=' -> j++

                ch == '/' || ch == '?' -> {
                    addStyle(p.punctuation, j, j + 1)
                    j++
                }

                else -> {
                    val attrStart = j
                    while (j < n) {
                        val c = text[j]
                        if (c.isWhitespace() || c == '=' || c == '>' || c == '/' || c == '"' || c == '\'') break
                        j++
                    }
                    if (j == attrStart) j++ else addStyle(p.attribute, attrStart, j)
                }
            }
        }

        if (j < n) {
            addStyle(p.punctuation, j, j + 1)
            j++
        }
        i = j
    }
}
