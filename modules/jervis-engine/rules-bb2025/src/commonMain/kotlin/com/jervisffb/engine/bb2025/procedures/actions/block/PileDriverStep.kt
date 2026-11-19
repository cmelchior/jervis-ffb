package com.jervisffb.engine.bb2025.procedures.actions.block

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.ConfirmWhenReady
import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.bb2025.procedures.injury.PlacedProne2025
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.AddContext
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.common.context.BlockContext
import com.jervisffb.engine.common.context.FoulContext
import com.jervisffb.engine.common.context.RiskingInjuryContext
import com.jervisffb.engine.common.procedures.actions.foul.FoulStep
import com.jervisffb.engine.common.procedures.tables.injury.RiskingInjuryMode
import com.jervisffb.engine.common.reports.ReportSkillUsed
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PlayerPitchState
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.utils.INVALID_ACTION

/**
 * Procedure for controlling the Pile Driver extra Foul that can happen
 * as part of a Block.
 * 
 * If the active player doesn't have Pile Driver, or it isn't applicable, this
 * procedure will exit without doing anything.
 */
object PileDriverStep : Procedure() {
    override val initialNode: Node = ChooseToUsePileDriver
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<BlockContext>()
    }

    object ChooseToUsePileDriver : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.activePlayerOrThrow().team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<BlockContext>()
            val attacker = context.attacker
            val defender = context.defender
            val isAttackerStanding = rules.isStanding(attacker)
            val isDefenderEligibleForFoul = defender.location.isOnPitch(rules) && (defender.state == PlayerPitchState.PRONE || defender.state == PlayerPitchState.STUNNED)
            val isAdjacentToDefender = attacker.location.isAdjacent(rules, defender.location)
            val hasPileDriver = attacker.team != defender.team && attacker.isSkillAvailable(SkillType.PILE_DRIVER)
            return when (isAttackerStanding && isDefenderEligibleForFoul && isAdjacentToDefender && hasPileDriver) {
                true -> listOf(ConfirmWhenReady, CancelWhenReady)
                false -> listOf(ContinueWhenReady)
            }
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val player = state.activePlayerOrThrow()
            return when (action) {
                Continue,
                Cancel -> ExitProcedure()
                Confirm -> compositeCommandOf(
                    ReportSkillUsed(player, SkillType.PILE_DRIVER),
                    GotoNode(ResolveFoul),
                )
                else -> INVALID_ACTION(action)
            }
        }
    }

    object ResolveFoul: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val blockContext = state.getContext<BlockContext>()
            val foulContext = FoulContext(
                fouler = blockContext.attacker,
                victim = blockContext.defender,
            )
            return AddContext(foulContext)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = FoulStep
        override fun onExitNode(state: Game, rules: Rules): Command {
            val foulContext = state.getContext<FoulContext>()
            return compositeCommandOf(
                RemoveContext(foulContext),
                when (foulContext.fouler.location.isOnPitch(rules)) {
                    true -> GotoNode(PlaceAttackerProne)
                    false -> ExitProcedure()
                },
            )
        }
    }

    object PlaceAttackerProne: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command = AddContext(RiskingInjuryContext(
            player = state.getContext<BlockContext>().attacker,
            mode = RiskingInjuryMode.PLACED_PRONE,
        ))
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = PlacedProne2025
        override fun onExitNode(state: Game, rules: Rules): Command = compositeCommandOf(
            RemoveContext<RiskingInjuryContext>(), ExitProcedure(),
        )
    }
}
