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

import androidx.compose.runtime.mutableStateOf
import de.openindex.zugferd.manager.model.TradeParty
import io.github.vinceglb.filekit.core.PlatformFile

class Recipients(data: List<TradeParty>) {
    private val _recipients = mutableStateOf(data)
    val recipients get() = _recipients.value.sortedBy { it.name.lowercase() }

    private val nextKey: UInt
        get() = if (_recipients.value.isNotEmpty()) {
            _recipients.value.maxOf { it._key ?: 0.toUInt() } + 1.toUInt()
        } else {
            1.toUInt()
        }

    fun put(recipient: TradeParty) {
        _recipients.value = if (recipient._key == null) {
            _recipients.value
                .plus(recipient.copy(_key = nextKey))
        } else {
            _recipients.value
                .filter { it._key != recipient._key }
                .plus(recipient)
        }
    }

    fun remove(recipient: TradeParty) {
        remove(recipient._key ?: 0.toUInt())
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun remove(key: UInt) {
        _recipients.value = _recipients.value
            .filter { it._key != key }
    }

    fun removeAll() {
        _recipients.value = listOf()
    }

    suspend fun save() {
        saveRecipientsData(
            data = recipients,
        )
    }

    suspend fun export(targetFile: PlatformFile) {
        saveRecipientsData(
            data = recipients.map { it.copy(_key = null) },
            targetFile = targetFile,
        )
    }

    suspend fun import(sourceFile: PlatformFile) {
        loadRecipientsData(
            sourceFile = sourceFile,
        )
            .map { it.copy(_key = null) }
            .forEach { import(it) }

        save()
    }

    @Suppress("DuplicatedCode")
    private fun import(recipient: TradeParty) {
        val id = recipient.id?.trimToNull()
        if (id == null) {
            put(recipient)
            return
        }

        val existingRecipient = _recipients.value
            .firstOrNull { it.id == recipient.id }

        val updatedRecipient = if (existingRecipient != null) {
            recipient.copy(_key = existingRecipient._key)
        } else {
            recipient.copy(_key = nextKey)
        }

        _recipients.value = _recipients.value
            .filter { it.id != updatedRecipient.id }
            .plus(updatedRecipient)
    }
}

suspend fun loadRecipients(): Recipients =
    Recipients(loadRecipientsData())

expect suspend fun loadRecipientsData(sourceFile: PlatformFile? = null): List<TradeParty>

expect suspend fun saveRecipientsData(data: List<TradeParty>, targetFile: PlatformFile? = null)
