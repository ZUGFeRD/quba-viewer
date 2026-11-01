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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Card
import androidx.compose.ui.graphics.Color
import de.openindex.zugferd.manager.gui.QubaButton
import de.openindex.zugferd.manager.gui.QubaButtonVariant
import de.openindex.zugferd.manager.theme.LocalQubaColors
import de.openindex.zugferd.manager.theme.LocalQubaTypography
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size as GeometrySize
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.isPressed
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.times
import androidx.compose.ui.zIndex
import de.openindex.zugferd.manager.AppSection
import de.openindex.zugferd.manager.LocalAppState
import de.openindex.zugferd.manager.gui.ActionButtonWithTooltip
import de.openindex.zugferd.manager.gui.Label
import de.openindex.zugferd.manager.gui.PdfViewer
import de.openindex.zugferd.manager.gui.SectionSubTitle
import de.openindex.zugferd.manager.gui.SectionTitle
import de.openindex.zugferd.manager.gui.Tooltip
import de.openindex.zugferd.manager.gui.VerticalScrollBox
import de.openindex.zugferd.manager.gui.WebViewer
import de.openindex.zugferd.manager.gui.XmlViewer
import de.openindex.zugferd.manager.model.ValidationSeverity
import de.openindex.zugferd.manager.model.ValidationType
import de.openindex.zugferd.manager.utils.Validation
import de.openindex.zugferd.manager.utils.ValidationMessage
import de.openindex.zugferd.manager.utils.createDragAndDropTarget
import de.openindex.zugferd.manager.utils.showTabContextMenu
import de.openindex.zugferd.manager.sections.VisualsSectionState
import de.openindex.zugferd.manager.utils.pluralStringResource
import de.openindex.zugferd.manager.utils.stringResource
import de.openindex.zugferd.manager.utils.title
import de.openindex.zugferd.quba.generated.resources.AppCheck
import de.openindex.zugferd.quba.generated.resources.AppCheckDetailsHtml
import de.openindex.zugferd.quba.generated.resources.AppCheckDetailsPdf
import de.openindex.zugferd.quba.generated.resources.AppCheckDetailsXml
import de.openindex.zugferd.quba.generated.resources.AppCheckFailed
import de.openindex.zugferd.quba.generated.resources.AppCheckMessageMessageCopy
import de.openindex.zugferd.quba.generated.resources.AppCheckMessages
import de.openindex.zugferd.quba.generated.resources.AppCheckMessagesFilter
import de.openindex.zugferd.quba.generated.resources.AppCheckMessagesFilterSeverity
import de.openindex.zugferd.quba.generated.resources.AppCheckMessagesFilterType
import de.openindex.zugferd.quba.generated.resources.AppCheckPassed
import de.openindex.zugferd.quba.generated.resources.AppCheckEmptyHint
import de.openindex.zugferd.quba.generated.resources.AppCheckEmptyTitle
import de.openindex.zugferd.quba.generated.resources.AppCheckSelect
import de.openindex.zugferd.quba.generated.resources.AppCheckSelectInfo
import de.openindex.zugferd.quba.generated.resources.AppCheckSelectMessage
import de.openindex.zugferd.quba.generated.resources.AppCheckSummary
import de.openindex.zugferd.quba.generated.resources.AppCheckSummaryErrors
import de.openindex.zugferd.quba.generated.resources.AppCheckSummaryExport
import de.openindex.zugferd.quba.generated.resources.AppCheckSummaryMessages
import de.openindex.zugferd.quba.generated.resources.AppCheckSummaryNotices
import de.openindex.zugferd.quba.generated.resources.AppCheckSummaryProfile
import de.openindex.zugferd.quba.generated.resources.AppCheckSummarySignature
import de.openindex.zugferd.quba.generated.resources.AppCheckSummaryUnknown
import de.openindex.zugferd.quba.generated.resources.AppCheckSummaryVersion
import de.openindex.zugferd.quba.generated.resources.AppCheckSummaryWarnings
import de.openindex.zugferd.quba.generated.resources.Res
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PlatformFile
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType

/**
 * Main view of the check section.
 */
