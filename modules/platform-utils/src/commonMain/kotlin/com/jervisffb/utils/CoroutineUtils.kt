@file:OptIn(ExperimentalCoroutinesApi::class)

package com.jervisffb.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.coroutines.CoroutineContext

expect fun <T> runBlocking(context: CoroutineContext? = null, block: suspend CoroutineScope.() -> T): T

/**
 * Returns a dispatcher that is guaranteed to be backed by only a single thread.
 * This can be used to guarantee serialization when adding jobs and messages to queues.
 */
public expect fun singleThreadDispatcher(id: String): CoroutineDispatcher

/**
 * Returns a dispatcher that attempts to allocate the selected number of threads.
 * There is no guarantee that it is possible to allocate the requested number of threads.
 * But at least 1 is created.
 * TODO: Should detect the number of CPU's and allocate threads based on that.
 */
public expect fun multiThreadDispatcher(id: String, size: Int = 8): CoroutineDispatcher
