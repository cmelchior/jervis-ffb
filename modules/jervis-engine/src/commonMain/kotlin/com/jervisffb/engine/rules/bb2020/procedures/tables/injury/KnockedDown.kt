package com.jervisffb.engine.rules.bb2020.procedures.tables.injury

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetBallLocation
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.commands.SetCurrentBall
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
import com.jervisffb.engine.model.context.MultipleBlockContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.Bounce
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * Resolve a player being knocked down as described on page 27 in the rulebook.
 */
object KnockedDown: Procedure() {
    override val initialNode: Node = RollForInjury
    override fun onEnterProcedure(state: Game, rules: Rules): Command?  = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? {
        val context = state.getContext<RiskingInjuryContext>()
        return if (context.player.team == state.activeTeamOrThrow()) return SetTurnOver(TurnOver.STANDARD) else null
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
            val player = context.player
            return if (player.hasBall()) {
                val ball = player.ball!!
                compositeCommandOf(
                    SetCurrentBall(ball),
                    SetBallState.bouncing(ball),
                    SetBallLocation(ball, player.coordinates),
                )
            } else null
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = RiskingInjuryRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<RiskingInjuryContext>()
            // If we are part of a Multiple Block, the bounce is delayed until later,
            // so in that case, we just knock the ball loose and handle it later, otherwise
            // we resolve it here.
            val isBouncing = state.currentBallOrNull()?.state == BallState.BOUNCING
            return when {
                isBouncing && context.isPartOfMultipleBlock -> {
                    val mbContext = state.getContext<MultipleBlockContext>()
                    compositeCommandOf(
                        SetCurrentBall(null),
                        ExitProcedure()
                    )
                }
                isBouncing && !context.isPartOfMultipleBlock-> {
                    GotoNode(BounceBall)
                }
                else -> {
                    compositeCommandOf(
                        SetCurrentBall(null),
                        ExitProcedure()
                    )
                }
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

