package com.jervisffb.engine.rules.bb2020.procedures

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.RemoveContext
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.commands.SetContext
import com.jervisffb.engine.commands.SetTurnOver
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.TurnOver
import com.jervisffb.engine.model.context.PickupRollContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.model.modifiers.MarkedModifier
import com.jervisffb.engine.model.modifiers.PickupModifier
import com.jervisffb.engine.reports.ReportPickup
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.actions.move.ResolveMoveTypeStep
import com.jervisffb.engine.rules.bb2020.tables.Weather

/**
 * Resolve a Pickup, i.e., when a player moves into a field where the ball is placed.
 * See page 46 in the rulebook.
 *
 * Scoring is checked in [ResolveMoveTypeStep] to avoid duplicating this check across
 * all procedures reacting to a player being moved.
 */
object Pickup : Procedure() {
    override val initialNode: Node = RollToPickup
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        // Determine target and modifiers for the Catch roll
        val ball = state.currentBall()
        // TODO java.lang.ArrayIndexOutOfBoundsException: Index 2147483647 out of bounds for length 26
        //  at com.jervisffb.engine.model.Field.get(Field.kt:27)
        val pickupPlayer = state.field[ball.location].player!!
        val diceRollTarget = pickupPlayer.agility
        val modifiers = mutableListOf<DiceModifier>()

        // Check for field being marked
        val marks = rules.calculateMarks(state, pickupPlayer.team, ball.location)
        modifiers.add(MarkedModifier(marks))

        // Other modifiers, like disturbing presence?

        // Weather
        if (state.weather == Weather.POURING_RAIN) {
            modifiers.add(PickupModifier.POURING_RAIN)
        }

        val rollContext = PickupRollContext(pickupPlayer, modifiers)
        return compositeCommandOf(
            SetContext(rollContext),
        )
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        return compositeCommandOf(
            RemoveContext<PickupRollContext>()
        )
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

    object RollToPickup : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = PickupRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val result = state.getContext<PickupRollContext>()
            val ball = state.currentBall()
            return if (result.isSuccess) {
                compositeCommandOf(
                    SetBallState.carried(ball, result.player),
                    ReportPickup(result.player, result.target, result.modifiers, result.roll!!.result, true),
                    ExitProcedure()
                )
            } else {
                compositeCommandOf(
                    SetBallState.bouncing(ball),
                    ReportPickup(result.player, result.target, result.modifiers, result.roll!!.result, false),
                    GotoNode(PickupFailed),
                )
            }
        }
    }

    object PickupFailed : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure =
            Bounce
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                // If it was the active player that failed the pickup, it is a turnover
                state.activePlayer?.let { SetTurnOver(TurnOver.STANDARD) },
                ExitProcedure(), // This is copied from Catch, which has a comment about it.
            )
        }
    }
}
