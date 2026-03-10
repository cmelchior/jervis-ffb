package com.jervisffb.engine.rules.bb2025.procedures.actions.block

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.AddContext
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.SetContextProperty
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.BB2025MultipleBlockContext
import com.jervisffb.engine.model.context.BlockContext
import com.jervisffb.engine.model.context.PushContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.context.getContextOrNull
import com.jervisffb.engine.reports.ReportPowResult
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2025.procedures.actions.block.push.PushedBack
import com.jervisffb.engine.rules.bb2025.procedures.tables.injury.BB2025KnockedDown
import com.jervisffb.engine.rules.common.procedures.tables.injury.RiskingInjuryContext

/**
 * Resolve POW! selected on the block dice.
 *
 * The logic for pushing the defender differs slightly depending on this
 * being a single block or part of a multiple block. For Multiple Block, we only
 * run the first part of the push sequence. The rest is delayed until later.
 *
 * @see PushStepInitialMoveSequence for a full description of how the Push
 * sequence works for single blocks.
 * @see MultipleBlockAction for a full description of Multiple Blocks work and
 * how a block result like POW! is resolved in that context.
 */
object BB2025Pow: Procedure() {
    override val initialNode: Node = ResolveInitialPushSequence
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        val pushContext = createPushContext(state)
        return compositeCommandOf(
            ReportPowResult(pushContext.firstPusher, pushContext.firstPushee),
            AddContext(pushContext)
        )
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        return RemoveContext<PushContext>()
    }

    object ResolveInitialPushSequence: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            return PushedBack
        }
        override fun onExitNode(state: Game, rules: Rules): Command {
            val blockContext = state.getContext<BlockContext>()
            val pushContext = state.getContext<PushContext>()
            return buildCompositeCommand {
                if (blockContext.isUsingMultiBlock) {
                    val multipleBlockContext = state.getContext<BB2025MultipleBlockContext>()
                    val property = if (multipleBlockContext.activeDefender == 0) {
                        BB2025MultipleBlockContext::defender1PushChain
                    } else {
                        BB2025MultipleBlockContext::defender2PushChain
                    }
                    add(SetContextProperty(property, multipleBlockContext, pushContext))
                }
                addAll(
                    UpdateContext(blockContext.copy(didFollowUp = pushContext.followsUp)),
                    GotoNode(ResolveDefenderKnockedDown)
                )
            }
        }
    }

    /**
     * Defender is knocked down, and will need to roll for Armour/Injury.
     * If they had a ball, it is now loose, ready to bounce.
     *
     * If this is part of a Multiple Block, the injury isn't fully resolved,
     * instead it is saved in the Injury Pool in [BB2025MultipleBlockContext] and
     * will be resolved later. The same for any loose ball. It is just knocked
     * loose, but the bounce is handled later.
     *
     * If this is just a single block, the entire sequence is handled now.
     */
    object ResolveDefenderKnockedDown: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<PushContext>()
            val defender = context.firstPushee
            val injuryContext = RiskingInjuryContext(
                player = defender,
                causedBy = context.firstPusher,
                isPartOfMultipleBlock = context.isMultipleBlock
            )
            return AddContext(injuryContext)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = BB2025KnockedDown
        override fun onExitNode(state: Game, rules: Rules): Command {
            val multipleBlockContext = state.getContextOrNull<BB2025MultipleBlockContext>()
            val injuryContext = state.getContext<RiskingInjuryContext>()
            val updateInjuryCommand = multipleBlockContext?.addInjuryReferenceForPlayer(
                injuryContext.player,
                injuryContext
            )
            return compositeCommandOf(
                RemoveContext<RiskingInjuryContext>(),
                updateInjuryCommand,
                ExitProcedure(),
            )
        }
    }
}
