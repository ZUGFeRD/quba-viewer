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
import de.openindex.zugferd.manager.model.TradeParty
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.copyTo
import kotlin.io.path.createParentDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.isRegularFile
import kotlin.io.path.moveTo
import kotlin.io.path.outputStream

private val SENDERS_FILE: Path by lazy {
    DATA_DIR.resolve("senders.json")
}

@OptIn(ExperimentalPathApi::class, ExperimentalSerializationApi::class)
actual suspend fun loadSendersData(sourceFile: PlatformFile?): List<TradeParty> {
    return withContext(Dispatchers.IO) {
        val isDataFile = sourceFile == null
        val sendersFile = sourceFile?.file?.toPath() ?: SENDERS_FILE

        if (!sendersFile.exists()) {
            return@withContext listOf()
        }

        if (!sendersFile.isRegularFile()) {
            APP_LOGGER.warn("Senders are invalid.")
            if (isDataFile) {
                SENDERS_FILE.deleteRecursively()
            }
            return@withContext listOf()
        }

        try {
            sendersFile
                .inputStream()
                .use { JSON_IMPORT.decodeFromStream(it) }
        } catch (e: Exception) {
            APP_LOGGER.warn("Senders are not readable.", e)

            if (isDataFile) {
                val backupFile = BACKUPS_DIR
                    .resolve("unreadable")
                    .resolve("senders.${System.currentTimeMillis()}.json")
                    .createParentDirectories()

                SENDERS_FILE.moveTo(backupFile, true)
            }

            listOf()
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
actual suspend fun saveSendersData(data: List<TradeParty>, targetFile: PlatformFile?) {
    withContext(Dispatchers.IO) {
        val isDataFile = targetFile == null
        val sendersFile = targetFile?.file?.toPath() ?: SENDERS_FILE

        val tempFile = if (SENDERS_FILE.isRegularFile() && isDataFile) {
            SENDERS_FILE.copyTo(
                target = Files.createTempFile("senders-", ".json"),
                overwrite = true,
            )
        } else {
            null
        }

        try {
            sendersFile
                .outputStream()
                .use {
                    if (isDataFile)
                        JSON_EXPORT.encodeToStream(data, it)
                    else
                        JSON_EXPORT_WITHOUT_DEFAULTS.encodeToStream(data, it)
                }
        } catch (e: Exception) {
            APP_LOGGER.warn("Senders are not writable.", e)
            tempFile?.copyTo(SENDERS_FILE)
        } finally {
            tempFile?.deleteIfExists()
        }
    }
}
