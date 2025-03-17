@file:OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)

package com.jervisffb.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.newSingleThreadContext
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
    return newSingleThreadContext(id)
}

actual fun multiThreadDispatcher(id: String, size: Int): CoroutineDispatcher {
    return newFixedThreadPoolContext(size, id)
}
