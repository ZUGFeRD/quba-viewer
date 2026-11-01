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

package de.openindex.zugferd.manager.utils

import de.openindex.zugferd.manager.APP_LOGGER
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import org.apache.commons.lang3.SystemUtils
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteIfExists
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.isRegularFile
import kotlin.io.path.outputStream

private val PREFERENCES_FILE: Path by lazy {
    APP_WORK_DIR.resolve("preferences.json")
}

@OptIn(ExperimentalPathApi::class, ExperimentalSerializationApi::class)
actual suspend fun loadPreferencesData(): PreferencesData {
    return withContext(Dispatchers.IO) {
        if (!PREFERENCES_FILE.exists()) {
            return@withContext PreferencesData()
        }

        if (!PREFERENCES_FILE.isRegularFile()) {
            APP_LOGGER.warn("Settings are invalid.")
            PREFERENCES_FILE.deleteRecursively()
            return@withContext PreferencesData()
        }

        try {
            PREFERENCES_FILE
                .inputStream()
                .use { JSON_IMPORT.decodeFromStream(it) }

        } catch (e: Exception) {
            APP_LOGGER.warn("Settings are not readable.", e)
            PREFERENCES_FILE.deleteIfExists()
            PreferencesData()
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
actual suspend fun savePreferencesData(data: PreferencesData) {
    withContext(Dispatchers.IO) {
        try {
            PREFERENCES_FILE
                .outputStream()
                .use { JSON_EXPORT.encodeToStream(data, it) }
        } catch (e: Exception) {
            APP_LOGGER.warn("Settings are not writable.", e)
            PREFERENCES_FILE.deleteIfExists()
        }
    }
}

actual suspend fun initPreferences(preferences: Preferences) {
    if (SystemUtils.IS_OS_MAC) {
        if (preferences.isThemeDark) {
            System.setProperty("apple.awt.application.appearance", "NSAppearanceNameDarkAqua")
        } else if (preferences.isThemeAuto) {
            System.setProperty("apple.awt.application.appearance", "system")
        }
    }
}