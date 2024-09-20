package dk.ilios.jervis.procedures.tables.injury

import compositeCommandOf
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.SetBallState
import dk.ilios.jervis.commands.SetCurrentBall
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
 * Resolve a player being knocked down as described on page 27 in the rulebook.
 */
object KnockedDown: Procedure() {
    override val initialNode: Node = RollForInjury
    override fun onEnterProcedure(state: Game, rules: Rules): Command?  = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? {
        val context = state.getContext<RiskingInjuryContext>()
        return if (context.player.team == state.activeTeam) return SetTurnOver(true) else null
    }
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<RiskingInjuryContext>()
        val context = state.getContext<RiskingInjuryContext>()
        if (context.mode != RiskingInjuryMode.KNOCKED_DOWN) {
            INVALID_GAME_STATE("Player needs to be knocked down over to use this procedure: ${context.mode}")
        }
    }

    object RollForInjury: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command? {
            val context = state.getContext<RiskingInjuryContext>()
            return if (context.player.hasBall()) {
                val ball = context.player.ball!!
                compositeCommandOf(
                    SetCurrentBall(ball),
                    SetBallState.bouncing(ball)
                )
            } else null
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = RiskingInjuryRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            return if (state.currentBall().state == BallState.BOUNCING) {
                GotoNode(BounceBall)
            } else {
                compositeCommandOf(
                    SetCurrentBall(null),
                    ExitProcedure()
                )
            }
        }
    }

    object BounceBall: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = Bounce
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                SetCurrentBall(null),
                ExitProcedure()
            )
        }
    }
}