@Composable
@OptIn(ExperimentalFoundationApi::class)
fun CheckSection(state: CheckSectionState) {
    val scope = rememberCoroutineScope()
    val appState = LocalAppState.current

    // Clear search state when search is closed.
    LaunchedEffect(state.isSearchOpen) {
        if (!state.isSearchOpen) {
            state.searchQuery = ""
            state.searchSequence = 0
        }
    }

    val dragAndDropCallback = remember {
        createDragAndDropTarget(
            onDrop = { file ->
                scope.launch {
                    state.selectFile(file = file, appState = appState)
                }
            }
        )
    }

    // Multi-file picker for empty-area click — same as the + button.
    val emptyAreaLauncher = rememberFilePickerLauncher(
        type = PickerType.File(extensions = listOf("pdf", "xml")),
        mode = PickerMode.Multiple(),
        title = "Dateien auswählen",
        onResult = { files: List<PlatformFile>? ->
            files?.forEach { file ->
                scope.launch {
                    state.selectFile(file = file, appState = appState)
                }
            }
        },
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Tab strip — visible as soon as at least one document is open.
            if (state.tabs.isNotEmpty()) {
                CheckTabStrip(state)
            }

            val selectedTab = state.selectedTab

            if (selectedTab == null) {
                // No document open yet — show the empty/welcome view.
                EmptyView(state)
            } else {
                // Two-column layout: validation results on the left, viewer on the right.
                Row(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.weight(0.6f, fill = true)) {
                        VerticalScrollBox(modifier = Modifier.weight(1f, fill = true)) {
                            CheckView(state, selectedTab)
                        }
                    }
                    Column(modifier = Modifier.weight(0.4f, fill = true)) {
                        DetailsView(state, selectedTab)
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (state.selectedTab == null)
                        Modifier.clickable { emptyAreaLauncher.launch() }
                    else Modifier
                )
                .dragAndDropTarget(
                    target = dragAndDropCallback,
                    shouldStartDragAndDrop = { true },
                ),
        )

    }
}

/**
 * Action buttons of the check section, shown on the top right.
 */
@Composable
fun CheckSectionActions(state: CheckSectionState) {
    // Datei-Auswahl-Button ausgeblendet — Drag & Drop und Klick auf leere Fläche genügen.
    // Suche ist in den Tab-Strip gewandert (zwischen + und Spacer).
    // ActionButtonWithTooltip(
    //     label = Res.string.AppCheckSelect,
    //     tooltip = Res.string.AppCheckSelectInfo,
    //     onClick = {
    //         scope.launch(Dispatchers.IO) {
    //             state.selectFile(appState = appState)
    //         }
    //     },
    // )
}

/**
 * Empty view of the check section.
 * The whole area is already clickable + drag-and-drop — no verbose instructions needed.
 */
@Composable
@Suppress("UNUSED_PARAMETER")
private fun EmptyView(state: CheckSectionState) {
    val colors = LocalQubaColors.current
    val typo = LocalQubaTypography.current

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Drop-zone indicator: dashed rounded box with icon
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(104.dp)
                    .drawBehind {
                        drawRoundRect(
                            color = colors.border,
                            style = Stroke(
                                width = 1.5.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f)),
                            ),
                            cornerRadius = CornerRadius(20.dp.toPx()),
                        )
                    },
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(48.dp),
                )
            }

            // Minimal labels — file types + action hint
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = stringResource(Res.string.AppCheckEmptyTitle),
                    style = typo.bodyMed.copy(color = colors.text2),
                )
                Text(
                    text = stringResource(Res.string.AppCheckEmptyHint),
                    style = typo.small.copy(color = colors.text4),
                )
            }
        }
    }
}

/**
 * Scrollable row of open document tabs with a button to open additional files.
 */
