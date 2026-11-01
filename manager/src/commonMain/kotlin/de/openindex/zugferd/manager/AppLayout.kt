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

package de.openindex.zugferd.manager

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.foundation.Image
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import de.openindex.zugferd.manager.theme.LocalQubaTypography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import de.openindex.zugferd.manager.gui.QubaTitleBar
import de.openindex.zugferd.manager.gui.QubaStatusBar
import de.openindex.zugferd.manager.gui.QubaDocumentStatus
import de.openindex.zugferd.manager.sections.CheckSectionState
import de.openindex.zugferd.manager.sections.VisualsSectionState
import de.openindex.zugferd.manager.theme.LocalQubaColors
import de.openindex.zugferd.manager.utils.stringResource
import de.openindex.zugferd.manager.utils.title
import de.openindex.zugferd.quba.generated.resources.AppSidebarCollapse
import de.openindex.zugferd.quba.generated.resources.AppSidebarExpand
import de.openindex.zugferd.quba.generated.resources.AppSidebarQuit
import de.openindex.zugferd.quba.generated.resources.Res
import de.openindex.zugferd.quba.generated.resources.ic_app_logo
import java.awt.KeyEventDispatcher
import java.awt.KeyboardFocusManager
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun AppLayout() {
    // Single global AWT key dispatcher for Ctrl+F / ESC search handling.
    DisposableEffect(Unit) {
        val dispatcher = KeyEventDispatcher { awtEvent ->
            if (awtEvent.id == java.awt.event.KeyEvent.KEY_PRESSED
                && awtEvent.keyCode == java.awt.event.KeyEvent.VK_F
                && awtEvent.isControlDown
            ) {
                when (_APP_STATE.sectionSync) {
                    AppSection.VISUALISATION ->
                        (AppSection.VISUALISATION.state as? VisualsSectionState)?.isSearchOpen = true
                    AppSection.CHECK ->
                        (AppSection.CHECK.state as? CheckSectionState)?.isSearchOpen = true
                    else -> {}
                }
                true
            } else if (awtEvent.id == java.awt.event.KeyEvent.KEY_PRESSED
                && awtEvent.keyCode == java.awt.event.KeyEvent.VK_ESCAPE
            ) {
                when (_APP_STATE.sectionSync) {
                    AppSection.VISUALISATION -> {
                        val state = AppSection.VISUALISATION.state as? VisualsSectionState
                        if (state?.isSearchOpen == true) { state.isSearchOpen = false; true } else false
                    }
                    AppSection.CHECK -> {
                        val state = AppSection.CHECK.state as? CheckSectionState
                        if (state?.isSearchOpen == true) { state.isSearchOpen = false; true } else false
                    }
                    else -> false
                }
            } else {
                false
            }
        }
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(dispatcher)
        onDispose {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(dispatcher)
        }
    }

    val appState = LocalAppState.current

    Column(modifier = Modifier.fillMaxSize()) {
        // Titlebar — 36h
        QubaTitleBar(
            sectionTitle = stringResource(appState.section.label).title(),
            actions = { appState.section.actions() },
        )

        // Middle: rail + content
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.weight(1f),
        ) {
            AppNavigation()
            VerticalDivider(color = LocalQubaColors.current.border)
            AppContent()
        }

        // Status bar — 26h
        QubaStatusBar()
    }
}

@Composable
private fun AppContent() {
    Surface(
        modifier = Modifier.fillMaxSize(),
    ) {
        LocalAppState.current.section.content()
    }
}

@Composable
private fun AppNavigation() {
    var shutdownRequested by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }
    val colors = LocalQubaColors.current
    val navWidth = if (isExpanded) 180.dp else 56.dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(navWidth)
            .animateContentSize()
            .fillMaxHeight()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(colors.surfaceInset, colors.surface2),
                )
            )
            .padding(vertical = 10.dp),
    ) {
        Spacer(modifier = Modifier.height(2.dp))

        // Main section items
        AppSection.entries.forEach {
            AppSectionNavigationItem(section = it, isExpanded = isExpanded)
        }

        Spacer(modifier = Modifier.weight(1f, fill = true))

        // Brand: Quba SVG logo + version (above divider)
        if (isExpanded) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .padding(bottom = 2.dp),
            ) {
                Image(
                    painter = painterResource(Res.drawable.ic_app_logo),
                    contentDescription = APP_TITLE,
                    modifier = Modifier.height(40.dp),
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "v$APP_VERSION_SHORT",
                    style = LocalQubaTypography.current.small.copy(color = colors.text4),
                    softWrap = false,
                )
            }
        } else {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .width(56.dp)
                    .height(32.dp),
            ) {
                Image(
                    painter = painterResource(Res.drawable.ic_app_logo),
                    contentDescription = APP_TITLE,
                    modifier = Modifier.height(28.dp),
                )
            }
        }

        // Divider before bottom actions
        HorizontalDivider(
            color = colors.border,
            modifier = Modifier
                .padding(vertical = 6.dp)
                .then(if (isExpanded) Modifier.padding(horizontal = 12.dp) else Modifier.width(32.dp)),
        )

        // Expand/collapse toggle
        AppNavigationItem(
            label = if (isExpanded) Res.string.AppSidebarCollapse else Res.string.AppSidebarExpand,
            icon = if (isExpanded) Icons.AutoMirrored.Filled.MenuOpen else Icons.Default.Menu,
            selected = false,
            isExpanded = isExpanded,
            onClick = { isExpanded = !isExpanded },
        )

        // Theme mode toggle (Hell / Auto / Dunkel)
        ThemeModeToggle(isExpanded = isExpanded)

        // Quit / Shutdown
        AppNavigationItem(
            label = Res.string.AppSidebarQuit,
            icon = Icons.Default.PowerSettingsNew,
            selected = false,
            isExpanded = isExpanded,
            onClick = { shutdownRequested = true },
        )

        Spacer(modifier = Modifier.height(4.dp))
    }

    if (shutdownRequested) {
        LocalShutdownHandler.current?.shutdown()
    }
}

