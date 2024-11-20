package com.jervisffb.utils

import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

expect fun <T> runBlocking(context: CoroutineContext? = null, block: suspend CoroutineScope.() -> T): T
