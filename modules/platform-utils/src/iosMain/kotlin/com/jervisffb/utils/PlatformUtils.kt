package com.jervisffb.utils

import co.touchlab.kermit.LogWriter
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.isSuccess
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIDevice
import platform.UIKit.UIPasteboard

public actual fun threadId(): ULong {
    return 0uL // TODO Figure out how to get it here
}

public actual suspend fun getPublicIpAddress(): String? {
    try {
        getHttpClient().use { client ->
            val response = client.get("https://api.ipify.org")
            return if (response.status.isSuccess()) {
                response.body<String>()
            } else {
                null
            }
        }
    } catch (ex: Exception) {
        return null
    }
}

public actual suspend fun getLocalIpAddress(): String {
    // This isn't correct, but let's fix it later.
    return "127.0.0.1"
}

public actual fun openUrlInBrowser(url: String): Boolean {
    val nsUrl = NSURL.URLWithString(url) ?: return false
    return UIApplication.sharedApplication.openURL(nsUrl)
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

public actual fun triggerGC() {
    // Do nothing
}

public actual fun getSystemEnvironmentVariable(key: String): String? {
    return platform.Foundation.NSProcessInfo.processInfo.environment[key]?.toString()
}
