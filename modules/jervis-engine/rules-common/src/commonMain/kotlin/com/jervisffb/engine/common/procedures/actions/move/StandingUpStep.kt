package com.jervisffb.engine.common.procedures.actions.move

import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetPlayerState
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.AddContext
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.common.commands.SetPlayerMoveLeft
import com.jervisffb.engine.common.context.ActivatePlayerContext
import com.jervisffb.engine.common.context.StandingUpRollContext
import com.jervisffb.engine.common.reports.ReportStandingUp
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PlayerPitchState
import com.jervisffb.engine.model.context.MoveContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import kotlinx.collections.immutable.toPersistentList

/**
 * Procedure for handling a prone player standing up as part of a Move, Blitz, Pass, Hand-Off or Foul action.
 * See page 44 in the rulebook.
 *
 * Moving normally are handled in [ResolveMoveTypeStep]
 * Jumping are handled via [Rules.jumpStep] (the ruleset's concrete `JumpStep`).
 */
object StandingUpStep : Procedure() {
    override val initialNode: Node = AttemptToStandUpAutomatically
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        val context = state.getContext<MoveContext>()
        val player = context.player
        if (player.state != PlayerPitchState.PRONE) {
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
            val hasJumpUp = movingPlayer.isSkillAvailable(SkillType.JUMP_UP)
            return if (movingPlayer.movesLeft >= rules.moveRequiredForStandingUp || hasJumpUp) {
                val adjustedMovesLeft = when (hasJumpUp) {
                    true -> movingPlayer.movesLeft
                    else -> movingPlayer.movesLeft - rules.moveRequiredForStandingUp
                }
                compositeCommandOf(
                    SetPlayerState(movingPlayer, PlayerPitchState.STANDING, hasTackleZones = true),
                    SetPlayerMoveLeft(movingPlayer, adjustedMovesLeft),
                    UpdateContext(context.copy(hasMoved = true)),
                    ExitProcedure()
                )
            } else {
                GotoNode(RollForStandingUp)
            }
        }
    }

    object RollForStandingUp : ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val player = state.getContext<MoveContext>().player
            val modifiers = mutableListOf<DiceModifier>()
            return AddContext(StandingUpRollContext(player, modifiers.toPersistentList()))
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = StandingUpRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val activeContext = state.getContext<ActivatePlayerContext>()
            val moveContext = state.getContext<MoveContext>()
            val context = state.getContext<StandingUpRollContext>()
            return buildCompositeCommand {
                add(RemoveContext(context))
                if (context.isSuccess) {
                    addAll(
                        UpdateContext(moveContext.copy(hasMoved = true)),
                        SetPlayerMoveLeft(context.player, 0),
                        SetPlayerState(context.player, PlayerPitchState.STANDING, hasTackleZones = true),
                    )
                } else {
                    addAll(
                        UpdateContext(moveContext.copy(hasMoved = true)),
                        UpdateContext(activeContext.copy(activationEndsImmediately = true)),
                    )
                }
                addAll(
                    ReportStandingUp(context),
                    ExitProcedure()
                )
            }
        }
    }
}
