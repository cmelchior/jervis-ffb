package com.jervisffb.engine.rules.common.procedures

import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.ConfirmWhenReady
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.commands.SetTurnOver
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.SetContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.TurnOver
import com.jervisffb.engine.model.context.PickupRollContext
import com.jervisffb.engine.model.context.ScoringATouchDownContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.context.getContextOrNull
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.model.modifiers.PickupModifier
import com.jervisffb.engine.reports.ReportNoBallAffectingAction
import com.jervisffb.engine.reports.ReportPickup
import com.jervisffb.engine.reports.ReportSkillUsed
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.actions.move.ScoringATouchdown
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.rules.common.tables.Weather

/**
 * Resolve a Pickup, i.e., when a player moves into a field where the ball is
 * placed. This procedure requires that the caller has set a current ball.
 *
 * See page 46 in the BB2020 rulebook.
 * See page 57 in the BB2025 rulebook.
 *
 * If the pickup failed, a turnover is triggered. If the pickup succeeded, we also
 * check if the player picking up the ball scored a touchdown.
 *
 * Developer's Commentary:
 * In BB2020, the only restriction is that a player has to move voluntarily into
 * the ball. In BB2025 there is an extra restriction: It also has to be during
 * the player's activation.
 *
 * It is unclear if there are any cases where a player can move voluntarily
 * without the player being active, so for now we assume the logic is the same
 * across BB2020 and BB2025.
 *
 * The order of choosing skills to use is not defined in the rules, so for now
 * we use the following order:
 *
 * 1. Has No Ball?
 * 2. Use Big Hands?
 * 3. Use Extra Arms?
 * 4. Roll for Pickup
 */
object Pickup : Procedure() {
    override val initialNode: Node = CheckForNoBallSkill
    override fun onEnterProcedure(state: Game, rules: Rules): Command? {
        return if (state.getContextOrNull<PickupRollContext>() == null) {
            val ball = state.currentBall()
            val pickupPlayer = state.field[ball.location].player!!
            val rollContext = PickupRollContext(pickupPlayer, ball)
            SetContext(rollContext)
        } else {
            null
        }
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        return RemoveContext<PickupRollContext>()
    }
    override fun isValid(state: Game, rules: Rules) {
        val ball = state.currentBall()
        val playerOnBall = state.field[ball.location].player
        if (ball.state != BallState.ON_GROUND) {
            throw IllegalStateException("Ball is not on the ground, but ${state.currentBall().state}")
        }
        if (playerOnBall?.location != ball.location) {
            throw IllegalStateException(
                "Active player is not on the ball: ${state.activePlayer?.location} vs. ${state.currentBall().location}",
            )
        }
    }

    object CheckForNoBallSkill: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<PickupRollContext>()
            val player = context.player
            val hasNoBall = player.isSkillAvailable(SkillType.NO_BALL)
            return if (hasNoBall) {
                compositeCommandOf(
                    SetBallState.bouncing(context.ball),
                    ReportNoBallAffectingAction(player, ReportNoBallAffectingAction.ActionType.PICKUP),
                    GotoNode(PickupFailed)
                )
            } else {
                GotoNode(ChooseToUseBigHand)
            }
        }
    }

    object ChooseToUseBigHand: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team {
            return state.getContext<PickupRollContext>().player.team
        }
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<PickupRollContext>()
            val player = context.player
            return if (player.isSkillAvailable(SkillType.BIG_HAND)) {
                listOf(ConfirmWhenReady, CancelWhenReady)
            } else {
                listOf(ContinueWhenReady)
            }
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<PickupRollContext>()
            val player = context.player
            val useBigHands = (action == Confirm)
            return compositeCommandOf(
                if (useBigHands) {
                    ReportSkillUsed(player, SkillType.BIG_HAND)
                } else {
                    null
                },
                SetContext(context.copy(useBigHands = useBigHands)),
                GotoNode(ChooseToUseExtraArms)
            )
        }
    }

    object ChooseToUseExtraArms: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team {
            return state.getContext<PickupRollContext>().player.team
        }
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<PickupRollContext>()
            val player = context.player
            return if (player.isSkillAvailable(SkillType.EXTRA_ARMS)) {
                listOf(ConfirmWhenReady, CancelWhenReady)
            } else {
                listOf(ContinueWhenReady)
            }
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<PickupRollContext>()
            val player = context.player
            val useExtraArms = (action == Confirm)
            return compositeCommandOf(
                if (useExtraArms) {
                    ReportSkillUsed(player, SkillType.EXTRA_ARMS)
                } else {
                    null
                },
                SetContext(context.copy(useExtraArms = useExtraArms)),
                GotoNode(CalculateModifiers)
            )
        }
    }

    object CalculateModifiers: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<PickupRollContext>()
            val modifiers = mutableListOf<DiceModifier>()
            if (!context.useBigHands) {
                // Add modifiers for other opponent players marking the field.
                rules.addMarkedModifiers(
                    state,
                    context.player.team,
                    context.ball.location,
                    modifiers,
                    PickupModifier.MARKED
                )
                // Weather
                if (state.weather == Weather.POURING_RAIN) {
                    modifiers.add(PickupModifier.POURING_RAIN)
                }
                // Other modifiers, like disturbing presence?
            }
            if (context.useExtraArms) {
                modifiers.add(PickupModifier.EXTRA_ARMS)
            }

            return compositeCommandOf(
                SetContext(context.copy(modifiers = modifiers)),
                GotoNode(RollToPickup)
            )
        }
    }

    object RollToPickup : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = PickupRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val result = state.getContext<PickupRollContext>()
            val ball = result.ball
            return if (result.isSuccess) {
                compositeCommandOf(
                    SetBallState.carried(ball, result.player),
                    ReportPickup(result.player, result.target, result.modifiers, result.roll!!.result, true),
                    GotoNode(CheckForScoring),
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
        override fun onEnterNode(state: Game, rules: Rules): Command? {
            // If it was the active player that failed the pickup, it is a turnover regardless
            // of where the ball ends up.
            return state.activePlayer?.let { SetTurnOver(TurnOver.STANDARD) }
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = Bounce
        override fun onExitNode(state: Game, rules: Rules): Command = ExitProcedure()
    }

    // Finally, once all rolls have been resolved, check if the player picking up the ball scored
    // a touchdown.
    object CheckForScoring : ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<PickupRollContext>()
            return SetContext(ScoringATouchDownContext(context.player))
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ScoringATouchdown
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                RemoveContext<ScoringATouchDownContext>(),
                ExitProcedure()
            )
        }
    }
}
