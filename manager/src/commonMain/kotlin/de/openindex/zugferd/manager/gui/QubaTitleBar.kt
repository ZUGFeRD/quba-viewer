/*
 * Copyright (c) 2024-2025 Andreas Rudolph <andy@openindex.de>.
 * Licensed under the Apache License, Version 2.0.
 */

package de.openindex.zugferd.manager.gui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.openindex.zugferd.manager.theme.LocalQubaColors
import de.openindex.zugferd.manager.theme.LocalQubaTypography

// ─────────────────────────────────────────────────────────────────────────────
// QubaTitleBar — 36h, 3-col grid
// Left: Q logo + "Quba" + version. Center: empty (command bar planned).
// Right: action icons + workspace chip.
// Spec: gradient #FCFCFD → #F4F4F7, hairline bottom.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun QubaTitleBar(
    sectionTitle: String = "",
    actions: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val colors = LocalQubaColors.current
    val typo = LocalQubaTypography.current
    androidx.compose.foundation.layout.Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(colors.surface, colors.surface2)
                    )
                )
                .padding(horizontal = 12.dp),
        ) {
            // Left — section title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                if (sectionTitle.isNotBlank()) {
                    Text(
                        text = sectionTitle,
                        style = typo.bodyMed.copy(color = colors.text),
                        softWrap = false,
                    )
                }
            }

            // Center — ⌘K command bar: deferred to next release,
            // see releases/NAECHSTES_RELEASE.md

            // Right — actions (toggle)
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                actions()
            }
        }

        HorizontalDivider(color = colors.border, thickness = 1.dp)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// QubaStatusBar — 26h, status dot + label + info chips + file path + save
// ─────────────────────────────────────────────────────────────────────────────

enum class QubaDocumentStatus {
    Draft, Invalid, Valid, None
}

@Composable
fun QubaStatusBar(
    status: QubaDocumentStatus = QubaDocumentStatus.None,
    profileLabel: String? = null,
    filePath: String? = null,
    modifier: Modifier = Modifier,
) {
    val colors = LocalQubaColors.current
    val typo = LocalQubaTypography.current

    val (dotColor, statusLabel) = when (status) {
        QubaDocumentStatus.Draft   -> colors.warn    to "Draft"
        QubaDocumentStatus.Invalid -> colors.danger  to "Invalid"
        QubaDocumentStatus.Valid   -> colors.success to "Valid"
        QubaDocumentStatus.None    -> colors.text4   to ""
    }

    androidx.compose.foundation.layout.Column(modifier = modifier.fillMaxWidth()) {
        HorizontalDivider(color = colors.border, thickness = 1.dp)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(26.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(colors.surface2, colors.surface)
                    )
                )
                .padding(horizontal = 12.dp),
        ) {
            // Status dot + label
            if (status != QubaDocumentStatus.None) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(dotColor),
                )
                Text(
                    text = statusLabel,
                    style = typo.label.copy(color = colors.text2),
                )
                StatusDivider()
            }

            // Profile chip
            if (profileLabel != null) {
                Text(
                    text = profileLabel,
                    style = typo.mono.copy(color = colors.text3, fontSize = 10.sp),
                    softWrap = false,
                )
                StatusDivider()
            }

            // File path
            if (filePath != null) {
                Text(
                    text = filePath,
                    style = typo.mono.copy(color = colors.text4, fontSize = 10.sp),
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.weight(1f),
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatusDivider() {
    val colors = LocalQubaColors.current
    Box(
        modifier = Modifier
            .height(12.dp)
            .width(1.dp)
            .background(colors.border),
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// KbdChip — mono 10.5, r4, Surface3, hairline
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun KbdChip(
    text: String,
    modifier: Modifier = Modifier,
) {
    val colors = LocalQubaColors.current
    val typo = LocalQubaTypography.current

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(colors.surface3)
            .border(1.dp, colors.border, RoundedCornerShape(4.dp))
            .padding(horizontal = 5.dp, vertical = 1.dp),
    ) {
        Text(
            text = text,
            style = typo.kbd.copy(color = colors.text3),
        )
    }
}

