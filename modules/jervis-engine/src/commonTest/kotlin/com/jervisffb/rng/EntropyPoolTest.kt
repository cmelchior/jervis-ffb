package com.jervisffb.rng

import com.jervisffb.engine.rng.EntropyPool
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertFailsWith

class EntropyPoolTest {

    @Test
    fun generateEntropy() {
        val pool = EntropyPool()
        val input = ByteArray(32) { i -> i.toByte() }

        input.forEach { b -> pool.addEntropy(b) }
        val entropyA = pool.getEntropy()
        input.forEach { b -> pool.addEntropy(b) }
        val entropyB = pool.getEntropy()

        assertContentEquals(entropyA, entropyB)
    }

    @Test
    fun throwIfMissingEntropy() {
        val pool = EntropyPool()
        assertFailsWith<IllegalStateException> { pool.getEntropy() }

        val input = ByteArray(31) { i -> i.toByte() }
        input.forEach { b -> pool.addEntropy(b) }
        assertFailsWith<IllegalStateException> { pool.getEntropy() }
    }
}
