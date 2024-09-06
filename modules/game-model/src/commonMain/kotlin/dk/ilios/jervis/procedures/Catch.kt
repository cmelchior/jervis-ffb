package dk.ilios.jervis.procedures

import compositeCommandOf
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.commands.GotoNode
import dk.ilios.jervis.commands.RemoveContext
import dk.ilios.jervis.commands.SetBallState
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.BallState
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.context.CatchRollContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.model.modifiers.CatchModifier
import dk.ilios.jervis.model.modifiers.DiceModifier
import dk.ilios.jervis.model.modifiers.MarkedModifier
import dk.ilios.jervis.reports.ReportCatch
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.tables.Weather
import dk.ilios.jervis.utils.INVALID_GAME_STATE

/**
 * Resolve a player attempting to catch the ball.
 *
 * This can be used as a placeholder during development or testing.
 */
object Catch : Procedure() {
    override val initialNode: Node = RollToCatch

    override fun isValid(
        state: Game,
        rules: Rules,
    ) {
        super.isValid(state, rules)
        // Check that this is only called on a standing player with tacklezones
        val ballLocation = state.ball.location
        if (state.field[ballLocation].player == null) {
            INVALID_GAME_STATE("No player available to catch the ball at: $ballLocation")
        }
        if (!rules.canCatch(state, state.field[ballLocation].player!!)) {
            INVALID_GAME_STATE("Player is not eligible for catching the ball at: $ballLocation")
        }
    }

    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        // Determine target and modifiers for the Catch roll
        val catchingPlayer = state.field[state.ball.location].player!!
        val diceRollTarget = catchingPlayer.agility
        val modifiers = mutableListOf<DiceModifier>()
        // TODO Convert deflection into Intercept
        if (state.ball.state == BallState.BOUNCING) modifiers.add(CatchModifier.BOUNCING)
        if (state.ball.state == BallState.THROW_IN) modifiers.add(CatchModifier.THROW_IN)
        if (state.ball.state == BallState.DEVIATING) modifiers.add(CatchModifier.DEVIATED)
        if (state.ball.state == BallState.SCATTERED) modifiers.add(CatchModifier.SCATTERED)
        // TODO Check for disturbing presence.
        // Check for field being marked
        val marks = rules.calculateMarks(state, catchingPlayer.team, state.ballSquare.coordinates)
        modifiers.add(MarkedModifier(marks * -1))

        // Check the weather
        if (state.weather == Weather.POURING_RAIN) {
            modifiers.add(CatchModifier.POURING_RAIN)
        }

        val rollContext = CatchRollContext(catchingPlayer, diceRollTarget, modifiers)
        return compositeCommandOf(
            SetContext(rollContext),
        )
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
            return if (context.isSuccess) {
                compositeCommandOf(
                    SetBallState.carried(context.catchingPlayer),
                    ReportCatch(context.catchingPlayer, context.target, context.modifiers, roll.result, true),
                    ExitProcedure(),
                )
            } else {
                compositeCommandOf(
                    SetBallState.bouncing(),
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
}
