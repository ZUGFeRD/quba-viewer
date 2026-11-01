/*
 * Copyright (c) 2024-2025 Andreas Rudolph <andy@openindex.de>.
 * Licensed under the Apache License, Version 2.0.
 */

package de.openindex.zugferd.manager.gui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.openindex.zugferd.manager.theme.LocalQubaColors
import de.openindex.zugferd.manager.theme.LocalQubaTypography

// ─────────────────────────────────────────────────────────────────────────────
// QubaCard — r10, 1px Border, shadowSm
// card-hdr: 11/14 padding, gradient bg Surface2 70→30%, hairline bottom,
//           12/600 title + Text3 subtitle + spacer + actions
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun QubaCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = LocalQubaColors.current
    val shape = RoundedCornerShape(10.dp)

    Column(
        modifier = modifier
            .shadow(
                elevation = 1.dp,
                shape = shape,
                ambientColor = Color(0x0A0F121E),
                spotColor = Color(0x0D0F121E),
            )
            .clip(shape)
            .background(colors.surface)
            .border(1.dp, colors.border, shape),
        content = content,
    )
}

@Composable
fun QubaCardHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val colors = LocalQubaColors.current
    val typo = LocalQubaTypography.current

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        0f to colors.surface2.copy(alpha = 0.7f),
                        1f to colors.surface2.copy(alpha = 0.3f),
                    )
                )
                .padding(horizontal = 14.dp, vertical = 11.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = typo.h2.copy(color = colors.text),
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = typo.small.copy(color = colors.text3),
                    )
                }
            }
            Spacer(modifier = Modifier.weight(0f))
            actions()
        }
        HorizontalDivider(color = colors.border, thickness = 1.dp)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// QubaGroup — like card but flush header; used inside sections
// Header: 10/14, hairline divider, body 12/14
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun QubaGroup(
    title: String,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = LocalQubaColors.current
    val typo = LocalQubaTypography.current

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 0.dp, vertical = 10.dp),
        ) {
            Text(
                text = title,
                style = typo.bodyMed.copy(color = colors.text),
                modifier = Modifier.weight(1f),
            )
            actions()
        }
        HorizontalDivider(color = colors.border, thickness = 1.dp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp),
            content = content,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// QubaSectionHeader — [01] Title · hint ─── actions
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun QubaSectionHeader(
    number: String? = null,
    title: String,
    hint: String? = null,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val colors = LocalQubaColors.current
    val typo = LocalQubaTypography.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth(),
    ) {
        // Number badge
        if (number != null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(colors.surface3)
                    .border(1.dp, colors.border, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 1.dp),
            ) {
                Text(
                    text = number,
                    style = typo.mono.copy(color = colors.text2, fontSize = 10.5.sp),
                )
            }
            Spacer(modifier = Modifier.padding(horizontal = 6.dp))
        }

        // Title
        Text(
            text = title,
            style = typo.h2.copy(color = colors.text),
        )

        // Hint
        if (hint != null) {
            Spacer(modifier = Modifier.padding(horizontal = 6.dp))
            Text(
                text = hint,
                style = typo.small.copy(color = colors.text3),
            )
        }

        // Hairline flex spacer
        HorizontalDivider(
            color = colors.border,
            thickness = 1.dp,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
        )

        // Actions
        actions()
    }
}

