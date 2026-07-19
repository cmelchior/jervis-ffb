package com.jervisffb.engine.fsm

/**
 * Generic interface for all nodes that make up a [Procedure].
 */
interface Node {
    fun name(): String = this::class.simpleName ?: error("Anonymous node has no name. Override name() to provide a custom name.")
}
