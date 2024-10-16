package com.jervisffb.engine.fsm

import com.jervisffb.engine.actions.ActionDescriptor
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.Rules

/**
 * Node type that represents the need of a users "action" to progress.
 *
 * Action "requests" are described using [ActionDescriptor], while the actual user action
 * is represented using a [GameAction]
 */
abstract class ActionNode : Node {

    /**
     * Returns which team are responsible for sending an action to [applyAction], `null` if
     * an action doesn't have a clear "owner". In that case, it will generally be the
     * home team who is responsible for providing the action, but it is not guaranteed.
     *
     * Developer's Commentary:
     * We need to have a way to tell the rest of the system who is responsible for
     * creating the [GameAction]. It might technically be more correct to store this
     * inside [Game] or the [ActionDescriptor], but either of these approaches
     * would make the code quite a bit more convoluted. So for now, we are just
     * treating it as metadata that are part of a node, similar to [Procedure.initialNode]
     *
     * This approach also assumes that any given node only accepts input from
     * one player. Which (for now) seems a reasonable restriction.
     */
    abstract fun actionOwner(state: Game, rules: Rules): Team?

    /**
     * Returns the set of valid [GameAction]s that will be accepted by this node.
     */
    abstract fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor>

    /**
     * Calculate all changes that will happen as a consequence of applying a specific
     * [GameAction] to this node.
     *
     * Warning: It is up to implementers of this method to also modify the [Procedure] state in order
     * to progress the [ProcedureStack]. The most common ways of doing this are through
     * [com.jervisffb.engine.commands.fsm.GotoNode] or [com.jervisffb.engine.commands.fsm.ExitProcedure]
     * commands. Failing to do this will result in the FSM not progressing and just appear stuck.
     */
    abstract fun applyAction(action: GameAction, state: Game, rules: Rules): Command
}