@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun CheckTabStrip(state: CheckSectionState) {
    val scope = rememberCoroutineScope()
    val appState = LocalAppState.current
    val density = LocalDensity.current

    val fileLauncher = rememberFilePickerLauncher(
        type = PickerType.File(extensions = listOf("pdf", "xml")),
        mode = PickerMode.Multiple(),
        title = "Dateien auswählen",
        onResult = { files: List<PlatformFile>? ->
            files?.forEach { file ->
                scope.launch(Dispatchers.IO) {
                    state.selectFile(file = file, appState = appState)
                }
            }
        },
    )

    // Layout constants.
    val maxTabWidth = 200.dp
    val minTabWidth = 80.dp
    val tabSpacing = 4.dp
    val plusButtonSize = 32.dp
    val stripPadding = 8.dp
    val searchIconWidth = 48.dp

    // Drag state — lifted here so all tabs can react in real-time.
    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffsetX by remember { mutableStateOf(0f) }
    // Mutable array so drag lambdas always read the current step without restarting.
    val tabStepPxRef = remember { floatArrayOf(0f) }

    val searchBarWidth = 288.dp // 280 content + 8 padding

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val tabCount = state.tabs.size
        val searchAreaWidth = if (state.isSearchOpen) searchBarWidth else searchIconWidth
        val availableForTabs = maxWidth - plusButtonSize - stripPadding * 2 - tabSpacing - searchAreaWidth
        val rawTabWidth = if (tabCount == 0) maxTabWidth
                          else (availableForTabs - tabSpacing * (tabCount - 1)) / tabCount
        val targetTabWidth = rawTabWidth.coerceIn(minTabWidth, maxTabWidth)

        val tabWidth by animateDpAsState(targetValue = targetTabWidth, label = "tabWidth")

        // Keep ref in sync each frame so drag lambdas always read the correct step.
        tabStepPxRef[0] = with(density) { (tabWidth + tabSpacing).toPx() }

        // Visual insertion index derived from current drag position.
        val insertIndex: Int? = draggedIndex?.let { from ->
            val step = tabStepPxRef[0]
            if (step > 0f) (from + dragOffsetX / step).roundToInt()
                .coerceIn(0, state.tabs.lastIndex)
            else from
        }

        val needsScroll = tabCount > 0 && rawTabWidth < minTabWidth
        val scrollState = rememberScrollState()

        LaunchedEffect(state.selectedIndex) {
            if (needsScroll && state.tabs.isNotEmpty()) {
                with(density) {
                    val itemWidth = (tabWidth + tabSpacing).toPx()
                    scrollState.animateScrollTo((itemWidth * state.selectedIndex).toInt())
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(LocalQubaColors.current.surface2)
                .padding(vertical = 4.dp),
        ) {
            // Tabs row — no weight so the + button sits directly after the last tab.
            Row(
                horizontalArrangement = Arrangement.spacedBy(tabSpacing),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(start = stripPadding)
                    .then(if (needsScroll) Modifier.horizontalScroll(scrollState) else Modifier),
            ) {
                state.tabs.forEachIndexed { index, tab ->
                    val from = draggedIndex
                    val to = insertIndex
                    val step = tabStepPxRef[0]

                    // How far should this tab shift visually to make room for the dragged tab?
                    val shiftTarget: Float = when {
                        from == null || to == null -> 0f
                        index == from -> 0f                             // dragged tab: offset applied directly
                        from < to && index in (from + 1)..to -> -step  // dragging right: shift left
                        from > to && index in to until from -> step    // dragging left: shift right
                        else -> 0f
                    }

                    CheckTabItem(
                        tab = tab,
                        width = tabWidth,
                        isSelected = index == state.selectedIndex,
                        isDragged = index == draggedIndex,
                        dragOffsetPx = if (index == draggedIndex) dragOffsetX else 0f,
                        shiftTarget = shiftTarget,
                        onSelect = { state.selectedIndex = index },
                        onClose = { state.removeTab(index) },
                        onRightClick = { screenX, screenY ->
                            val visualsState = AppSection.VISUALISATION.state as? VisualsSectionState
                            showTabContextMenu(
                                screenX = screenX,
                                screenY = screenY,
                                onClose = { state.removeTab(index) },
                                onCloseOthers = { state.removeOtherTabs(index) },
                                onCloseToRight = if (index < state.tabs.lastIndex) ({ state.removeTabsToRight(index) }) else null,
                                openInOtherLabel = "In Visualisierung öffnen",
                                onOpenInOther = if (visualsState != null) ({
                                    // Use an independent scope — the local composable scope is
                                    // cancelled when this section leaves the composition on switch.
                                    CoroutineScope(Dispatchers.IO).launch {
                                        visualsState.addTabWithFile(tab.file, appState)
                                    }
                                    appState.setSection(AppSection.VISUALISATION)
                                }) else null,
                            )
                        },
                        onDragStart = {
                            draggedIndex = index
                            dragOffsetX = 0f
                        },
                        onDragDelta = { delta ->
                            dragOffsetX += delta
                        },
                        onDragEnd = {
                            val fromIdx = draggedIndex
                            if (fromIdx != null) {
                                val s = tabStepPxRef[0]
                                val toIdx = if (s > 0f)
                                    (fromIdx + dragOffsetX / s).roundToInt()
                                        .coerceIn(0, state.tabs.lastIndex)
                                else fromIdx
                                draggedIndex = null
                                dragOffsetX = 0f
                                if (fromIdx != toIdx) state.moveTab(fromIdx, toIdx)
                            } else {
                                draggedIndex = null
                                dragOffsetX = 0f
                            }
                        },
                    )
                }
            }

            // + button sits directly after the last tab (Chrome style).
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(start = tabSpacing)
                    .size(plusButtonSize)
                    .clip(RoundedCornerShape(6.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
                    .clickable { fileLauncher.launch() },
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Weitere Datei öffnen",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }

            // Fills remaining space so the tab strip doesn't stretch.
            Spacer(modifier = Modifier.weight(1f))

            // Search: inline bar when open, icon when closed. Lives in the header above JCEF.
            if (state.isSearchOpen) {
                CompactSearchBar(
                    value = state.searchQuery,
                    onValueChange = { state.searchQuery = it; state.searchSequence = 0 },
                    onSubmit = { if (state.searchQuery.isNotBlank()) state.searchSequence++ },
                    onClose = { state.isSearchOpen = false },
                    modifier = Modifier.width(280.dp).padding(end = 4.dp),
                )
            } else {
                Tooltip(text = "Suchen (Strg+F) — Enter: nächster Treffer · Esc: schließen") {
                    IconButton(
                        onClick = { state.isSearchOpen = true },
                        modifier = Modifier.padding(end = 4.dp),
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Suchen")
                    }
                }
            }
        }
    }
}

/**
 * A single tab item in the check tab strip.
 */
@Composable
private fun CheckTabItem(
    tab: CheckTab,
    width: Dp,
    isSelected: Boolean,
    isDragged: Boolean,
    dragOffsetPx: Float,
    shiftTarget: Float,
    onSelect: () -> Unit,
    onClose: () -> Unit,
    onRightClick: (screenX: Int, screenY: Int) -> Unit,
    onDragStart: () -> Unit,
    onDragDelta: (Float) -> Unit,
    onDragEnd: () -> Unit,
) {
    // Sibling tabs animate smoothly to their shifted positions.
    // The dragged tab uses the raw dragOffsetPx for instant cursor-following.
    val animatedShift by animateFloatAsState(
        targetValue = if (isDragged) 0f else shiftTarget,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "tabShift",
    )
    val visualOffsetPx = if (isDragged) dragOffsetPx else animatedShift

    // Keep callbacks fresh without restarting the pointerInput coroutine.
    val updatedOnDragStart = rememberUpdatedState(onDragStart)
    val updatedOnDragDelta = rememberUpdatedState(onDragDelta)
    val updatedOnDragEnd = rememberUpdatedState(onDragEnd)

    val colors = LocalQubaColors.current
    val typo = LocalQubaTypography.current
    val tabShape = RoundedCornerShape(6.dp)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .width(width)
            .zIndex(if (isDragged) 2f else if (isSelected) 1f else 0f)
            .offset { IntOffset(visualOffsetPx.roundToInt(), 0) }
            .scale(if (isDragged) 1.05f else 1f)
            .shadow(
                elevation = if (isSelected || isDragged) 2.dp else 0.dp,
                shape = tabShape,
            )
            .clip(tabShape)
            .background(if (isSelected || isDragged) colors.surface else Color.Transparent)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { updatedOnDragStart.value() },
                    onDragEnd = { updatedOnDragEnd.value() },
                    onDragCancel = { updatedOnDragEnd.value() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        updatedOnDragDelta.value(dragAmount.x)
                    },
                )
            }
            .clickable(onClick = onSelect)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.type == PointerEventType.Press && event.buttons.isPressed(1)) {
                            val mouseEvent = event.nativeEvent as? java.awt.event.MouseEvent
                            onRightClick(mouseEvent?.xOnScreen ?: 0, mouseEvent?.yOnScreen ?: 0)
                        }
                    }
                }
            }
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        // Status icon — spinner while loading, check/x based on validation result otherwise.
        if (tab.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(12.dp),
                strokeWidth = 1.5.dp,
                color = colors.text3,
            )
        } else {
            val validation = tab.validation
            when {
                validation == null -> Icon(
                    imageVector = Icons.Default.CheckBoxOutlineBlank,
                    contentDescription = null,
                    modifier = Modifier.size(13.dp),
                    tint = if (isSelected) colors.accent else colors.text3,
                )
                validation.isValid -> Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(13.dp),
                    tint = if (isSelected) colors.accent else colors.text3,
                )
                else -> Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(13.dp),
                    tint = colors.danger,
                )
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = tab.name,
            color = if (isSelected) colors.text else colors.text2,
            style = typo.small,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )

        Spacer(modifier = Modifier.width(4.dp))

        if (!tab.isLoading) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Tab schließen",
                modifier = Modifier
                    .size(14.dp)
                    .clickable(onClick = onClose),
                tint = if (isSelected) colors.text2 else colors.text3,
            )
        }
    }
}

