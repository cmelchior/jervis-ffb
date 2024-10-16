package com.jervisffb.engine.commands

/**
 * Creates a [CompositeCommand] from a known list of sub commands. `null` commands
 * will be ignored.
 */
fun compositeCommandOf(vararg commands: Command?): Command {
    return CompositeCommand.create {
        commands.forEach {
            it?.let { add(it) }
        }
    }
}

/**
 * Build a [CompositeCommand] using a declarative approach similar to [buildList] etc.
 */
fun buildCompositeCommand(init: CompositeCommand.Builder.() -> Unit): Command {
    return CompositeCommand.create(init)
}
