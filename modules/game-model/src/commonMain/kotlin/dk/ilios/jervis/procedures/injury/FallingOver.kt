package dk.ilios.jervis.procedures.injury

import compositeCommandOf
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.commands.GotoNode
import dk.ilios.jervis.commands.SetBallLocation
import dk.ilios.jervis.commands.SetBallState
import dk.ilios.jervis.commands.SetTurnOver
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
        return if (context.player.hasBall()) {
            compositeCommandOf(
                SetBallState.bouncing(),
                SetBallLocation(context.player.location.coordinate),
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
            return if (state.ball.state == BallState.BOUNCING) {
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
