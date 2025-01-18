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
