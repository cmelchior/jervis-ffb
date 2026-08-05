package com.jervisffb.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private const val THREAD_STOP_TIMEOUT_MS = 5_000L

class CoroutineUtilsTest {

    /** Runs on [dispatcher] to get hold of the thread it is backed by. */
    private fun threadBehind(dispatcher: kotlinx.coroutines.CoroutineDispatcher): Thread =
        runBlocking { withContext(dispatcher) { Thread.currentThread() } }

    @Test
    fun closingASingleThreadDispatcherStopsItsThread() {
        val threadId = "test-dispatcher"
        val dispatcher = singleThreadDispatcher(threadId)
        val thread = threadBehind(dispatcher)
        assertTrue(thread.name.startsWith(threadId), message = "Thread name was: ${thread.name}")
        assertTrue(thread.isAlive, "the dispatcher should have started a thread")

        dispatcher.closeIfPossible()

        // This is the whole point: cancelling a scope leaves this thread running,
        // closing the dispatcher is what lets it go.
        thread.join(THREAD_STOP_TIMEOUT_MS)
        assertFalse(thread.isAlive, "thread was still alive ${THREAD_STOP_TIMEOUT_MS}ms after closing")
    }

    @Test
    fun closingAMultiThreadDispatcherStopsItsThreads() {
        val dispatcher = multiThreadDispatcher("test-pool", size = 2)
        val thread = threadBehind(dispatcher)
        assertTrue(thread.isAlive)

        dispatcher.closeIfPossible()

        thread.join(THREAD_STOP_TIMEOUT_MS)
        assertFalse(thread.isAlive)
    }

    @Test
    fun sharedDispatchersAreLeftAlone() {
        // Wasm returns Dispatchers.Default from both factories, so closing it there would
        // take down the whole app. Note these report as CloseableCoroutineDispatcher and
        // throw UnsupportedOperationException on close, so the type alone is no guard.
        Dispatchers.Default.closeIfPossible()
        Dispatchers.IO.closeIfPossible()
        Dispatchers.Unconfined.closeIfPossible()

        val ranAfterwards = runBlocking { withContext(Dispatchers.Default) { true } }
        assertTrue(ranAfterwards, "Dispatchers.Default should still be usable")
    }

    @Test
    fun closingTwiceIsHarmless() {
        val dispatcher = singleThreadDispatcher("test-double-close")
        threadBehind(dispatcher)

        dispatcher.closeIfPossible()
        dispatcher.closeIfPossible()
    }
}
