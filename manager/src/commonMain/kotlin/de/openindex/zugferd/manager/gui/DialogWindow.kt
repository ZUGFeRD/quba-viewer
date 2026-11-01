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

package de.openindex.zugferd.manager.gui

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.openindex.zugferd.manager.utils.pluralStringResource
import de.openindex.zugferd.manager.utils.stringResource
import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.StringResource

@Composable
expect fun DialogWindow(
    title: String,
    onCloseRequest: () -> Unit,
    width: Dp = 700.dp,
    height: Dp = 600.dp,
    resizable: Boolean = true,
    content: @Composable () -> Unit,
)

@Composable
fun DialogWindow(
    title: StringResource,
    onCloseRequest: () -> Unit,
    width: Dp = 700.dp,
    height: Dp = 600.dp,
    resizable: Boolean = true,
    content: @Composable () -> Unit,
) = DialogWindow(
    title = stringResource(title),
    onCloseRequest = onCloseRequest,
    width = width,
    height = height,
    resizable = resizable,
    content = content,
)

@Composable
@Suppress("unused")
fun DialogWindow(
    title: PluralStringResource,
    titleQuantity: Int = 1,
    onCloseRequest: () -> Unit,
    width: Dp = 700.dp,
    height: Dp = 600.dp,
    resizable: Boolean = true,
    content: @Composable () -> Unit,
) = DialogWindow(
    title = pluralStringResource(title, titleQuantity),
    onCloseRequest = onCloseRequest,
    width = width,
    height = height,
    resizable = resizable,
    content = content,
)

/*
@Composable
fun LockingDialogWindow(
    title: String,
    onCloseRequest: () -> Unit,
    width: Dp = 700.dp,
    height: Dp = 600.dp,
    content: @Composable () -> Unit,
) {
    val appState = LocalAppState.current
    appState.setLocked(true)

    DialogWindow(
        title = title,
        onCloseRequest = {
            onCloseRequest()
            appState.setLocked(false)
        },
        width = width,
        height = height,
        content = content,
    )
}
*/