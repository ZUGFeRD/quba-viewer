/*
 * Copyright (c) 2024-2025 Andreas Rudolph <andy@openindex.de>.
 * Licensed under the Apache License, Version 2.0.
 */

package de.openindex.zugferd.manager.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────────────────────
// Quba Design System — Color Tokens
// Source: Quba Redesign.html prototype
// ─────────────────────────────────────────────────────────────────────────────

@Immutable
data class QubaColorScheme(
    // Surfaces
    val bg: Color,
    val surface: Color,
    val surface2: Color,
    val surface3: Color,
    val surfaceInset: Color,

    // Borders
    val border: Color,
    val borderStrong: Color,
    val borderSubtle: Color,

    // Text
    val text: Color,
    val text2: Color,
    val text3: Color,
    val text4: Color,

    // Brand accent (indigo)
    val accent: Color,
    val accentHover: Color,
    val accentPressed: Color,
    val accentSoft: Color,
    val accentSoft2: Color,

    // Brand yellow
    val qubaYellow: Color,
    val qubaYellowSoft: Color,

    // Semantic — success
    val success: Color,
    val successSoft: Color,

    // Semantic — warning
    val warn: Color,
    val warnSoft: Color,

    // Semantic — danger / error
    val danger: Color,
    val dangerSoft: Color,

    // Semantic — info
    val info: Color,
    val infoSoft: Color,

    // Overlay
    val scrim: Color,
)

// ─────────────────────────────────────────────────────────────────────────────
// Light palette
// ─────────────────────────────────────────────────────────────────────────────

val QubaLightColors = QubaColorScheme(
    // Surfaces
    bg              = Color(0xFFFBFBFC),
    surface         = Color(0xFFFFFFFF),
    surface2        = Color(0xFFF7F7F9),
    surface3        = Color(0xFFF1F1F4),
    surfaceInset    = Color(0xFFFAFAFC),

    // Borders
    border          = Color(0xFFE8E8EE),
    borderStrong    = Color(0xFFD6D6DC),
    borderSubtle    = Color(0xFFF0F0F4),

    // Text
    text            = Color(0xFF0A0A0F),
    text2           = Color(0xFF4A4A52),
    text3           = Color(0xFF7A7A82),
    text4           = Color(0xFFA0A0A8),

    // Brand accent
    accent          = Color(0xFF4F46E5),
    accentHover     = Color(0xFF4338CA),
    accentPressed   = Color(0xFF3730A3),
    accentSoft      = Color(0xFFEEF0FE),
    accentSoft2     = Color(0xFFE0E3FC),

    // Brand yellow
    qubaYellow      = Color(0xFFFFB300),
    qubaYellowSoft  = Color(0xFFFFF6D9),

    // Semantic
    success         = Color(0xFF059669),
    successSoft     = Color(0xFFE7F7EF),
    warn            = Color(0xFFD97706),
    warnSoft        = Color(0xFFFEF3E0),
    danger          = Color(0xFFDC2626),
    dangerSoft      = Color(0xFFFEEAEA),
    info            = Color(0xFF0284C7),
    infoSoft        = Color(0xFFE0F2FE),

    scrim           = Color(0x66000000), // rgba(15,18,30,0.4) ≈ 40%
)

// ─────────────────────────────────────────────────────────────────────────────
// Dark palette
// ─────────────────────────────────────────────────────────────────────────────

val QubaDarkColors = QubaColorScheme(
    // Surfaces
    bg              = Color(0xFF0A0A0F),
    surface         = Color(0xFF141418),
    surface2        = Color(0xFF1A1A20),
    surface3        = Color(0xFF222228),
    surfaceInset    = Color(0xFF111116),

    // Borders
    border          = Color(0xFF2A2A32),
    borderStrong    = Color(0xFF38383E),
    borderSubtle    = Color(0xFF1E1E24),

    // Text
    text            = Color(0xFFF1F1F4),
    text2           = Color(0xFFAAAAAE),
    text3           = Color(0xFF727278),
    text4           = Color(0xFF4A4A52),

    // Brand accent
    accent          = Color(0xFF818CF8),
    accentHover     = Color(0xFF6366F1),
    accentPressed   = Color(0xFF4F46E5),
    accentSoft      = Color(0xFF1E1D4C),
    accentSoft2     = Color(0xFF2A2959),

    // Brand yellow
    qubaYellow      = Color(0xFFFFD54F),
    qubaYellowSoft  = Color(0xFF2A2510),

    // Semantic
    success         = Color(0xFF34D399),
    successSoft     = Color(0xFF0A2E20),
    warn            = Color(0xFFFBBF24),
    warnSoft        = Color(0xFF2E2008),
    danger          = Color(0xFFF87171),
    dangerSoft      = Color(0xFF300A0A),
    info            = Color(0xFF38BDF8),
    infoSoft        = Color(0xFF08243A),

    scrim           = Color(0x80000000),
)

// ─────────────────────────────────────────────────────────────────────────────
// CompositionLocal
// ─────────────────────────────────────────────────────────────────────────────

val LocalQubaColors = staticCompositionLocalOf { QubaLightColors }
