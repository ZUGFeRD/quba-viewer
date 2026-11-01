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

package de.openindex.zugferd.manager.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import de.openindex.zugferd.manager.utils.getString
import de.openindex.zugferd.quba.generated.resources.Res
import de.openindex.zugferd.quba.generated.resources.ValidationSeverity_ERROR
import de.openindex.zugferd.quba.generated.resources.ValidationSeverity_FATAL
import de.openindex.zugferd.quba.generated.resources.ValidationSeverity_NOTICE
import de.openindex.zugferd.quba.generated.resources.ValidationSeverity_WARNING
import org.jetbrains.compose.resources.StringResource

enum class ValidationSeverity(
    val title: StringResource,
    val icon: ImageVector,
    val lightModeColor: Color,
    val darkModeColor: Color,
) {
    NOTICE(
        title = Res.string.ValidationSeverity_NOTICE,
        icon = Icons.Default.Info,
        lightModeColor = Color(0xFF4F46E5),   // indigo
        darkModeColor = Color(0xFFA5B4FC),    // indigo-300
    ),
    WARNING(
        title = Res.string.ValidationSeverity_WARNING,
        icon = Icons.Default.Warning,
        lightModeColor = Color(0xFFD97706),   // amber-600
        darkModeColor = Color(0xFFFCD34D),    // amber-300
    ),
    ERROR(
        title = Res.string.ValidationSeverity_ERROR,
        icon = Icons.Default.Error,
        lightModeColor = Color(0xFFDC2626),   // red-600
        darkModeColor = Color(0xFFFCA5A5),    // red-300
    ),
    FATAL(
        title = Res.string.ValidationSeverity_FATAL,
        icon = Icons.Default.Error,
        lightModeColor = Color(0xFFB91C1C),   // red-700
        darkModeColor = Color(0xFFF87171),    // red-400
    ),

    ;

    @Suppress("unused")
    suspend fun translateTitle(): String = getString(title)
}