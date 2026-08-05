@file:OptIn(ExperimentalCoroutinesApi::class)

package com.jervisffb.utils

import kotlinx.coroutines.CloseableCoroutineDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi

// Lives apart from `CoroutineUtils.kt`, which has a JVM-specific file of the same
// name; two same-named files with top-level declarations collide on the JVM facade
// class they generate.

/**
 * Releases the threads behind a dispatcher created by [singleThreadDispatcher] or
 * [multiThreadDispatcher].
 *
 * Cancelling a [CoroutineScope] only stops the coroutines running in it — the executor
 * underneath keeps its threads alive. Anything that creates its own dispatcher are also
 * responsible for closing it.
 *
 * Shared dispatchers are left alone, which makes this safe to call on Wasm where both
 * factories return `Dispatchers.Default`.
 *
 * Note that being a [CloseableCoroutineDispatcher] is not enough to tell the two apart:
 * `Dispatchers.Default` is one, and throws `UnsupportedOperationException` if you
 * actually try to close it.
 *
 * Calling this more than once is harmless.
 */
public fun CoroutineDispatcher.closeIfPossible() {
    if (this !is CloseableCoroutineDispatcher) return
    if (this === Dispatchers.Default || this === Dispatchers.Unconfined) return
    try {
        close()
    } catch (_: UnsupportedOperationException) {
        // Dispatchers.Default refuses this way.
    } catch (_: IllegalStateException) {
        // Dispatchers.IO refuses this way. It is JVM-only so it cannot be checked by
        // identity from common code, and coroutines give no way to ask a dispatcher
        // whether closing it will be accepted, so refusals have to be caught. A shared
        // dispatcher owns no threads of ours, so there is nothing to release anyway.
    }
}
