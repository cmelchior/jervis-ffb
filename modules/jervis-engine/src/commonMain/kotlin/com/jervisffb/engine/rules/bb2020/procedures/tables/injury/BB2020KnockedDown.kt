package com.jervisffb.engine.rules.bb2020.procedures.tables.injury

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetBallLocation
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.commands.SetCurrentBall
import com.jervisffb.engine.commands.SetPlayerState
import com.jervisffb.engine.commands.SetTurnOver
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.TurnOver
import com.jervisffb.engine.model.context.BB2020MultipleBlockContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.Bounce
import com.jervisffb.engine.rules.common.procedures.tables.injury.RiskingInjuryContext
import com.jervisffb.engine.rules.common.procedures.tables.injury.RiskingInjuryMode
import com.jervisffb.engine.rules.common.procedures.tables.injury.RiskingInjuryRoll
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * Resolve a player being Knocked Down.
 *
 * See page 27 in the BB2020 rulebook.
 */
object BB2020KnockedDown: Procedure() {
    override val initialNode: Node = RollForInjury
    override fun onEnterProcedure(state: Game, rules: Rules): Command? {
        val context = state.getContext<RiskingInjuryContext>()
        // See page 23 in the BB2020 rulebook
        val isOnActiveTeam = (context.player.team == state.activeTeam)
        // It is only a turnover if a thrown player is knocked down holding the ball (see Errata May 2025)
        val hasBall = context.player.hasBall()
        val playerThrown = (context.mode == RiskingInjuryMode.BAD_LANDING)

        return buildCompositeCommand {
            add(SetPlayerState(context.player, PlayerState.KNOCKED_DOWN, hasTackleZones = false))
            if ((isOnActiveTeam && !playerThrown) || (isOnActiveTeam && playerThrown && hasBall)) {
                add(SetTurnOver(TurnOver.STANDARD))
            }
        }
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<RiskingInjuryContext>()
        val context = state.getContext<RiskingInjuryContext>()
        if (context.mode != RiskingInjuryMode.KNOCKED_DOWN && context.mode != RiskingInjuryMode.BAD_LANDING) {
            INVALID_GAME_STATE("Player needs to have a bad landing or be knocked down to use this procedure: ${context.mode}")
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
                    val mbContext = state.getContext<BB2020MultipleBlockContext>()
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

