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

import com.jetbrains.cef.JCefAppConfig
import de.openindex.zugferd.manager.APP_LOGGER
import de.openindex.zugferd.manager.AppInfo
import de.openindex.zugferd.manager.AppSection
import de.openindex.zugferd.manager._APP_STATE
import de.openindex.zugferd.manager.sections.VisualsSectionState
import de.openindex.zugferd.quba.generated.resources.AppVisualisationAttachmentSaveAs
import de.openindex.zugferd.quba.generated.resources.AppVisualisationAttachmentSaveTitle
import de.openindex.zugferd.quba.generated.resources.AppVisualisationContextMenuBack
import de.openindex.zugferd.quba.generated.resources.AppVisualisationContextMenuCopy
import de.openindex.zugferd.quba.generated.resources.AppVisualisationContextMenuForward
import de.openindex.zugferd.quba.generated.resources.AppVisualisationContextMenuPrint
import de.openindex.zugferd.quba.generated.resources.AppVisualisationContextMenuSelectAll
import de.openindex.zugferd.quba.generated.resources.AppVisualisationContextMenuViewSource
import de.openindex.zugferd.quba.generated.resources.AppVisualisationExportPdfContextMenu
import de.openindex.zugferd.quba.generated.resources.Res
import io.github.vinceglb.filekit.core.FileKit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.cef.CefApp
import org.cef.CefClient
import org.cef.CefSettings
import org.cef.browser.CefBrowser
import org.cef.browser.CefRendering
import org.cef.callback.CefMenuModel
import org.cef.handler.CefAppHandlerAdapter
import org.cef.handler.CefContextMenuHandlerAdapter
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists

private const val CEF_WINDOWLESS_RENDERING_ENABLED = false
private const val CEF_OFFSCREEN_RENDERING_ENABLED = false
private const val CEF_TRANSPARENCY_ENABLED = false

@Suppress("KotlinConstantConditions")
private const val CEF_DEBUG = AppInfo.Git.GIT_BRANCH != "master"

private val CEF_CACHE_DIR: Path by lazy {
    CACHE_DIR
        .resolve("jcef")
        .createDirectories()
}

private val CEF_LOG_FILE: Path by lazy {
    LOGS_DIR
        .resolve("jcef.log")
}

private val OLD_CEF_CACHE_DIR: Path by lazy {
    CACHE_DIR
        .resolve("chrome")
        .createDirectories()
}

private val OLD_CEF_INSTALLATION_DIR: Path by lazy {
    APP_WORK_DIR
        .resolve("chrome")
        .createDirectories()
}

private val OLD_CEF_LOG_FILE: Path by lazy {
    LOGS_DIR
        .resolve("chrome.log")
}

private var CEF_APP: CefApp? = null

private var CEF_CLIENT: CefClient? = null

private var CEF_BROWSER: CefBrowser? = null

private const val EXPORT_VISUALIZATION_PDF_COMMAND_ID = CefMenuModel.MenuId.MENU_ID_USER_FIRST

private fun exportVisualizationPdfMenuLabel(): String =
    runBlocking { getString(Res.string.AppVisualisationExportPdfContextMenu) }

/** Chromium's own context menu entries and the app texts that replace their labels. */
private val CHROMIUM_MENU_LABELS = mapOf(
    CefMenuModel.MenuId.MENU_ID_BACK to Res.string.AppVisualisationContextMenuBack,
    CefMenuModel.MenuId.MENU_ID_FORWARD to Res.string.AppVisualisationContextMenuForward,
    CefMenuModel.MenuId.MENU_ID_PRINT to Res.string.AppVisualisationContextMenuPrint,
    CefMenuModel.MenuId.MENU_ID_VIEW_SOURCE to Res.string.AppVisualisationContextMenuViewSource,
    CefMenuModel.MenuId.MENU_ID_COPY to Res.string.AppVisualisationContextMenuCopy,
    CefMenuModel.MenuId.MENU_ID_SELECT_ALL to Res.string.AppVisualisationContextMenuSelectAll,
)

