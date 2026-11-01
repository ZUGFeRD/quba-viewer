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

import java.awt.KeyboardFocusManager
import javax.swing.JMenuItem
import javax.swing.JPopupMenu
import javax.swing.JSeparator
import javax.swing.SwingUtilities

/**
 * Shows a native Swing popup menu at the given screen coordinates.
 *
 * Because it is a heavyweight OS-level window, it always renders above
 * JCEF/Chromium components — no z-order conflict with WebViewer or PdfViewer.
 */
fun showTabContextMenu(
    screenX: Int,
    screenY: Int,
    onClose: () -> Unit,
    onCloseOthers: () -> Unit,
    onCloseToRight: (() -> Unit)?,
    openInOtherLabel: String? = null,
    onOpenInOther: (() -> Unit)? = null,
) {
    SwingUtilities.invokeLater {
        val menu = JPopupMenu()

        menu.add(JMenuItem("Schließen").apply {
            addActionListener { onClose() }
        })

        menu.add(JMenuItem("Andere Tabs schließen").apply {
            addActionListener { onCloseOthers() }
        })

        menu.add(JMenuItem("Tabs rechts schließen").apply {
            isEnabled = onCloseToRight != null
            addActionListener { onCloseToRight?.invoke() }
        })

        if (openInOtherLabel != null && onOpenInOther != null) {
            menu.add(JSeparator())
            menu.add(JMenuItem(openInOtherLabel).apply {
                addActionListener { onOpenInOther() }
            })
        }

        val window = KeyboardFocusManager.getCurrentKeyboardFocusManager().focusedWindow
            ?: return@invokeLater
        val windowLoc = window.locationOnScreen
        menu.show(window, screenX - windowLoc.x, screenY - windowLoc.y)
    }
}
