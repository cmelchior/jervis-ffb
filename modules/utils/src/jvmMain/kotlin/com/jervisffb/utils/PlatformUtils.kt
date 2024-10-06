package com.jervisffb.utils

public actual fun threadId(): ULong {
    return Thread.currentThread().id.toULong()
}
