package com.jervisffb.engine.rules.bb2020.procedures.actions.handoff

import com.jervisffb.engine.actions.ActionDescriptor
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.EndActionWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.RemoveContext
import com.jervisffb.engine.commands.SetBallLocation
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.commands.SetContext
import com.jervisffb.engine.commands.SetCurrentBall
import com.jervisffb.engine.commands.SetTurnOver
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.TurnOver
import com.jervisffb.engine.model.context.MoveContext
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.ActivatePlayerContext
import com.jervisffb.engine.rules.bb2020.procedures.Catch
import com.jervisffb.engine.rules.bb2020.procedures.actions.move.ResolveMoveTypeStep
import com.jervisffb.engine.rules.bb2020.procedures.actions.move.calculateMoveTypesAvailable
import com.jervisffb.engine.rules.bb2020.procedures.getSetPlayerRushesCommand
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import com.jervisffb.rules.bb2020.procedures.actions.handoff.ThrowTeamMateContext
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
        val player = state.activePlayer!!
        return compositeCommandOf(
            getSetPlayerRushesCommand(rules, player),
            SetContext(HandOffContext(player))
        )
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        val context = state.getContext<HandOffContext>()
        val activePlayerContext = state.getContext<ActivatePlayerContext>()
        return compositeCommandOf(
            RemoveContext<ThrowTeamMateContext>(),
            SetContext(activePlayerContext.copy(markActionAsUsed = context.hasMoved || context.catcher != null))
        )
    }
    override fun isValid(state: Game, rules: Rules) {
        if (state.activePlayer == null) INVALID_GAME_STATE("No active player")
    }

    object MoveOrHandOffOrEndAction : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<HandOffContext>().thrower.team
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            val context = state.getContext<HandOffContext>()
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
            val handOffContext = state.getContext<HandOffContext>()
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
            val moveContext = state.getContext<MoveContext>()
            val context = state.getContext<HandOffContext>()
            return buildCompositeCommand {
                if (moveContext.hasMoved) {
                    add(SetContext(context.copy(hasMoved = true)))
                }
                if (state.isTurnOver()) {
                    add(ExitProcedure())
                } else if (!context.thrower.isStanding(rules)) {
                    add(SetTurnOver(TurnOver.STANDARD))
                    add(ExitProcedure())
                } else {
                    add(GotoNode(com.jervisffb.engine.rules.bb2020.procedures.actions.handoff.HandOffAction.MoveOrHandOffOrEndAction))
                }
            }
        }
    }

    object ResolveCatch : ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<HandOffContext>()
            // Only one ball should be present on the field at
            val ball = state.field[context.catcher!!.coordinates].balls.singleOrNull() ?: INVALID_GAME_STATE("Multiple balls in ${context.catcher.location}")
            return SetCurrentBall(ball)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = Catch
        override fun onExitNode(state: Game, rules: Rules): Command {
            // If no player on the holds the ball after the hand-off is complete, it is a turnover.
            // otherwise the action just ends
            val context = state.getContext<HandOffContext>()
            return compositeCommandOf(
                SetCurrentBall(null),
                if (!rules.teamHasBall(context.thrower.team)) SetTurnOver(TurnOver.STANDARD) else null,
                ExitProcedure()
            )
        }
    }
}
