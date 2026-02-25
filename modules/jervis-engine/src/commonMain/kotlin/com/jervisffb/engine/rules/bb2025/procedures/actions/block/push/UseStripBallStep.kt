package com.jervisffb.engine.rules.bb2025.procedures.actions.block.push

import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.ConfirmWhenReady
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.BlockContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.reports.ReportSkillUsed
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2025.procedures.actions.block.BB2025PushBack
import com.jervisffb.engine.rules.bb2025.procedures.actions.block.MultipleBlockAction
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
    override val initialNode: Node = ChooseToUseStripBall
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object ChooseToUseStripBall: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team? {
            val context = state.getContext<BlockContext>()
            return context.attacker.team
        }
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val player = state.getContext<BlockContext>().attacker
            return when (player.isSkillAvailable(SkillType.STRIP_BALL)) {
                true -> listOf(ConfirmWhenReady, CancelWhenReady)
                false -> listOf(ContinueWhenReady)

            }
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<BlockContext>()
            val useStripBall = (action == Confirm)
            return if (useStripBall) {
                compositeCommandOf(
                    ReportSkillUsed(context.attacker, SkillType.STRIP_BALL),
                    // TODO Prepare ball for bouncing
                    // GotoNode(BounceStrippedBall)
                    ExitProcedure()
                )

            } else {
                ExitProcedure()
            }
        }
    }

    object BounceStrippedBall: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            TODO("Not yet implemented")
        }

        override fun onExitNode(state: Game, rules: Rules): Command {
            TODO("Not yet implemented")
        }
    }
}
