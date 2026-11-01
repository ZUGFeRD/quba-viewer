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
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import de.openindex.zugferd.quba.generated.resources.Res
import de.openindex.zugferd.quba.generated.resources.ic_app_logo
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.window.DialogWindow as DesktopDialogWindow

@Composable
actual fun DialogWindow(
    title: String,
    onCloseRequest: () -> Unit,
    width: Dp,
    height: Dp,
    resizable: Boolean,
    content: @Composable () -> Unit,
) {
    val dialogWindowState = rememberDialogState(
        position = WindowPosition.PlatformDefault,
        width = width,
        height = height,
    )

    DesktopDialogWindow(
        onCloseRequest = onCloseRequest,
        state = dialogWindowState,
        title = title,
        resizable = resizable,
        icon = painterResource(Res.drawable.ic_app_logo),
    ) {
        content()
    }
}
