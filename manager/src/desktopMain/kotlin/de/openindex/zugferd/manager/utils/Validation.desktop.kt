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
import de.openindex.zugferd.manager.model.ValidationSeverity
import de.openindex.zugferd.manager.model.ValidationType
import io.github.vinceglb.filekit.core.PlatformFile
import org.mustangproject.validator.EPart
import org.mustangproject.validator.ESeverity
import org.mustangproject.validator.ValidationContext
import org.mustangproject.validator.ValidationResultItem
import org.mustangproject.validator.ZUGFeRDValidator
import java.io.InputStream
import de.openindex.zugferd.manager.utils.trimToNull

private class CustomValidator : ZUGFeRDValidator() {
    val cachedResultItems: MutableList<ValidationResultItem> = mutableListOf()
    var cachedSignature: String? = null

    init {
        context = object : ValidationContext(APP_LOGGER) {
            override fun addResultItem(vr: ValidationResultItem?) {
                super.addResultItem(vr)
                if (vr != null) {
                    cachedResultItems.add(vr)
                }
            }

            override fun setSignature(signature: String?): ValidationContext {
                if (cachedSignature == null && signature != null) {
                    cachedSignature = signature
                }
                return super.setSignature(signature)
            }
        }
    }

    fun getValidation(inputStream: InputStream, fileNameOfInputStream: String): Validation {
        validate(inputStream, fileNameOfInputStream)

        return Validation(
            isValid = wasCompletelyValid,
            signature = cachedSignature,
            profile = context.profile,
            version = context.generation,
            messages = cachedResultItems.map { getMessage(it) },
        )
    }

    private fun getMessage(item: ValidationResultItem): ValidationMessage {
        return ValidationMessage(
            message = item.message,
            severity = when (item.severity) {
                ESeverity.notice -> ValidationSeverity.NOTICE
                ESeverity.warning -> ValidationSeverity.WARNING
                ESeverity.error -> ValidationSeverity.ERROR
                else -> ValidationSeverity.FATAL
            },
            type = when (item.part) {
                EPart.pdf -> ValidationType.PDF
                EPart.fx, EPart.ox -> ValidationType.XML
                else -> ValidationType.OTHER
            },
            location = item.location?.trimToNull(),
        )
    }
}

actual suspend fun validatePdf(pdf: PlatformFile): Validation {
    return pdf.file
        .inputStream().use { input ->
            CustomValidator()
                .getValidation(input, pdf.name)
        }
}

actual suspend fun getValidationXmlReport(pdf: PlatformFile): String? {
    return try {
        pdf.file.inputStream().use { input ->
            ZUGFeRDValidator().validate(input, pdf.name)
        }
    } catch (e: Exception) {
        APP_LOGGER.error("Fehler beim Erstellen des Validierungsberichts.", e)
        null
    }
}
