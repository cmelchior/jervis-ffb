package com.jervisffb.engine.rules.bb2025.procedures.tables.injury

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetBallLocation
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.commands.SetCurrentBall
import com.jervisffb.engine.commands.SetPlayerState
import com.jervisffb.engine.commands.SetTurnOver
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.SetContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.TurnOver
import com.jervisffb.engine.model.context.ActivatePlayerContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2025.procedures.skills.SafePairOfHandsStep
import com.jervisffb.engine.rules.common.procedures.Bounce
import com.jervisffb.engine.rules.common.procedures.tables.injury.RiskingInjuryContext

/**
 * Resolve a player being Place Prone.
 *
 * See page 39 in the BB2025 rulebook.
 */
object BB2025PlacedProne: Procedure() {
    override val initialNode: Node = ChooseToUseSafePairOfHands
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? {
        return when (state.currentBallOrNull() != null) {
            true -> SetCurrentBall(null)
            false -> null
        }
    }
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<RiskingInjuryContext>()
    }

    object ChooseToUseSafePairOfHands: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = SafePairOfHandsStep
        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(PlayerPlacedProne)
        }
    }

    object PlayerPlacedProne: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<RiskingInjuryContext>()
            val activePlayerContext = state.getContext<ActivatePlayerContext>()
            val player = context.player
            return buildCompositeCommand {
                add(SetPlayerState(player, PlayerState.PRONE, hasTackleZones = false))
                // Being placed prone is only a turnover if it is the active player and they hold the ball.
                // If they do not hold the ball, their activation still ends immediately.
                val isActive = (activePlayerContext.player == player)
                val hasBall = player.hasBall()
                if (isActive) {
                    add(SetContext(activePlayerContext.copy(activationEndsImmediately = true)))
                }
                if (hasBall) {
                    val ball = player.ball!!
                    addAll(
                        SetCurrentBall(ball),
                        SetBallLocation(ball, player.coordinates),
                        SetBallState.bouncing(ball),
                    )
                }
                if (isActive && hasBall) {
                    add(SetTurnOver(TurnOver.STANDARD))
                }
                if (hasBall) {
                    add(GotoNode(BounceBall))
                } else {
                    add(ExitProcedure())
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
