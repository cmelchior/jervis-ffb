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
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ActivatePlayerContext
import com.jervisffb.engine.model.context.BB2025MultipleBlockContext
import com.jervisffb.engine.model.context.BlockContext
import com.jervisffb.engine.model.context.BothDownContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.reports.ReportBothDownResult
import com.jervisffb.engine.reports.ReportSkillUsed
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2025.procedures.tables.injury.BB2025KnockedDown
import com.jervisffb.engine.rules.bb2025.procedures.tables.injury.BB2025PlacedProne
import com.jervisffb.engine.rules.common.procedures.tables.injury.RiskingInjuryContext
import com.jervisffb.engine.rules.common.procedures.tables.injury.RiskingInjuryMode
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.utils.INVALID_ACTION

/**
 * Resolve a "Both Down" selected as a block result.
 * See page 62 in the BB2025 rulebook.
 *
 * Developer's Commentary:
 * The order of choosing skills is unclear from the rules. In this
 * implementation, we have chosen that the defender goes first. It is somewhat
 * arbitrary but means that the order of checking is:
 *
 * 1. Defender chooses to use Wrestle
 * 2. Attacker chooses to use Wrestle
 * 3. Defender chooses to use block
 * 4. Attacker chooses to use block
 *
 * If this is part of a Multiple Block, neither attacker nor defender is
 * actually knocked down yet. Only skills that affect "Both Down" will be
 * handled, and if a player is knocked down, they will be marked for it, but
 * not actually knocked down yet.
 */