/**
 * Left side view of the check section.
 * This provides the validation view.
 */
@Composable
private fun CheckView(state: CheckSectionState, tab: CheckTab) {
    val scope = rememberCoroutineScope()
    val validation = tab.validation

    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 20.dp)
    ) {
        // Section title with validation icon.
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            AnimatedVisibility(visible = validation != null) {
                Icon(
                    imageVector = Icons.Default.ThumbUp
                        .takeIf { validation?.isValid == true }
                        ?: Icons.Default.ThumbDown,
                    contentDescription = stringResource(
                        Res.string.AppCheckPassed
                            .takeIf { validation?.isValid == true }
                            ?: Res.string.AppCheckFailed,
                        tab.name,
                    ),
                    modifier = Modifier
                        .size(32.dp)
                )
            }

            SectionTitle(
                text = when (validation?.isValid) {
                    true -> stringResource(Res.string.AppCheckPassed, tab.name)
                    false -> stringResource(Res.string.AppCheckFailed, tab.name)
                    else -> stringResource(Res.string.AppCheck, tab.name)
                },
                modifier = Modifier
                    .weight(1f, fill = true),
            )
        }

        // Validation result.
        if (validation == null) {
            CircularProgressIndicator()
        } else {
            // Subsection with validation summary.
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                SectionSubTitle(
                    text = Res.string.AppCheckSummary,
                ) {
                    QubaButton(
                        label = stringResource(Res.string.AppCheckSummaryExport).title(),
                        onClick = {
                            scope.launch {
                                state.exportValidation(tab)
                            }
                        },
                    )
                }

                ValidationSummary(validation)
            }

            // Subsection with validation messages.
            if (validation.messages.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    SectionSubTitle(
                        text = Res.string.AppCheckMessages,
                    ) {
                        Box {
                            var expanded by remember { mutableStateOf(false) }

                            QubaButton(
                                label = stringResource(Res.string.AppCheckMessagesFilter).title(),
                                onClick = { expanded = true },
                            )

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                            ) {
                                Text(
                                    text = stringResource(Res.string.AppCheckMessagesFilterType),
                                    style = MaterialTheme.typography.labelMedium,
                                    softWrap = false,
                                    modifier = Modifier
                                        .padding(
                                            horizontal = 12.dp,
                                            vertical = 12.dp,
                                        )
                                )

                                ValidationType.entries
                                    .filter { type ->
                                        validation.messages.firstOrNull {
                                            it.type == type
                                        } != null
                                    }
                                    .forEach { type ->
                                        val isSelected = tab.filterType.contains(type)

                                        DropdownMenuItem(
                                            text = {
                                                Label(
                                                    text = stringResource(type.title),
                                                )
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = if (isSelected)
                                                        Icons.Default.CheckBox
                                                    else
                                                        Icons.Default.CheckBoxOutlineBlank,
                                                    contentDescription = stringResource(type.title),
                                                )
                                            },
                                            onClick = {
                                                tab.filterType =
                                                    if (tab.filterType.contains(type))
                                                        tab.filterType.minus(type)
                                                    else
                                                        tab.filterType.plus(type)
                                            }
                                        )
                                    }

                                HorizontalDivider()

                                Text(
                                    text = stringResource(Res.string.AppCheckMessagesFilterSeverity),
                                    style = MaterialTheme.typography.labelMedium,
                                    softWrap = false,
                                    modifier = Modifier
                                        .padding(
                                            horizontal = 12.dp,
                                            vertical = 12.dp,
                                        )
                                )

                                ValidationSeverity.entries
                                    .filter { severity ->
                                        validation.messages.firstOrNull {
                                            it.severity == severity
                                        } != null
                                    }
                                    .forEach { severity ->
                                        val isSelected = tab.filterSeverity.contains(severity)

                                        DropdownMenuItem(
                                            text = {
                                                Label(
                                                    text = stringResource(severity.title),
                                                )
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = if (isSelected)
                                                        Icons.Default.CheckBox
                                                    else
                                                        Icons.Default.CheckBoxOutlineBlank,
                                                    contentDescription = stringResource(severity.title),
                                                )
                                            },
                                            onClick = {
                                                tab.filterSeverity =
                                                    if (tab.filterSeverity.contains(severity))
                                                        tab.filterSeverity.minus(severity)
                                                    else
                                                        tab.filterSeverity.plus(severity)
                                            }
                                        )
                                    }
                            }
                        }
                    }

                    ValidationMessages(tab)
                }
            }
        }
    }

    /*TextField(
        value = JSON_EXPORT.encodeToString(validation),
        readOnly = true,
        singleLine = false,
        onValueChange = {},
        modifier = Modifier.fillMaxSize(),
    )*/
}

