package com.jervisffb.engine.rules.bb2020.procedures.tables.injury

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetBallLocation
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.commands.SetCurrentBall
import com.jervisffb.engine.commands.SetTurnOver
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.TurnOver
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.Bounce
import com.jervisffb.engine.rules.bb2020.procedures.actions.move.MovePlayerIntoSquare
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * Resolve a player falling over as described on page 27 in the rulebook.
 */
object FallingOver: Procedure() {
    override val initialNode: Node = RollForInjury
    override fun onEnterProcedure(state: Game, rules: Rules): Command? {
        val context = state.getContext<RiskingInjuryContext>()
        return buildCompositeCommand {
            /**
             * If the player falling over, carried a ball, they will drop the ball, and it
             * will bounce from this square.
             *
             * In case a ball was lying on the ground in the square the player was falling
             * over in. It will bounce from the square as part of [MovePlayerIntoSquare],
             * so when we get to this procedure and the player drops the ball, there should only
             * be one ball in the square.
             */
            if (context.player.hasBall()) {
                val ball = context.player.ball!!
                addAll(
                    SetBallState.bouncing(ball),
                    SetBallLocation(ball, context.player.coordinates),
                    SetCurrentBall(ball),
                )
            }
            // Falling over results in a turn-over pr. the list of turnovers on page 23 in the rulebook.
            if (context.player.team == state.activeTeam) {
                add(SetTurnOver(TurnOver.STANDARD))
            }
        }
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command? {
        return if (state.currentBallOrNull() != null) SetCurrentBall(null) else null
    }
    override fun isValid(state: Game, rules: Rules) {
        val context = state.getContext<RiskingInjuryContext>()
        if (context.mode != RiskingInjuryMode.FALLING_OVER) {
            INVALID_GAME_STATE("Player needs to be falling over to use this procedure: ${context.mode}")
        }
    }

    object RollForInjury: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = RiskingInjuryRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<RiskingInjuryContext>()
            val ball = state.currentBallOrNull()
            return if (ball?.state == BallState.BOUNCING) {
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