object BB2025BothDown: Procedure() {
    override val initialNode: Node = DefenderChooseToUseWrestle
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        val blockContext = state.getContext<BlockContext>()
        return AddContext(
            BothDownContext(
                blockContext.attacker,
                blockContext.defender,
            )
        )
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        return compositeCommandOf(
            // Not sure if this is where we should report this. It might look wrong for Multiple Block
            ReportBothDownResult(state.getContext<BothDownContext>()),
            RemoveContext<BothDownContext>()
        )
    }

    object DefenderChooseToUseWrestle: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<BothDownContext>().defender.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<BothDownContext>()
            val hasWrestle = context.defender.isSkillAvailable(SkillType.WRESTLE)
            return when (hasWrestle) {
                true -> listOf(ConfirmWhenReady, CancelWhenReady)
                false -> listOf(ContinueWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<BothDownContext>()
            val useWrestle = when (action) {
                Confirm -> true
                Cancel,
                Continue -> false
                else -> INVALID_ACTION(action)
            }
            return compositeCommandOf(
                if (useWrestle) ReportSkillUsed(context.defender, SkillType.WRESTLE) else null,
                UpdateContext(context.copy(defenderUsesWrestle = useWrestle)),
                if (useWrestle) GotoNode(ResolveBothDown) else GotoNode(AttackerChooseToUseWrestle)
            )
        }
    }

    object AttackerChooseToUseWrestle: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<BothDownContext>().attacker.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<BothDownContext>()
            val hasWrestle = context.attacker.isSkillAvailable(SkillType.WRESTLE)
            return when (hasWrestle) {
                true -> listOf(ConfirmWhenReady, CancelWhenReady)
                false -> listOf(ContinueWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<BothDownContext>()
            val useWrestle = when (action) {
                Confirm -> true
                Cancel,
                Continue -> false
                else -> INVALID_ACTION(action)
            }
            val updatedContext = context.copy(attackerUsesWrestle = useWrestle)
            return compositeCommandOf(
                if (useWrestle) ReportSkillUsed(context.attacker, SkillType.WRESTLE) else null,
                UpdateContext(updatedContext),
                if (updatedContext.attackerUsesWrestle || updatedContext.defenderUsesWrestle) {
                    GotoNode(ResolveBothDown)
                } else {
                    GotoNode(DefenderChooseToUseBlock)
                }
            )
        }
    }

    object DefenderChooseToUseBlock: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<BothDownContext>().defender.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<BothDownContext>()
            val canUseBlock = context.defender.isSkillAvailable(SkillType.BLOCK)
            return when (canUseBlock) {
                true -> listOf(ConfirmWhenReady, CancelWhenReady)
                false -> listOf(ContinueWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<BothDownContext>()
            val useBlock = when (action) {
                Confirm -> true
                Cancel,
                Continue -> false
                else -> INVALID_ACTION(action)
            }
            return compositeCommandOf(
                UpdateContext(context.copy(defenderUsesBlock = useBlock)),
                GotoNode(AttackerChooseToUseBlock)
            )
        }
    }

    object AttackerChooseToUseBlock: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<BothDownContext>().attacker.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<BothDownContext>()
            val canUseBlock = context.attacker.isSkillAvailable(SkillType.BLOCK)
            return when (canUseBlock) {
                true -> listOf(ConfirmWhenReady, CancelWhenReady)
                false -> listOf(ContinueWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<BothDownContext>()
            val useBlock = when (action) {
                Confirm -> true
                Cancel,
                Continue -> false
                else -> INVALID_ACTION(action)
            }
            return compositeCommandOf(
                UpdateContext(context.copy(attackUsesBlock = useBlock)),
                GotoNode(ResolveBothDown)
            )
        }
    }

    // When resolving a Both Down, we potentially need to resolve 2 Knocked Downs.
    // If that happens, we resolve them one after the other, starting with the defender.
    object ResolveBothDown: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<BothDownContext>()
            val activeContext = state.getContext<ActivatePlayerContext>()

            // If Wrestle was used, both players are Place Prone. All side-effects are handled there.
            if (context.attackerUsesWrestle || context.defenderUsesWrestle) {
                return GotoNode(ResolveBothPlaceProne)
            }

            // If Block was used for both attacker and defender, nothing further happens
            if (context.attackUsesBlock && context.defenderUsesBlock) {
                return ExitProcedure()
            }

            // TODO We need special handling for Multiple Block here - This is just the old BB2020 logic
            val blockContext = state.getContext<BlockContext>()
            if (blockContext.isUsingMultiBlock) {
                val defenderKnockedDown = !context.defenderUsesBlock
                val attackerKnockedDown = !context.attackUsesBlock
                val multiContext = state.getContext<BB2025MultipleBlockContext>()
                var updatedContext = multiContext.copyAndKnockDownActiveDefender(defenderKnockedDown)
                updatedContext = updatedContext.copy(attackerKnockedDown = attackerKnockedDown)
                return compositeCommandOf(
                    UpdateContext(updatedContext),
                    ExitProcedure()
                )
            }

            // Otherwise, resolve one or both players being Knocked Down.
            return GotoNode(ResolveDefenderKnockedDown)
        }
    }

    object ResolveDefenderKnockedDown: ParentNode() {
        override fun skipNodeFor(state: Game, rules: Rules): Node? {
            val context = state.getContext<BothDownContext>()
            return if (context.defenderUsesBlock) {
                ResolveAttackerKnockedDown
            } else {
                null
            }
        }
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val bothDownContext = state.getContext<BothDownContext>()
            val context = RiskingInjuryContext(
                player = bothDownContext.defender,
                causedBy = bothDownContext.attacker,
                isPartOfMultipleBlock = false,
                mode = RiskingInjuryMode.KNOCKED_DOWN
            )
            return AddContext(context)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = BB2025KnockedDown
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                RemoveContext<RiskingInjuryContext>(),
                GotoNode(ResolveAttackerKnockedDown)
            )
        }
    }

    object ResolveAttackerKnockedDown: ParentNode() {
        override fun skipNodeFor(state: Game, rules: Rules): Node? {
            val context = state.getContext<BothDownContext>()
            return if (context.attackUsesBlock) {
                ExitProcedureNode
            } else {
                null
            }
        }
        override fun onEnterNode(state: Game, rules: Rules): Command? {
            val bothDownContext = state.getContext<BothDownContext>()
            val context = RiskingInjuryContext(
                player = bothDownContext.attacker,
                causedBy = bothDownContext.defender,
                isPartOfMultipleBlock = false,
                mode = RiskingInjuryMode.KNOCKED_DOWN
            )
            return AddContext(context)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = BB2025KnockedDown
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                RemoveContext<RiskingInjuryContext>(),
                ExitProcedure()
            )
        }
    }

    // When using Wrestle, both players are Placed Prone.
    // If that happens, we resolve them one after the other, starting with the defender.
    object ResolveBothPlaceProne: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val activeContext = state.getContext<ActivatePlayerContext>()
            return compositeCommandOf(
                // TODO Figure out how Multiple Block work here later
                UpdateContext(activeContext.copy(activationEndsImmediately = true)),
                GotoNode(ResolveDefenderPlacedProne)
            )
        }
    }

    object ResolveDefenderPlacedProne: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<BothDownContext>()
            val injuryContext = RiskingInjuryContext(
                player = context.defender,
                causedBy = null, // The opponent does not get to use their skills when players are placed prone
                mode = RiskingInjuryMode.PLACED_PRONE
            )
            return AddContext(injuryContext)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = BB2025PlacedProne
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                RemoveContext<RiskingInjuryContext>(),
                GotoNode(ResolveAttackerPlacedProne)
            )
        }
    }

    object ResolveAttackerPlacedProne: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<BothDownContext>()
            val injuryContext = RiskingInjuryContext(
                player = context.attacker,
                causedBy = null, // The opponent does not get to use their skills when players are placed prone
                mode = RiskingInjuryMode.PLACED_PRONE
            )
            return AddContext(injuryContext)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = BB2025PlacedProne
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                RemoveContext<RiskingInjuryContext>(),
                ExitProcedure()
            )
        }
    }
}
