package com.jervisffb.utils

public actual fun threadId(): ULong {
    return Thread.currentThread().id.toULong()
}

public actual fun getPublicIp(): String {
    TODO()
}

public actual fun getLocalIpAddress(): String {
    TODO()
}
