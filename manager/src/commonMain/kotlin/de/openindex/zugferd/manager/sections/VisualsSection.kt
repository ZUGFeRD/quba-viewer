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

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.VerticalSplit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.Layout
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.openindex.zugferd.manager.AppSection
import de.openindex.zugferd.manager.LocalAppState
import de.openindex.zugferd.manager.gui.ActionButtonWithTooltip
import de.openindex.zugferd.manager.gui.NotificationBar
import de.openindex.zugferd.manager.gui.PdfViewer
import de.openindex.zugferd.manager.gui.Tooltip
import de.openindex.zugferd.manager.gui.WebViewer
import de.openindex.zugferd.manager.gui.XmlViewer
import de.openindex.zugferd.manager.model.DocumentTab
import de.openindex.zugferd.manager.utils.createDragAndDropTarget
import de.openindex.zugferd.manager.utils.showTabContextMenu
import de.openindex.zugferd.manager.sections.CheckSectionState
import de.openindex.zugferd.manager.theme.LocalQubaColors
import de.openindex.zugferd.manager.theme.LocalQubaTypography
import de.openindex.zugferd.manager.utils.stringResource
import de.openindex.zugferd.manager.utils.title
import de.openindex.zugferd.quba.generated.resources.AppCheckSelectMessage
import de.openindex.zugferd.quba.generated.resources.AppVisualisationEmptyHint
import de.openindex.zugferd.quba.generated.resources.AppVisualisationEmptyTitle
import de.openindex.zugferd.quba.generated.resources.AppVisualisationNoXml
import de.openindex.zugferd.quba.generated.resources.AppVisualisationViewCode
import de.openindex.zugferd.quba.generated.resources.AppVisualisationViewPdf
import de.openindex.zugferd.quba.generated.resources.AppVisualisationViewSplit
import de.openindex.zugferd.quba.generated.resources.Res
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.awt.Cursor
import de.openindex.zugferd.quba.generated.resources.AppVisualisationSelectFile
import de.openindex.zugferd.quba.generated.resources.AppVisualisationSelectInfo
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.input.key.*
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.isPressed
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PlatformFile
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType


data class SearchState(
    val query: String,
    val sequence: Int = 0,
)


