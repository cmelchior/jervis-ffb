package com.jervisffb.engine.rules.bb2020.procedures

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.RemoveContext
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.commands.SetContext
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.CatchRollContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.modifiers.CatchModifier
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.model.modifiers.MarkedModifier
import com.jervisffb.engine.reports.ReportCatch
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.actions.move.ScoringATouchDownContext
import com.jervisffb.engine.rules.bb2020.procedures.actions.move.ScoringATouchdown
import com.jervisffb.engine.rules.bb2020.tables.Weather
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * Resolve a player attempting to catch the ball.
 * This procedure assumes that the parent already checked if the cath is valid
 * in the first place.
 */
object Catch : Procedure() {
    override val initialNode: Node = RollToCatch
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        // Determine target and modifiers for the Catch roll
        val ball = state.currentBall()
        val catchingPlayer = state.field[ball.location].player!!
        val diceRollTarget = catchingPlayer.agility
        val modifiers = mutableListOf<DiceModifier>()
        // TODO Convert deflection into Intercept
        if (ball.state == BallState.BOUNCING) modifiers.add(CatchModifier.BOUNCING)
        if (ball.state == BallState.THROW_IN) modifiers.add(CatchModifier.THROW_IN)
        if (ball.state == BallState.DEVIATING) modifiers.add(CatchModifier.DEVIATED)
        if (ball.state == BallState.SCATTERED) modifiers.add(CatchModifier.SCATTERED)
        // TODO Check for disturbing presence.
        // Check for field being marked
        val marks = rules.calculateMarks(state, catchingPlayer.team, ball.location)
        modifiers.add(MarkedModifier(marks))

        // Check the weather
        if (state.weather == Weather.POURING_RAIN) {
            modifiers.add(CatchModifier.POURING_RAIN)
        }

        val rollContext = CatchRollContext(catchingPlayer, diceRollTarget, modifiers)
        return SetContext(rollContext)
    }
    override fun isValid(state: Game, rules: Rules) {
        super.isValid(state, rules)
        // Check that this is only called on a standing player with tackle zones
        val ballLocation = state.currentBall().location
        if (state.field[ballLocation].player == null) {
            INVALID_GAME_STATE("No player available to catch the ball at: $ballLocation")
        }
        if (!rules.canCatch(state, state.field[ballLocation].player!!)) {
            INVALID_GAME_STATE("Player is not eligible for catching the ball at: $ballLocation")
        }
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        return compositeCommandOf(
            RemoveContext<CatchRollContext>(),
        )
    }

    object RollToCatch : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = CatchRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<CatchRollContext>()
            val roll = context.roll!!
            val ball = state.currentBall()
            return if (context.isSuccess) {
                compositeCommandOf(
                    SetBallState.carried(ball, context.catchingPlayer),
                    ReportCatch(context.catchingPlayer, context.target, context.modifiers, roll.result, true),
                    GotoNode(CheckForTouchDown)
                )
            } else {
                compositeCommandOf(
                    SetBallState.bouncing(ball),
                    ReportCatch(context.catchingPlayer, context.target, context.modifiers, roll.result, false),
                    GotoNode(CatchFailed),
                )
            }
        }
    }

    object CatchFailed : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = Bounce
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                ExitProcedure(), // TODO Not 100% sure what to do here?
            )
        }
    }

    object CheckForTouchDown : ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<CatchRollContext>()
            return SetContext(ScoringATouchDownContext(context.catchingPlayer))
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ScoringATouchdown
        override fun onExitNode(state: Game, rules: Rules): Command {
            return ExitProcedure()
        }
    }
}
