package com.jervisffb.test

import com.jervisffb.engine.utils.combinations
import kotlin.test.Test
import kotlin.test.assertTrue

class UtilTests {

    @Test
    fun combinations() {
        // Smoke test for List.combinations(size)
        val list = listOf(1, 2, 3, 4)
        val combinations = list.combinations(3)

        val expectedCombinations = listOf(
            setOf(1, 2, 3),
            setOf(1, 2, 4),
            setOf(1, 3, 4),
            setOf(2, 3, 4),
        )
        assertTrue(combinations.containsAll(expectedCombinations))
        assertTrue(expectedCombinations.containsAll(combinations))
    }
}