/**
 * Right side view of the check section.
 * This provides the PDF- / HTML- / XML-viewer.
 */
@Composable
private fun DetailsView(state: CheckSectionState, tab: CheckTab) {
    var tabState by remember(tab) { mutableStateOf(0) }
    val isPdfTabSelected by derivedStateOf { tabState == 0 }
    val isHtmlTabSelected by derivedStateOf { tabState == 1 && tab.html != null }
    val isXmlTabSelected by derivedStateOf { tabState == 2 && tab.xml != null }

    val activeSearch = remember(state.isSearchOpen, state.searchQuery, state.searchSequence) {
        if (state.isSearchOpen && state.searchQuery.isNotBlank()) {
            SearchState(query = state.searchQuery, sequence = state.searchSequence)
        } else {
            null
        }
    }

    TabRow(
        selectedTabIndex = tabState,
    ) {
        // Add tab for PDF viewer.
        Tab(
            selected = isPdfTabSelected,
            onClick = { tabState = 0 },
            text = {
                Label(
                    text = Res.string.AppCheckDetailsPdf,
                )
            },
        )

        // Add tab for HTML viewer.
        if (tab.html != null) {
            Tab(
                selected = isHtmlTabSelected,
                onClick = { tabState = 1 },
                text = {
                    Label(
                        text = Res.string.AppCheckDetailsHtml,
                    )
                },
            )
        }

        // Add tab for XML viewer.
        if (tab.xml != null) {
            Tab(
                selected = isXmlTabSelected,
                onClick = { tabState = 2 },
                text = {
                    Label(
                        text = Res.string.AppCheckDetailsXml,
                    )
                },
            )
        }
    }

    // Show PDF viewer.
    if (isPdfTabSelected) {
        PdfViewer(
            pdf = tab.file,
            modifier = Modifier.fillMaxSize(),
            search = activeSearch,
        )
    }

    // Show HTML viewer.
    if (isHtmlTabSelected) {
        WebViewer(
            html = tab.html ?: "",
            modifier = Modifier.fillMaxSize(),
            search = activeSearch,
        )
    }

    // Show XML viewer.
    if (isXmlTabSelected) {
        XmlViewer(
            xml = tab.xml ?: "",
            modifier = Modifier.fillMaxSize(),
            search = activeSearch,
        )
    }
}

