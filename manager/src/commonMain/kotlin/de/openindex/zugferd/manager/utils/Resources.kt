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

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.Resource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getPluralString as _getPluralString
import org.jetbrains.compose.resources.getString as _getString
import org.jetbrains.compose.resources.pluralStringResource as _pluralStringResource
import org.jetbrains.compose.resources.stringResource as _stringResource

private const val LINE_BREAK = "[br]"

@Composable
fun Resource.translate(pluralQuantity: Int = 1): String = when (this) {
    is PluralStringResource -> pluralStringResource(this, pluralQuantity)
    is StringResource -> stringResource(this)
    else -> throw IllegalArgumentException("Provided resource is not a translatable string! (${this::class.java.name})")
}

/**
 * Fix multiline string resources / translations.
 *
 * Handling of newlines in string resources / translations is currently pretty broken in Compose,
 * as described here https://github.com/JetBrains/compose-multiplatform/issues/4910#issue-2328543436
 *
 * This method removes leading and trailing spaces and concatenates the string resource / translation into one line.
 * Any \n is ignored. To enforce a line break, use [br] within the string resource / translation.
 */
@Suppress("KDocUnresolvedReference")
private fun String.fixString(): String =
    lines()
        .mapNotNull { it.trimToNull() }
        .joinToString(separator = " ")
        .replace(LINE_BREAK, "\n")
        .lines().joinToString(separator = "\n") { it.trim() }

/**
 * Custom stringResource implementation, that fixes newlines and indentation.
 * @see String.fixString
 */
@Composable
fun stringResource(resource: StringResource): String =
    _stringResource(resource).fixString()

/**
 * Custom stringResource implementation, that fixes newlines and indentation.
 * @see String.fixString
 */
@Composable
fun stringResource(resource: StringResource, vararg formatArgs: Any): String =
    _stringResource(resource, *formatArgs).fixString()

/**
 * Custom pluralStringResource implementation, that fixes newlines and indentation.
 * @see String.fixString
 */
@Composable
fun pluralStringResource(resource: PluralStringResource, quantity: Int): String =
    _pluralStringResource(resource, quantity).fixString()

/**
 * Custom pluralStringResource implementation, that fixes newlines and indentation.
 * @see String.fixString
 */
@Composable
fun pluralStringResource(resource: PluralStringResource, quantity: Int, vararg formatArgs: Any): String =
    _pluralStringResource(resource, quantity, *formatArgs).fixString()

/**
 * Custom getString implementation, that fixes newlines and indentation.
 * @see String.fixString
 */
suspend fun getString(resource: StringResource): String =
    _getString(resource).fixString()

/**
 * Custom getString implementation, that fixes newlines and indentation.
 * @see String.fixString
 */
suspend fun getString(resource: StringResource, vararg formatArgs: Any): String =
    _getString(resource, *formatArgs).fixString()

/**
 * Custom getPluralString implementation, that fixes newlines and indentation.
 * @see String.fixString
 */
suspend fun getPluralString(resource: PluralStringResource, quantity: Int): String =
    _getPluralString(resource, quantity).fixString()

/**
 * Custom getPluralString implementation, that fixes newlines and indentation.
 * @see String.fixString
 */
@Suppress("unused")
suspend fun getPluralString(resource: PluralStringResource, quantity: Int, vararg formatArgs: Any): String =
    _getPluralString(resource, quantity, *formatArgs).fixString()
