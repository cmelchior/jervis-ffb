package dk.ilios.jervis.utils

import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

actual fun <T> runBlocking(context: CoroutineContext, block: suspend CoroutineScope.() -> T): T {
    TODO()
    // return GlobalScope.promise { block() }
}
