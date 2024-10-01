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
import dk.ilios.jervis.commands.RemoveContext
import dk.ilios.jervis.commands.SetBallLocation
import dk.ilios.jervis.commands.SetBallState
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.commands.SetCurrentBall
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
import dk.ilios.jervis.model.TurnOver
import dk.ilios.jervis.model.context.MoveContext
import dk.ilios.jervis.model.context.ProcedureContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.procedures.ActivatePlayerContext
import dk.ilios.jervis.procedures.Catch
import dk.ilios.jervis.procedures.actions.move.ResolveMoveTypeStep
import dk.ilios.jervis.procedures.actions.move.calculateMoveTypesAvailable
import dk.ilios.jervis.procedures.getSetPlayerRushesCommand
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.utils.INVALID_ACTION
import dk.ilios.jervis.utils.INVALID_GAME_STATE
import kotlinx.serialization.Serializable


data class ThrowTeamMateContext(
    val thrower: Player,
    val catcher: Player? = null,
    val hasMoved: Boolean = false,
) : ProcedureContext

/**
 * Procedure for controlling a player's Hand-off action.
 * See page 51 in the rulebook.
 */
@Serializable
object ThrowTeamMateAction : Procedure() {
    override val initialNode: Node = MoveOrHandOffOrEndAction
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        val player = state.activePlayer!!
        return compositeCommandOf(
            getSetPlayerRushesCommand(rules, player),
            SetContext(ThrowTeamMateContext(player))
        )
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        val context = state.getContext<ThrowTeamMateContext>()
        return compositeCommandOf(
            RemoveContext<ThrowTeamMateContext>(),
            SetContext(state.getContext<ActivatePlayerContext>().copy(markActionAsUsed = context.hasMoved))
        )
    }
    override fun isValid(state: Game, rules: Rules) {
        if (state.activePlayer == null) INVALID_GAME_STATE("No active player")
    }

    object MoveOrHandOffOrEndAction : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<ThrowTeamMateContext>().thrower.team
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            val context = state.getContext<ThrowTeamMateContext>()
            val options = mutableListOf<ActionDescriptor>()

            // Find possible move types
            options.addAll(calculateMoveTypesAvailable(context.thrower, rules))

            // Check if adjacent to a possible receiver
            if (context.thrower.hasBall()) {
                context.thrower.coordinates.getSurroundingCoordinates(rules, 1)
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
            val handOffContext = state.getContext<ThrowTeamMateContext>()
            return when (action) {
                EndAction -> ExitProcedure()
                is MoveTypeSelected -> {
                    val moveContext = MoveContext(handOffContext.thrower, action.moveType)
                    compositeCommandOf(
                        SetContext(handOffContext.copy(hasMoved = true)),
                        SetContext(moveContext),
                        GotoNode(ResolveMove)
                    )
                }
                is PlayerSelected -> {
                    val ball = handOffContext.thrower.ball!!
                    compositeCommandOf(
                        SetContext(handOffContext.copy(catcher = action.getPlayer(state))),
                        SetBallState.accurateThrow(ball),
                        SetBallLocation(ball, action.getPlayer(state).coordinates),
                        GotoNode(ResolveCatch)
                    )
                }

                else -> INVALID_ACTION(action)
            }
        }
    }

    object ResolveMove : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ResolveMoveTypeStep
        override fun onExitNode(state: Game, rules: Rules): Command {
            // If player is not standing on the field after the move, it is a turn over,
            // otherwise they are free to continue their hand-off.
            val context = state.getContext<ThrowTeamMateContext>()
            return if (!context.thrower.isStanding(rules)) {
                compositeCommandOf(
                    SetTurnOver(TurnOver.STANDARD),
                    ExitProcedure()
                )
            } else {
                GotoNode(MoveOrHandOffOrEndAction)
            }
        }
    }

    object ResolveCatch : ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            return SetCurrentBall(state.getContext<ThrowTeamMateContext>().thrower.ball!!)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = Catch
        override fun onExitNode(state: Game, rules: Rules): Command {
            // If no player on the holds the ball after the hand-off is complete, it is a turnover.
            // otherwise the action just ends
            val context = state.getContext<ThrowTeamMateContext>()
            return compositeCommandOf(
                SetCurrentBall(null),
                if (!rules.teamHasBall(context.thrower.team)) SetTurnOver(TurnOver.STANDARD) else null,
                ExitProcedure()
            )
        }
    }
}
