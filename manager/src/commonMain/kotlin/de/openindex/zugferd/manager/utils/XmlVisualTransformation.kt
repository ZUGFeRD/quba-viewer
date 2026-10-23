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

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import de.openindex.zugferd.manager.theme.secondaryContainerDark

/**
 * inspired by https://xa1.at/compose-textfield-format/
 */
class XmlVisualTransformation(
    val darkMode: Boolean = false,
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        return TransformedText(
            text = buildAnnotatedString {
                append(text)
                //addStyle(FONT_STYLE, 0, text.length - 1)

                XML_TEXT_REGEX.findAll(text)
                    .mapNotNull { match ->
                        match.groups[1]
                            ?.takeIf { it.value.isNotBlank() }
                    }
                    .forEach { group ->
                        addStyle(
                            style = XML_TEXT_STYLE_DARK.takeIf { darkMode } ?: XML_TEXT_STYLE,
                            start = group.range.first,
                            end = group.range.last + 1,
                        )
                    }

                XML_ATTRIB_REGEX.findAll(text)
                    .mapNotNull { match ->
                        match.groups[1]
                            ?.takeIf { it.value.isNotBlank() }
                    }
                    .filter { group ->
                        !group.value.contains("xmlns:")
                    }
                    .forEach { group ->
                        addStyle(
                            style = XML_ATTRIB_STYLE_DARK.takeIf { darkMode } ?: XML_ATTRIB_STYLE,
                            start = group.range.first,
                            end = group.range.last + 1,
                        )
                    }

                XML_COMMENT_REGEX.findAll(text)
                    .forEach { match ->
                        addStyle(
                            style = XML_COMMENT_STYLE,
                            start = match.range.first,
                            end = match.range.last + 1,
                        )
                    }
            },
            offsetMapping = OffsetMapping.Identity,
        )
    }

    companion object {
        //private val FONT_STYLE: SpanStyle = SpanStyle(fontFamily = FontFamily.Monospace)
        private val FONT_BOLD: SpanStyle = SpanStyle(fontWeight = FontWeight.Bold)

        private val XML_TEXT_REGEX: Regex = Regex(">([^>]+)<")
        private val XML_TEXT_STYLE: SpanStyle = FONT_BOLD.copy(color = secondaryContainerDark)
        private val XML_TEXT_STYLE_DARK: SpanStyle = FONT_BOLD.copy(color = Color.Green)

        //private val XML_ATTRIB_REGEX: Regex = Regex("<[^>\\s]+ ([^>]+)>")
        private val XML_ATTRIB_REGEX: Regex = Regex("<[^>?!\\s]+ ([^>]+)>")
        private val XML_ATTRIB_STYLE: SpanStyle = FONT_BOLD.copy(color = secondaryContainerDark)
        private val XML_ATTRIB_STYLE_DARK: SpanStyle = FONT_BOLD.copy(color = Color.Green)

        private val XML_COMMENT_REGEX: Regex = Regex("<!--[^>]+-->")
        private val XML_COMMENT_STYLE: SpanStyle = SpanStyle(color = Color.Gray)
    }
}

/*
class SimpleFormattingVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        return TransformedText(
            text = buildAnnotatedString {
                append(text)

                styleRegex(text, EMPHASIS_REGEX, EMPHASIS_STYLE)
                styleRegex(text, CODE_REGEX, CODE_STYLE)
            },
            offsetMapping = OffsetMapping.Identity
        )
    }

    private fun AnnotatedString.Builder.styleRegex(text: AnnotatedString, regex: Regex, spanStyle: SpanStyle) {
        regex.findAll(text).forEach { match ->
            addStyle(spanStyle, match.range.first, match.range.endExclusive)
        }
    }

    companion object {
        private val EMPHASIS_REGEX: Regex = Regex("\\*.*\\*")
        private val EMPHASIS_STYLE : SpanStyle = SpanStyle(fontWeight = FontWeight.Bold)

        private val CODE_REGEX: Regex = Regex("`.*`")
        private val CODE_STYLE : SpanStyle = SpanStyle(fontFamily = FontFamily.Monospace, background = Color.Gray)
    }
}
*/