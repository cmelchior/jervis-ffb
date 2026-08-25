package com.jervisffb.fuzzer.cli.test

import com.jervisffb.fuzzer.cli.runBB2025Standard
import org.junit.Test

/**
 * Just to ensure that the fuzzer doesn't break immediately when run.
 */
class FuzzerSmokeTest {
    @Test
    fun single2025RunWithStatistics() {
        runBB2025Standard(seed = 12345L, enableStatistics = true)
    }
}
