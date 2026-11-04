package de.openindex.zugferd.manager.utils

import de.openindex.zugferd.manager.APP_LOGGER
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.nio.channels.FileChannel
import java.nio.channels.FileLock
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.UUID
import kotlin.concurrent.thread
import kotlin.io.path.readLines
import kotlin.io.path.writeText

private val INSTANCE_LOCK_FILE: Path by lazy {
    APP_WORK_DIR.resolve("instance.lock")
}

private val INSTANCE_PORT_FILE: Path by lazy {
    APP_WORK_DIR.resolve("instance.port")
}

/**
 * Makes sure that only one Quba process runs per user. A second process could not
 * initialize CEF, because the first one holds its cache directory, and would only
 * show blank web views.
 */
object SingleInstance {
    private var channel: FileChannel? = null
    private var lock: FileLock? = null

    /**
     * Returns true if this is the first running instance; [onActivate] is then called
     * whenever another instance is started. Otherwise the running instance is asked to
     * bring its window to the front and false is returned.
     */
    fun acquire(onActivate: () -> Unit): Boolean {
        val channel = FileChannel.open(
            INSTANCE_LOCK_FILE,
            StandardOpenOption.CREATE,
            StandardOpenOption.WRITE,
        )
        val lock = try {
            channel.tryLock()
        } catch (e: IOException) {
            APP_LOGGER.warn("Could not lock instance file.", e)
            null
        }

        if (lock == null) {
            channel.close()
            if (!activateRunningInstance()) {
                APP_LOGGER.warn("Another instance is running, but could not be activated.")
            }
            return false
        }

        this.channel = channel
        this.lock = lock
        listenForActivation(onActivate)
        return true
    }

    private fun listenForActivation(onActivate: () -> Unit) {
        val server = try {
            ServerSocket(0, 5, InetAddress.getLoopbackAddress())
        } catch (e: IOException) {
            APP_LOGGER.warn("Could not listen for further instances.", e)
            return
        }

        // The token keeps other local users from triggering the activation.
        val token = UUID.randomUUID().toString()
        INSTANCE_PORT_FILE.writeText("${server.localPort}\n$token\n")

        thread(name = "single-instance", isDaemon = true) {
            while (!server.isClosed) {
                try {
                    server.accept().use { socket ->
                        socket.soTimeout = 2000
                        val received = socket.getInputStream().bufferedReader().readLine()
                        if (received == token) {
                            APP_LOGGER.info("Another instance was started, bringing window to front.")
                            onActivate()
                        }
                    }
                } catch (e: IOException) {
                    APP_LOGGER.warn("Could not handle activation request.", e)
                }
            }
        }
    }

    private fun activateRunningInstance(): Boolean = try {
        val (port, token) = INSTANCE_PORT_FILE.readLines()
        Socket().use { socket ->
            socket.connect(InetSocketAddress(InetAddress.getLoopbackAddress(), port.toInt()), 2000)
            socket.getOutputStream().write("$token\n".toByteArray())
        }
        true
    } catch (e: Exception) {
        APP_LOGGER.warn("Could not contact running instance.", e)
        false
    }
}
