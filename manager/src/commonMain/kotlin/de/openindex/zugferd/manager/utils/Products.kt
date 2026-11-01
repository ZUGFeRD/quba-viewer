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
import de.openindex.zugferd.manager.model.Product
import io.github.vinceglb.filekit.core.PlatformFile

class Products(data: List<Product>) {
    private val _products = mutableStateOf(data)
    val products get() = _products.value.sortedBy { it.name.lowercase() }

    private val nextKey: UInt
        get() = if (_products.value.isNotEmpty()) {
            _products.value.maxOf { it._key ?: 0.toUInt() } + 1.toUInt()
        } else {
            1.toUInt()
        }

    fun put(product: Product) {
        _products.value = if (product._key == null) {
            _products.value
                .plus(product.copy(_key = nextKey))
        } else {
            _products.value
                .filter { it._key != product._key }
                .plus(product)
        }
    }

    fun remove(product: Product) {
        remove(product._key ?: 0.toUInt())
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun remove(key: UInt) {
        _products.value = _products.value
            .filter { it._key != key }
    }

    fun removeAll() {
        _products.value = listOf()
    }

    suspend fun save() {
        saveProductsData(
            data = products,
        )
    }

    suspend fun export(targetFile: PlatformFile) {
        saveProductsData(
            data = products.map { it.copy(_key = null) },
            targetFile = targetFile,
        )
    }

    suspend fun import(sourceFile: PlatformFile) {
        loadProductsData(
            sourceFile = sourceFile,
        )
            .map { it.copy(_key = null) }
            .forEach { import(it) }

        save()
    }

    private fun import(product: Product) {
        put(product)
    }
}

suspend fun loadProducts(): Products =
    Products(loadProductsData())

expect suspend fun loadProductsData(sourceFile: PlatformFile? = null): List<Product>

expect suspend fun saveProductsData(data: List<Product>, targetFile: PlatformFile? = null)
