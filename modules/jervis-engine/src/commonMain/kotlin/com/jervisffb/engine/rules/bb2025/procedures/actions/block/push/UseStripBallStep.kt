package com.jervisffb.engine.rules.bb2025.procedures.actions.block.push

import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.ConfirmWhenReady
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetBallLocation
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.commands.SetCurrentBall
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.BlockContext
import com.jervisffb.engine.model.context.PushContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.reports.ReportSkillUsed
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2025.procedures.actions.block.BB2025PushBack
import com.jervisffb.engine.rules.bb2025.procedures.actions.block.MultipleBlockAction
import com.jervisffb.engine.rules.common.procedures.Bounce
import com.jervisffb.engine.rules.common.skills.SkillType

/**
 * After following up (or not), but before resolving any events in Push Chain,
 * we need to choose to use Strip Ball or not.
 *
 * See page 136 in the BB2025 rulebook.
 *
 * A Pushback is split into multiple phases to support both normal blocks and
 * Multiple Block as their order of resolution differs.
 *
 * See [BB2025PushBack] and [MultipleBlockAction] for more details on each.
 */
object UseStripBallStep: Procedure() {
    override val initialNode: Node = CheckIfStripBallIsApplicable
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object CheckIfStripBallIsApplicable: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<BlockContext>()
            val pushContext = state.getContext<PushContext>()
            val player = context.attacker
            val defenderHasBall = context.defender.hasBall()
            val hasStripBall = player.isSkillAvailable(SkillType.STRIP_BALL)
            val defenderIsImmovable = pushContext.isDefenderImmovable
            return if (defenderHasBall && hasStripBall && !defenderIsImmovable) {
                GotoNode(ChooseToUseSureHands)
            } else {
                ExitProcedure()
            }
        }
    }

    object ChooseToUseSureHands: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<PushContext>().firstPushee.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<PushContext>()
            val defenderCanUseSureHands = context.firstPushee.isSkillAvailable(SkillType.SURE_HANDS)
            return when (defenderCanUseSureHands) {
                true -> listOf(ConfirmWhenReady, CancelWhenReady)
                false -> listOf(ContinueWhenReady)
            }
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val useSureHands = (action == Confirm)
            return if (useSureHands) {
                val player = state.getContext<PushContext>().firstPushee
                compositeCommandOf(
                    ReportSkillUsed(player, SkillType.SURE_HANDS),
                    ExitProcedure(),
                )
            } else {
                GotoNode(ChooseToUseStripBall)
            }
        }
    }

    object ChooseToUseStripBall: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team? {
            val context = state.getContext<BlockContext>()
            return context.attacker.team
        }
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<BlockContext>()
            val player = context.attacker
            val defenderHasBall = context.defender.hasBall()
            val hasStripBall = player.isSkillAvailable(SkillType.STRIP_BALL)
            return when (hasStripBall && defenderHasBall) {
                true -> listOf(ConfirmWhenReady, CancelWhenReady)
                false -> listOf(ContinueWhenReady)
            }
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<BlockContext>()
            val useStripBall = (action == Confirm)
            return if (useStripBall) {
                val ball = context.defender.ball!!
                compositeCommandOf(
                    ReportSkillUsed(context.attacker, SkillType.STRIP_BALL),
                    SetCurrentBall(ball),
                    SetBallLocation(ball, context.defender.coordinates),
                    SetBallState.bouncing(ball),
                    GotoNode(BounceStrippedBall)
                )
            } else {
                ExitProcedure()
            }
        }
    }

    object BounceStrippedBall: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = Bounce
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                SetCurrentBall(null),
                ExitProcedure()
            )
        }
    }
}