/**
 * CEF takes its language once at startup (`settings.locale`), so Chromium's own menu entries
 * would stay in that language after a language switch. Label them with the app's texts, which
 * follow the current language.
 */
private fun relabelChromiumMenuItems(model: CefMenuModel) {
    for ((commandId, label) in CHROMIUM_MENU_LABELS) {
        if (model.getIndexOf(commandId) >= 0) {
            model.setLabel(commandId, runBlocking { getString(label) })
        }
    }
}

/**
 * Renders the invoice XML of the currently active Visualisieren tab via Mustang's own
 * PDF template (all sections combined, no tab bar — see [exportVisualizationAsPdf]),
 * embeds the XML into it, and lets the user choose where to save the result.
 */
private fun exportCurrentVisualizationAsPdf() {
    val tab = (AppSection.VISUALISATION.state as? VisualsSectionState)
        ?.let { state -> state.documents.getOrNull(state.selectedIndex) }
        ?: return
    val xml = tab.xml ?: return
    val suggestedName = tab.name.substringBeforeLast(".").ifBlank { "visualisierung" }

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val targetFile = FileKit.saveFile(
                bytes = null,
                baseName = suggestedName,
                extension = "pdf",
            ) ?: return@launch

            exportVisualizationAsPdf(xml, targetFile)
        } catch (e: Exception) {
            APP_LOGGER.error("Visualisierung konnte nicht als PDF exportiert werden.", e)
        }
    }
}

/** An invoice attachment kept in memory; a temp file is only written when the user opens it. */
class AttachmentEntry(val name: String, val bytes: ByteArray, val mimeType: String)

/**
 * Attachment key → attachment. Filled by the HTML post-processing, read on CEF threads.
 * The key holds a content hash, so two invoices with an attachment of the same name never
 * open each other's file.
 */
private val globalAttachmentData = java.util.concurrent.ConcurrentHashMap<String, AttachmentEntry>()

/** Registers an attachment and returns the key to put into a `zugferd-attachment://` link. */
fun registerAttachment(name: String, bytes: ByteArray, mimeType: String): String {
    val key = "${Integer.toHexString(bytes.contentHashCode())}${bytes.size}-$name"
    globalAttachmentData[key] = AttachmentEntry(name, bytes, mimeType)
    return key
}

/** Formats the embedded Chromium shows properly; everything else goes to the operating system. */
private val VIEWER_EXTENSIONS = setOf("pdf", "png", "jpg", "jpeg", "gif", "bmp", "webp", "svg", "txt", "xml", "htm", "html")

private val INVALID_FILE_NAME_CHARS = Regex("""[\\/:*?"<>|\p{Cntrl}]""")

/**
 * Opens an attachment. PDF, images, text and XML appear in Quba's own viewer window.
 * Everything else (Excel, Word, CSV, ZIP, …) opens in the program the operating system
 * associates with the file type. If there is none, a save dialog appears, so the file is
 * always reachable. Runs off the CEF thread; the temp file is only written on click.
 */
fun openAttachment(entry: AttachmentEntry) {
    Thread({
        try {
            val file = writeAttachmentToTempFile(entry)
            if (isShownInViewer(entry)) {
                openAttachmentInWindow(file.toURI().toString(), entry)
            } else {
                openWithSystemApp(file, entry)
            }
        } catch (e: Exception) {
            APP_LOGGER.error("Anhang '${entry.name}' konnte nicht geöffnet werden.", e)
            javax.swing.SwingUtilities.invokeLater { saveAttachmentAs(entry, null) }
        }
    }, "quba-open-attachment").apply {
        isDaemon = true
        start()
    }
}

private fun isShownInViewer(entry: AttachmentEntry): Boolean {
    extensionOf(entry.name)?.let { return it in VIEWER_EXTENSIONS }
    val mime = entry.mimeType.lowercase()
    return mime == "application/pdf" || mime.startsWith("image/") || mime == "text/plain" || mime.endsWith("/xml")
}

