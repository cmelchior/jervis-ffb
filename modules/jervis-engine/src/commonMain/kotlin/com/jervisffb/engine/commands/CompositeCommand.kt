package com.jervisffb.engine.commands

import com.jervisffb.engine.controller.GameController
import com.jervisffb.engine.model.Game

/**
 * Class for wrapping multiple commands while still exposing them as one command.
 */
class CompositeCommand private constructor(private val commands: List<Command>) : Command {

    class Builder {
        private val commands = mutableListOf<Command>()
        fun add(command: Command) = commands.add(command)
        fun build(): CompositeCommand {
            return CompositeCommand(commands)
        }
    }

    override fun undo(
        state: Game,
        controller: GameController,
    ) {
        for (i in commands.size - 1 downTo 0) {
            commands[i].undo(state, controller)
        }
    }

    override fun execute(
        state: Game,
        controller: GameController,
    ) {
        commands.forEach { it.execute(state, controller) }
    }

    companion object {
        fun create(function: Builder.() -> Unit): Command {
            val builder = Builder()
            function(builder)
            return builder.build()
        }
    }
}
