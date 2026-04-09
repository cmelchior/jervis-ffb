package com.jervisffb.engine.rules.bb2025.procedures.actions.block.singleblock

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.context.AddContext
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.BlockContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2025.procedures.actions.block.HitAndRunStep
import com.jervisffb.engine.rules.bb2025.procedures.actions.block.PileDriverStep
import com.jervisffb.engine.rules.common.procedures.actions.block.FoulAppearanceContext
import com.jervisffb.engine.rules.common.procedures.actions.block.FoulAppearanceRoll
import com.jervisffb.engine.rules.common.skills.SkillType

/**
 * Procedure for handling a single standard block once attacker and defender
 * have been identified. This includes rolling dice and resolving the result.
 *
 * This procedure can be called as part of a [BlockAction] or
 * [com.jervisffb.engine.rules.common.procedures.actions.blitz.BlitzAction].
 *
 * Developer's Commentary:
 * To share logic between the Single and Multiple Block, the Block sequence is
 * split into a lot of sub-procedures. This will allow us to combine them
 * differently depending on if we are in a single block or Multiple Block
 * scenario.
 *
 * A Block has therefore been broken down into the following steps:
 *
 * Block Phases (for standard blocks)
 * 1. Determine Strength Modifiers
 * 2. Roll Dice
 * 3. Choose to Reroll or not
 * 4. Use Skills that modify the Block roll
 * 5. Select Result
 * 6. Determine Push and Chain Push. This we call the Push Chain. No dice are
 *    rolled and no players are moved yet.
 * 7. Move all players in the Push Chain. Do not roll for Crowd Injury,
 *    Trapdoors or Bounce yet.
 * 8. Follow Up? Move attacker if Yes.
 * 9. Use Strip Ball? Bounce ball now if Yes
 * 10. Go through Push Chain, starting with Defender (until the end), then
 *    Attacker:
 *    a. If pushed into a ball. It will bounce.
 *    b. If standing on a trapdoor, roll to see if you fall though. Roll for
 *       Injury and Bounce ball.
 *    c. If pushed into the crowd: Roll Injury, then Throw in ball until it is
 *       at rest.
 *    d. Check for Touchdown.
 * 11. Knock Down defending player and resolve armour / injury.
 * 12. Knock Down attacking player and resolve armour / injury.
 * 13. Bounce ball if the defender had it.
 * 14. Bounce ball if the attacker had it.
 * 15. Use Pile Driver (if available)
 *
 * Implementations for each step are found in the
 * [com.jervisffb.engine.rules.bb2025.procedures.actions.block.singleblock]
 * package.
 */
object SingleStandardBlockStep : Procedure() {
    override val initialNode: Node = CheckForFoulAppearance
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<BlockContext>()

    object CheckForFoulAppearance: ParentNode() {
        override fun skipNodeFor(state: Game, rules: Rules): Node? {
            val context = state.getContext<BlockContext>()
            val hasFoulAppearance = context.defender.isSkillAvailable(SkillType.FOUL_APPEARANCE)
            return when (hasFoulAppearance) {
                true -> null
                false -> DetermineAssists
            }
        }
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val blockContext = state.getContext<BlockContext>()
            val foulAppearanceContext = FoulAppearanceContext(blockContext.attacker, blockContext.defender)
            return AddContext(foulAppearanceContext)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = FoulAppearanceRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val blockContext = state.getContext<BlockContext>()
            val context = state.getContext<FoulAppearanceContext>()
            return buildCompositeCommand {
                add(RemoveContext<FoulAppearanceContext>())
                when (context.isSuccess) {
                    true -> add(GotoNode(DetermineAssists))
                    false -> addAll(
                        UpdateContext(blockContext.copy(aborted = true)),
                        ExitProcedure()
                    )
                }
            }
        }
    }

    object DetermineAssists: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = SingleStandardBlockDetermineModifiers
        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(RollBlockDice)
        }
    }

    object RollBlockDice : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = SingleStandardBlockRollDice
        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(SelectRerollType)
        }
    }

    object SelectRerollType : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = SingleStandardBlockChooseReroll
        override fun onExitNode(state: Game, rules: Rules): Command {
            return if (state.rerollContext != null) {
                GotoNode(RerollDice)
            } else {
                // We can select multiple rerolls
                GotoNode(SelectBlockResult)
            }
        }
    }

    object RerollDice : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = SingleStandardBlockRerollDice
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<BlockContext>()
            // If all dice have been rerolled, we know for sure the final result.
            // Otherwise, it might be allowed to reroll more dice.
            return when (rules.isRerollAllowed(context.roll)) {
                true -> GotoNode(SelectRerollType)
                false -> GotoNode(SelectBlockResult)
            }
        }
    }

    object SelectBlockResult : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = SingleStandardBlockChooseResult
        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(ResolveBlockResult)
        }
    }

    object ResolveBlockResult : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = SingleStandardBlockApplyResult
        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(ChooseToUsePileDriver)
        }
    }

    object ChooseToUsePileDriver: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = PileDriverStep
        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(ChooseToUseHitAndRun)
        }
    }

    object ChooseToUseHitAndRun: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = HitAndRunStep
        override fun onExitNode(state: Game, rules: Rules): Command {
            return ExitProcedure()
        }
    }
}