/**
 * Writes the attachment under its own name (plus a fitting extension) into a fresh temp
 * folder. The viewer window title and external programs then show the real file name, and
 * the operating system can pick the right program by extension.
 */
private fun writeAttachmentToTempFile(entry: AttachmentEntry): java.io.File {
    val dir = java.nio.file.Files.createTempDirectory("quba_att_").toFile().apply { deleteOnExit() }
    return java.io.File(dir, safeFileName(withExtension(entry.name, entry.mimeType))).apply {
        deleteOnExit()
        writeBytes(entry.bytes)
    }
}

private fun safeFileName(name: String): String {
    val cleaned = name.replace(INVALID_FILE_NAME_CHARS, "_").trim().trim('.').ifEmpty { "Anhang" }
    if (cleaned.length <= 150) return cleaned
    val ext = extensionOf(cleaned)
    return if (ext != null) cleaned.substringBeforeLast('.').take(140) + "." + ext else cleaned.take(150)
}

private fun openWithSystemApp(file: java.io.File, entry: AttachmentEntry) {
    val desktop = if (java.awt.Desktop.isDesktopSupported()) java.awt.Desktop.getDesktop() else null
    if (desktop != null && desktop.isSupported(java.awt.Desktop.Action.OPEN)) {
        try {
            desktop.open(file)
            return
        } catch (e: Exception) {
            APP_LOGGER.warn("Kein Programm zum Öffnen von '${entry.name}' gefunden: ${e.message}")
        }
    }
    javax.swing.SwingUtilities.invokeLater { saveAttachmentAs(entry, null) }
}

/** Save dialog for an attachment; also used by the viewer window's save actions. */
private fun saveAttachmentAs(entry: AttachmentEntry, parent: java.awt.Component?, suggestedName: String? = null) {
    val chooser = javax.swing.JFileChooser()
    chooser.dialogTitle = runBlocking { getString(Res.string.AppVisualisationAttachmentSaveTitle) }
    chooser.selectedFile = java.io.File(suggestedName ?: safeFileName(withExtension(entry.name, entry.mimeType)))
    if (chooser.showSaveDialog(parent) == javax.swing.JFileChooser.APPROVE_OPTION) {
        try {
            chooser.selectedFile.writeBytes(entry.bytes)
        } catch (e: Exception) {
            APP_LOGGER.error("Anhang konnte nicht gespeichert werden.", e)
        }
    }
}


/**
 * As we never show multiple browser instances at once,
 * we keep one browser instance permanently in memory and only
 * switch the loaded url.
 */
