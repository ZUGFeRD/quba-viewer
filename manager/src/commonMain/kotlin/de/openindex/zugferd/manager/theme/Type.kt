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

package de.openindex.zugferd.manager.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import de.openindex.zugferd.quba.generated.resources.Res
import de.openindex.zugferd.quba.generated.resources.inter_bold
import de.openindex.zugferd.quba.generated.resources.inter_medium
import de.openindex.zugferd.quba.generated.resources.inter_regular
import de.openindex.zugferd.quba.generated.resources.inter_semibold
import org.jetbrains.compose.resources.Font

/**
 * Inter font family — loaded from composeResources/font/.
 * Inter is the standard SaaS/fintech UI font (Linear, Stripe, Figma, Vercel).
 * Must be called inside a @Composable context.
 */
@Composable
fun interFontFamily(): FontFamily = FontFamily(
    Font(Res.font.inter_regular, weight = FontWeight.Normal),
    Font(Res.font.inter_medium, weight = FontWeight.Medium),
    Font(Res.font.inter_semibold, weight = FontWeight.SemiBold),
    Font(Res.font.inter_bold, weight = FontWeight.Bold),
)

/**
 * Builds the app typography using the provided font family.
 * Call once at theme root (AppTheme) and pass to MaterialTheme.
 *
 * Typography scale (modular, 4pt baseline grid):
 *
 * HEADLINE  — page / section titles
 *   headlineLarge  : 30sp SemiBold  -0.5sp   36sp line
 *   headlineMedium : 24sp SemiBold  -0.25sp  32sp line  ← SectionTitle
 *   headlineSmall  : 20sp SemiBold   0sp     28sp line
 *
 * TITLE  — card headers, subsections
 *   titleLarge     : 18sp SemiBold   0sp     26sp line  ← SectionSubTitle, card titles
 *   titleMedium    : 15sp Medium     0.15sp  22sp line  ← dialog titles, tab headers
 *   titleSmall     : 13sp Medium     0.1sp   20sp line  ← tab labels, minor headers
 *
 * BODY  — content text, form values
 *   bodyLarge      : 15sp Regular    0sp     22sp line  ← primary body, input values
 *   bodyMedium     : 13sp Regular    0.25sp  20sp line  ← secondary body, SectionInfo
 *   bodySmall      : 12sp Regular    0.4sp   16sp line  ← captions, helper text
 *
 * LABEL  — form labels, buttons, badges
 *   labelLarge     : 13sp Medium     0.1sp   18sp line  ← form field labels, buttons
 *   labelMedium    : 11sp Medium     0.5sp   16sp line  ← tooltips, nav item labels
 *   labelSmall     : 10sp Medium     0.5sp   14sp line  ← badges, status chips, version
 */
fun buildAppTypography(inter: FontFamily) = Typography(
    // Headline — page/section titles
    headlineLarge = TextStyle(
        fontFamily = inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 30.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.5).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.25).sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
    ),

    // Title — card headers, subsections
    titleLarge = TextStyle(
        fontFamily = inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = inter,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.15.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = inter,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),

    // Body — content and form values
    bodyLarge = TextStyle(
        fontFamily = inter,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = inter,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = inter,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
    ),

    // Label — form labels, buttons, captions
    labelLarge = TextStyle(
        fontFamily = inter,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = inter,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = inter,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp,
    ),
)
