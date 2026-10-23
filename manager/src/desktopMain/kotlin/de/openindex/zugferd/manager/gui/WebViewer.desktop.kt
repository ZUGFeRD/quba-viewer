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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import de.openindex.zugferd.manager.LocalAppState
import de.openindex.zugferd.manager.sections.SearchState
import de.openindex.zugferd.manager.utils.getCefBrowser
import de.openindex.zugferd.manager.utils.installWebView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cef.browser.CefBrowser
import java.awt.BorderLayout
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.io.File
import javax.swing.JPanel

@Composable
@Suppress("DuplicatedCode")
actual fun WebViewer(html: String, modifier: Modifier, search: SearchState?) {
    val scope = rememberCoroutineScope()
    var isInstalled by remember { mutableStateOf(false) }
    val preferences = LocalAppState.current.preferences
    val chromeGpuEnabled = preferences.chromeGpuEnabled
    val isDark = preferences.darkMode ?: isSystemInDarkTheme()

    LaunchedEffect(Unit) {
        scope.launch {
            installWebView(gpuEnabled = chromeGpuEnabled)
        }.invokeOnCompletion {
            isInstalled = true
        }
    }

    var browserState by remember { mutableStateOf<CefBrowser?>(null) }

    LaunchedEffect(browserState, search) {
        val browser = browserState ?: return@LaunchedEffect
        if (search != null && search.query.isNotBlank()) {
            browser.find(search.query, true, false, search.sequence > 0)
        } else {
            browser.stopFinding(true)
        }
    }

    // HTML in Temp-Datei schreiben statt Base64 Data-URL (vermeidet CEF-URL-Längengrenze bei großen Dateien)
    var fileUrl by remember { mutableStateOf("") }
    var currentTempFile by remember { mutableStateOf<File?>(null) }

    LaunchedEffect(html, isDark) {
        val finalHtml = if (isDark) html.withQubaDarkStyle() else html
        val f = withContext(Dispatchers.IO) {
            File.createTempFile("quba_html_", ".html").also { file ->
                file.writeText(finalHtml, Charsets.UTF_8)
            }
        }
        currentTempFile?.delete()
        currentTempFile = f
        fileUrl = "file:///${f.absolutePath.replace('\\', '/')}"
    }

    DisposableEffect(Unit) {
        onDispose {
            browserState?.close(true)
            currentTempFile?.delete()
        }
    }

    if (isInstalled && fileUrl.isNotBlank()) {
        SwingPanel(
            background = MaterialTheme.colorScheme.surface,
            factory = {
                val browser = getCefBrowser(fileUrl)
                browserState = browser
                WebPanel(browser)
            },
            update = { panel: WebPanel ->
                panel.browser.stopLoad()
                panel.browser.loadURL(fileUrl)
            },
            modifier = modifier,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Dark-mode style injection for ZUGFeRD HTML previews.
// Injects CSS (handles bgcolor attrs + structural elements) and a small JS
// snippet that normalises inline style= colours after DOMContentLoaded.
// Only called when isDark == true; light mode HTML is left completely unchanged.
// ─────────────────────────────────────────────────────────────────────────────

private fun String.withQubaDarkStyle(): String {
    // language=CSS
    val css = """
        html,body{background:#141418!important;color:#d0d0d8!important}
        *{border-color:#2a2a32!important}
        a{color:#818cf8!important}
        a:visited{color:#a78bfa!important}
        /* Blue table headers */
        th{background:#0f3d6e!important;color:#e0e0f0!important}
        [bgcolor="#003366"],[bgcolor="#003399"],[bgcolor="#336699"],
        [bgcolor="#0066cc"],[bgcolor="#004080"],[bgcolor="#1a3a6e"]{
            background:#0f3d6e!important;color:#e0e0f0!important}
        /* Yellow disclaimer / warning rows */
        [bgcolor="#fff3cd"],[bgcolor="#FFFDE7"],[bgcolor="#ffffe0"],
        [bgcolor="#ffffcc"],[bgcolor="#ffd700"]{
            background:#2a2510!important;color:#c8a84b!important}
        /* Light grey / white rows */
        [bgcolor="#ffffff"],[bgcolor="#FFFFFF"],[bgcolor="#f9f9f9"],
        [bgcolor="#F9F9F9"],[bgcolor="#f5f5f5"],[bgcolor="#F5F5F5"],
        [bgcolor="#eeeeee"],[bgcolor="#EEEEEE"],[bgcolor="#e0e0e0"]{
            background:#1a1a20!important}
    """.trimIndent().replace(Regex("\\s*\n\\s*"), " ")

    // language=JavaScript
    val js = """
        document.addEventListener('DOMContentLoaded',function(){
            function luma(r,g,b){return 0.299*r+0.587*g+0.114*b}
            function parseRgb(s){var m=s.match(/\d+/g);return m&&m.length>=3?[+m[0],+m[1],+m[2]]:null}
            document.querySelectorAll('[style]').forEach(function(el){
                var s=el.style;
                var bg=parseRgb(s.backgroundColor||'');
                if(bg){
                    var l=luma(bg[0],bg[1],bg[2]);
                    if(l>210) s.backgroundColor='#1a1a20';
                    else if(bg[0]<80&&bg[1]<80&&bg[2]>100) s.backgroundColor='#0f3d6e';
                    else if(bg[0]>150&&bg[1]>120&&bg[2]<80) s.backgroundColor='#2a2510';
                }
                var fg=parseRgb(s.color||'');
                if(fg&&luma(fg[0],fg[1],fg[2])<60) s.color='#d0d0d8';
            });
        });
    """.trimIndent().replace(Regex("\\s*\n\\s*"), " ")

    val inject = "<style>$css</style><script>$js</script>"
    return when {
        contains("</head>", ignoreCase = true) ->
            replaceFirst(Regex("</head>", RegexOption.IGNORE_CASE), "$inject</head>")
        contains(Regex("<body[^>]*>", RegexOption.IGNORE_CASE)) ->
            replaceFirst(Regex("(<body[^>]*>)", RegexOption.IGNORE_CASE), "$1$inject")
        else -> inject + this
    }
}

private class WebPanel(val browser: CefBrowser) : JPanel(BorderLayout()) {
    init {
        add(browser.uiComponent, BorderLayout.CENTER)
        addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent?) {
                val w = width
                val h = height
                if (w > 0 && h > 0) {
                    browser.uiComponent.setBounds(0, 0, w, h)
                    validate()
                }
            }
        })
    }
}