fun getCefBrowser(url: String): CefBrowser {
    if (CEF_CLIENT == null) {
        try {
            CEF_CLIENT = CEF_APP!!.createClient()
                // Disable Drag & Drop on the browser component,
                // as we don't want external files to be dragged directly into the browser.
                .also {
                    it.removeDragHandler()
                    it.addDragHandler { _, _, _ -> true }
                }
                .also { client ->
                    // Block popups (prevents blank windows from original downloadData/window.open calls).
                    client.addLifeSpanHandler(object : org.cef.handler.CefLifeSpanHandlerAdapter() {
                        override fun onBeforePopup(
                            browser: org.cef.browser.CefBrowser?,
                            frame: org.cef.browser.CefFrame?,
                            targetUrl: String?,
                            targetFrameName: String?,
                        ): Boolean = true
                    })
                }
                .also { client ->
                    // Intercept zugferd-attachment:// links to open attachments in a popup window.
                    client.addRequestHandler(object : org.cef.handler.CefRequestHandlerAdapter() {
                        override fun onBeforeBrowse(
                            browser: org.cef.browser.CefBrowser?,
                            frame: org.cef.browser.CefFrame?,
                            request: org.cef.network.CefRequest?,
                            user_gesture: Boolean,
                            is_redirect: Boolean
                        ): Boolean {
                            val url = request?.url ?: return false

                            // Intercept attachment links → write temp file on-demand, open via file:// URL.
                            if (url.startsWith("zugferd-attachment://")) {
                                try {
                                    val key = java.net.URLDecoder.decode(
                                        url.removePrefix("zugferd-attachment://").trimEnd('/'), "UTF-8"
                                    )
                                    val entry = globalAttachmentData[key]
                                    if (entry != null) {
                                        openAttachment(entry)
                                    } else {
                                        APP_LOGGER.warn("No attachment data found for: $key")
                                    }
                                } catch (e: Exception) {
                                    APP_LOGGER.error("Fehler beim Öffnen des Anhangs.", e)
                                }
                                return true // Cancel navigation in main browser
                            }

                            return super.onBeforeBrowse(browser, frame, request, user_gesture, is_redirect)
                        }
                    })
                }
                .also { client ->
                    client.addKeyboardHandler(object : org.cef.handler.CefKeyboardHandlerAdapter() {
                        override fun onKeyEvent(
                            browser: org.cef.browser.CefBrowser?,
                            event: org.cef.handler.CefKeyboardHandler.CefKeyEvent?
                        ): Boolean {
                            if (event == null) return false
                            if (event.type != org.cef.handler.CefKeyboardHandler.CefKeyEvent.EventType.KEYEVENT_RAWKEYDOWN &&
                                event.type != org.cef.handler.CefKeyboardHandler.CefKeyEvent.EventType.KEYEVENT_KEYDOWN) {
                                return false
                            }

                            // EVENTFLAG_CONTROL_DOWN = 1 << 2, EVENTFLAG_COMMAND_DOWN = 1 << 7 (Meta/Cmd)
                            val controlPressed = (event.modifiers and 4 != 0) || (event.modifiers and 128 != 0)

                            if (event.windows_key_code == 70 && controlPressed) { // Ctrl+F
                                when (_APP_STATE.sectionSync) {
                                    AppSection.VISUALISATION ->
                                        (AppSection.VISUALISATION.state as? de.openindex.zugferd.manager.sections.VisualsSectionState)
                                            ?.isSearchOpen = true
                                    AppSection.CHECK ->
                                        (AppSection.CHECK.state as? de.openindex.zugferd.manager.sections.CheckSectionState)
                                            ?.isSearchOpen = true
                                    else -> {}
                                }
                                return true // consume — open our search, not the browser's
                            }

                            if (event.windows_key_code == 27) { // ESC
                                when (_APP_STATE.sectionSync) {
                                    AppSection.VISUALISATION -> {
                                        val state = AppSection.VISUALISATION.state as? de.openindex.zugferd.manager.sections.VisualsSectionState
                                        if (state?.isSearchOpen == true) {
                                            state.isSearchOpen = false
                                            return true
                                        }
                                    }
                                    AppSection.CHECK -> {
                                        val state = AppSection.CHECK.state as? de.openindex.zugferd.manager.sections.CheckSectionState
                                        if (state?.isSearchOpen == true) {
                                            state.isSearchOpen = false
                                            return true
                                        }
                                    }
                                    else -> {}
                                }
                            }

                            return false
                        }
                    })
                }
                .also { client ->
                    // Label Chromium's own entries in the current app language, and add a custom
                    // "Export visualization as PDF" entry next to "Print..." (Visualisieren only).
                    client.addContextMenuHandler(object : CefContextMenuHandlerAdapter() {
                        override fun onBeforeContextMenu(
                            browser: CefBrowser?,
                            frame: org.cef.browser.CefFrame?,
                            params: org.cef.callback.CefContextMenuParams?,
                            model: CefMenuModel?,
                        ) {
                            if (model == null) return
                            relabelChromiumMenuItems(model)
                            if (_APP_STATE.sectionSync != AppSection.VISUALISATION) return

                            val printIndex = model.getIndexOf(CefMenuModel.MenuId.MENU_ID_PRINT)
                            val insertAt = if (printIndex >= 0) printIndex + 1 else model.count
                            model.insertSeparatorAt(insertAt)
                            model.insertItemAt(insertAt + 1, EXPORT_VISUALIZATION_PDF_COMMAND_ID, exportVisualizationPdfMenuLabel())
                        }

                        override fun onContextMenuCommand(
                            browser: CefBrowser?,
                            frame: org.cef.browser.CefFrame?,
                            params: org.cef.callback.CefContextMenuParams?,
                            commandId: Int,
                            eventFlags: Int,
                        ): Boolean {
                            if (commandId != EXPORT_VISUALIZATION_PDF_COMMAND_ID) {
                                return false
                            }
                            exportCurrentVisualizationAsPdf()
                            return true
                        }
                    })
                }
        } catch (e: Exception) {
            APP_LOGGER.error("Browser is not properly initialized!", e)
            throw RuntimeException("Browser is not properly initialized!", e)
        }
    }

    return if (CEF_BROWSER == null) {
        CEF_CLIENT!!.createBrowser(
            url,
            CefRendering.OFFSCREEN
                .takeIf { CEF_OFFSCREEN_RENDERING_ENABLED }
                ?: CefRendering.DEFAULT,
            CEF_TRANSPARENCY_ENABLED,
        )
    } else {
        CEF_BROWSER!!.let {
            it.stopLoad()
            it.loadURL(url)
            it
        }
    }
}

