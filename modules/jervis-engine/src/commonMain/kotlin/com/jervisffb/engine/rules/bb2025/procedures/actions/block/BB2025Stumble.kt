package com.jervisffb.engine.rules.bb2025.procedures.actions.block

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.ConfirmWhenReady
import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.AddContext
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.BlockContext
import com.jervisffb.engine.model.context.StumbleContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.hasSkill
import com.jervisffb.engine.reports.ReportSkillUsed
import com.jervisffb.engine.reports.ReportStumbleResult
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2025.procedures.actions.block.push.PushedBack
import com.jervisffb.engine.rules.bb2025.procedures.tables.injury.BB2025KnockedDown
import com.jervisffb.engine.rules.common.procedures.tables.injury.RiskingInjuryContext
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * Resolve a Stumble when selected on a block die.
 *
 * See page 57 in the BB2020 rulebook.
 * See page 62 in the BB2025 rulebook.
 */
object BB2025Stumble: Procedure() {
    override val initialNode: Node = ChooseToUseTackle
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        val blockContext = state.getContext<BlockContext>()
        val stumbleContext = StumbleContext(
            blockContext.attacker,
            blockContext.defender,
        )
        return AddContext(stumbleContext)
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        val stumbleContext = state.getContext<StumbleContext>()
        val pushContext = stumbleContext.pushContext ?: INVALID_GAME_STATE("StumbleContext should contain PushContext: $stumbleContext")
        return compositeCommandOf(
            RemoveContext<StumbleContext>(),
            ReportStumbleResult(pushContext.firstPusher, pushContext.firstPushee, stumbleContext.isDefenderDown())
        )
    }
    override fun isValid(state: Game, rules: Rules) = state.assertContext<BlockContext>()

    object ChooseToUseTackle: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<StumbleContext>().attacker.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val stumbleContext = state.getContext<StumbleContext>()
            val attackerHasTackle = stumbleContext.attacker.hasSkill(SkillType.TACKLE)
            val defenderHasDodge = stumbleContext.defender.hasSkill(SkillType.DODGE)
            return if (attackerHasTackle && defenderHasDodge) {
                listOf(ConfirmWhenReady, CancelWhenReady)
            } else {
                listOf(ContinueWhenReady)
            }
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val useTackle = (action == Confirm)
            val updatedContext = state.getContext<StumbleContext>().copy(attackerUsesTackle = useTackle)
            return buildCompositeCommand {
                add(UpdateContext(updatedContext))
                if (useTackle) {
                    add(ReportSkillUsed(updatedContext.attacker, SkillType.TACKLE))
                    add(GotoNode(ResolvePush))
                } else {
                    add(GotoNode(ChooseToUseDodge))
                }
            }
        }
    }

    object ChooseToUseDodge: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team? = state.getContext<StumbleContext>().defender.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val stumbleContext = state.getContext<StumbleContext>()
            return if (stumbleContext.defender.hasSkill(SkillType.DODGE)) {
                listOf(ConfirmWhenReady, CancelWhenReady)
            } else {
                listOf(ContinueWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val useDodge = when (action) {
                Confirm -> true
                Cancel,
                Continue -> false
                else -> INVALID_ACTION(action)
            }
            val updatedContext = state.getContext<StumbleContext>().copy(defenderUsesDodge = useDodge)
            return compositeCommandOf(
                UpdateContext(updatedContext),
                GotoNode(ResolvePush)
            )
        }
    }

    // Push the player, including chain pushes. At the end of the push, the player
    // is Knocked Down if either the attacker was using Tackle or the defender
    // didn't have Dodge.
    object ResolvePush: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = PushedBack
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<StumbleContext>()
            return if (context.defender.location.isOnField(rules) && context.isDefenderDown()) {
                GotoNode(ResolvePlayerDown)
            } else {
                ExitProcedure()
            }
        }
    }

    // If the player is still on the field, resolve them going down.
    // Otherwise, it was resolved as part of the Chain Push
    object ResolvePlayerDown: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val defender = state.getContext<StumbleContext>().defender
            val blockContext = state.getContext<BlockContext>()
            val injuryContext = RiskingInjuryContext(
                player = defender,
                causedBy = blockContext.attacker,
                isPartOfMultipleBlock = blockContext.isUsingMultiBlock
            )
            return AddContext(injuryContext)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = BB2025KnockedDown
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                RemoveContext<RiskingInjuryContext>(),
                ExitProcedure()
            )
        }
    }
}
