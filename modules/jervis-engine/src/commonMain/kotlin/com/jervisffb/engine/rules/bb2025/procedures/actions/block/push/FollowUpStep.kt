package com.jervisffb.engine.rules.bb2025.procedures.actions.block.push

import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.ConfirmWhenReady
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetPlayerLocation
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.context.SetContext
import com.jervisffb.engine.commands.context.SetContextProperty
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.PushContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.hasSkill
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.reports.ReportSkillUsed
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2025.procedures.actions.block.BB2025PushBack
import com.jervisffb.engine.rules.bb2025.procedures.actions.block.MultipleBlockAction
import com.jervisffb.engine.rules.common.skills.SkillType

/**
 * Procedure for handling the "Follow Up" part of a Push Back sequence.
 * This will happen after moving players in the chain, but before resolving
 * any events (as they all require rolling dice).
 *
 * A Pushback is split into multiple phases to support both normal blocks and
 * Multiple Block as their order of resolution differs.
 *
 * See [BB2025PushBack] and [MultipleBlockAction] for more details on each.
 */
object FollowUpStep: Procedure() {
    override val initialNode: Node = ChooseToUseFend
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object ChooseToUseFend: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team {
            return state.getContext<PushContext>().firstPushee.team
        }

        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<PushContext>()
            val isAttackerUsingJuggernaut = context.firstPusher.isSkillAvailable(SkillType.JUGGERNAUT)
            val defenderHasFend = context.firstPushee.isSkillAvailable(SkillType.FEND)
            // If Juggernaut is being used by the attacker, it prevents the use of Fend.
            // See the Juggernaut skill description on page 129 in the BB2025 rulebook.
            return if (defenderHasFend && !isAttackerUsingJuggernaut) {
                listOf(ConfirmWhenReady, CancelWhenReady)
            } else {
                listOf(ContinueWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<PushContext>()
            val useFend = (action == Confirm)
            return buildCompositeCommand {
                if (useFend) {
                    add(ReportSkillUsed(context.firstPushee, SkillType.FEND))
                    add(SetContext(context.copy(defenderIsUsingFend = true)))
                }
                add(GotoNode(ChooseToUseTaunt))
            }
        }
    }

    object ChooseToUseTaunt: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team {
            return state.getContext<PushContext>().firstPushee.team
        }
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<PushContext>()
            val defenderHasTaunt = context.firstPushee.isSkillAvailable(SkillType.TAUNT)
            val attackerHasFrenzy = context.firstPusher.hasSkill(SkillType.FRENZY)
            return if (!context.defenderIsUsingFend && !attackerHasFrenzy && defenderHasTaunt) {
                listOf(ConfirmWhenReady, CancelWhenReady)
            } else {
                listOf(ContinueWhenReady)
            }
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<PushContext>()
            val useTaunt = (action == Confirm)
            return buildCompositeCommand {
                if (useTaunt) {
                    add(ReportSkillUsed(context.firstPushee, SkillType.TAUNT))
                    add(SetContext(context.copy(defenderIsUsingTaunt = true)))
                }
                add(GotoNode(ChooseToFollowUp))
            }
        }
    }

    object ChooseToFollowUp: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<PushContext>().firstPusher.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<PushContext>()
            val followUpStatus = calculateFollowUpStatus(context)
            return when (followUpStatus) {
                FollowUpStatus.NOT_ALLOWED,
                FollowUpStatus.MUST_FOLLOW_UP -> listOf(ContinueWhenReady)
                FollowUpStatus.OPTIONAL -> listOf(ConfirmWhenReady, CancelWhenReady)
            }
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val pushContext = state.getContext<PushContext>()
            val followUpStatus = calculateFollowUpStatus(pushContext)
            val manualFollowUp = (followUpStatus == FollowUpStatus.OPTIONAL) && (action == Confirm)
            val forcedFollowUp = (followUpStatus == FollowUpStatus.MUST_FOLLOW_UP)
            return buildCompositeCommand {
                if (manualFollowUp || forcedFollowUp) {
                    add(SetContextProperty(PushContext::followsUp, pushContext, manualFollowUp))
                    add(SetPlayerLocation(pushContext.firstPusher, pushContext.pushChain.first().from))
                }
                // The parent procedure is responsible for delegating to the next
                // parts of the push chain which should be resolving defender
                // injuries. But this will differ slightly between single and multiple
                // blocks.
                add(ExitProcedure())
            }
        }
    }

    // HELPER METHODS
    enum class FollowUpStatus { OPTIONAL, NOT_ALLOWED, MUST_FOLLOW_UP }

    private fun calculateFollowUpStatus(context: PushContext): FollowUpStatus {
        // The following rules are in effect:
        // - If the attacker used Frenzy, they must follow up if allowed.
        // - If the attacker used Multiple Block, they cannot follow up.
        // - If the defender used Taunt, the attacker must follow up, unless they are Rooted or used Multiple Block.
        // - If the defender is using Fend, the attacker cannot follow up.
        // - If the defender is using Taunt, the attacker must follow up if allowed.
        val cannotFollowUp = context.isMultipleBlock || context.defenderIsUsingFend /* Add support for Rooted */
        val mustFollowUp = context.defenderIsUsingTaunt || context.isAttackerUsingFrenzy
        return when {
            cannotFollowUp -> FollowUpStatus.NOT_ALLOWED
            mustFollowUp -> FollowUpStatus.MUST_FOLLOW_UP
            else -> FollowUpStatus.OPTIONAL
        }
    }
}
