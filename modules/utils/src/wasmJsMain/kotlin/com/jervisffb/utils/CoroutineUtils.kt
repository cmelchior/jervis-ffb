package com.jervisffb.utils

import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

actual fun <T> runBlocking(context: CoroutineContext, block: suspend CoroutineScope.() -> T): T {
    TODO("runBlocking not supported yet in wasm")
//    var isDone = false
//    var result: T? = null
//    val promise = GlobalScope.promise { block() }.finally { it
//        isDone = true
//        result = it
//    }
//    while (promise)
}