fun uninstallWebView() {
    CEF_BROWSER?.close(true)
    CEF_CLIENT?.dispose()
    CEF_APP?.dispose()
}

@OptIn(ExperimentalPathApi::class)
suspend fun installWebView(gpuEnabled: Boolean = true) {
    if (CEF_APP != null) {
        return
    }

    //
    // Cleanup some old JCEF files from user directory.
    //
    if (OLD_CEF_CACHE_DIR.exists()) {
        OLD_CEF_CACHE_DIR.deleteRecursively()
    }
    if (OLD_CEF_INSTALLATION_DIR.exists()) {
        OLD_CEF_INSTALLATION_DIR.deleteRecursively()
    }
    if (OLD_CEF_LOG_FILE.exists()) {
        OLD_CEF_LOG_FILE.deleteRecursively()
    }

    //
    // Setup paths to bundled JCEF binaries.
    //
    // see org.cef.CefApp#startupAsync
    // see org.cef.Startup
    //
    //if (getPlatform().isRunningFromInstallation) {
    //    if (SystemUtils.IS_OS_MAC) {
    //        val frameworksPath = withContext(Dispatchers.IO) {
    //            APP_LAUNCHER!!.parent.parent.resolve("Frameworks").toRealPath()
    //        }
    //
    //        val cefFrameworkPath = frameworksPath.resolve("Chromium Embedded Framework.framework").absolute()
    //        if (!cefFrameworkPath.isDirectory()) {
    //            APP_LOGGER.error("Can't find bundled CEF Framework: $cefFrameworkPath")
    //        } else {
    //            APP_LOGGER.info("Using bundled CEF Framework: $cefFrameworkPath")
    //            System.setProperty(
    //                "ALT_CEF_FRAMEWORK_DIR",
    //                cefFrameworkPath.pathString,
    //            )
    //        }
    //
    //        val cefHelperPath = frameworksPath.resolve("jcef Helper.app").absolute()
    //        if (!cefHelperPath.isDirectory()) {
    //            APP_LOGGER.error("Can't find bundled CEF Helper: $cefHelperPath")
    //        } else {
    //            APP_LOGGER.info("Using bundled CEF Helper: $cefHelperPath")
    //            System.setProperty(
    //                "ALT_CEF_HELPER_APP_DIR",
    //                cefHelperPath.pathString,
    //            )
    //        }
    //
    //        //System.setProperty(
    //        //    "ALT_JCEF_LIB_DIR",
    //        //    "",
    //        //)
    //    }
    //    //else {
    //    //    System.setProperty(
    //    //        "ALT_CEF_FRAMEWORK_DIR",
    //    //        Path.of(SystemUtils.JAVA_HOME, "lib").absolutePathString(),
    //    //    )
    //    //}
    //}

    CEF_APP = withContext(Dispatchers.IO) {
        try {
            CEF_LOG_FILE.deleteIfExists()
        } catch (e: Exception) {
            APP_LOGGER.warn("Could not delete CEF log file: ${e.message}")
        }

        if (CEF_DEBUG) {
            enableVerboseLogging()
        }

        //if (CEF_DEBUG) {
        //    CefLog.init(
        //        CEF_LOG_FILE.absolutePathString(),
        //        CefSettings.LogSeverity.LOGSEVERITY_VERBOSE,
        //    )
        //} else {
        //    CefLog.init(
        //        CEF_LOG_FILE.absolutePathString(),
        //        CefSettings.LogSeverity.LOGSEVERITY_WARNING,
        //    )
        //}

        // Perform startup initialization on platforms that require it.
        APP_LOGGER.debug("Starting up CEF application...")
        CefApp.startup(emptyArray())

        val config = JCefAppConfig.getInstance()

        //
        // Chrome command line arguments.
        // https://peter.sh/experiments/chromium-command-line-switches/
        //

        val appArgs = config.appArgsAsList

        // Disable DRM / Widevine
        // https://magpcss.org/ceforum/viewtopic.php?f=6&t=19093
        appArgs.add("--disable-component-update")
        
        // Allow local file access and disable web security to support data URLs and local resources
        appArgs.add("--allow-file-access-from-files")
        appArgs.add("--disable-web-security")

        // Disable Hardware Acceleration
        if (!gpuEnabled) {
            appArgs.addAll(
                listOf(
                    "--disable-gpu",
                    "--disable-gpu-compositing",
                    "--disable-gpu-vsync",
                    "--disable-software-rasterizer",
                    //"--disable-extensions",
                )
            )
        }

        val settings = config.cefSettings
        //settings.no_sandbox = true
        // Matches the app's current language (user preference, or system language as fallback —
        // see getCurrentLanguage()). Chromium's own UI strings (native context menu, etc.) are
        // fixed for the lifetime of the embedded browser, so a later in-app language change only
        // takes effect after the app is restarted.
        settings.locale = getCurrentLanguage().code
        settings.cache_path = CEF_CACHE_DIR.absolutePathString()
        //settings.root_cache_path = CEF_CACHE_DIR.absolutePathString()
        settings.windowless_rendering_enabled = CEF_WINDOWLESS_RENDERING_ENABLED
        settings.log_file = CEF_LOG_FILE.absolutePathString()
        settings.log_severity = CefSettings.LogSeverity.LOGSEVERITY_VERBOSE
            .takeIf { CEF_DEBUG }
            ?: CefSettings.LogSeverity.LOGSEVERITY_WARNING

        CefApp.addAppHandler(object : CefAppHandlerAdapter(appArgs.toTypedArray()) {
            override fun stateHasChanged(state: CefApp.CefAppState) {
                // Shutdown the app if the native CEF part is terminated
                //if (state == CefAppState.TERMINATED) System.exit(0)

                @Suppress("LoggingStringTemplateAsArgument")
                APP_LOGGER.debug("Changing CEF state to $state")
                super.stateHasChanged(state)
            }

            override fun onBeforeChildProcessLaunch(commandLine: String) {
                APP_LOGGER.info("Launching CEF process: $commandLine")
                super.onBeforeChildProcessLaunch(commandLine)
            }
        })

        APP_LOGGER.debug("Getting CEF application instance...")
        CefApp.getInstance(appArgs.toTypedArray(), settings)
    }
}

