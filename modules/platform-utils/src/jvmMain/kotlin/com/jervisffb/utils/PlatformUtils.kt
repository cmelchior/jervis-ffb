package com.jervisffb.utils

import java.awt.Desktop
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
