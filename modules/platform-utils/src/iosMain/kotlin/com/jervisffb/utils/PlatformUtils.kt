package com.jervisffb.utils

import co.touchlab.kermit.LogWriter
import platform.UIKit.UIDevice
import platform.UIKit.UIPasteboard

public actual fun threadId(): ULong {
    return 0uL // TODO Figure out how to get it here
}

public actual fun getPublicIp(): String {
    TODO()
}

public actual fun getLocalIpAddress(): String {
    TODO()
}

public actual fun openUrlInBrowser(url: String): Boolean {
    TODO()
}

public actual fun canBeHost(): Boolean = true

public actual fun getBuildType(): String = "iOS"

public actual fun getPlatformDescription(): String {
    return buildString {
        appendLine("Device: ${UIDevice.currentDevice.model}")
        appendLine("System Name: ${UIDevice.currentDevice.systemName()}")
        appendLine("System Version: ${UIDevice.currentDevice.systemVersion}")
    }
}

public actual fun initializePlatform() {
    // Do nothing
}

public actual fun getPlatformLogWriter(): LogWriter? {
    return null // Unclear if we want to do anything on iOS?
}

public actual fun copyToClipboard(text: String) {
    UIPasteboard.generalPasteboard.string = text
}

