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

package de.openindex.zugferd.manager

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
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

    CompositionLocalProvider(LocalShutdownHandler provides shutdownHandler) {
        CompositionLocalProvider(LocalLanguage provides language) {
            AppLayout()
        }
    }
}
