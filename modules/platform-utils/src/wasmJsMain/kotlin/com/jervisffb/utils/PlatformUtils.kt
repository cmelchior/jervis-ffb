package com.jervisffb.utils

import co.touchlab.kermit.LogWriter
import kotlinx.browser.window

public actual fun threadId(): ULong {
    return 0uL // TODO Figure out how to get it here
}

public actual suspend fun getPublicIpAddress(): String? {
    error("Not supported on WASM")
}

public actual suspend fun getLocalIpAddress(): String {
    error("Not supported on WASM")
}

public actual fun openUrlInBrowser(url: String): Boolean {
    window.open(url, "_blank")
    return true
}

public actual fun canBeHost(): Boolean = false

public actual fun getBuildType(): String = "WASM"

public actual fun getPlatformDescription(): String {
    val userAgent = window.navigator.userAgent
    return buildString {
        appendLine("User Agent: $userAgent")
    }
}

public actual fun initializePlatform() {
    // Do nothing
}

public actual fun getPlatformLogWriter(): LogWriter? {
    return null // Unclear if we want to do anything on Web?
}

public actual fun copyToClipboard(text: String) {
    window.navigator.clipboard.writeText(text)
}

public actual fun triggerGC() {
    // Do nothing
}
