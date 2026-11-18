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

package de.openindex.zugferd.manager.sections

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import de.openindex.zugferd.manager.AppState
import de.openindex.zugferd.manager.model.DocumentTab
import de.openindex.zugferd.manager.utils.InvoiceDocuments
import de.openindex.zugferd.manager.utils.SectionState
import de.openindex.zugferd.manager.utils.directory
import de.openindex.zugferd.manager.utils.trimToNull
import io.github.vinceglb.filekit.core.FileKit
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File

class VisualsSectionState : SectionState() {
    val documents = mutableStateListOf<DocumentTab>()
    var selectedIndex by mutableStateOf(0)

    // Search State
    var isSearchOpen by mutableStateOf(false)
    var searchQuery by mutableStateOf("")
    var searchSequence by mutableStateOf(0)

    // View mode — shared with title bar actions
    var viewMode by mutableStateOf(ViewMode.PDF_ONLY)
    var showViewModeToggle by mutableStateOf(false)

    /**
     * Loading runs here instead of in a composable scope, so switching to another section
     * doesn't cancel it halfway and leave a tab without its HTML view.
     */
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    fun addNewTab() {
        documents.add(DocumentTab(name = "Neuer Tab", pdf = null, isLoading = false))
        selectedIndex = documents.lastIndex
    }

    fun removeTab(index: Int) {
        if (index !in documents.indices) return
        documents.removeAt(index).loadJob?.cancel()
        selectedIndex = when {
            documents.isEmpty() -> 0
            index <= selectedIndex -> (selectedIndex - 1).coerceAtLeast(0)
            else -> selectedIndex
        }
    }

    /** Closes all tabs except the one at [index]. */
    fun removeOtherTabs(index: Int) {
        val keep = documents.getOrNull(index) ?: return
        documents.filter { it !== keep }.forEach { it.loadJob?.cancel() }
        documents.retainAll { it === keep }
        selectedIndex = 0
    }

    /** Closes all tabs to the right of [index]. */
    fun removeTabsToRight(index: Int) {
        while (documents.size > index + 1) {
            documents.removeAt(documents.lastIndex).loadJob?.cancel()
        }
        selectedIndex = selectedIndex.coerceAtMost(documents.lastIndex.coerceAtLeast(0))
    }

    fun moveTab(from: Int, to: Int) {
        if (from == to || from !in documents.indices || to !in documents.indices) return
        documents.add(to, documents.removeAt(from))
        selectedIndex = to
    }

    suspend fun selectFile(appState: AppState) {
        val file = FileKit.pickFile(
            type = PickerType.File(extensions = listOf("pdf", "xml")),
            mode = PickerMode.Single,
            title = "Datei auswählen",
            initialDirectory = appState.preferences.previousPdfLocation
        ) ?: return

        addTabWithFile(file, appState)
    }

    suspend fun loadFileInCurrentTab(file: PlatformFile, appState: AppState) {
        if (documents.isEmpty()) {
            addTabWithFile(file, appState)
        } else {
            val currentTab = documents[selectedIndex]
            loadFileInTab(currentTab, file, appState)
        }
    }

    /**
     * Opens [file] in a new tab, or switches to the tab that already shows it.
     * Returns right away; reading and rendering continue in the background.
     */
    suspend fun addTabWithFile(file: PlatformFile, appState: AppState) {
        val existingIndex = indexOfFile(file)
        if (existingIndex >= 0) {
            selectedIndex = existingIndex
            return
        }

        val newTab = DocumentTab(name = file.name, pdf = null, isLoading = true)
        documents.add(newTab)
        selectedIndex = documents.lastIndex

        loadFileInTab(newTab, file, appState)
    }

    /**
     * Recreates the tabs of the last session. Only the tab list is built here, so the window
     * opens immediately; the files are processed in the background, the selected tab first.
     */
    fun restoreSession(appState: AppState) {
        val files = appState.preferences.openTabPaths
            .map(::File)
            .filter { it.exists() }
            .map(::PlatformFile)
        if (files.isEmpty()) return

        val restored = files.map { file ->
            DocumentTab(name = file.name, pdf = null, isLoading = true).also {
                it.sourceFile = file
                documents.add(it)
            }
        }

        val idx = appState.preferences.openTabSelectedIndex
        if (idx in documents.indices) selectedIndex = idx

        val first = restored.indexOf(documents.getOrNull(selectedIndex)).coerceAtLeast(0)
        val order = listOf(first) + restored.indices.filter { it != first }
        for (i in order) startLoading(restored[i], files[i])

        appState.lastSelectedFile = files[first]
    }

    suspend fun loadFileInTab(tab: DocumentTab, file: PlatformFile, appState: AppState) {
        appState.lastSelectedFile = file
        file.directory?.let { appState.preferences.setPreviousPdfLocation(it) }
        startLoading(tab, file)
    }

    private fun startLoading(tab: DocumentTab, file: PlatformFile) {
        tab.loadJob?.cancel()
        tab.sourceFile = file
        tab.name = file.name
        // Show the PDF viewer right away; corrected below once the content has been checked.
        tab.pdf = if (file.name.endsWith(".pdf", ignoreCase = true)) file else null
        tab.xml = null
        tab.html = null
        tab.isLoading = true
        tab.isHtmlLoading = false

        tab.loadJob = scope.launch {
            // Phase 1: XML (one read of the file), shown as soon as it's there.
            try {
                val source = InvoiceDocuments.source(file)
                tab.pdf = if (source.isPdf) file else null
                tab.xml = source.prettyXml
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                println("Fehler beim Lesen der Datei: ${e.message}")
            } finally {
                tab.isLoading = false
            }

            // Phase 2: HTML in the background; the HTML tab appears when it's ready.
            try {
                tab.isHtmlLoading = true
                tab.html = InvoiceDocuments.html(file)?.trimToNull()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                println("Fehler bei der HTML-Visualisierung: ${e.message}")
            } finally {
                tab.isHtmlLoading = false
            }
        }
    }

    private fun indexOfFile(file: PlatformFile): Int {
        val path = file.file.absolutePath
        return documents.indexOfFirst { it.sourceFile?.file?.absolutePath == path }
    }
}