@Suppress("SpellCheckingInspection")
private fun enableVerboseLogging() {
    System.setProperty("jcef.tests.verbose", "true")
    System.setProperty("jcef.trace.cefbrowser_n.lifespan", "true")
    System.setProperty("jcef.trace.cefclient.lifespan", "true")
    System.setProperty("jcef.trace.cefapp.lifespan", "true")
    System.setProperty("jcef.trace.cefbrowserwr.addnotify", "true")
    System.setProperty("jcef.log.trace_thread", "true")
}

private fun openAttachmentInWindow(url: String, entry: AttachmentEntry) {
    val title = entry.name
    javax.swing.SwingUtilities.invokeLater {
        try {
            // Use a dedicated CefClient per popup — sharing CEF_CLIENT with the main browser
            // causes the second browser to render blank.
            val popupClient = CEF_APP!!.createClient()

            // Handle download triggered by Chrome's built-in PDF viewer download button.
            popupClient.addDownloadHandler(object : org.cef.handler.CefDownloadHandlerAdapter() {
                override fun onBeforeDownload(
                    browser: org.cef.browser.CefBrowser?,
                    downloadItem: org.cef.callback.CefDownloadItem?,
                    suggestedName: String?,
                    callback: org.cef.callback.CefBeforeDownloadCallback?,
                ) {
                    javax.swing.SwingUtilities.invokeLater { saveAttachmentAs(entry, null, suggestedName) }
                }
            })

            val popupBrowser = popupClient.createBrowser(url, org.cef.browser.CefRendering.DEFAULT, false)

            val frame = javax.swing.JFrame(title)
            frame.defaultCloseOperation = javax.swing.JFrame.DISPOSE_ON_CLOSE
            frame.setSize(900, 700)
            frame.setLocationRelativeTo(null)
            // Reuse the icon from the main Compose window (has the full SVG rendered).
            // Fall back to the painted icon if no main window found yet.
            val mainIcons = java.awt.Frame.getFrames().firstOrNull()?.iconImages
            if (!mainIcons.isNullOrEmpty()) {
                frame.iconImages = mainIcons
            } else {
                frame.iconImage = createQubaAwtIcon()
            }

            // For non-PDF attachments (e.g. images), Chrome shows no download button.
            // Add a minimal toolbar with a save button only in that case.
            // Check both MIME type and file extension, as the MIME type from the HTML may be unreliable.
            val isPdf = entry.mimeType.equals("application/pdf", ignoreCase = true)
                || extensionOf(title) == "pdf"
            if (!isPdf) {
                val toolbar = javax.swing.JToolBar()
                toolbar.isFloatable = false
                val saveButton = javax.swing.JButton(runBlocking { getString(Res.string.AppVisualisationAttachmentSaveAs) })
                saveButton.addActionListener { saveAttachmentAs(entry, frame) }
                toolbar.add(saveButton)
                frame.contentPane.add(toolbar, java.awt.BorderLayout.NORTH)
            }

            frame.contentPane.add(popupBrowser.uiComponent, java.awt.BorderLayout.CENTER)

            // Show the frame first so the native window exists before CEF tries to render into it.
            frame.isVisible = true
            frame.contentPane.validate()

            // Clean up the popup client when the window is closed.
            frame.addWindowListener(object : java.awt.event.WindowAdapter() {
                override fun windowClosed(e: java.awt.event.WindowEvent?) {
                    popupBrowser.close(true)
                    popupClient.dispose()
                }
            })
        } catch (e: Exception) {
            APP_LOGGER.error("Anhang-Fenster konnte nicht geöffnet werden.", e)
        }
    }
}

/**
 * Draws the Quba brand icon as a BufferedImage for use in plain Swing JFrames.
 * Matches the golden-yellow color (#ead017) of ic_app_logo.svg.
 */
private fun createQubaAwtIcon(): java.awt.image.BufferedImage {
    val size = 64
    val img = java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB)
    val g = img.createGraphics()
    g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON)
    g.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
    // Golden-yellow background — matches ic_app_logo.svg fill color
    g.color = java.awt.Color(0xEA, 0xD0, 0x17)
    g.fillRoundRect(0, 0, size, size, 14, 14)
    // "Q" in dark color for contrast
    g.color = java.awt.Color(0x1A, 0x0F, 0x00)
    g.font = java.awt.Font("SansSerif", java.awt.Font.BOLD, 42)
    val fm = g.fontMetrics
    g.drawString("Q", (size - fm.stringWidth("Q")) / 2, (size - fm.height) / 2 + fm.ascent)
    g.dispose()
    return img
}
