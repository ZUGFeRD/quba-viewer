/*
 * Copyright (c) 2024-2025 Andreas Rudolph <andy@openindex.de>.
 * Licensed under the Apache License, Version 2.0.
 */

package de.openindex.zugferd.manager.gui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.openindex.zugferd.manager.theme.LocalQubaColors
import de.openindex.zugferd.manager.theme.LocalQubaTypography

// ─────────────────────────────────────────────────────────────────────────────
// QubaTextField — 30h compact input, label-above, no Material chrome
// Spec: 30h, Surface bg, 1px Border, r6, shadowXs.
//       Hover → BorderStrong. Focus → Accent border + ringFocus.
//       Error → Danger + ringError.
// ─────────────────────────────────────────────────────────────────────────────

enum class QubaTextFieldSize(val height: Dp) {
    Compact(26.dp),
    Default(30.dp),
    Large(34.dp),
}

@Composable
fun QubaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    size: QubaTextFieldSize = QubaTextFieldSize.Default,
    requiredIndicator: Boolean = false,
) {
    val colors = LocalQubaColors.current
    val typo = LocalQubaTypography.current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()

    val borderColor = when {
        isError && isFocused -> colors.danger
        isError              -> colors.danger
        isFocused            -> colors.accent
        isHovered            -> colors.borderStrong
        else                 -> colors.border
    }

    val bgColor = colors.surface

    Column(modifier = modifier) {
        if (label != null) {
            QubaFieldLabel(
                text = if (requiredIndicator) "$label *" else label,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }

        val shape = RoundedCornerShape(6.dp)

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            readOnly = readOnly,
            singleLine = singleLine,
            textStyle = typo.body.copy(color = colors.text),
            cursorBrush = SolidColor(colors.accent),
            interactionSource = interactionSource,
            decorationBox = { innerTextField ->
                Row(
                    verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top,
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (singleLine) Modifier.height(size.height) else Modifier)
                        .clip(shape)
                        .background(bgColor)
                        .border(
                            width = if (isFocused) 1.5.dp else 1.dp,
                            color = borderColor,
                            shape = shape,
                        )
                        .padding(
                            horizontal = if (leadingIcon != null) 6.dp else 10.dp,
                            vertical = if (singleLine) 0.dp else 8.dp,
                        ),
                ) {
                    if (leadingIcon != null) {
                        leadingIcon()
                    }
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = if (singleLine) Alignment.CenterStart else Alignment.TopStart,
                    ) {
                        if (value.isEmpty() && placeholder != null) {
                            Text(
                                text = placeholder,
                                style = typo.body.copy(color = colors.text3),
                            )
                        }
                        innerTextField()
                    }
                    if (trailingIcon != null) {
                        trailingIcon()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Field label — 11/500, text3, above the input
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun QubaFieldLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    val colors = LocalQubaColors.current
    val typo = LocalQubaTypography.current
    Text(
        text = text,
        style = typo.label.copy(color = colors.text3),
        softWrap = false,
        modifier = modifier,
    )
}
