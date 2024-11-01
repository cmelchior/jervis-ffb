package com.jervisffb.test

import com.jervisffb.engine.utils.combinations
import kotlin.test.Test

class UtilTests {

    @Test
    fun combinations() {
        // Smoke test for List.combinations(size)
        val list = listOf(1, 2, 3, 4)
        val combinations = list.combinations(3)
        println(combinations)
    }
}