@Composable
@OptIn(ExperimentalFoundationApi::class)
fun VisualsSection(state: VisualsSectionState) {
    val scope = rememberCoroutineScope()
    val appState = LocalAppState.current

    val activeSearch = remember(state.isSearchOpen, state.searchQuery, state.searchSequence) {
        if (state.isSearchOpen && state.searchQuery.isNotBlank()) {
            SearchState(query = state.searchQuery, sequence = state.searchSequence)
        } else {
            null
        }
    }

    LaunchedEffect(state.isSearchOpen) {
        if (!state.isSearchOpen) {
            state.searchQuery = ""
            state.searchSequence = 0
        }
    }

    val focusRequester = remember { androidx.compose.ui.focus.FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // Multi-file picker for empty-area click — same as the + button.
    val emptyAreaLauncher = rememberFilePickerLauncher(
        type = PickerType.File(extensions = listOf("pdf", "xml")),
        mode = PickerMode.Multiple(),
        title = "Dateien auswählen",
        onResult = { files: List<PlatformFile>? ->
            files?.forEach { file ->
                scope.launch {
                    try {
                        state.addTabWithFile(file, appState)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        },
    )

    // Current tab info — needed by title bar toggle and CurrentTabContent.
    val currentTab = state.documents.getOrNull(state.selectedIndex)
    val hasPdf = currentTab?.pdf != null
    val hasXml = currentTab?.xml != null
    val hasHtml = currentTab?.html != null
    val hasCode = hasXml || hasHtml || (currentTab?.isHtmlLoading == true)
    val hasStableCode = hasXml || hasHtml

    // Reset viewMode when the tab changes.
    LaunchedEffect(currentTab) {
        state.viewMode = when {
            hasPdf && hasStableCode -> ViewMode.SPLIT
            hasStableCode           -> ViewMode.CODE_ONLY
            else                    -> ViewMode.PDF_ONLY
        }
    }

    // Correct viewMode and toggle visibility when content changes asynchronously.
    LaunchedEffect(hasPdf, hasStableCode) {
        state.showViewModeToggle = hasPdf && hasCode
        when {
            !hasPdf && state.viewMode != ViewMode.CODE_ONLY                -> state.viewMode = ViewMode.CODE_ONLY
            !hasStableCode && state.viewMode == ViewMode.CODE_ONLY         -> state.viewMode = ViewMode.PDF_ONLY
            !hasStableCode && state.viewMode == ViewMode.SPLIT             -> state.viewMode = ViewMode.PDF_ONLY
            hasStableCode && hasPdf && state.viewMode == ViewMode.PDF_ONLY -> state.viewMode = ViewMode.SPLIT
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable()
    ) {
        if (state.documents.isNotEmpty()) {
            TabRowWithControls(state)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                .then(
                    if (state.documents.isEmpty())
                        Modifier.clickable { emptyAreaLauncher.launch() }
                    else Modifier
                )
                .dragAndDropTarget(
                    target = remember {
                        createDragAndDropTarget { file ->
                            // Each dropped file opens in its own new tab.
                            scope.launch {
                                try {
                                    state.addTabWithFile(file, appState)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    },
                    shouldStartDragAndDrop = { true }
                )
        ) {
            if (state.documents.isEmpty()) {
                EmptyVisualsView(state)
            } else {
                CurrentTabContent(
                    state = state,
                    search = activeSearch,
                    viewMode = state.viewMode,
                    onViewModeChange = { state.viewMode = it },
                )
            }
        }
    }
}

@Composable
fun VisualsSectionActions(state: VisualsSectionState) {
    if (state.showViewModeToggle) {
        ViewModeOrbitMenu(
            currentMode = state.viewMode,
            onModeChanged = { state.viewMode = it },
        )
    }
}

@Composable
internal fun CompactSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val textStyle = MaterialTheme.typography.bodySmall
    val focusRequester = remember { androidx.compose.ui.focus.FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = textStyle.copy(color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onPreviewKeyEvent { event ->
                            when {
                                (event.key == Key.Enter || event.key == Key.NumPadEnter) && event.type == KeyEventType.KeyUp -> {
                                    if (value.isNotBlank()) onSubmit()
                                    true
                                }
                                event.key == Key.Escape && event.type == KeyEventType.KeyUp -> {
                                    onClose()
                                    true
                                }
                                else -> false
                            }
                        },
                )
            }

            IconButton(
                onClick = { if (value.isNotBlank()) onSubmit() },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Nächster Treffer",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Suche schließen",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun TabRowWithControls(state: VisualsSectionState) {
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
                    state.addTabWithFile(file, appState)
                }
            }
        },
    )

    val maxTabWidth = 200.dp
    val minTabWidth = 80.dp
    val tabSpacing = 4.dp
    val plusButtonSize = 32.dp
    val stripPadding = 8.dp
    val searchIconWidth = 48.dp
    val searchBarWidth = 288.dp // 280 content + 8 padding

    // Drag state — lifted here so all tabs can react in real-time.
    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffsetX by remember { mutableStateOf(0f) }
    // Mutable array so drag lambdas always read the current step without restarting.
    val tabStepPxRef = remember { floatArrayOf(0f) }

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val tabCount = state.documents.size
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
                .coerceIn(0, state.documents.lastIndex)
            else from
        }

        val needsScroll = tabCount > 0 && rawTabWidth < minTabWidth
        val scrollState = rememberScrollState()

        LaunchedEffect(state.selectedIndex) {
            if (needsScroll && state.documents.isNotEmpty()) {
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
                .padding(horizontal = 0.dp, vertical = 4.dp),
        ) {
            // Tabs row — no weight so the + button sits directly after the last tab.
            Row(
                horizontalArrangement = Arrangement.spacedBy(tabSpacing),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(start = stripPadding)
                    .then(if (needsScroll) Modifier.horizontalScroll(scrollState) else Modifier),
            ) {
                state.documents.forEachIndexed { index, doc ->
                    val from = draggedIndex
                    val to = insertIndex
                    val step = tabStepPxRef[0]

                    // How far should this tab shift visually to make room for the dragged tab?
                    val shiftTarget: Float = when {
                        from == null || to == null -> 0f
                        index == from -> 0f                          // dragged tab: offset applied directly
                        from < to && index in (from + 1)..to -> -step  // dragging right: shift left
                        from > to && index in to until from -> step    // dragging left: shift right
                        else -> 0f
                    }

                    DocumentTabItem(
                        document = doc,
                        width = tabWidth,
                        isSelected = index == state.selectedIndex,
                        isDragged = index == draggedIndex,
                        dragOffsetPx = if (index == draggedIndex) dragOffsetX else 0f,
                        shiftTarget = shiftTarget,
                        onSelect = { state.selectedIndex = index },
                        onClose = { state.removeTab(index) },
                        onRightClick = { screenX, screenY ->
                            val checkState = AppSection.CHECK.state as? CheckSectionState
                            val docFile = doc.sourceFile
                            showTabContextMenu(
                                screenX = screenX,
                                screenY = screenY,
                                onClose = { state.removeTab(index) },
                                onCloseOthers = { state.removeOtherTabs(index) },
                                onCloseToRight = if (index < state.documents.lastIndex) ({ state.removeTabsToRight(index) }) else null,
                                openInOtherLabel = "In Prüfen öffnen",
                                onOpenInOther = if (checkState != null && docFile != null) ({
                                    // Use an independent scope — the local composable scope is
                                    // cancelled when this section leaves the composition on switch.
                                    CoroutineScope(Dispatchers.IO).launch {
                                        checkState.selectFile(docFile, appState)
                                    }
                                    appState.setSection(AppSection.CHECK)
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
                                        .coerceIn(0, state.documents.lastIndex)
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

@Composable
private fun DocumentTabItem(
    document: DocumentTab,
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
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        // Status icon — spinner while loading, document icon otherwise.
        if (document.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(12.dp),
                strokeWidth = 1.5.dp,
                color = colors.text3,
            )
        } else {
            Icon(
                imageVector = Icons.Default.Visibility,
                contentDescription = null,
                modifier = Modifier.size(13.dp),
                tint = if (isSelected) colors.accent else colors.text3,
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = document.name.ifBlank { "Neuer Tab" },
            color = if (isSelected) colors.text else colors.text2,
            style = typo.small,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )

        Spacer(modifier = Modifier.width(4.dp))

        if (!document.isLoading) {
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

@Composable
private fun CurrentTabContent(
    state: VisualsSectionState,
    search: SearchState?,
    viewMode: ViewMode,
    onViewModeChange: (ViewMode) -> Unit,
) {
    val currentTab = state.documents.getOrNull(state.selectedIndex) ?: return

    if (currentTab.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val hasPdf = currentTab.pdf != null
    val hasXml = currentTab.xml != null
    val hasHtml = currentTab.html != null
    val hasCode = hasXml || hasHtml || currentTab.isHtmlLoading

    if (!hasPdf && !hasCode) {
        EmptyVisualsView(state)
        return
    }

    var tabState by remember(currentTab) { mutableStateOf(0) }
    val splitRatioState = remember(currentTab) { mutableStateOf(0.5f) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Informational banner for plain PDFs without embedded e-invoice data
        if (hasPdf && !hasCode) {
            NotificationBar(text = stringResource(Res.string.AppVisualisationNoXml))
        }

        // ContentArea keeps PdfViewer at a fixed slot in the composition tree.
        // This prevents JCEF browser disposal when switching between SPLIT and PDF_ONLY,
        // which would otherwise cause blank rendering (two browsers sharing one CEF client).
        ContentArea(
            viewMode = viewMode,
            splitRatioState = splitRatioState,
            codeContent = {
                if (hasCode) CodeOnlyView(currentTab, tabState, { tabState = it }, search)
            },
            pdfContent = {
                currentTab.pdf?.let { pdf ->
                    PdfViewer(pdf = pdf, modifier = Modifier.fillMaxSize(), search = search)
                }
            },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun EmptyVisualsView(state: VisualsSectionState) {
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
                    imageVector = Icons.Default.Visibility,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(48.dp),
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = stringResource(Res.string.AppVisualisationEmptyTitle),
                    style = typo.bodyMed.copy(color = colors.text2),
                )
                Text(
                    text = stringResource(Res.string.AppVisualisationEmptyHint),
                    style = typo.small.copy(color = colors.text4),
                )
            }
        }
    }
}

@Composable
private fun CodeOnlyView(tab: DocumentTab, selectedTab: Int, onTabChange: (Int) -> Unit, search: SearchState?) {
    val hasHtml = tab.html != null
    val hasXml = tab.xml != null
    // HTML-Tab anzeigen sobald HTML bereit ODER noch geladen wird
    val showHtmlTab = hasHtml || tab.isHtmlLoading

    Column(modifier = Modifier.fillMaxSize()) {
        // Tab row: HTML-Tab (ggf. mit Lade-Indikator) + XML-Tab
        if (showHtmlTab && hasXml) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { onTabChange(0) },
                    text = {
                        if (tab.isHtmlLoading && !hasHtml) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text("HTML Vorschau")
                                Spacer(modifier = Modifier.width(6.dp))
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        } else {
                            Text("HTML Vorschau")
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { onTabChange(1) },
                    text = { Text("XML") }
                )
            }
        }

        // Content
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                selectedTab == 0 && hasHtml -> {
                    WebViewer(html = tab.html!!, modifier = Modifier.fillMaxSize(), search = search)
                }

                selectedTab == 0 && tab.isHtmlLoading -> {
                    // HTML wird noch generiert – Spinner anzeigen
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "HTML wird generiert…",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                hasXml && (selectedTab == 1 || !showHtmlTab) -> {
                    XmlViewer(xml = tab.xml!!, modifier = Modifier.fillMaxSize(), search = search)
                }

                else -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Kein Inhalt verfügbar")
                    }
                }
            }
        }
    }
}

// Ansichtsmodi
enum class ViewMode {
    PDF_ONLY,
    CODE_ONLY,
    SPLIT
}

// Orbit-Menü für die Ansichtsauswahl.
// Trigger (32 dp) passt in die 36 dp Titelleiste.
// Orbit-Items (28 dp) klappen beim Hover nach LINKS auf — bleiben in der Titelleiste,
// keine Überlappung mit dem Inhaltsbereich darunter.
@Composable
@OptIn(ExperimentalComposeUiApi::class)
private fun ViewModeOrbitMenu(
    currentMode: ViewMode,
    onModeChanged: (ViewMode) -> Unit,
) {
    val colors = LocalQubaColors.current
    val scope = rememberCoroutineScope()
    var expanded by remember { mutableStateOf(false) }
    var closeJob by remember { mutableStateOf<Job?>(null) }

    fun keepOpen() {
        closeJob?.cancel()
        closeJob = null
    }

    fun scheduleClose() {
        closeJob?.cancel()
        closeJob = scope.launch {
            delay(200L)
            expanded = false
        }
    }

    // Left-to-right order when spread: PDF | Split | Code | [trigger]
    // Items slide out to the left; each is 28 dp with 8 dp gap → 36 dp step.
    val orbitModes  = listOf(ViewMode.PDF_ONLY, ViewMode.SPLIT, ViewMode.CODE_ONLY)
    val orbitIcons  = listOf(Icons.Default.PictureAsPdf, Icons.Default.VerticalSplit, Icons.Default.Code)
    val orbitLabels = listOf(
        stringResource(Res.string.AppVisualisationViewPdf),
        stringResource(Res.string.AppVisualisationViewSplit),
        stringResource(Res.string.AppVisualisationViewCode),
    )
    // Collapsed → (0, 0); open: 3 items to the left at equal spacing
    val openOffsets = listOf(-108.dp, -72.dp, -36.dp)

    val triggerIcon = when (currentMode) {
        ViewMode.PDF_ONLY  -> Icons.Default.PictureAsPdf
        ViewMode.SPLIT     -> Icons.Default.VerticalSplit
        ViewMode.CODE_ONLY -> Icons.Default.Code
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .zIndex(10f)
            .size(32.dp),
    ) {
        // Orbit items — rendered first so trigger sits on top in z-order
        orbitModes.indices.forEach { i ->
            val targetX = if (expanded) openOffsets[i] else 0.dp
            val animX by animateDpAsState(
                targetValue = targetX,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
                label = "orbitX$i",
            )
            val animAlpha by animateFloatAsState(
                targetValue = if (expanded) 1f else 0f,
                label = "orbitAlpha$i",
            )
            val animScale by animateFloatAsState(
                targetValue = if (expanded) 1f else 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
                label = "orbitScale$i",
            )
            val isActive = orbitModes[i] == currentMode

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(28.dp)
                    .offset(x = animX, y = 0.dp)
                    .alpha(animAlpha)
                    .scale(animScale)
                    .clip(CircleShape)
                    .background(if (isActive) colors.accentSoft else colors.surface2)
                    .border(1.dp, if (isActive) colors.accent else colors.border, CircleShape)
                    .clickable(enabled = expanded) { onModeChanged(orbitModes[i]); expanded = false }
                    .onPointerEvent(PointerEventType.Enter) { keepOpen() }
                    .onPointerEvent(PointerEventType.Exit) { scheduleClose() },
            ) {
                Icon(
                    imageVector = orbitIcons[i],
                    contentDescription = orbitLabels[i],
                    modifier = Modifier.size(15.dp),
                    tint = if (isActive) colors.accent else colors.text2,
                )
            }
        }

        // Trigger — on top, shows current mode icon
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (expanded) colors.accentSoft else Color.Transparent)
                .border(1.dp, if (expanded) colors.accent else colors.border, CircleShape)
                .onPointerEvent(PointerEventType.Enter) { keepOpen(); expanded = true }
                .onPointerEvent(PointerEventType.Exit) { scheduleClose() },
        ) {
            Icon(
                imageVector = triggerIcon,
                contentDescription = "Ansicht wechseln",
                modifier = Modifier.size(16.dp),
                tint = if (expanded) colors.accent else colors.text2,
            )
        }
    }
}

// Three fixed composition slots: [0] code viewer | [1] divider | [2] PDF viewer.
// Drag deltas are accumulated each mouse event and flushed once per display frame
// via withFrameNanos — throttles JCEF resize calls to ~60/sec.
@Composable
private fun ContentArea(
    viewMode: ViewMode,
    splitRatioState: MutableState<Float>,
    codeContent: @Composable () -> Unit,
    pdfContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dividerPx = with(LocalDensity.current) { 6.dp.roundToPx() }
    val containerWidth = remember { intArrayOf(0) }
    val pendingDelta = remember { floatArrayOf(0f) }

    fun applyDelta() {
        val d = pendingDelta[0]
        val w = containerWidth[0]
        if (d != 0f && w > 0) {
            pendingDelta[0] = 0f
            splitRatioState.value = (splitRatioState.value + d / w).coerceIn(0.1f, 0.9f)
        }
    }

    LaunchedEffect(splitRatioState) {
        while (true) { withFrameNanos { applyDelta() } }
    }

    val dividerColor = MaterialTheme.colorScheme.outline
    Layout(
        modifier = modifier.fillMaxSize(),
        content = {
            Box { if (viewMode != ViewMode.PDF_ONLY) codeContent() }
            Box(
                modifier = if (viewMode == ViewMode.SPLIT) Modifier
                    .background(dividerColor)
                    .pointerHoverIcon(PointerIcon(Cursor(Cursor.E_RESIZE_CURSOR)))
                    .draggable(
                        orientation = Orientation.Horizontal,
                        state = rememberDraggableState { pendingDelta[0] += it },
                        onDragStopped = { applyDelta() },
                    )
                else Modifier
            )
            Box { if (viewMode != ViewMode.CODE_ONLY) pdfContent() }
        }
    ) { measurables, constraints ->
        containerWidth[0] = constraints.maxWidth
        val ratio = splitRatioState.value
        val h = if (constraints.hasBoundedHeight) constraints.maxHeight else 0

        val (codeW, divW, pdfW) = when (viewMode) {
            ViewMode.PDF_ONLY  -> Triple(0, 0, constraints.maxWidth)
            ViewMode.CODE_ONLY -> Triple(constraints.maxWidth, 0, 0)
            ViewMode.SPLIT -> {
                val cw = ((constraints.maxWidth - dividerPx) * ratio).toInt()
                    .coerceIn(0, constraints.maxWidth - dividerPx)
                Triple(cw, dividerPx, constraints.maxWidth - cw - dividerPx)
            }
        }

        fun slot(w: Int) = constraints.copy(minWidth = w, maxWidth = w, minHeight = h, maxHeight = h)
        val code    = measurables[0].measure(slot(codeW))
        val divider = measurables[1].measure(slot(divW))
        val pdf     = measurables[2].measure(slot(pdfW))

        layout(constraints.maxWidth, h) {
            code.placeRelative(0, 0)
            divider.placeRelative(codeW, 0)
            pdf.placeRelative(codeW + divW, 0)
        }
    }
}

