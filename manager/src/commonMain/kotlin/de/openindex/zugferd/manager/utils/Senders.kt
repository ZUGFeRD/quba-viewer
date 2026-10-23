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

class Senders(data: List<TradeParty>) {
    private val _senders = mutableStateOf(data)
    val senders get() = _senders.value.sortedBy { it.name.lowercase() }

    private val nextKey: UInt
        get() = if (_senders.value.isNotEmpty()) {
            _senders.value.maxOf { it._key ?: 0.toUInt() } + 1.toUInt()
        } else {
            1.toUInt()
        }

    fun put(sender: TradeParty, preferences: Preferences? = null) {
        _senders.value = if (sender._key == null) {
            val newSender = sender.copy(_key = nextKey)
            preferences?.setPreviousSenderKey(newSender._key)

            _senders.value
                .plus(newSender)
        } else {
            preferences?.setPreviousSenderKey(sender._key)

            _senders.value
                .filter { it._key != sender._key }
                .plus(sender)
        }
    }

    fun remove(sender: TradeParty) {
        remove(sender._key ?: 0.toUInt())
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun remove(key: UInt) {
        _senders.value = _senders.value
            .filter { it._key != key }
    }

    fun removeAll() {
        _senders.value = listOf()
    }

    suspend fun save() {
        saveSendersData(
            data = senders,
        )
    }

    suspend fun export(targetFile: PlatformFile) {
        saveSendersData(
            data = senders.map { it.copy(_key = null) },
            targetFile = targetFile,
        )
    }

    suspend fun import(sourceFile: PlatformFile) {
        loadSendersData(
            sourceFile = sourceFile,
        )
            .map { it.copy(_key = null) }
            .forEach { import(it) }

        save()
    }

    @Suppress("DuplicatedCode")
    private fun import(sender: TradeParty) {
        val id = sender.id?.trimToNull()
        if (id == null) {
            put(sender)
            return
        }

        val existingSender = _senders.value
            .firstOrNull { it.id == sender.id }

        val updatedSender = if (existingSender != null) {
            sender.copy(_key = existingSender._key)
        } else {
            sender.copy(_key = nextKey)
        }

        _senders.value = _senders.value
            .filter { it.id != updatedSender.id }
            .plus(updatedSender)
    }
}

suspend fun loadSenders(): Senders =
    Senders(loadSendersData())

expect suspend fun loadSendersData(sourceFile: PlatformFile? = null): List<TradeParty>

expect suspend fun saveSendersData(data: List<TradeParty>, targetFile: PlatformFile? = null)
