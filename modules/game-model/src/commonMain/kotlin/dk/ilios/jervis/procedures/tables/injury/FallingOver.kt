package dk.ilios.jervis.procedures.tables.injury

import compositeCommandOf
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.SetBallLocation
import dk.ilios.jervis.commands.SetBallState
import dk.ilios.jervis.commands.SetTurnOver
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.commands.fsm.GotoNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.BallState
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.context.assertContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.procedures.Bounce
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.utils.INVALID_GAME_STATE

/**
 * Resolve a player falling over as described on page 27 in the rulebook.
 */
object FallingOver: Procedure() {
    override val initialNode: Node = RollForInjury
    override fun onEnterProcedure(state: Game, rules: Rules): Command? {
        val context = state.getContext<RiskingInjuryContext>()
        // Since a ball is only picked up after the player finished current move step in the square,
        // at this point there might stil be a ball on the ground, that will bounce.
        // In case of an active Ball Clone, we might have two balls bouncing.
        return if (context.player.hasBall()) {
            val ball = state.currentBall()
            compositeCommandOf(
                SetBallState.bouncing(ball),
                SetBallLocation(ball, context.player.coordinates),
            )
        } else {
            null
        }
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        return SetTurnOver(true)
    }
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<RiskingInjuryContext>()
        val context = state.getContext<RiskingInjuryContext>()
        if (context.mode != RiskingInjuryMode.FALLING_OVER) {
            INVALID_GAME_STATE("Player needs to be falling over to use this procedure: ${context.mode}")
        }
    }

    object RollForInjury: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = RiskingInjuryRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<RiskingInjuryContext>()
            val ball = state.currentBall()
            return if (ball.state == BallState.BOUNCING) {
                GotoNode(BounceBall)
            } else {
                ExitProcedure()
            }
        }
    }

    object BounceBall: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = Bounce
        override fun onExitNode(state: Game, rules: Rules): Command {
            return ExitProcedure()
        }
    }
}
