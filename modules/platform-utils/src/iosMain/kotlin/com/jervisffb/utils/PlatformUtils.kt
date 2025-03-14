package com.jervisffb.utils
import platform.UIKit.UIDevice

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
