package com.jervisffb.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

actual fun <T> runBlocking(context: CoroutineContext?, block: suspend CoroutineScope.() -> T): T {
    TODO("runBlocking not supported yet in wasm")
//    var isDone = false
//    var result: T? = null
//    val promise = GlobalScope.promise { block() }.finally { it
//        isDone = true
//        result = it
//    }
//    while (promise)
}

actual fun singleThreadDispatcher(id: String): CoroutineDispatcher {
    // WASM is kinda special, so for now, just return a default thread.
    // See https://slack-chats.kotlinlang.org/t/22923399/doesn-t-kotlinx-coroutines-support-true-multi-threading-on-w
    return Dispatchers.Default
}

actual fun multiThreadDispatcher(id: String, size: Int): CoroutineDispatcher {
    // WASM is kinda special, so for now, just return a default thread.
    // See https://slack-chats.kotlinlang.org/t/22923399/doesn-t-kotlinx-coroutines-support-true-multi-threading-on-w
    return Dispatchers.Default
}
