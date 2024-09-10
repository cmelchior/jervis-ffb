package dk.ilios.jervis.procedures.actions.handoff

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.EndAction
import dk.ilios.jervis.actions.EndActionWhenReady
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.MoveTypeSelected
import dk.ilios.jervis.actions.PlayerSelected
import dk.ilios.jervis.actions.SelectPlayer
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.SetAvailableActions
import dk.ilios.jervis.commands.SetBallLocation
import dk.ilios.jervis.commands.SetBallState
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.commands.SetOldContext
import dk.ilios.jervis.commands.SetTurnOver
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.commands.fsm.GotoNode
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.PlayerState
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.model.context.MoveContext
import dk.ilios.jervis.model.context.ProcedureContext
import dk.ilios.jervis.procedures.Catch
import dk.ilios.jervis.procedures.actions.move.MoveTypeSelectorStep
import dk.ilios.jervis.procedures.actions.move.calculateMoveTypesAvailable
import dk.ilios.jervis.procedures.getSetPlayerRushesCommand
import dk.ilios.jervis.reports.ReportActionEnded
import dk.ilios.jervis.rules.PlayerActionType
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.utils.INVALID_ACTION
import dk.ilios.jervis.utils.INVALID_GAME_STATE
import kotlinx.serialization.Serializable


data class HandOffContext(
    val thrower: Player,
    val catcher: Player? = null,
    val hasMoved: Boolean = false,
) : ProcedureContext

/**
 * Procedure for controlling a player's Hand-off action.
 * See page 51 in the rulebook.
 */
@Serializable
object HandOffAction : Procedure() {
    override val initialNode: Node = MoveOrHandOffOrEndAction
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        val player = state.activePlayer ?: INVALID_GAME_STATE("No active player")
        return compositeCommandOf(
            getSetPlayerRushesCommand(rules, player),
            SetOldContext(Game::handOffContext, HandOffContext(player))
        )
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        val context = state.handOffContext!!
        return compositeCommandOf(
            SetOldContext(Game::handOffContext, null),
            if (context.hasMoved) {
                val team = context.thrower.team
                SetAvailableActions(team, PlayerActionType.HAND_OFF, team.turnData.handOffActions - 1)
            } else {
                null
            },
            ReportActionEnded(state.activePlayer!!, state.activePlayerAction!!)
        )
    }

    object MoveOrHandOffOrEndAction : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.handOffContext!!.thrower.team
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            val context = state.handOffContext!!
            val options = mutableListOf<ActionDescriptor>()

            // Find possible move types
            options.addAll(calculateMoveTypesAvailable(context.thrower, rules))

            // Check if adjacent to a possible receiver
            if (context.thrower.hasBall()) {
                context.thrower.location.coordinate.getSurroundingCoordinates(rules, 1)
                    .mapNotNull { state.field[it].player }
                    .filter { it.team == context.thrower.team && it.state == PlayerState.STANDING }
                    .forEach {
                        options.add(SelectPlayer(it))
                    }
            }

            // Just end the action
            options.add(EndActionWhenReady)
            return options
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.handOffContext!!
            return when (action) {
                EndAction -> ExitProcedure()
                is MoveTypeSelected -> {
                    val moveContext = MoveContext(context.thrower, action.moveType)
                    compositeCommandOf(
                        SetOldContext(Game::handOffContext, context.copy(hasMoved = true)),
                        SetContext(moveContext),
                        GotoNode(ResolveMove)
                    )
                }
                is PlayerSelected -> {
                    val context = state.handOffContext!!
                    compositeCommandOf(
                        SetOldContext(Game::handOffContext, context.copy(catcher = action.getPlayer(state))),
                        SetBallState.accurateThrow(),
                        SetBallLocation(action.getPlayer(state).location.coordinate),
                        GotoNode(ResolveCatch)
                    )
                }

                else -> INVALID_ACTION(action)
            }
        }
    }

    object ResolveMove : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = MoveTypeSelectorStep
        override fun onExitNode(state: Game, rules: Rules): Command {
            // If player is not standing on the field after the move, it is a turn over,
            // otherwise they are free to continue their hand-off.
            val context = state.handOffContext!!
            return if (!context.thrower.isStanding(rules)) {
                compositeCommandOf(
                    SetTurnOver(true),
                    ExitProcedure()
                )
            } else {
                GotoNode(MoveOrHandOffOrEndAction)
            }
        }
    }

    object ResolveCatch : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = Catch
        override fun onExitNode(state: Game, rules: Rules): Command {
            // If no player on the holds the ball after the hand-off is complete, it is a turnover.
            // otherwise the action just ends
            val context = state.handOffContext!!
            return compositeCommandOf(
                if (!rules.teamHasBall(context.thrower.team)) SetTurnOver(true) else null,
                ExitProcedure()
            )
        }
    }
}
