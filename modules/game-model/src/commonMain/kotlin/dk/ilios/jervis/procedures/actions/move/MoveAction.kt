package dk.ilios.jervis.procedures.actions.move

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.Cancel
import dk.ilios.jervis.actions.EndAction
import dk.ilios.jervis.actions.EndActionWhenReady
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.MoveTypeSelected
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.commands.GotoNode
import dk.ilios.jervis.commands.SetAvailableActions
import dk.ilios.jervis.commands.SetOldContext
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.context.MoveContext
import dk.ilios.jervis.procedures.getSetPlayerRushesCommand
import dk.ilios.jervis.reports.ReportActionEnded
import dk.ilios.jervis.rules.PlayerActionType
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.utils.INVALID_ACTION
import dk.ilios.jervis.utils.INVALID_GAME_STATE
import kotlinx.serialization.Serializable

/**
 * Procedure controlling a Move action as described on page XX in the rulebook.
 */
@Serializable
object MoveAction : Procedure() {
    override val initialNode: Node = SelectMoveType

    override fun onEnterProcedure(
        state: Game,
        rules: Rules,
    ): Command {
        val player = state.activePlayer ?: INVALID_GAME_STATE("No active player")
        return getSetPlayerRushesCommand(rules, player)
    }

    override fun onExitProcedure(
        state: Game,
        rules: Rules,
    ): Command {
        val activeTeam = state.activeTeam
        return compositeCommandOf(
            SetAvailableActions(activeTeam, PlayerActionType.MOVE, activeTeam.turnData.moveActions - 1 ),
            ReportActionEnded(state.activePlayer!!, state.activePlayerAction!!)
        )
    }

    object SelectMoveType : ActionNode() {
        override fun getAvailableActions(
            state: Game,
            rules: Rules,
        ): List<ActionDescriptor> {
            val moveOptions = calculateMoveTypesAvailable(state.activePlayer!!, rules)
            return moveOptions + listOf(EndActionWhenReady)
        }

        override fun applyAction(
            action: GameAction,
            state: Game,
            rules: Rules,
        ): Command {
            return when (action) {
                is EndAction -> ExitProcedure()
                is MoveTypeSelected -> {
                    compositeCommandOf(
                        SetOldContext(
                            Game::moveContext,
                            MoveContext(state.activePlayer!!, action.moveType),
                        ),
                        GotoNode(ResolveMoveType)
                    )
                }
                is Cancel -> ExitProcedure() // End action
                else -> INVALID_ACTION(action)
            }
        }
    }

    object ResolveMoveType : ParentNode() {
        override fun getChildProcedure(
            state: Game,
            rules: Rules,
        ): Procedure = MoveTypeSelectorStep

        override fun onExitNode(
            state: Game,
            rules: Rules,
        ): Command {
            return if (state.isTurnOver) {
                ExitProcedure()
            } else {
                GotoNode(SelectMoveType)
            }
        }
    }
}
