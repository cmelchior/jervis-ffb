package com.jervisffb.utils

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import kotlinx.datetime.Clock
import java.awt.Desktop
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.io.File
import java.io.FileWriter
import java.io.OutputStream
import java.io.PrintWriter
import java.net.URI

public actual fun threadId(): ULong {
    return Thread.currentThread().id.toULong()
}

public actual fun getPublicIp(): String {
    TODO()
}

public actual fun getLocalIpAddress(): String {
    TODO()
}

public actual fun openUrlInBrowser(url: String): Boolean {
    try {
        val uri = URI(url)
        return if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(uri)
            true
        } else {
            jervisLogger().e { "Desktop does not support BROWSE action." }
            false
        }
    } catch (ex: Exception) {
        jervisLogger().e { "Calling browser failed: $ex" }
        return false
    }
}

public actual fun canBeHost(): Boolean = true

public actual fun getBuildType(): String = "JVM"

public actual fun getPlatformDescription(): String {
    val systemProps = listOf(
        "OS Name" to System.getProperty("os.name"),
        "OS Version" to System.getProperty("os.version"),
        "OS Architecture" to System.getProperty("os.arch"),
        "JVM Name" to System.getProperty("java.vm.name"),
        "JVM Version" to System.getProperty("java.vm.version"),
        "JVM Vendor" to System.getProperty("java.vendor"),
        "Java Version" to System.getProperty("java.version")
    )
    return buildString {
        systemProps.forEach { (key, value) -> appendLine("$key: $value") }
    }
}


/**
 * Helper class, so we can split log output between a persistent file and normal
 * StdOut. This is relevant on e.g. MacOS.
 */
class TeeOutputStream(
    private val system: OutputStream,
    private val file: OutputStream
) : OutputStream() {

    override fun write(b: Int) {
        system.write(b)
        file.write(b)
    }

    override fun write(b: ByteArray?) {
        system.write(b)
        file.write(b)
    }

    override fun write(b: ByteArray?, off: Int, len: Int) {
        system.write(b, off, len)
        file.write(b, off, len)
    }

    override fun flush() {
        system.flush()
        file.flush()
    }

    override fun close() {
        system.close()
        file.close()
    }
}

public actual fun initializePlatform() {
    val applicationDir = File(APPLICATION_DIRECTORY)
    applicationDir.mkdirs()
}

public actual fun getPlatformLogWriter(): LogWriter? {
    // To be sure that logs are not just silently ignored we save them to a file on disk.
    // This is relevant on e.g. MacOS. The file will be reused every time the application
    // is opened, so space should not be a big concern.
    return object: LogWriter() {
        private val writer: PrintWriter
        init {
            val logFile = File("$APPLICATION_DIRECTORY${File.separator}log.txt")
            logFile.parentFile.mkdirs()
            writer = PrintWriter(FileWriter(logFile, true))
        }
        override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
            val throwableMsg = if (throwable == null) "" else "\n${throwable.stackTraceToString()}"
            writer.println("${Clock.System.now().toString()} ${severity.name}: $message$throwableMsg")
            writer.flush()
        }
    }
}


public actual fun copyToClipboard(text: String) {
    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
    val selection = StringSelection(text)
    clipboard.setContents(selection, null)
}

public actual fun triggerGC() {
    // There is no guarantee this will actually do anything, so this is
    // just best-effort.
    System.gc()
    System.runFinalization()
}
