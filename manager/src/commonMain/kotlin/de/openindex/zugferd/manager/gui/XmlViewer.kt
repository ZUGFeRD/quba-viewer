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

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import de.openindex.zugferd.manager.LocalAppState
import de.openindex.zugferd.manager.sections.SearchState
import androidx.compose.ui.text.input.VisualTransformation
import de.openindex.zugferd.manager.utils.XmlVisualTransformation

// Ab dieser Größe wird Syntax-Highlighting deaktiviert (verhindert UI-Freeze bei großen Dateien)
private const val LARGE_XML_HIGHLIGHT_CHARS = 500_000

@Composable
fun XmlViewer(
    xml: String,
    modifier: Modifier = Modifier,
    search: SearchState? = null,
) {
    val preferences = LocalAppState.current.preferences
    val systemIsDark = isSystemInDarkTheme()
    val isLargeXml = xml.length > LARGE_XML_HIGHLIGHT_CHARS
    val xmlVisualTransformation = remember(preferences.isThemeDark, systemIsDark, isLargeXml) {
        if (isLargeXml) VisualTransformation.None
        else XmlVisualTransformation(darkMode = preferences.darkMode ?: systemIsDark)
    }
    val xmlTextStyle = MaterialTheme.typography.bodyMedium.copy(
        fontFamily = FontFamily.Monospace,
    )

    var internalTextFieldValue by remember(xml) { mutableStateOf(TextFieldValue(xml)) }

    LaunchedEffect(xml, search) {
        if (search != null && search.query.isNotBlank()) {
            val query = search.query
            val matches = mutableListOf<Pair<Int, Int>>()
            var index = xml.indexOf(query, ignoreCase = true)
            while (index >= 0) {
                matches.add(index to index + query.length)
                index = xml.indexOf(query, index + 1, ignoreCase = true)
            }

            if (matches.isNotEmpty()) {
                val matchIndex = (search.sequence % matches.size + matches.size) % matches.size
                val (start, end) = matches[matchIndex]
                internalTextFieldValue = internalTextFieldValue.copy(
                    selection = TextRange(start, end)
                )
            }
        }
    }

    TextField(
        value = internalTextFieldValue,
        onValueChange = { internalTextFieldValue = it },
        readOnly = true,
        singleLine = false,
        shape = RectangleShape,
        textStyle = xmlTextStyle,
        visualTransformation = xmlVisualTransformation,
        modifier = modifier,
    )
}