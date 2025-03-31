package com.jervisffb.utils

import kotlinx.browser.window

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


