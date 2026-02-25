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
import com.jervisffb.engine.commands.SetPlayerState
import com.jervisffb.engine.commands.SetTurnOver
import com.jervisffb.engine.commands.buildCompositeCommand
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
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.TurnOver
import com.jervisffb.engine.model.context.BB2025MultipleBlockContext
import com.jervisffb.engine.model.context.BlockContext
import com.jervisffb.engine.model.context.BothDownContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.reports.ReportBothDownResult
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.tables.injury.BB2020KnockedDown
import com.jervisffb.engine.rules.bb2025.procedures.tables.injury.BB2025KnockedDown
import com.jervisffb.engine.rules.common.procedures.tables.injury.RiskingInjuryContext
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
        return SetContext(
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
                SetContext(context.copy(defenderUsesWrestle = useWrestle)),
                GotoNode(AttackerChooseToUseWrestle)
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
                SetContext(updatedContext),
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
            val hasBlock = context.defender.getSkillOrNull(SkillType.BLOCK) != null
            return when (hasBlock) {
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
                SetContext(context.copy(defenderUsesBlock = useBlock)),
                GotoNode(AttackerChooseToUseBlock)
            )
        }
    }

    object AttackerChooseToUseBlock: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<BothDownContext>().attacker.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<BothDownContext>()
            val hasBlock = (context.attacker.getSkillOrNull(SkillType.BLOCK) != null)
            return when (hasBlock) {
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
                SetContext(context.copy(attackUsesBlock = useBlock)),
                GotoNode(ResolveBothDown)
            )
        }
    }

    object ResolveBothDown: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<BothDownContext>()

            // If Wrestle was used, both players are just placed prone and nothing more happens.
            // Otherwise check if one or both players need to roll injury
            return if (context.attackerUsesWrestle || context.defenderUsesWrestle) {
                compositeCommandOf(
                    SetPlayerState(context.attacker, PlayerState.PRONE, hasTackleZones = false),
                    SetPlayerState(context.defender, PlayerState.PRONE, hasTackleZones = false),
                    ExitProcedure()
                )
            } else {
                buildCompositeCommand {
                    if (!context.attackUsesBlock) {
                        add(SetTurnOver(TurnOver.STANDARD))
                        add(SetPlayerState(context.attacker, PlayerState.KNOCKED_DOWN, hasTackleZones = false))
                    }
                    if (!context.defenderUsesBlock) {
                        add(SetPlayerState(context.defender, PlayerState.KNOCKED_DOWN, hasTackleZones = false))
                    }
                    add(GotoNode(ResolveDefenderInjury))
                }
            }
        }
    }

    object ResolveDefenderInjury: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val blockContext = state.getContext<BlockContext>()
            val context = state.getContext<BothDownContext>()
            return if (!context.defenderUsesBlock) {
                // If this is part of a Multiple, just mark the defender as being Knocked Down
                // and handle it later.
                if (blockContext.isUsingMultiBlock) {
                    val multiContext = state.getContext<BB2025MultipleBlockContext>()
                    val updatedContext = multiContext.copyAndKnockDownActiveDefender(true)
                    compositeCommandOf(
                        SetContext(updatedContext),
                        GotoNode(ResolveAttackerInjury)
                    )
                } else {
                    GotoNode(RollDefenderInjury)
                }
            } else {
                GotoNode(ResolveAttackerInjury)
            }
        }
    }

    object RollDefenderInjury: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val blockContext = state.getContext<BlockContext>()
            val context = state.getContext<BothDownContext>()
            return SetContext(
                RiskingInjuryContext(
                    player = context.defender,
                    isPartOfMultipleBlock = blockContext.isUsingMultiBlock
                )
            )
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = BB2020KnockedDown
        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(ResolveAttackerInjury)
        }
    }

    object ResolveAttackerInjury: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val blockContext = state.getContext<BlockContext>()
            val context = state.getContext<BothDownContext>()
            // If this is part of a Multiple, just mark the attacker as being Knocked Down
            // and handle it later.
            return if (blockContext.isUsingMultiBlock) {
                val multiContext = state.getContext<BB2025MultipleBlockContext>()
                val updatedContext = multiContext.copy(attackerKnockedDown = true)
                compositeCommandOf(
                    SetContext(updatedContext),
                    ExitProcedure()
                )
            } else {
                GotoNode(RollAttackerInjury)
            }
        }
    }

    object RollAttackerInjury: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val blockContext = state.getContext<BlockContext>()
            val context = state.getContext<BothDownContext>()
            return SetContext(
                RiskingInjuryContext(
                    player = context.attacker,
                    isPartOfMultipleBlock = blockContext.isUsingMultiBlock
                )
            )
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = BB2025KnockedDown
        override fun onExitNode(state: Game, rules: Rules): Command {
            // Attacker went down, so its turn ends immediately, commonly because it is a turnover,
            // but if it happened during a kick-off blitz, it just ends the Blitz.
            return compositeCommandOf(
                RemoveContext<RiskingInjuryContext>(),
                ExitProcedure()
            )
        }
    }
}
