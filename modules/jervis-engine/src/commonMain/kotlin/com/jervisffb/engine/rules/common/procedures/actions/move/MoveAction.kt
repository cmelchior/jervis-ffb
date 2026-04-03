package com.jervisffb.engine.rules.common.procedures.actions.move

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.EndActionWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.AddContext
import com.jervisffb.engine.commands.context.RemoveContext
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
import com.jervisffb.engine.rules.common.procedures.calculateMoveTypesAvailable
import com.jervisffb.engine.rules.common.procedures.getResetPlayerTemporaryModifiersCommands
import com.jervisffb.engine.rules.common.procedures.getSetPlayerRushesCommand
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * Procedure controlling a Move action.
 *
 * See page 44 in the BB2020 rulebook.
 * See page 54 in the BB2025 rulebook.
 */
object MoveAction : Procedure() {
    override val initialNode: Node = SelectMoveType
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        val player = state.activePlayer!!
        return getSetPlayerRushesCommand(rules, player)
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        return compositeCommandOf(
            *getResetPlayerTemporaryModifiersCommands(state, rules, state.activePlayer!!, Duration.END_OF_ACTION),
        )
    }
    override fun isValid(state: Game, rules: Rules) {
        if (state.activePlayer == null) INVALID_GAME_STATE("No active player")
    }

    object SelectMoveType : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.activePlayer!!.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val moveOptions = calculateMoveTypesAvailable(state, state.activePlayer!!)
            return buildList {
                moveOptions?.let { add(it) }
                add(EndActionWhenReady)
            }
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                is EndAction -> {
                    compositeCommandOf(
                        ExitProcedure()
                    )
                }
                is MoveTypeSelected -> {
                    if (calculateMoveTypesAvailable(state, state.activePlayer!!)?.types?.contains(action.moveType) != true) {
                        INVALID_ACTION(action)
                    }
                    compositeCommandOf(
                        AddContext(MoveContext(state.activePlayer!!, action.moveType)),
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
            return buildCompositeCommand {
                add(RemoveContext<MoveContext>())
                when (state.endActionImmediately()) {
                    true -> add(ExitProcedure())
                    false -> add(GotoNode(SelectMoveType))
                }
            }
        }
    }
}
