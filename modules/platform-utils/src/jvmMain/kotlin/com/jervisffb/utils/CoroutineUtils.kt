@file:OptIn(ExperimentalCoroutinesApi::class)

package com.jervisffb.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

actual fun <T> runBlocking(context: CoroutineContext?, block: suspend CoroutineScope.() -> T): T {
    return if (context != null) {
        kotlinx.coroutines.runBlocking(context) { block() }
    } else {
        kotlinx.coroutines.runBlocking { block() }
    }
}

actual fun singleThreadDispatcher(id: String): CoroutineDispatcher {
    return Executors.newSingleThreadExecutor { action: Runnable ->
        Thread(action).apply {
            name = id
            priority = Thread.NORM_PRIORITY
        }
    }.asCoroutineDispatcher()
}

actual fun multiThreadDispatcher(id: String, size: Int): CoroutineDispatcher {
    // Figure out how to incorporate the id into the thread name
    return Executors.newFixedThreadPool(size).asCoroutineDispatcher()
}

