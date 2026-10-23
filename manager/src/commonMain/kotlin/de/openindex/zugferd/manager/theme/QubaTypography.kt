/*
 * Copyright (c) 2024-2025 Andreas Rudolph <andy@openindex.de>.
 * Licensed under the Apache License, Version 2.0.
 */

package de.openindex.zugferd.manager.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import de.openindex.zugferd.quba.generated.resources.Res
import de.openindex.zugferd.quba.generated.resources.geist_bold
import de.openindex.zugferd.quba.generated.resources.geist_light
import de.openindex.zugferd.quba.generated.resources.geist_medium
import de.openindex.zugferd.quba.generated.resources.geist_mono_medium
import de.openindex.zugferd.quba.generated.resources.geist_mono_regular
import de.openindex.zugferd.quba.generated.resources.geist_regular
import de.openindex.zugferd.quba.generated.resources.geist_semibold
import org.jetbrains.compose.resources.Font

// ─────────────────────────────────────────────────────────────────────────────
// Font families
// Replace the placeholder TTF files in composeResources/font/ with actual
// Geist files from https://github.com/vercel/geist-font when available.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun geistFontFamily(): FontFamily = FontFamily(
    Font(Res.font.geist_light,    weight = FontWeight.Light),
    Font(Res.font.geist_regular,  weight = FontWeight.Normal),
    Font(Res.font.geist_medium,   weight = FontWeight.Medium),
    Font(Res.font.geist_semibold, weight = FontWeight.SemiBold),
    Font(Res.font.geist_bold,     weight = FontWeight.Bold),
)

@Composable
fun geistMonoFontFamily(): FontFamily = FontFamily(
    Font(Res.font.geist_mono_regular, weight = FontWeight.Normal),
    Font(Res.font.geist_mono_medium,  weight = FontWeight.Medium),
)

// ─────────────────────────────────────────────────────────────────────────────
// Typography tokens
// All values from the Quba design spec
// ─────────────────────────────────────────────────────────────────────────────

@Immutable
data class QubaTypography(
    val geist: FontFamily,
    val geistMono: FontFamily,

    // display — 22/600, lh 1.15, ls -0.02em
    val display: TextStyle,

    // h1 — 18/600, lh 1.2, ls -0.015em
    val h1: TextStyle,

    // h2 — 15/600, lh 1.25, ls -0.01em
    val h2: TextStyle,

    // body — 12.5/400, lh 1.45
    val body: TextStyle,

    // bodyMed — 12.5/500, lh 1.45
    val bodyMed: TextStyle,

    // small — 11.5/400, lh 1.4
    val small: TextStyle,

    // label — 11/500, lh 1.4, ls -0.005em
    val label: TextStyle,

    // caption — 10.5/500 UPPERCASE, lh 1.3, ls 0.06em
    val caption: TextStyle,

    // mono — 11.5 Geist Mono/500, lh 1.5
    val mono: TextStyle,

    // kbd — 10.5 Geist Mono/500, lh 1
    val kbd: TextStyle,
)

@Composable
fun buildQubaTypography(
    geist: FontFamily,
    geistMono: FontFamily,
): QubaTypography = QubaTypography(
    geist = geist,
    geistMono = geistMono,

    display = TextStyle(
        fontFamily = geist,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = (22 * 1.15).sp,
        letterSpacing = (-0.44).sp,  // -0.02em of 22
    ),
    h1 = TextStyle(
        fontFamily = geist,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = (18 * 1.2).sp,
        letterSpacing = (-0.27).sp,  // -0.015em of 18
    ),
    h2 = TextStyle(
        fontFamily = geist,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = (15 * 1.25).sp,
        letterSpacing = (-0.15).sp,  // -0.01em of 15
    ),
    body = TextStyle(
        fontFamily = geist,
        fontWeight = FontWeight.Normal,
        fontSize = 12.5.sp,
        lineHeight = (12.5 * 1.45).sp,
        letterSpacing = 0.sp,
    ),
    bodyMed = TextStyle(
        fontFamily = geist,
        fontWeight = FontWeight.Medium,
        fontSize = 12.5.sp,
        lineHeight = (12.5 * 1.45).sp,
        letterSpacing = 0.sp,
    ),
    small = TextStyle(
        fontFamily = geist,
        fontWeight = FontWeight.Normal,
        fontSize = 11.5.sp,
        lineHeight = (11.5 * 1.4).sp,
        letterSpacing = 0.sp,
    ),
    label = TextStyle(
        fontFamily = geist,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = (11 * 1.4).sp,
        letterSpacing = (-0.055).sp,  // -0.005em of 11
    ),
    caption = TextStyle(
        fontFamily = geist,
        fontWeight = FontWeight.Medium,
        fontSize = 10.5.sp,
        lineHeight = (10.5 * 1.3).sp,
        letterSpacing = 0.63.sp,  // 0.06em of 10.5
    ),
    mono = TextStyle(
        fontFamily = geistMono,
        fontWeight = FontWeight.Medium,
        fontSize = 11.5.sp,
        lineHeight = (11.5 * 1.5).sp,
        letterSpacing = 0.sp,
    ),
    kbd = TextStyle(
        fontFamily = geistMono,
        fontWeight = FontWeight.Medium,
        fontSize = 10.5.sp,
        lineHeight = 10.5.sp,
        letterSpacing = 0.sp,
    ),
)

// ─────────────────────────────────────────────────────────────────────────────
// CompositionLocal — lazy default (overridden in QubaTheme)
// ─────────────────────────────────────────────────────────────────────────────

val LocalQubaTypography = staticCompositionLocalOf<QubaTypography> {
    error("No QubaTypography provided — wrap your UI with QubaTheme { ... }")
}
