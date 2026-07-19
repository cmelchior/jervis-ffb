package com.jervisffb.engine.rules.common.procedures.tables.kickoff

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.commands.AddTeamFeature
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
import com.jervisffb.engine.model.modifiers.CheeringFansModifiers
import com.jervisffb.engine.model.modifiers.CheerleadersModifiers
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.model.modifiers.TeamFeature
import com.jervisffb.engine.model.modifiers.TeamFeatureType
import com.jervisffb.engine.reports.ReportCheeringFansResult
import com.jervisffb.engine.reports.ReportDiceRoll
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import com.jervisffb.engine.utils.sum

/**
 * Procedure for handling the Kick-Off Event: "Cheering Fans" as described on
 * page 48 of the BB2025 rulebook.
 */
object BB2025CheeringFans : Procedure() {
    override val initialNode: Node = DetermineModifiers
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command = RemoveContext<CheeringFansContext>()

    object DetermineModifiers: ComputationNode() {

        private fun addModifiers(list: MutableList<DiceModifier>, team: Team): List<DiceModifier> {
            val cheerleaders = team.cheerleaders
            if (cheerleaders > 0) {
                list.add(CheerleadersModifiers(cheerleaders))
            }
            val hasMascot = team.mascots.isNotEmpty()
            if (hasMascot) {
                list.add(CheeringFansModifiers.TEAM_MASCOT)
            }
            return list
        }

        override fun apply(state: Game, rules: Rules): Command {
            val context = CheeringFansContext(
                kickingTeamModifiers = addModifiers(mutableListOf(), state.kickingTeam),
                receivingTeamModifiers = addModifiers(mutableListOf(), state.receivingTeam)
            )
            return compositeCommandOf(
                AddContext(context),
                GotoNode(KickingTeamRollDie)
            )
        }
    }


    object KickingTeamRollDie : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.kickingTeam
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(RollDice(Dice.D6))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<CheeringFansContext>()
            return castDiceRoll<D6Result>(action) { d6 ->
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.CHEERING_FANS, d6),
                    UpdateContext(context.copy(kickingTeamRoll = d6)),
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
            val winner = calculateWinner(state, context)
            val kickingTeam = state.kickingTeam
            val receivingTeam = state.receivingTeam
            return buildCompositeCommand {
                if ((winner == null || winner == kickingTeam) && !kickingTeam.hasFeature(TeamFeatureType.CHEERING_FANS_OFFENSIVE_ASSIST)) {
                    add(AddTeamFeature(kickingTeam, TeamFeature.cheeringFans()))
                }
                if ((winner == null || winner == receivingTeam) && !receivingTeam.hasFeature(TeamFeatureType.CHEERING_FANS_OFFENSIVE_ASSIST)) {
                    add(AddTeamFeature(receivingTeam, TeamFeature.cheeringFans()))
                }
                add(
                    ReportCheeringFansResult(
                        kickingTeam,
                        receivingTeam,
                        context
                    )
                )
                if (winner == kickingTeam) {
                    UpdateContext(state.getContext<CheeringFansContext>().copy(winner = kickingTeam))
                }
                if (winner == receivingTeam) {
                    UpdateContext(state.getContext<CheeringFansContext>().copy(winner = receivingTeam))
                }
                add(ExitProcedure())
            }
        }
    }

    // -- HELPER METHODS --

    private fun calculateWinner(state: Game, context: CheeringFansContext): Team? {
        return with(context) {
            val kickingDieRoll = kickingTeamRoll?.value ?: 0
            val receivingDieRoll = receivingTeamRoll?.value ?: 0
            val kickingResult = kickingDieRoll + kickingTeamModifiers.sum()
            val receivingResult = receivingDieRoll + receivingTeamModifiers.sum()
            when {
                kickingResult > receivingResult -> state.kickingTeam
                receivingResult > kickingResult -> state.receivingTeam
                kickingResult == receivingResult -> null
                else -> INVALID_GAME_STATE("Unknown state")
            }
        }
    }
}
