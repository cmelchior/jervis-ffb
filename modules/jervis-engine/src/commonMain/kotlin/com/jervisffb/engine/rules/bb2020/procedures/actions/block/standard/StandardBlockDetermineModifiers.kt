package com.jervisffb.engine.rules.bb2020.procedures.actions.block.standard

import com.jervisffb.engine.commands.AddPlayerStatModifier
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetContext
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.hasSkill
import com.jervisffb.engine.model.modifiers.SkillStatModifier
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.BlockContext
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.MultipleBlockAction
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.StandardBlockStep
import com.jervisffb.engine.rules.bb2020.skills.Horns

/**
 * Calculate all modifiers before rolling the block dice.
 *
 * @see [MultipleBlockAction]
 * @see [StandardBlockStep]
 */
object StandardBlockDetermineModifiers: Procedure() {
    override val initialNode: Node = DetermineAssists
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<BlockContext>()
    }

    // Horns are applied before applying any other skills/traits and before counting assists
    // See page 78 in the rulebook.
    object ResolveHorns : ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            // TODO Implement Horns logic. Modify strength using the modifier system
            val context = state.getContext<BlockContext>()
            return buildCompositeCommand {
                if (context.isBlitzing && context.attacker.hasSkill<Horns>()) {
                    add(AddPlayerStatModifier(context.attacker, SkillStatModifier.HORNS))
                }
                add(GotoNode(ResolveDauntless))
            }
        }
    }

    // Dauntless is applied before counting assists
    // See page 76 in the rulebook.
    object ResolveDauntless : ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            // TODO Implement Dauntless logic. Multiple block/Dauntless should just modify the players
            //  strength through the modifier system.
            return GotoNode(DetermineAssists)
        }
    }

    // Offensive/Defensive assists. Technically, you are allowed to choose whether to assist.
    // However, I cannot come up with a single (even bad) reason for why you would ever choose
    // to not assist, so we just automatically include all assists on both sides
    object DetermineAssists : ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<BlockContext>()
            val offensiveAssists =
                context.defender.coordinates.getSurroundingCoordinates(rules)
                    .mapNotNull { state.field[it].player }
                    .filter { it != context.attacker}
                    .count { player -> rules.canOfferAssist(player, context.defender) }

            val defensiveAssists =
                context.attacker.coordinates.getSurroundingCoordinates(rules)
                    .mapNotNull { state.field[it].player }
                    .filter { it != context.defender}
                    .count { player -> rules.canOfferAssist(player, context.attacker) }

            return compositeCommandOf(
                SetContext(context.copy(offensiveAssists = offensiveAssists, defensiveAssists = defensiveAssists)),
                ExitProcedure()
            )
        }
    }
}