/**
 * KPI strip + profile/version details for validation summary.
 */
@Composable
private fun ValidationSummary(validation: Validation) {
    val colors = LocalQubaColors.current
    val typo = LocalQubaTypography.current

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        // 4-card KPI strip
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            ValidationKpiCard(
                label = pluralStringResource(Res.plurals.AppCheckSummaryErrors, validation.countErrors, validation.countErrors),
                value = validation.countErrors.toString(),
                bgColor = colors.dangerSoft,
                textColor = colors.danger,
                modifier = Modifier.weight(1f),
            )
            ValidationKpiCard(
                label = pluralStringResource(Res.plurals.AppCheckSummaryWarnings, validation.countWarnings, validation.countWarnings),
                value = validation.countWarnings.toString(),
                bgColor = colors.warnSoft,
                textColor = colors.warn,
                modifier = Modifier.weight(1f),
            )
            ValidationKpiCard(
                label = pluralStringResource(Res.plurals.AppCheckSummaryNotices, validation.countNotices, validation.countNotices),
                value = validation.countNotices.toString(),
                bgColor = colors.accentSoft,
                textColor = colors.accent,
                modifier = Modifier.weight(1f),
            )
            ValidationKpiCard(
                label = stringResource(Res.string.AppCheckSummaryMessages).title(),
                value = validation.messages.size.toString(),
                bgColor = if (validation.isValid) colors.successSoft else colors.surface2,
                textColor = if (validation.isValid) colors.success else colors.text2,
                modifier = Modifier.weight(1f),
            )
        }

        // Profile / version / signature details block
        val shape = RoundedCornerShape(8.dp)
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(colors.surface2)
                .border(1.dp, colors.border, shape)
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = stringResource(Res.string.AppCheckSummaryProfile).title(),
                    style = typo.small.copy(color = colors.text3),
                )
                Text(
                    text = validation.profile?.split(":")?.joinToString(" · ")
                        ?: stringResource(Res.string.AppCheckSummaryUnknown),
                    style = typo.bodyMed.copy(color = colors.text),
                )
                if (!validation.signature.isNullOrBlank()) {
                    Text(
                        text = "${stringResource(Res.string.AppCheckSummarySignature).title()}: ${validation.signature}",
                        style = typo.small.copy(color = colors.text3),
                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = stringResource(Res.string.AppCheckSummaryVersion).title(),
                    style = typo.small.copy(color = colors.text3),
                )
                Text(
                    text = validation.version ?: stringResource(Res.string.AppCheckSummaryUnknown),
                    style = typo.bodyMed.copy(color = colors.text),
                )
            }
        }
    }
}

