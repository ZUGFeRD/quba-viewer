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
import de.openindex.zugferd.manager.model.ValidationSeverity
import de.openindex.zugferd.manager.model.ValidationType
import de.openindex.zugferd.manager.utils.SectionState
import de.openindex.zugferd.manager.utils.Validation
import de.openindex.zugferd.manager.utils.directory
import de.openindex.zugferd.manager.utils.getHtmlVisualizationFromXML
import de.openindex.zugferd.manager.utils.getHtmlWithAttachments
import de.openindex.zugferd.manager.utils.getPrettyPrintedXml
import de.openindex.zugferd.manager.utils.getString
import de.openindex.zugferd.manager.utils.getValidationXmlReport
import de.openindex.zugferd.manager.utils.getXmlFromPdf
import de.openindex.zugferd.manager.utils.title
import de.openindex.zugferd.manager.utils.trimToNull
import de.openindex.zugferd.manager.utils.validatePdf
import de.openindex.zugferd.quba.generated.resources.AppCheckSelectFile
import de.openindex.zugferd.quba.generated.resources.Res
import io.github.vinceglb.filekit.core.FileKit
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Holds all state for a single document tab in the check section.
 * All fields are Compose-observable via mutableStateOf.
 */
class CheckTab(file: PlatformFile) {
    var file by mutableStateOf(file)
    var name by mutableStateOf(file.name)
    var isLoading by mutableStateOf(false)
    var xml by mutableStateOf<String?>(null)
    var html by mutableStateOf<String?>(null)
    var validation by mutableStateOf<Validation?>(null)
    var filterType by mutableStateOf(ValidationType.entries.toList())
    var filterSeverity by mutableStateOf(ValidationSeverity.entries.toList())
}

class CheckSectionState : SectionState() {

    /** All currently open document tabs. */
    val tabs = mutableStateListOf<CheckTab>()

    /** Index of the currently active tab. */
    var selectedIndex by mutableStateOf(0)

    /** The currently active tab, or null if no tabs are open. */
    val selectedTab: CheckTab?
        get() = tabs.getOrNull(selectedIndex)

    /**
     * The file of the active tab — kept as a computed property for
     * backward compatibility with the Visualisieren ↔ Prüfen file sync.
     */
    val selectedPdf: PlatformFile?
        get() = selectedTab?.file

    // Search state is section-level (shared across all tabs).
    var isSearchOpen by mutableStateOf(false)
    var searchQuery by mutableStateOf("")
    var searchSequence by mutableStateOf(0)

    /** Closes the tab at [index] and adjusts the selected index accordingly. */
    fun removeTab(index: Int) {
        if (index !in tabs.indices) return
        tabs.removeAt(index)
        selectedIndex = when {
            tabs.isEmpty() -> 0
            index <= selectedIndex -> (selectedIndex - 1).coerceAtLeast(0)
            else -> selectedIndex
        }
    }

    /** Closes all tabs except the one at [index]. */
    fun removeOtherTabs(index: Int) {
        val keep = tabs.getOrNull(index) ?: return
        tabs.retainAll { it === keep }
        selectedIndex = 0
    }

    /** Closes all tabs to the right of [index]. */
    fun removeTabsToRight(index: Int) {
        while (tabs.size > index + 1) {
            tabs.removeAt(tabs.lastIndex)
        }
        selectedIndex = selectedIndex.coerceAtMost(tabs.lastIndex.coerceAtLeast(0))
    }

    /** Moves the tab at [from] to position [to], adjusting the selected index. */
    fun moveTab(from: Int, to: Int) {
        if (from == to || from !in tabs.indices || to !in tabs.indices) return
        tabs.add(to, tabs.removeAt(from))
        selectedIndex = to
    }

    /**
     * Opens a file picker and loads the chosen file in a new tab.
     * If the file is already open in a tab, switches to that tab instead.
     */
    suspend fun selectFile(appState: AppState) {
        val file = FileKit.pickFile(
            type = PickerType.File(extensions = listOf("pdf", "xml")),
            mode = PickerMode.Single,
            title = getString(Res.string.AppCheckSelectFile).title(),
            initialDirectory = appState.preferences.previousPdfLocation,
        ) ?: return
        openFile(file, appState)
    }

    /**
     * Loads [file] directly — called by the Visualisieren ↔ Prüfen auto-sync.
     * If the file is already open in a tab, switches to that tab.
     */
    suspend fun selectFile(file: PlatformFile, appState: AppState) {
        val existingIndex = tabs.indexOfFirst { it.file.name == file.name }
        if (existingIndex >= 0) {
            selectedIndex = existingIndex
            return
        }
        openFile(file, appState)
    }

    private suspend fun openFile(file: PlatformFile, appState: AppState) {
        appState.lastSelectedFile = file
        file.directory?.let { appState.preferences.setPreviousPdfLocation(it) }

        val tab = CheckTab(file)
        tabs.add(tab)
        selectedIndex = tabs.lastIndex

        loadTab(tab)
    }

    private suspend fun loadTab(tab: CheckTab) {
        tab.isLoading = true
        tab.xml = null
        tab.html = null
        tab.validation = null

        val file = tab.file
        val isXml = file.name.endsWith(".xml", ignoreCase = true)

        // Phase 1: extract XML so the viewer can show it immediately.
        try {
            tab.xml = withContext(Dispatchers.IO) {
                if (isXml) {
                    val content = file.readBytes().decodeToString()
                    getPrettyPrintedXml(content)?.trimToNull() ?: content.trimToNull()
                } else {
                    getXmlFromPdf(file)?.let { getPrettyPrintedXml(it) }?.trimToNull()
                }
            }
        } finally {
            tab.isLoading = false
        }

        // Phase 2: HTML rendering and validation run concurrently.
        coroutineScope {
            launch(Dispatchers.IO) {
                tab.html = if (isXml) getHtmlVisualizationFromXML(file)
                           else getHtmlWithAttachments(file)
            }
            launch(Dispatchers.IO) {
                tab.validation = validatePdf(file)
                tab.filterType = ValidationType.entries.toList()
                tab.filterSeverity = ValidationSeverity.entries.toList()
            }
        }
    }

    /** Exports the XML validation report for [tab]'s file to a user-chosen location. */
    suspend fun exportValidation(tab: CheckTab) {
        val xmlReport = getValidationXmlReport(tab.file) ?: return
        FileKit.saveFile(
            bytes = xmlReport.toByteArray(Charsets.UTF_8),
            baseName = tab.file.name.substringBeforeLast(".").plus("_validation"),
            extension = "xml",
            initialDirectory = tab.file.path,
        )
    }
}
