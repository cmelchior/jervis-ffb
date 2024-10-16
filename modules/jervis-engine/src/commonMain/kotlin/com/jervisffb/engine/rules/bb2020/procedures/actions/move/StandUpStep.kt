package com.jervisffb.engine.rules.bb2020.procedures.actions.move

import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetContext
import com.jervisffb.engine.commands.SetPlayerMoveLeft
import com.jervisffb.engine.commands.SetPlayerState
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.context.MoveContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * Procedure for handling a prone player standing up as part of a Move, Blitz, Pass, Hand-Off or Foul action.
 * See page 44 in the rulebook.
 *
 * Moving normally are handled in [ResolveMoveTypeStep]
 * Jumping are handled in [JumpStep]
 */
object StandUpStep : Procedure() {
    override val initialNode: Node = AttemptToStandUpAutomatically
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        val context = state.getContext<MoveContext>()
        val player = context.player
        if (player.state != PlayerState.PRONE) {
            INVALID_GAME_STATE("Player ${player.name} must be prone: ${player.state}")
        }
        if (context.moveType != MoveType.STAND_UP) {
            INVALID_GAME_STATE("Move type ${context.moveType} must be ${MoveType.STAND_UP}")
        }
    }

    // If Player has 3+ movement, they stand up automatically,
    // otherwise they need to roll for it
    object AttemptToStandUpAutomatically : ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<MoveContext>()
            val movingPlayer = context.player
            return if (movingPlayer.movesLeft >= 3) {
                compositeCommandOf(
                    SetPlayerState(movingPlayer, PlayerState.STANDING, hasTackleZones = true),
                    SetPlayerMoveLeft(movingPlayer, movingPlayer.movesLeft - 3),
                    SetContext(context.copy(hasMoved = true)),
                    ExitProcedure()
                )
            } else {
                GotoNode(RollForStandingUp)
            }
        }
    }

    object RollForStandingUp : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            TODO("Not yet implemented")
        }

        override fun onExitNode(state: Game, rules: Rules): Command {
            TODO("Not yet implemented")
        }
    }
}
