package com.jervisffb.engine.bb2025.procedures.actions.foul

import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.ConfirmWhenReady
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.bb2025.procedures.actions.block.ChainsawRoll
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetTurnOver
import com.jervisffb.engine.commands.buildCompositeCommand
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
import com.jervisffb.engine.model.TurnOver
import com.jervisffb.engine.model.context.ChainsawContext
import com.jervisffb.engine.model.context.FoulContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.model.modifiers.ArmourModifier
import com.jervisffb.engine.reports.ReportSkillUsed
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.tables.injury.RiskingInjuryContext
import com.jervisffb.engine.rules.common.procedures.tables.injury.RiskingInjuryMode
import com.jervisffb.engine.rules.common.skills.SkillType

/**
 * Procedure for handling the "Chainsaw"-part of using a Chainsaw during
 * Foul.
 *
 * Both success and failure is handled here, so the parent just need to move to the next step.
 */
object ChainsawFoulStep: Procedure() {
    override val initialNode: Node = ChooseToUseChainsaw
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<FoulContext>()

    object ChooseToUseChainsaw: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<FoulContext>().fouler.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<FoulContext>()
            return when (context.fouler.isSkillAvailable(SkillType.CHAINSAW)) {
                true -> listOf(ConfirmWhenReady, CancelWhenReady)
                false -> listOf(ContinueWhenReady)
            }
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<FoulContext>()
            val useChainsaw = (action is Confirm)
            return when (useChainsaw) {
                true -> compositeCommandOf(
                    ReportSkillUsed(context.fouler, SkillType.CHAINSAW),
                    GotoNode(RollForChainsaw)
                )
                false -> ExitProcedure()
            }
        }
    }

    object RollForChainsaw: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val foulContext = state.getContext<FoulContext>()
            val context = ChainsawContext(
                attacker = foulContext.fouler,
                attackerOriginalCoordinates = foulContext.fouler.coordinates,
                defender = foulContext.victim!!,
                defenderOriginalCoordinates = foulContext.victim!!.coordinates
            )
            return AddContext(context)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ChainsawRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val injuryContext = state.getContext<RiskingInjuryContext>()
            val context = state.getContext<ChainsawContext>()
            return buildCompositeCommand {
                add(RemoveContext(context))
                when (context.isSuccess) {
                    true -> addAll(
                        UpdateContext(injuryContext.copy(armourModifiers = injuryContext.armourModifiers.add(ArmourModifier.CHAINSAW))),
                        ExitProcedure()
                    )
                    false -> add(GotoNode(ResolveAttackerKnockedDown))
                }
            }
        }
    }

    object ResolveAttackerKnockedDown: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<FoulContext>()
            val injuryContext = RiskingInjuryContext(
                player = context.fouler,
                causedBy = null,
                mode = RiskingInjuryMode.KNOCKED_DOWN
            )
            return AddContext(injuryContext)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = rules.knockedDownStep
        override fun onExitNode(state: Game, rules: Rules): Command {
            val injuryContext = state.getContext<RiskingInjuryContext>()
            val isStanding = rules.isStanding(injuryContext.player)
            return compositeCommandOf(
                RemoveContext(injuryContext),
                when (!isStanding) {
                    true -> GotoNode(AbortFoul)
                    false -> ExitProcedure()
                }
            )
        }
    }

    // Do this in its own Node, so we can access the correct Injury context
    object AbortFoul: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<RiskingInjuryContext>()
            return compositeCommandOf(
                SetTurnOver(TurnOver.STANDARD),
                UpdateContext(context.copy(armourRollAborted = true)),
                ExitProcedure()
            )
        }
    }

}
