/*
 * Copyright (c) 2024-2025 Andreas Rudolph <andy@openindex.de>.
 * Licensed under the Apache License, Version 2.0.
 */

package de.openindex.zugferd.manager.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import de.openindex.zugferd.manager.LocalAppState

// ─────────────────────────────────────────────────────────────────────────────
// Material3 bridge — maps Quba tokens → M3 slots so existing M3 composables
// (dialogs, menus, etc.) continue to look correct.
// ─────────────────────────────────────────────────────────────────────────────

private fun qubaToM3Light(c: QubaColorScheme) = lightColorScheme(
    primary             = c.accent,
    onPrimary           = c.surface,
    primaryContainer    = c.accentSoft,
    onPrimaryContainer  = c.accentPressed,
    secondary           = c.text2,
    onSecondary         = c.surface,
    secondaryContainer  = c.surface3,
    onSecondaryContainer= c.text,
    tertiary            = c.info,
    onTertiary          = c.surface,
    tertiaryContainer   = c.infoSoft,
    onTertiaryContainer = c.info,
    error               = c.danger,
    onError             = c.surface,
    errorContainer      = c.dangerSoft,
    onErrorContainer    = c.danger,
    background          = c.bg,
    onBackground        = c.text,
    surface             = c.surface,
    onSurface           = c.text,
    surfaceVariant      = c.surface2,
    onSurfaceVariant    = c.text2,
    outline             = c.border,
    outlineVariant      = c.borderSubtle,
    scrim               = c.scrim,
    inverseSurface      = c.text,
    inverseOnSurface    = c.surface,
    inversePrimary      = c.accentSoft,
    surfaceContainerLowest  = c.surface,
    surfaceContainerLow     = c.bg,
    surfaceContainer        = c.surface2,
    surfaceContainerHigh    = c.surface3,
    surfaceContainerHighest = c.border,
)

private fun qubaToM3Dark(c: QubaColorScheme) = darkColorScheme(
    primary             = c.accent,
    onPrimary           = c.surface,
    primaryContainer    = c.accentSoft,
    onPrimaryContainer  = c.accentSoft2,
    secondary           = c.text2,
    onSecondary         = c.surface,
    secondaryContainer  = c.surface3,
    onSecondaryContainer= c.text,
    tertiary            = c.info,
    onTertiary          = c.surface,
    tertiaryContainer   = c.infoSoft,
    onTertiaryContainer = c.info,
    error               = c.danger,
    onError             = c.surface,
    errorContainer      = c.dangerSoft,
    onErrorContainer    = c.danger,
    background          = c.bg,
    onBackground        = c.text,
    surface             = c.surface,
    onSurface           = c.text,
    surfaceVariant      = c.surface2,
    onSurfaceVariant    = c.text2,
    outline             = c.border,
    outlineVariant      = c.borderSubtle,
    scrim               = c.scrim,
    inverseSurface      = c.text,
    inverseOnSurface    = c.surface,
    inversePrimary      = c.accentSoft,
    surfaceContainerLowest  = c.surface,
    surfaceContainerLow     = c.bg,
    surfaceContainer        = c.surface2,
    surfaceContainerHigh    = c.surface3,
    surfaceContainerHighest = c.border,
)

// ─────────────────────────────────────────────────────────────────────────────
// QubaTheme — app root theme wrapper
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun QubaTheme(
    content: @Composable () -> Unit,
) {
    val preferences = LocalAppState.current.preferences
    val isDark = preferences.darkMode ?: isSystemInDarkTheme()

    val colors = if (isDark) QubaDarkColors else QubaLightColors
    val geist = geistFontFamily()
    val geistMono = geistMonoFontFamily()
    val typography = buildQubaTypography(geist, geistMono)

    // Build M3 typography that mirrors Quba tokens so M3 components look right
    val m3Typography = buildQubaM3Typography(geist, geistMono)
    val m3Colors = if (isDark) qubaToM3Dark(colors) else qubaToM3Light(colors)

    CompositionLocalProvider(
        LocalQubaColors provides colors,
        LocalQubaTypography provides typography,
    ) {
        MaterialTheme(
            colorScheme = m3Colors,
            typography = m3Typography,
            content = content,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// M3 typography built from Geist + Quba scale (used by M3 dialogs, menus, etc.)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun buildQubaM3Typography(
    geist: androidx.compose.ui.text.font.FontFamily,
    geistMono: androidx.compose.ui.text.font.FontFamily,
): androidx.compose.material3.Typography {
    val t = buildQubaTypography(geist, geistMono)
    return androidx.compose.material3.Typography(
        displayLarge    = t.display,
        displayMedium   = t.display,
        displaySmall    = t.display,
        headlineLarge   = t.h1,
        headlineMedium  = t.h1,
        headlineSmall   = t.h2,
        titleLarge      = t.h2,
        titleMedium     = t.bodyMed,
        titleSmall      = t.label,
        bodyLarge       = t.body,
        bodyMedium      = t.body,
        bodySmall       = t.small,
        labelLarge      = t.label,
        labelMedium     = t.label,
        labelSmall      = t.caption,
    )
}
