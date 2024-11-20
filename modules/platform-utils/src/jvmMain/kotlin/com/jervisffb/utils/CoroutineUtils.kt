package com.jervisffb.utils

import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

actual fun <T> runBlocking(context: CoroutineContext?, block: suspend CoroutineScope.() -> T): T {
    return if (context != null) {
        kotlinx.coroutines.runBlocking(context) { block() }
    } else {
        kotlinx.coroutines.runBlocking { block() }
    }
}
