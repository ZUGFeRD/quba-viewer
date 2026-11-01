package de.openindex.zugferd.manager.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.window.WindowState
import de.openindex.zugferd.manager.APP_LOGGER
import de.openindex.zugferd.manager.LocalApplicationScope
import kotlinx.coroutines.runBlocking

interface DesktopShutdownHandler : ShutdownHandler {
    fun saveSettingsOnShutdown(
        preferences: Preferences,
        windowState: WindowState,
    )
}

val SHUTDOWN_HANDLER = object : DesktopShutdownHandler {
    @Composable
    override fun shutdown() {
        LocalApplicationScope.current?.exitApplication()
    }

    override fun saveSettingsOnShutdown(
        preferences: Preferences,
        windowState: WindowState,
    ) {
        APP_LOGGER.info("Unload JCEF on shutdown...")
        uninstallWebView()

        APP_LOGGER.info("Save settings on shutdown...")
        val pos = windowState.position
        preferences.setWindowPosition(
            x = if (pos.isSpecified) pos.x.value.toInt() else null,
            y = if (pos.isSpecified) pos.y.value.toInt() else null,
        )
        //APP_LOGGER.debug("window position: ${preferences.windowPosition.x} / ${preferences.windowPosition.y}")

        val size = windowState.size
        preferences.setWindowSize(
            width = if (size.isSpecified) size.width.value.toInt() else 1000,
            height = if (size.isSpecified) size.height.value.toInt() else 800,
        )
        //APP_LOGGER.debug("window size: ${preferences.windowSize.width} / ${preferences.windowSize.height}")

        runBlocking {
            preferences.save()
        }
    }
}

actual fun getShutdownHandler(): ShutdownHandler = SHUTDOWN_HANDLER
