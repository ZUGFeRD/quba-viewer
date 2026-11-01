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

import io.github.vinceglb.filekit.core.PlatformDirectory
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.encodeToStream
import java.net.URI
import kotlin.io.path.toPath

actual val PlatformFile.directory: PlatformDirectory?
    get() = if (file.parentFile != null) PlatformDirectory(file.parentFile) else null

@OptIn(ExperimentalSerializationApi::class)
actual suspend inline fun <reified T> PlatformFile.writeJson(data: T) {
    this.file.outputStream().use { output ->
        JSON_EXPORT.encodeToStream(data, output)
    }
}

actual fun getPlatformFileFromURI(uri: String): PlatformFile =
    PlatformFile(URI.create(uri).toPath().toFile())
