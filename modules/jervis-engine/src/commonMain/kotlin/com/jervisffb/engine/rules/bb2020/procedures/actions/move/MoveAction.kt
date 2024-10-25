package com.jervisffb.engine.rules.bb2020.procedures.actions.move

import com.jervisffb.engine.actions.ActionDescriptor
import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.EndActionWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetContext
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.MoveContext
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.getSetPlayerRushesCommand
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import kotlinx.serialization.Serializable

/**
 * Procedure controlling a Move action as described on page 44 in the rulebook.
 */
@Serializable
object MoveAction : Procedure() {
    override val initialNode: Node = SelectMoveType
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        val player = state.activePlayer!!
        return getSetPlayerRushesCommand(rules, player)
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        if (state.activePlayer == null) INVALID_GAME_STATE("No active player")
    }

    object SelectMoveType : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.activePlayer!!.team

        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            val moveOptions = calculateMoveTypesAvailable(state.activePlayer!!, rules)
            return moveOptions + listOf(EndActionWhenReady)
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                is EndAction -> {
                    compositeCommandOf(
                        ExitProcedure()
                    )
                }
                is MoveTypeSelected -> {
                    compositeCommandOf(
                        SetContext(MoveContext(state.activePlayer!!, action.moveType)),
                        GotoNode(ResolveMoveType)
                    )
                }
                is Cancel -> ExitProcedure() // End action
                else -> INVALID_ACTION(action)
            }
        }
    }

    object ResolveMoveType : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ResolveMoveTypeStep
        override fun onExitNode(state: Game, rules: Rules): Command {
            return if (state.isTurnOver()) {
                ExitProcedure()
            } else {
                GotoNode(SelectMoveType)
            }
        }
    }
}
