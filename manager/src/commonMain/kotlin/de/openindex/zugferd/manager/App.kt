/*
 * Copyright (c) 2024-2025 Andreas Rudolph <andy@openindex.de>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.openindex.zugferd.manager

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import de.openindex.zugferd.manager.sections.CheckSectionState
import de.openindex.zugferd.manager.sections.VisualsSectionState
import de.openindex.zugferd.manager.theme.QubaTheme
import de.openindex.zugferd.manager.utils.DEFAULT_LANGUAGE
import de.openindex.zugferd.manager.utils.ShutdownHandler
import de.openindex.zugferd.manager.utils.getShutdownHandler
import org.jetbrains.compose.ui.tooling.preview.Preview

const val APP_NAME: String = AppInfo.Project.NAME
const val APP_VERSION: String = AppInfo.Project.VERSION
const val APP_VENDOR: String = AppInfo.Custom.VENDOR

val APP_TITLE: String = APP_NAME.replace('-', ' ').trim()
val APP_TITLE_FULL: String = "$APP_VENDOR $APP_TITLE"
val APP_VERSION_SHORT: String = APP_VERSION.substringBefore('-').trim()

val LocalLanguage = staticCompositionLocalOf { DEFAULT_LANGUAGE }
val LocalShutdownHandler = staticCompositionLocalOf<ShutdownHandler?> { null }

@Composable
@Preview
fun App() = QubaTheme {

    val shutdownHandler = getShutdownHandler()
    val language = LocalAppState.current.preferences.language

    // Open invoices were rendered in the previous language; render them again so the
    // switch takes effect without restarting the app. Nothing to do on the first run.
    var renderedLanguage by remember { mutableStateOf(language) }
    LaunchedEffect(language) {
        if (language != renderedLanguage) {
            renderedLanguage = language
            (AppSection.VISUALISATION.state as? VisualsSectionState)?.refreshHtml()
            (AppSection.CHECK.state as? CheckSectionState)?.refreshHtml()
        }
    }

    CompositionLocalProvider(LocalShutdownHandler provides shutdownHandler) {
        CompositionLocalProvider(LocalLanguage provides language) {
            AppLayout()
        }
    }
}
