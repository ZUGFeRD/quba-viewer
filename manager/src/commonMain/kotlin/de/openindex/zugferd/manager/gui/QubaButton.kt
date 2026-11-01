/*
 * Copyright (c) 2024-2025 Andreas Rudolph <andy@openindex.de>.
 * Licensed under the Apache License, Version 2.0.
 */

package de.openindex.zugferd.manager.gui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.openindex.zugferd.manager.theme.LocalQubaColors
import de.openindex.zugferd.manager.theme.LocalQubaTypography

// ─────────────────────────────────────────────────────────────────────────────
// QubaButton variants
// Spec: btn 28h, padded 0/11, r6, 12sp 500
//       btn-primary: Accent bg, white text
//       btn-ghost: transparent, hover Surface3
//       btn-danger: Danger text, hover DangerSoft bg
// ─────────────────────────────────────────────────────────────────────────────

enum class QubaButtonVariant { Default, Primary, Ghost, Danger }
enum class QubaButtonSize(val height: Dp, val hPad: Dp) {
    Small(24.dp, 8.dp),
    Default(28.dp, 11.dp),
    Large(34.dp, 14.dp),
}

@Composable
fun QubaButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: QubaButtonVariant = QubaButtonVariant.Default,
    size: QubaButtonSize = QubaButtonSize.Default,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    val colors = LocalQubaColors.current
    val typo = LocalQubaTypography.current
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()

    val shape = RoundedCornerShape(6.dp)

    val (bg, contentColor, borderColor) = when (variant) {
        QubaButtonVariant.Primary -> Triple(
            when {
                isPressed -> colors.accentPressed
                isHovered -> colors.accentHover
                else -> colors.accent
            },
            Color.White,
            Color.Transparent,
        )
        QubaButtonVariant.Ghost -> Triple(
            when {
                isPressed -> colors.surface3
                isHovered -> colors.surface2
                else -> Color.Transparent
            },
            colors.text2,
            Color.Transparent,
        )
        QubaButtonVariant.Danger -> Triple(
            when {
                isPressed -> colors.dangerSoft
                isHovered -> colors.dangerSoft
                else -> Color.Transparent
            },
            colors.danger,
            Color.Transparent,
        )
        QubaButtonVariant.Default -> Triple(
            when {
                isPressed -> colors.surface2
                isHovered -> colors.surface2
                else -> colors.surface
            },
            colors.text,
            colors.border,
        )
    }

    val alpha = if (enabled) 1f else 0.45f

    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .height(size.height)
            .clip(shape)
            .background(bg.copy(alpha = bg.alpha * alpha))
            .then(
                if (borderColor != Color.Transparent)
                    Modifier.border(1.dp, borderColor.copy(alpha = alpha), shape)
                else Modifier
            )
            .clickable(
                enabled = enabled && !loading,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .pointerHoverIcon(if (enabled) PointerIcon.Default else PointerIcon.Default)
            .padding(horizontal = size.hPad),
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(12.dp),
                color = contentColor.copy(alpha = alpha),
                strokeWidth = 1.5.dp,
            )
        } else if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = contentColor.copy(alpha = alpha),
                modifier = Modifier.size(14.dp),
            )
        }
        Text(
            text = label,
            style = typo.bodyMed.copy(color = contentColor.copy(alpha = alpha)),
            softWrap = false,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Icon-only square button
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun QubaIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: QubaButtonVariant = QubaButtonVariant.Default,
    size: QubaButtonSize = QubaButtonSize.Default,
    enabled: Boolean = true,
) {
    val colors = LocalQubaColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()

    val shape = RoundedCornerShape(6.dp)

    val (bg, contentColor, borderColor) = when (variant) {
        QubaButtonVariant.Primary -> Triple(
            when {
                isPressed -> colors.accentPressed
                isHovered -> colors.accentHover
                else -> colors.accent
            },
            Color.White,
            Color.Transparent,
        )
        QubaButtonVariant.Ghost -> Triple(
            when {
                isPressed -> colors.surface3
                isHovered -> colors.surface2
                else -> Color.Transparent
            },
            colors.text2,
            Color.Transparent,
        )
        QubaButtonVariant.Danger -> Triple(
            when { isHovered || isPressed -> colors.dangerSoft; else -> Color.Transparent },
            colors.danger,
            Color.Transparent,
        )
        QubaButtonVariant.Default -> Triple(
            when { isHovered || isPressed -> colors.surface2; else -> colors.surface },
            colors.text2,
            colors.border,
        )
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size.height)
            .clip(shape)
            .background(bg)
            .then(
                if (borderColor != Color.Transparent)
                    Modifier.border(1.dp, borderColor, shape)
                else Modifier
            )
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = contentColor.copy(alpha = if (enabled) 1f else 0.45f),
            modifier = Modifier.size(15.dp),
        )
    }
}
