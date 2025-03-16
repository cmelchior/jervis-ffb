package com.jervisffb.engine

import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.reports.LogCategory
import com.jervisffb.engine.reports.LogEntry

/**
 * Class encapsulating changes to the model state happening due to a single
 * [GameAction]. It captures all changes from one user facing action to the
 * next. This means that e.g. [com.jervisffb.engine.actions.Continue] events
 * that are applied internally are considered part of the current Action, so
 * all commands executed due to this are considered part of the current
 * [ActionStep]
 *
 * [com.jervisffb.engine.actions.CompositeGameAction]s will create an
 * [ActionStep] for each action it consists of.
 */
data class ActionStep(
    val action: GameAction,
    val procedure: Procedure,
    val node: Node,
    // Commands are flattened, i.e., a hierarchy of CompositeCommands is unrolled into a single long list.
    val commands: List<Command>,
) {
    val logs: List<LogEntry> = commands.filterIsInstance<LogEntry>()
    val gameProgress: List<LogEntry> = commands
        .filterIsInstance<LogEntry>()
        .filter { it.category == LogCategory.GAME_PROGRESS }
}
