package com.jervisffb.engine.rules.bb2025.procedures.actions

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.commands.SetTurnOver
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.SetContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.TurnOver
import com.jervisffb.engine.model.context.PickupRollContext
import com.jervisffb.engine.model.context.SecureTheBallRollContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.reports.ReportSecuredTheBallResult
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.Bounce

/**
 * Procedure for when a player moved into the ball, and must now attempt to
 * secure it.
 *
 * The result is stored in [SecureTheBallContext], it is up to the caller to
 * handle it.
 */
object SecureTheBallStep: Procedure() {
    override val initialNode: Node = RollToSecureBall
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        val ball = state.currentBall()
        val securingPlayer = state.field[ball.location].player!!
        val modifiers = emptyList<DiceModifier>() // Currently, no known modifiers affect this roll
        val rollContext = PickupRollContext(securingPlayer, modifiers)
        return SetContext(rollContext)
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        return RemoveContext<SecureTheBallRollContext>()
    }
    override fun isValid(state: Game, rules: Rules) {
        if (state.currentBall().state != BallState.ON_GROUND) {
            throw IllegalStateException("Ball is not on the ground, but ${state.currentBall().state}")
        }
        if (state.activePlayer?.location != state.currentBall().location) {
            throw IllegalStateException(
                "Active player is not on the ball: ${state.activePlayer?.location} vs. ${state.currentBall().location}",
            )
        }
    }

    object RollToSecureBall : ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val actionContext = state.getContext<SecureTheBallContext>()
            return SetContext(SecureTheBallRollContext(actionContext.player))
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = SecureTheBallRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val rollContext = state.getContext<SecureTheBallRollContext>()
            val actionContext = state.getContext<SecureTheBallContext>()
            val ball = state.currentBall()
            return if (rollContext.isSuccess) {
                compositeCommandOf(
                    SetBallState.carried(ball, rollContext.player),
                    SetContext(actionContext.copy(roll = rollContext)),
                    ReportSecuredTheBallResult(rollContext),
                    ExitProcedure()
                )
            } else {
                compositeCommandOf(
                    SetBallState.bouncing(ball),
                    SetContext(actionContext.copy(roll = rollContext)),
                    ReportSecuredTheBallResult(rollContext),
                    GotoNode(SecuringTheBallFailed),
                )
            }
        }
    }

    object SecuringTheBallFailed : ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command? {
            // If it was the active player that failed to secure the ball, it is a turnover regardless
            // of where the ball ends up.
            return state.activePlayer?.let { SetTurnOver(TurnOver.STANDARD) }
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = Bounce
        override fun onExitNode(state: Game, rules: Rules): Command = ExitProcedure()
    }
}
