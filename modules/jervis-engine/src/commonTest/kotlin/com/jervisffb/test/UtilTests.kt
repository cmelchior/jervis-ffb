package com.jervisffb.test

import com.jervisffb.engine.utils.allCombinations
import com.jervisffb.engine.utils.cartesianProduct
import com.jervisffb.engine.utils.combinations
import kotlin.test.Test
import kotlin.test.assertEquals
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

    @Test
    fun allCombinations() {
        // Smoke test for List.allCombinations(size)
        val list = listOf(1, 2, 3)
        val combinations = list.allCombinations().toSet()

        val expectedCombinations = setOf(
            listOf(1),
            listOf(2),
            listOf(3),
            listOf(1, 2),
            listOf(1, 3),
            listOf(2, 3),
            listOf(1, 2, 3),
        )

        assertEquals(expectedCombinations.size, combinations.size)
        expectedCombinations.forEach {
            assertTrue(combinations.contains(it), "failed: $it")
        }
    }

    @Test
    fun testCartesianProduct() {
        val input = listOf(
            listOf(1, 2, 3),
            listOf("a", "b", "c"),
        )
        val result = cartesianProduct(input, 1)
        assertEquals(9, result.size)
        val expectedCombinations = listOf(
            listOf(1, "a"),
            listOf(1, "b"),
            listOf(1, "c"),
            listOf(2, "a"),
            listOf(2, "b"),
            listOf(2, "c"),
            listOf(3, "a"),
            listOf(3, "b"),
            listOf(3, "c"),
        )
        expectedCombinations.forEachIndexed { index, it ->
            assertTrue(it.containsAll(result[index]), "failed at index: $index")
            assertTrue(result[index].containsAll(it), "failed at index: $index")
        }
    }
}
