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

import de.openindex.zugferd.manager.APP_NAME
import de.openindex.zugferd.manager.APP_VENDOR
import net.harawata.appdirs.AppDirsFactory
import org.apache.commons.lang3.SystemUtils
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.pathString

val APP_LAUNCHER: Path? by lazy {
    @Suppress("SpellCheckingInspection")
    System.getProperty("jpackage.app-path")?.let { Path(it) }
}

val APP_WORK_DIR: Path by lazy {
    Path(
        AppDirsFactory
            .getInstance()
            .getUserDataDir(APP_NAME, null, APP_VENDOR)
    ).createDirectories()
}

val CACHE_DIR: Path by lazy {
    APP_WORK_DIR
        .resolve("cache")
        .createDirectories()
}

val DATA_DIR: Path by lazy {
    APP_WORK_DIR
        .resolve("data")
        .createDirectories()
}

val LOGS_DIR: Path by lazy {
    APP_WORK_DIR
        .resolve("logs")
        .createDirectories()
}

val BACKUPS_DIR: Path by lazy {
    APP_WORK_DIR
        .resolve("backups")
        .createDirectories()
}

val RESOURCES_DIR: Path by lazy {
    //
    // Path to packaged resources.
    // https://github.com/JetBrains/compose-multiplatform/blob/master/tutorials/Native_distributions_and_local_execution/README.md#packaging-resources
    //
    val path = System.getProperty("compose.application.resources.dir")
        ?.trimToNull()
        ?: SystemUtils.JAVA_IO_TMPDIR

    Path.of(path)
        .createDirectories()
}


private class JvmPlatform(
    override val name: String = "Java ${SystemUtils.JAVA_VERSION}",
    override val type: PlatformType = PlatformType.DESKTOP,
) : Platform {

    override val os: OsType
        get() = if (SystemUtils.IS_OS_WINDOWS) OsType.WINDOWS
        else if (SystemUtils.IS_OS_MAC) OsType.MAC
        else OsType.LINUX

    override val isRunningFromInstallation: Boolean
        get() {
            return if (SystemUtils.IS_OS_MAC) {
                APP_LAUNCHER?.parent?.pathString?.endsWith("/Contents/MacOS") == true
            } else if (SystemUtils.IS_OS_LINUX) {
                APP_LAUNCHER?.parent?.pathString?.endsWith("/bin") == true
            } else {
                false
            }
        }
}


actual fun getPlatform(): Platform = JvmPlatform()
