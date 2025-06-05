package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game

/**
 * Class for wrapping multiple commands while still exposing them as one command.
 */
class CompositeCommand private constructor(val commands: List<Command>) : Command {

    class Builder {
        private val commands = mutableListOf<Command>()
        fun add(command: Command) = commands.add(command)
        fun addAll(vararg commands: Command) = commands.forEach { add(it) }
        fun build(): CompositeCommand {
            return CompositeCommand(commands)
        }
    }

    override fun undo(
        state: Game,
    ) {
        for (i in commands.size - 1 downTo 0) {
            commands[i].undo(state)
        }
    }

    override fun execute(
        state: Game,
    ) {
        commands.forEach { it.execute(state) }
    }

    companion object {
        fun create(function: Builder.() -> Unit): Command {
            val builder = Builder()
            function(builder)
            return builder.build()
        }
    }
}
