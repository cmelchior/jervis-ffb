package com.jervisffb.engine.reports

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.model.Game
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Developer's Commentary:
 * Compose 1.7.0 should add support for using HTML in strings so it can
 * be shown using Spannables in Compose. This would allow us to theme it.
 * https://issuetracker.google.com/issues/139320238
 */
@OptIn(ExperimentalUuidApi::class)
abstract class LogEntry : Command {
    val id: Uuid = Uuid.random()
    abstract val category: LogCategory
    abstract val message: String

    override fun execute(state: Game) {
        state.addLog(this)
    }

    override fun undo(state: Game) {
        state.removeLog(this)
    }

    override fun toString(): String {
        return "${this::class.simpleName}(id=${id.toHexString()}, category=$category, message='$message')"
    }
}
