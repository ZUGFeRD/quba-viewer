package de.openindex.zugferd.manager.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.vinceglb.filekit.core.PlatformFile


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