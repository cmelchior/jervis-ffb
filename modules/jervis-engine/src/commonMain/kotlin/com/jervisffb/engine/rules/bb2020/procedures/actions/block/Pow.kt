package com.jervisffb.engine.rules.bb2020.procedures.actions.block

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetBallLocation
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.SetContext
import com.jervisffb.engine.commands.context.SetContextProperty
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.MultipleBlockContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.context.getContextOrNull
import com.jervisffb.engine.model.context.hasContext
import com.jervisffb.engine.reports.ReportPowResult
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.tables.injury.RiskingInjuryContext
import com.jervisffb.engine.rules.bb2020.procedures.tables.injury.RiskingInjuryRoll

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
object Pow: Procedure() {
    override val initialNode: Node = ResolveInitialPushSequence
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        val pushContext = createPushContext(state)
        return compositeCommandOf(
            ReportPowResult(pushContext.firstPusher, pushContext.firstPushee),
            SetContext(pushContext)
        )
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        return RemoveContext<PushContext>()
    }

    object ResolveInitialPushSequence: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = PushStepInitialMoveSequence
        override fun onExitNode(state: Game, rules: Rules): Command {
            val blockContext = state.getContext<BlockContext>()
            val pushContext = state.getContext<PushContext>()
            return buildCompositeCommand {
                if (blockContext.isUsingMultiBlock) {
                    val multipleBlockContext = state.getContext<MultipleBlockContext>()
                    val property = if (multipleBlockContext.activeDefender == 0) {
                        MultipleBlockContext::defender1PushChain
                    } else {
                        MultipleBlockContext::defender2PushChain
                    }
                    add(SetContextProperty(property, multipleBlockContext, pushContext))
                }
                addAll(
                    SetContext(blockContext.copy(didFollowUp = pushContext.followsUp)),
                    GotoNode(ResolveDefenderDownInjury)
                )
            }
        }
    }

    /**
     * Defender is knocked down, and will need to roll for Armour/Injury.
     * If they had a ball, it is now loose, ready to bounce.
     *
     * If this is part of a Multiple Block, the injury isn't fully resolved,
     * instead it is saved in the Injury Pool in [MultipleBlockContext] and
     * will be resolved later. The same for any loose ball. It is just knocked
     * loose, but the bounce is handled later.
     *
     * If this is just a single block, the entire sequence is handled now.
     */
    object ResolveDefenderDownInjury: ParentNode() {
        override fun skipNodeFor(state: Game, rules: Rules): Node? {
            val context = state.getContext<PushContext>()
            val isOnField = context.firstPushee.location.isOnField(rules)
            return when {
                !context.isDefenderKnockedDown -> {
                    when (isOnField) {
                        true -> DecideToUseStripBall
                        false -> {
                            if (context.isMultipleBlock) {
                                ExitProcedureNode
                            } else {
                                ResolveRemainingPushSequenceForSingleBlock
                            }
                        }
                    }
                }
                else -> null
            }
        }
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<PushContext>()
            val defender = context.firstPushee
            val injuryContext = RiskingInjuryContext(defender, isPartOfMultipleBlock = context.isMultipleBlock)
            return buildCompositeCommand {
                addAll(
                    SetContext(injuryContext)
                )
                if (defender.hasBall()) {
                    val ball = defender.ball!!
                    addAll(
                        SetBallLocation(ball, defender.coordinates),
                        SetBallState.bouncing(ball)
                    )
                }
            }
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = RiskingInjuryRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val multipleBlockContext = state.getContextOrNull<MultipleBlockContext>()
            val injuryContext = state.getContext<RiskingInjuryContext>()
            val updateInjuryCommand = multipleBlockContext?.addInjuryReferenceForPlayer(
                injuryContext.player,
                injuryContext
            )
            return compositeCommandOf(
                RemoveContext<RiskingInjuryContext>(),
                updateInjuryCommand,
                if (multipleBlockContext != null) {
                    ExitProcedure()
                } else {
                    GotoNode(ResolveRemainingPushSequenceForSingleBlock)
                }
            )
        }
    }

    // TODO
    object DecideToUseStripBall: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val isMultipleBlock = state.hasContext<MultipleBlockContext>()
            return when (isMultipleBlock) {
                true -> ExitProcedure()
                false -> GotoNode(ResolveRemainingPushSequenceForSingleBlock)
            }
        }
    }

    // Only called for single blocks
    object ResolveRemainingPushSequenceForSingleBlock: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = PushStepResolveSingleBlockPushChain
        override fun onExitNode(state: Game, rules: Rules): Command {
            return ExitProcedure()
        }
    }
}
