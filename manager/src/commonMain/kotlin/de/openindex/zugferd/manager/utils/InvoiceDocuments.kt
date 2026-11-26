/*
 * Copyright (c) 2024-2025 Andreas Rudolph <andy@openindex.de>.
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

package de.openindex.zugferd.manager.utils

import de.openindex.zugferd.manager.model.ValidationSeverity
import de.openindex.zugferd.manager.model.ValidationType
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext

/**
 * Everything read from an e-invoice file in one pass.
 *
 * @property isPdf whether the file is a PDF (detected from the content, not the extension)
 * @property xml the invoice XML as stored in the file (no BOM), or null if none was found
 * @property prettyXml [xml] formatted for display (unchanged if it was already indented)
 * @property attachments files embedded into the PDF (empty for plain XML files)
 */
class InvoiceSource(
    val isPdf: Boolean,
    val xml: String?,
    val prettyXml: String?,
    val attachments: List<Pair<String, ByteArray>>,
)

/** Reads [file] once and extracts XML and attachments. Runs on the IO dispatcher. */
expect suspend fun readInvoiceSource(file: PlatformFile): InvoiceSource

/** Renders the HTML visualization for [source] in [language], or null if it contains no XML. */
expect suspend fun renderInvoiceHtml(source: InvoiceSource, language: Language): String?

/** Identifies the current content of a file (path + size + modification time). */
expect fun PlatformFile.contentKey(): String

/**
 * Shared, de-duplicated processing of invoice files for the Check and Visualisation sections.
 *
 * - The same file opened in both sections (or twice in a row) is read, rendered and validated
 *   only once. Requests arriving while the work is running wait for the same result.
 * - The work runs in an app-wide scope, so switching sections or closing a composable never
 *   aborts it halfway.
 * - CPU-heavy steps (XSLT rendering, validation) are limited to a few at a time, so dropping
 *   many large files doesn't exhaust memory or freeze the machine.
 * - Results are keyed by path, size and modification time; editing a file on disk yields a
 *   fresh entry. Only the most recent [MAX_CACHED_FILES] files are kept.
 * - The HTML is kept per language, so a file opened again after a language switch is
 *   rendered in the new language.
 */
object InvoiceDocuments {
    private const val MAX_CACHED_FILES = 6

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val readLimit = Semaphore(3)
    private val heavyLimit = Semaphore(maxOf(2, Runtime.getRuntime().availableProcessors() / 2))

    private class Entry {
        var source: Deferred<InvoiceSource>? = null
        val html = mutableMapOf<Language, Deferred<String?>>()
        var validation: Deferred<Validation>? = null
    }

    private val lock = Mutex()
    private val entries = object : LinkedHashMap<String, Entry>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Entry>?): Boolean =
            size > MAX_CACHED_FILES
    }

    suspend fun source(file: PlatformFile): InvoiceSource =
        shared(file, { it.source }, { e, d -> e.source = d }) {
            readLimit.withPermit { readInvoiceSource(file) }
        }

    /** HTML visualization in the app's current language. */
    suspend fun html(file: PlatformFile): String? {
        val language = getCurrentLanguage()
        return shared(
            file,
            { it.html[language] },
            { e, d -> if (d == null) e.html.remove(language) else e.html[language] = d },
        ) {
            val source = source(file)
            heavyLimit.withPermit { renderInvoiceHtml(source, language) }
        }
    }

    /** Validation result; a crash inside the validator becomes a FATAL message instead of an endless spinner. */
    suspend fun validation(file: PlatformFile): Validation =
        shared(file, { it.validation }, { e, d -> e.validation = d }) {
            heavyLimit.withPermit {
                try {
                    validatePdf(file)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Throwable) {
                    Validation(
                        isValid = false,
                        signature = null,
                        profile = null,
                        version = null,
                        messages = listOf(
                            ValidationMessage(
                                message = e.message ?: e.toString(),
                                type = ValidationType.OTHER,
                                severity = ValidationSeverity.FATAL,
                            )
                        ),
                    )
                }
            }
        }

    private suspend fun <T> shared(
        file: PlatformFile,
        get: (Entry) -> Deferred<T>?,
        set: (Entry, Deferred<T>?) -> Unit,
        compute: suspend () -> T,
    ): T {
        val key = withContext(Dispatchers.IO) { file.contentKey() }
        val deferred = lock.withLock {
            val entry = entries.getOrPut(key) { Entry() }
            get(entry) ?: scope.async { compute() }.also { set(entry, it) }
        }
        return try {
            deferred.await()
        } catch (e: Throwable) {
            // Don't keep failures around; the next request starts over.
            if (deferred.isCompleted) {
                lock.withLock { entries[key]?.let { if (get(it) === deferred) set(it, null) } }
            }
            throw e
        }
    }
}
