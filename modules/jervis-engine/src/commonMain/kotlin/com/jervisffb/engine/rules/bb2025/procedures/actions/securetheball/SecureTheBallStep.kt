package com.jervisffb.engine.rules.bb2025.procedures.actions.securetheball

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
import com.jervisffb.engine.commands.context.AddContext
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.UpdateContext
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
import com.jervisffb.engine.model.context.SecureTheBallContext
import com.jervisffb.engine.model.context.SecureTheBallRollContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.model.modifiers.SecureTheBallModifier
import com.jervisffb.engine.reports.ReportNoBallAffectingAction
import com.jervisffb.engine.reports.ReportSecuredTheBallResult
import com.jervisffb.engine.reports.ReportSkillUsed
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.Bounce
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.rules.common.tables.Weather

/**
 * Procedure for when a player moved into the ball and must now attempt to
 * secure it.
 *
 * The result is stored in [SecureTheBallContext], it is up to the caller to
 * handle it.
 *
 * Developer's Commentary:
 * With the NAF interpretation that Secure the Ball is also a "pickup", it would
 * probably make sense to try to combine the two procedures into one, but until
 * that has been fully clarified in an official FAQ, we are keeping them
 * separate, even though it means duplicating logic.
 */
object SecureTheBallStep: Procedure() {
    override val initialNode: Node = CheckForNoBallSkill
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        val ball = state.currentBall()
        val securingPlayer = state.field[ball.location].player!!
        val secureContext = SecureTheBallRollContext(securingPlayer, ball)
        return AddContext(secureContext)
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        return RemoveContext<SecureTheBallRollContext>()
    }
    override fun isValid(state: Game, rules: Rules) {
        if (state.currentBall().state != BallState.ON_GROUND) {
            throw IllegalStateException("Ball is not on the ground, but ${state.currentBall().state}")
        }
        if (state.activePlayer?.location != state.currentBall().location) {
            throw IllegalStateException(
                "Active player is not on the ball: ${state.activePlayer?.location} vs. ${state.currentBall().location}",
            )
        }
    }

    object CheckForNoBallSkill: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<SecureTheBallRollContext>()
            val player = context.player
            val hasNoBall = player.isSkillAvailable(SkillType.NO_BALL)
            return if (hasNoBall) {
                compositeCommandOf(
                    SetBallState.bouncing(context.ball),
                    ReportNoBallAffectingAction(player, ReportNoBallAffectingAction.ActionType.SECURE_THE_BALL),
                    GotoNode(SecuringTheBallFailed)
                )
            } else {
                GotoNode(ChooseToUseBigHand)
            }
        }
    }

    object ChooseToUseBigHand: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team {
            return state.getContext<SecureTheBallRollContext>().player.team
        }
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<SecureTheBallRollContext>()
            val player = context.player
            return if (player.isSkillAvailable(SkillType.BIG_HAND)) {
                listOf(ConfirmWhenReady, CancelWhenReady)
            } else {
                listOf(ContinueWhenReady)
            }
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<SecureTheBallRollContext>()
            val player = context.player
            val useBigHands = (action == Confirm)
            return compositeCommandOf(
                if (useBigHands) {
                    ReportSkillUsed(player, SkillType.BIG_HAND)
                } else {
                    null
                },
                UpdateContext(context.copy(useBigHands = useBigHands)),
                GotoNode(ChooseToUseExtraArms)
            )
        }
    }

    object ChooseToUseExtraArms: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team {
            return state.getContext<SecureTheBallRollContext>().player.team
        }
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<SecureTheBallRollContext>()
            val player = context.player
            return if (player.isSkillAvailable(SkillType.EXTRA_ARMS)) {
                listOf(ConfirmWhenReady, CancelWhenReady)
            } else {
                listOf(ContinueWhenReady)
            }
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<SecureTheBallRollContext>()
            val player = context.player
            val useExtraArms = (action == Confirm)
            return compositeCommandOf(
                if (useExtraArms) {
                    ReportSkillUsed(player, SkillType.EXTRA_ARMS)
                } else {
                    null
                },
                UpdateContext(context.copy(useExtraArms = useExtraArms)),
                GotoNode(CalculateModifiers)
            )
        }
    }

    object CalculateModifiers: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<SecureTheBallRollContext>()
            val modifiers = mutableListOf<DiceModifier>()
            if (!context.useBigHands) {
                // Add modifiers for other opponent players marking the field.
                rules.addMarkedModifiers(
                    state,
                    context.player.team,
                    state.currentBall().location,
                    modifiers,
                    SecureTheBallModifier.MARKED
                )
                // Weather
                if (state.weather == Weather.POURING_RAIN) {
                    modifiers.add(SecureTheBallModifier.POURING_RAIN)
                }
                // Other modifiers, like disturbing presence?
            }
            if (context.useExtraArms) {
                modifiers.add(SecureTheBallModifier.EXTRA_ARMS)
            }

            return compositeCommandOf(
                UpdateContext(context.copy(modifiers = modifiers)),
                GotoNode(RollToSecureBall)
            )
        }
    }

    object RollToSecureBall : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = SecureTheBallRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val rollContext = state.getContext<SecureTheBallRollContext>()
            val actionContext = state.getContext<SecureTheBallContext>()
            val ball = state.currentBall()
            return if (rollContext.isSuccess) {
                compositeCommandOf(
                    SetBallState.carried(ball, rollContext.player),
                    UpdateContext(actionContext.copy(roll = rollContext, securedTheBall = true)),
                    ReportSecuredTheBallResult(rollContext),
                    ExitProcedure()
                )
            } else {
                compositeCommandOf(
                    SetBallState.bouncing(ball),
                    UpdateContext(actionContext.copy(roll = rollContext, securedTheBall = false)),
                    ReportSecuredTheBallResult(rollContext),
                    GotoNode(SecuringTheBallFailed),
                )
            }
        }
    }

    object SecuringTheBallFailed : ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command? {
            // If it was the active player that failed to secure the ball, it is a turnover regardless
            // of where the ball ends up.
            return state.activePlayer?.let { SetTurnOver(TurnOver.STANDARD) }
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = Bounce
        override fun onExitNode(state: Game, rules: Rules): Command = ExitProcedure()
    }
}
