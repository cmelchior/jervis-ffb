package com.jervisffb.engine.rules.bb2025.procedures.actions.block.singleblock

import com.jervisffb.engine.commands.AddPlayerStatModifier
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.AddContext
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.BlockContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.model.modifiers.SkillStatModifier
import com.jervisffb.engine.reports.ReportSkillUsed
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2025.procedures.actions.block.MultipleBlockAction
import com.jervisffb.engine.rules.common.procedures.actions.block.DauntlessRoll
import com.jervisffb.engine.rules.common.procedures.actions.block.DauntlessRollContext
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.skills.SkillType

/**
 * Calculate all modifiers before rolling the block dice.
 *
 * Developer's Commentary:
 * In BB2020, Horns were applied before Dauntless. In BB2025, this has been
 * swapped, so Dauntless is now applied first.
 *
 * All positive block modifiers are applied automatically, even though they
 * are technically optional. The reasoning being that it only affects the
 * number of dice rolled, and there isn't a use case (even a bad one) for
 * wanting to roll _less_ sdice.
 *
 * @see [MultipleBlockAction]
 * @see [StandardBlockStep]
 */
object SingleStandardBlockDetermineModifiers: Procedure() {
    override val initialNode: Node = ResolveDauntless
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<BlockContext>()
    }

    object ResolveDauntless : ParentNode() {
        override fun skipNodeFor(state: Game, rules: Rules): Node? {
            val context = state.getContext<BlockContext>()
            val hasSkill = context.attacker.isSkillAvailable(SkillType.DAUNTLESS)
            val attacker = context.attacker
            val defender = context.defender
            val defenderStrength = defender.strengthModifiers.filter { it.expiresAt == Duration.PERMANENT }.sumOf { it.modifier } + defender.baseStrength
            val attackerStrength = attacker.strengthModifiers.filter { it.expiresAt == Duration.PERMANENT }.sumOf { it.modifier } + attacker.baseStrength
            val isWeaker = attackerStrength < defenderStrength
            return when (hasSkill && isWeaker) {
                true -> null
                false -> ResolveHorns
            }
        }
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val blockContext = state.getContext<BlockContext>()
            val rollContext = DauntlessRollContext(
                attacker = blockContext.attacker,
                defender = blockContext.defender
            )
            return AddContext(rollContext)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = DauntlessRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val rollContext = state.getContext<DauntlessRollContext>()
            return buildCompositeCommand {
                add(RemoveContext<DauntlessRollContext>())
                if (rollContext.isSuccess) {
                    add(
                        AddPlayerStatModifier(
                            rollContext.attacker,
                            rollContext.modifier!!
                        )
                    )
                }
                add(GotoNode(ResolveHorns))
            }
        }
    }

    object ResolveHorns : ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<BlockContext>()
            val isBlitz = context.isBlitzing
            val hasHorns = context.attacker.isSkillAvailable(SkillType.HORNS)
            return buildCompositeCommand {
                if (isBlitz && hasHorns) {
                    addAll(
                        ReportSkillUsed(context.attacker, SkillType.HORNS),
                        AddPlayerStatModifier(context.attacker, SkillStatModifier.HORNS)
                    )
                }
                add(GotoNode(DetermineAssists))
            }
        }
    }

    // Offensive/Defensive assists. Technically, you are allowed to choose whether to assist.
    // However, I cannot come up with a single (even bad) reason for why you would ever choose
    // to not assist, so we just automatically include all assists on both sides
    object DetermineAssists : ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<BlockContext>()
            val offensiveAssists = rules.calculateOffensiveAssists(context.attacker, context.defender)
            val defensiveAssists = rules.calculateDefensiveAssists(context.defender, context.attacker)
            return compositeCommandOf(
                UpdateContext(context.copy(offensiveAssists = offensiveAssists, defensiveAssists = defensiveAssists)),
                ExitProcedure()
            )
        }
    }
}
