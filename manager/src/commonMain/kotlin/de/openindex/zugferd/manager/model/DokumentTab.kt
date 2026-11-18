/*
 * Copyright (c) 2025 Quba contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.openindex.zugferd.manager.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.Job


/* orginal
class DocumentTab(
    name: String = "",
    pdf: PlatformFile? = null,
    tags: List<String> = emptyList()
) {
     var name by mutableStateOf(name)
    var pdf by mutableStateOf(pdf)
    var tags by mutableStateOf(tags)
    var html by mutableStateOf<String?>(null)
    var xml by mutableStateOf<String?>(null)
}

 */


class DocumentTab(
    name: String = "",
    pdf: PlatformFile? = null,
    tags: List<String> = emptyList(),
    isLoading: Boolean = false
) {
    var name by mutableStateOf(name)
    var pdf by mutableStateOf(pdf)
    var tags by mutableStateOf(tags)
    var html by mutableStateOf<String?>(null)
    var xml by mutableStateOf<String?>(null)
    var isLoading by mutableStateOf(isLoading)
    var isHtmlLoading by mutableStateOf(false)
    // Original source file (PDF or XML) — persisted across app restarts.
    var sourceFile: PlatformFile? = null
    // Background loading of this tab; cancelled when the tab is closed or reloaded.
    var loadJob: Job? = null

    // Fixed copy method
    fun copy(
        name: String = this.name,
        pdf: PlatformFile? = this.pdf,
        tags: List<String> = this.tags,
        html: String? = this.html,
        xml: String? = this.xml,
        isLoading: Boolean = this.isLoading,
        isHtmlLoading: Boolean = this.isHtmlLoading
    ): DocumentTab {
        return DocumentTab(name, pdf, tags, isLoading).apply {
            this.html = html
            this.xml = xml
            this.isHtmlLoading = isHtmlLoading
        }
    }
}