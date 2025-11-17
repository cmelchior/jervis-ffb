package com.jervisffb.engine.rules.bb2025.procedures.actions

import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.EndActionWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetTurnOver
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.SetContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.TurnOver
import com.jervisffb.engine.model.context.MoveContext
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.SecureTheBallRollContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.ActivatePlayerContext
import com.jervisffb.engine.rules.common.procedures.actions.foul.FoulContext
import com.jervisffb.engine.rules.common.procedures.actions.move.ResolveMoveTypeStep
import com.jervisffb.engine.rules.common.procedures.calculateMoveTypesAvailable
import com.jervisffb.engine.rules.common.procedures.getSetPlayerRushesCommand
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import com.jervisffb.engine.utils.addIfNotNull


data class SecureTheBallContext(
    val player: Player,
    val hasMoved: Boolean = false,
    val roll: SecureTheBallRollContext? = null,
    val securedTheBall: Boolean = false
) : ProcedureContext

/**
 * Procedure for controlling a player's Secure the Ball action.
 *
 * See page 59 in the BB2025 rulebook.
 *
 * Developer's Commentary:
 * It is currently a bit unclear which modifiers (if any) apply to rolling for
 * securing the ball. For now, we assume that the wording "they will
 * automatically pick up the ball" means that this is a straight 2+ roll
 * with no modifiers - This roll is specifically NOT a pickup-roll.
 */
object SecureTheBallAction : Procedure() {
    override val initialNode: Node = MoveOrEndAction
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        val player = state.activePlayer ?: INVALID_GAME_STATE("No active player")
        return compositeCommandOf(
            getSetPlayerRushesCommand(rules, player),
            SetContext(SecureTheBallContext(player))
        )
    }

    /**
     * We report the result of the Secure the Ball action in [SecureTheBallStep].
     */
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        val context = state.getContext<SecureTheBallContext>()
        val activePlayerContext = state.getContext<ActivatePlayerContext>()
        return compositeCommandOf(
            RemoveContext<FoulContext>(),
            SetContext(
                activePlayerContext.copy(
                    markActionAsUsed = context.hasMoved
                )
            )
        )
    }

    // Move the player. If the player moves into the ball, Securing the ball is handled as part of the move
    object MoveOrEndAction : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<SecureTheBallContext>().player.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return buildList<GameActionDescriptor> {
                // Find possible move types
                addIfNotNull(calculateMoveTypesAvailable(state, state.activePlayer!!))
                // End action before picking up the ball. If no move has happened, we do not count the action as used.
                add(EndActionWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<SecureTheBallContext>()
            return when (action) {
                EndAction -> ExitProcedure()
                is MoveTypeSelected -> {
                    val moveContext = MoveContext(context.player, action.moveType)
                    compositeCommandOf(
                        SetContext(context.copy(hasMoved = true)),
                        SetContext(moveContext),
                        GotoNode(ResolveMove)
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
            // otherwise they are free to continue their action
            val moveContext = state.getContext<MoveContext>()
            val secureBallContext = state.getContext<SecureTheBallContext>()
            val rollForSecureTheBall = (secureBallContext.roll != null)
            val endNow = state.endActionImmediately()
            return buildCompositeCommand {
                if (moveContext.hasMoved) {
                    add(SetContext(moveContext.copy(hasMoved = true)))
                }
                if (endNow) {
                    // Some turnover happened while securing the ball.
                    // This does not include falling over due to movement
                    add(ExitProcedure())
                } else if (!rules.isStanding(moveContext.player)) {
                    // ResolveMoveStep does explicitely not handle these turnovers, so do that here.
                    add(SetTurnOver(TurnOver.STANDARD))
                    add(ExitProcedure())
                } else if (rollForSecureTheBall) {
                    // Once we roll for securing the ball, activation ends
                    add(ExitProcedure())
                } else {
                    add(GotoNode(MoveOrEndAction))
                }
            }
        }
    }
}

