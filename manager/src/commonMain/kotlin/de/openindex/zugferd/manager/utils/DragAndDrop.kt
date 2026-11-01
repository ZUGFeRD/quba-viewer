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

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragData
import androidx.compose.ui.draganddrop.dragData
import io.github.vinceglb.filekit.core.PlatformFile

/*
@OptIn(ExperimentalComposeUiApi::class)
fun createDragAndDropTarget(
    onDrop: (pdfFile: PlatformFile) -> Unit,
): DragAndDropTarget =
    object : DragAndDropTarget {
        override fun onDrop(event: DragAndDropEvent): Boolean {
            val dragData = event.dragData()
            if (dragData !is DragData.FilesList) {
                return false
            }

            val pdfFileUri = dragData.readFiles().firstOrNull {
                it.lowercase().endsWith(".pdf")
            } ?: return false

            val pdfFile = getPlatformFileFromURI(pdfFileUri)
            onDrop(pdfFile)
            return true
        }
    }
 */



@OptIn(ExperimentalComposeUiApi::class)
fun createDragAndDropTarget(
    onDrop: (file: PlatformFile) -> Unit,
): DragAndDropTarget =
    object : DragAndDropTarget {
        override fun onDrop(event: DragAndDropEvent): Boolean {
            val dragData = event.dragData()
            if (dragData !is DragData.FilesList) return false

            val supportedFiles = dragData.readFiles().filter {
                it.lowercase().endsWith(".pdf") || it.lowercase().endsWith(".xml")
            }
            if (supportedFiles.isEmpty()) return false

            // Open every dropped file — each gets its own tab.
            supportedFiles.forEach { fileUri ->
                onDrop(getPlatformFileFromURI(fileUri))
            }
            return true
        }
    }