/**
 * Single KPI metric card used in the validation summary strip.
 */
@Composable
private fun ValidationKpiCard(
    label: String,
    value: String,
    bgColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
) {
    val typo = LocalQubaTypography.current
    val shape = RoundedCornerShape(8.dp)

    Column(
        modifier = modifier
            .clip(shape)
            .background(bgColor)
            .border(1.dp, textColor.copy(alpha = 0.15f), shape)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Text(
            text = value,
            style = typo.h1.copy(color = textColor),
        )
        Text(
            text = label,
            style = typo.small.copy(color = textColor.copy(alpha = 0.75f)),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * List of validation messages.
 */
@Composable
private fun ValidationMessages(tab: CheckTab) {
    val validation = tab.validation!!
    val filterType = tab.filterType
    val filterSeverity = tab.filterSeverity

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        validation.messages
            .filter { filterType.contains(it.type) }
            .filter { filterSeverity.contains(it.severity) }
            .forEach {
                ValidationMessage(it)
            }
    }
}

/**
 * A single validation message.
 */
@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun ValidationMessage(message: ValidationMessage) {
    val isDarkMode = LocalAppState.current.preferences.darkMode ?: isSystemInDarkTheme()
    val accentColor = if (isDarkMode) message.severity.darkModeColor else message.severity.lightModeColor
    val clipboard = LocalClipboardManager.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(8.dp),
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawRect(color = accentColor, size = GeometrySize(4.dp.toPx(), size.height))
                }
                .padding(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 10.dp),
        ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        imageVector = message.severity.icon,
                        tint = accentColor,
                        contentDescription = stringResource(message.severity.title),
                        modifier = Modifier.size(18.dp),
                    )

                    Text(
                        text = buildString {
                            append(stringResource(message.severity.title).title())
                            append(" · ")
                            append(stringResource(message.type.title))
                        },
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.labelLarge.copy(color = accentColor),
                    )

                    Spacer(Modifier.weight(1f))

                    Tooltip(text = stringResource(Res.string.AppCheckMessageMessageCopy)) {
                        IconButton(
                            onClick = { clipboard.setText(AnnotatedString(message.message)) },
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = stringResource(Res.string.AppCheckMessageMessageCopy),
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }

                Text(
                    text = message.message.trim(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 6.dp),
                )

                if (!message.location.isNullOrBlank()) {
                    Text(
                        text = message.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
        }
    }
}
