package dk.ilios.jervis.procedures

import compositeCommandOf
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.RemoveContext
import dk.ilios.jervis.commands.SetBallState
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.commands.SetTurnOver
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.commands.fsm.GotoNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.BallState
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.TurnOver
import dk.ilios.jervis.model.context.PickupRollContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.model.modifiers.DiceModifier
import dk.ilios.jervis.model.modifiers.MarkedModifier
import dk.ilios.jervis.model.modifiers.PickupModifier
import dk.ilios.jervis.procedures.actions.move.ScoringATouchDownContext
import dk.ilios.jervis.procedures.actions.move.ScoringATouchdown
import dk.ilios.jervis.reports.ReportPickup
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.tables.Weather

/**
 * Resolve a Pickup, i.e., when a player moves into a field where the ball is placed.
 * See page 46 in the rulebook.
 */
object Pickup : Procedure() {
    override val initialNode: Node = RollToPickup
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        // Determine target and modifiers for the Catch roll
        val ball = state.currentBall()
        val pickupPlayer = state.field[ball.location].player!!
        val diceRollTarget = pickupPlayer.agility
        val modifiers = mutableListOf<DiceModifier>()

        // Check for field being marked
        val marks = rules.calculateMarks(state, pickupPlayer.team, ball.location)
        modifiers.add(MarkedModifier(marks * -1))

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
                    GotoNode(CheckForTouchDown),
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
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = Bounce
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                // If it was the active player that failed the pickup, it is a turnover
                state.activePlayer?.let { SetTurnOver(TurnOver.STANDARD) },
                ExitProcedure(), // This is copied from Catch, which has a comment about it.
            )
        }
    }

    object CheckForTouchDown : ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<PickupRollContext>()
            return SetContext(ScoringATouchDownContext(context.player))
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ScoringATouchdown
        override fun onExitNode(state: Game, rules: Rules): Command {
            return ExitProcedure()
        }
    }
}
