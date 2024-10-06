package com.jervisffb.engine.fsm

/**
 * Generic interface for all nodes that make up a [Procedure].
 */
interface Node {
    fun name(): String = this::class.simpleName!!
}