@Composable
private fun AppNavigationItem(
    label: StringResource,
    icon: ImageVector,
    selected: Boolean,
    isExpanded: Boolean,
    onClick: () -> Unit,
) {
    val colors = LocalQubaColors.current
    val typo = LocalQubaTypography.current
    val displayLabel = stringResource(label)
    val iconTint = if (selected) colors.accent else colors.text3

    if (isExpanded) {
        // Expanded: full-width row with icon + label
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .fillMaxWidth()
                .height(36.dp)
                .drawBehind {
                    if (selected) {
                        drawRoundRect(
                            color = colors.accent,
                            topLeft = Offset(0f, 4.dp.toPx()),
                            size = Size(3.dp.toPx(), size.height - 8.dp.toPx()),
                            cornerRadius = CornerRadius(2.dp.toPx()),
                        )
                    }
                }
                .clip(RoundedCornerShape(8.dp))
                .background(if (selected) colors.accentSoft else Color.Transparent)
                .clickable(onClick = onClick)
                .padding(start = 10.dp, end = 8.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = displayLabel,
                tint = iconTint,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = displayLabel,
                style = typo.bodyMed.copy(
                    color = if (selected) colors.accent else colors.text2,
                ),
                softWrap = false,
            )
        }
    } else {
        // Collapsed: icon-only, 3px accent bar on left when active
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .width(56.dp)
                .height(40.dp)
                .drawBehind {
                    if (selected) {
                        drawRoundRect(
                            color = colors.accent,
                            topLeft = Offset(0f, 8.dp.toPx()),
                            size = Size(3.dp.toPx(), size.height - 16.dp.toPx()),
                            cornerRadius = CornerRadius(2.dp.toPx()),
                        )
                    }
                },
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(width = 40.dp, height = 36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selected) colors.accentSoft else Color.Transparent)
                    .clickable(onClick = onClick),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = displayLabel,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun AppSectionNavigationItem(section: AppSection, isExpanded: Boolean) {
    val appState = LocalAppState.current

    AppNavigationItem(
        label = section.label,
        icon = if (appState.isSection(section)) section.activeIcon else section.inactiveIcon,
        selected = appState.isSection(section),
        isExpanded = isExpanded,
        onClick = { appState.setSection(section) },
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Theme mode quick-toggle
// Expanded: 3-segment icon pill  (☀ Hell | 🖥 Auto | ☾ Dunkel)
// Collapsed: single icon cycling Auto → Hell → Dunkel → Auto
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ThemeModeToggle(isExpanded: Boolean) {
    val preferences = LocalAppState.current.preferences
    val colors = LocalQubaColors.current
    val scope = rememberCoroutineScope()

    fun setTheme(mode: Boolean?) {
        preferences.setDarkMode(mode)
        scope.launch { preferences.save() }
    }

    if (isExpanded) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .fillMaxWidth()
                .height(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                .background(colors.surface3),
        ) {
            ThemeSegment(
                icon = Icons.Default.LightMode,
                label = "Hell",
                active = preferences.isThemeLight,
                modifier = Modifier.weight(1f),
                onClick = { setTheme(false) },
            )
            Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(colors.border))
            ThemeSegment(
                icon = Icons.Default.BrightnessAuto,
                label = "Auto",
                active = preferences.isThemeAuto,
                modifier = Modifier.weight(1f),
                onClick = { setTheme(null) },
            )
            Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(colors.border))
            ThemeSegment(
                icon = Icons.Default.DarkMode,
                label = "Dunkel",
                active = preferences.isThemeDark,
                modifier = Modifier.weight(1f),
                onClick = { setTheme(true) },
            )
        }
    } else {
        val currentIcon = when {
            preferences.isThemeLight -> Icons.Default.LightMode
            preferences.isThemeDark  -> Icons.Default.DarkMode
            else                     -> Icons.Default.BrightnessAuto
        }
        val nextMode: Boolean? = when {
            preferences.isThemeAuto  -> false   // auto → hell
            preferences.isThemeLight -> true    // hell → dunkel
            else                     -> null    // dunkel → auto
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .width(56.dp)
                .height(40.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(width = 40.dp, height = 36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { setTheme(nextMode) },
            ) {
                Icon(
                    imageVector = currentIcon,
                    contentDescription = "Theme wechseln",
                    tint = colors.text3,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun ThemeSegment(
    icon: ImageVector,
    label: String,
    active: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val colors = LocalQubaColors.current
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(7.dp))
            .background(if (active) colors.accentSoft else Color.Transparent)
            .clickable(onClick = onClick),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (active) colors.accent else colors.text3,
            modifier = Modifier.size(16.dp),
        )
    }
}
