package com.jervisffb.test

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.test.assertTrue

@OptIn(ExperimentalContracts::class)
inline fun <reified T : Any> assertTypeOf(obj: Any?): T {
    contract {
        returns() implies (obj is T)
    }
    assertTrue(obj is T, "Expected ${T::class.simpleName}, got ${obj?.let { it::class.simpleName }}")
    return obj
}
