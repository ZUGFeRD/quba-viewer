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

import de.openindex.zugferd.manager.model.ValidationSeverity
import de.openindex.zugferd.manager.model.ValidationType
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.serialization.Serializable

@Serializable
data class Validation(
    val isValid: Boolean,
    val signature: String?,
    val profile: String?,
    val version: String?,
    val messages: List<ValidationMessage>
) {
    val countNotices: Int
        get() = messages.count { it.severity == ValidationSeverity.NOTICE }

    val countWarnings: Int
        get() = messages.count { it.severity == ValidationSeverity.WARNING }

    val countErrors: Int
        get() = messages.count { it.severity == ValidationSeverity.ERROR || it.severity == ValidationSeverity.FATAL }
}

@Serializable
data class ValidationMessage(
    val message: String,
    val type: ValidationType,
    val severity: ValidationSeverity,
    val location: String? = null,
)

expect suspend fun validatePdf(pdf: PlatformFile): Validation

expect suspend fun getValidationXmlReport(pdf: PlatformFile): String?
