/*
 * Copyright (c) 2024-2025 Andreas Rudolph <andy@openindex.de>.
 * Licensed under the Apache License, Version 2.0.
 */

package de.openindex.zugferd.manager.gui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.openindex.zugferd.manager.theme.LocalQubaColors
import de.openindex.zugferd.manager.theme.LocalQubaTypography

// ─────────────────────────────────────────────────────────────────────────────
// QubaSegmented — inline pill group
// Spec: Surface2 bg, 2px internal padding, hairline border.
//       Active: Surface bg + shadowSm + Text. Inactive: Text3.
// ─────────────────────────────────────────────────────────────────────────────

data class QubaSegmentedItem<T>(
    val key: T,
    val label: String,
)

@Composable
fun <T> QubaSegmented(
    items: List<QubaSegmentedItem<T>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalQubaColors.current
    val typo = LocalQubaTypography.current
    val outerShape = RoundedCornerShape(999.dp)
    val innerShape = RoundedCornerShape(999.dp)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .height(28.dp)
            .clip(outerShape)
            .background(colors.surface2)
            .border(1.dp, colors.border, outerShape)
            .padding(2.dp),
    ) {
        items.forEach { item ->
            val isSelected = item.key == selected
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .height(24.dp)
                    .clip(innerShape)
                    .then(
                        if (isSelected)
                            Modifier
                                .shadow(
                                    elevation = 1.dp,
                                    shape = innerShape,
                                    ambientColor = Color(0x0D0F121E),
                                )
                                .background(colors.surface)
                        else Modifier.background(Color.Transparent)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onSelect(item.key) },
                    )
                    .padding(horizontal = 10.dp),
            ) {
                Text(
                    text = item.label,
                    style = typo.label.copy(
                        color = if (isSelected) colors.text else colors.text3,
                    ),
                    softWrap = false,
                )
            }
        }
    }
}
