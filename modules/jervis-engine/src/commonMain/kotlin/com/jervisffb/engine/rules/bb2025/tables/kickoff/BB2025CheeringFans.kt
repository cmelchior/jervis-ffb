package com.jervisffb.engine.rules.common.procedures.tables.kickoff

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.commands.AddTeamStatusEffect
import com.jervisffb.engine.commands.Command
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
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.castDiceRoll
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.CheeringFansContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.modifiers.TeamStatusEffect
import com.jervisffb.engine.model.modifiers.TeamStatusEffectType
import com.jervisffb.engine.reports.ReportCheeringFansResult
import com.jervisffb.engine.reports.ReportDiceRoll
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules

/**
 * Procedure for handling the Kick-Off Event: "Cheering Fans" as described on page 41
 * of the BB2020 rulebook.
 */
object BB2025CheeringFans : Procedure() {
    override val initialNode: Node = KickingTeamRollDie
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command = RemoveContext<CheeringFansContext>()

    object KickingTeamRollDie : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.kickingTeam
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(RollDice(Dice.D6))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return castDiceRoll<D6Result>(action) { d6 ->
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.CHEERING_FANS, d6),
                    AddContext(CheeringFansContext(d6)),
                    GotoNode(ReceivingTeamRollDie),
                )
            }
        }
    }

    object ReceivingTeamRollDie : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.receivingTeam
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(RollDice(Dice.D6))
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return castDiceRoll<D6Result>(action) { d6 ->
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.CHEERING_FANS, d6),
                    UpdateContext(state.getContext<CheeringFansContext>().copy(receivingTeamRoll = d6)),
                    GotoNode(ResolveCheeringFans),
                )
            }
        }
    }

    object ResolveCheeringFans : ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<CheeringFansContext>()
            val kickingTeam = state.kickingTeam
            val receivingTeam = state.receivingTeam
            val kickingTeamResult = context.kickingTeamRoll.value + kickingTeam.cheerleaders
            val receivingTeamResult = context.receivingTeamRoll!!.value + receivingTeam.cheerleaders
            return buildCompositeCommand {
                if (kickingTeamResult >= receivingTeamResult && !kickingTeam.hasStatusEffect(TeamStatusEffectType.CHEERING_FANS_OFFENSIVE_ASSIST)) {
                    add(AddTeamStatusEffect(kickingTeam, TeamStatusEffect.cheeringFans()))
                }
                if (receivingTeamResult >= kickingTeamResult && !receivingTeam.hasStatusEffect(TeamStatusEffectType.CHEERING_FANS_OFFENSIVE_ASSIST)) {
                    add(AddTeamStatusEffect(receivingTeam, TeamStatusEffect.cheeringFans()))
                }
                add(
                    ReportCheeringFansResult(
                        kickingTeam,
                        receivingTeam,
                        context.kickingTeamRoll,
                        kickingTeam.cheerleaders,
                        context.receivingTeamRoll,
                        receivingTeam.cheerleaders,
                    )
                )
                if (kickingTeamResult > receivingTeamResult) {
                    UpdateContext(state.getContext<CheeringFansContext>().copy(winner = kickingTeam))
                }
                if (receivingTeamResult > kickingTeamResult) {
                    UpdateContext(state.getContext<CheeringFansContext>().copy(winner = receivingTeam))
                }
                add(ExitProcedure())
            }
        }
    }
}
