package com.jervisffb.engine.common.procedures

import com.jervisffb.engine.actions.D3Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.commands.probabiliy.AddChanceObservation
import com.jervisffb.engine.common.commands.SetFairWeatherFans
import com.jervisffb.engine.common.reports.ReportDiceRoll
import com.jervisffb.engine.common.reports.ReportFanFactor
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.castDiceRoll
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.statistics.probability.event.ChanceOutcomeCategory
import com.jervisffb.engine.statistics.probability.event.OutcomeRatio
import com.jervisffb.engine.statistics.probability.observation.ChanceDieResult
import com.jervisffb.engine.statistics.probability.observation.ChanceOutcome
import com.jervisffb.engine.statistics.probability.observation.ChanceObservation
import com.jervisffb.engine.statistics.probability.observation.ChanceObservationHandler
import com.jervisffb.engine.statistics.probability.observation.ChanceObservationScope
import com.jervisffb.engine.statistics.probability.observation.ChanceResultId
import kotlin.text.Typography.half

/**
 * This procedure controls rolling for "The Fans".
 *
 * See page 37 in the BB2020 rulebook.
 * See page 45 in the BB2025 rulebook.
 */
object FanFactorRolls : Procedure(), ChanceObservationHandler {
    override val initialNode: Node = SetFanFactorForHomeTeam
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object SetFanFactorForHomeTeam : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.homeTeam
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(RollDice(Dice.D3))
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val team = state.homeTeam
            return castDiceRoll<D3Result>(action) { d3 ->
                val dedicatedFans = team.dedicatedFans
                val chanceObservation = createChanceObservation(state, team, d3)
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.FAN_FACTOR, d3),
                    SetFairWeatherFans(team, d3.value),
                    ReportFanFactor(team, d3.value, dedicatedFans),
                    chanceObservation?.let(::AddChanceObservation),
                    GotoNode(SetFanFactorForAwayTeam),
                )
            }
        }
    }

    object SetFanFactorForAwayTeam : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.awayTeam
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(RollDice(Dice.D3))
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val team = state.awayTeam
            val dedicatedFans = team.dedicatedFans
            return castDiceRoll<D3Result>(action) { d3 ->
                val chanceObservation = createChanceObservation(state, team, d3)
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.FAN_FACTOR, d3),
                    SetFairWeatherFans(team, d3.value),
                    ReportFanFactor(team, d3.value, dedicatedFans),
                    chanceObservation?.let(::AddChanceObservation),
                    ExitProcedure(),
                )
            }
        }
    }

    private fun createChanceObservation(
        state: Game,
        team: Team,
        d3: D3Result,
    ): ChanceObservation.DiceRoll? {
        if (!state.collectChanceData) return null

        val index = state.nextAvailableChanceObservationIndex
        return ChanceObservation.DiceRoll(
            index = index,
            rollType = DiceRollType.FAN_FACTOR,
            teamId = team.id,
            dice = listOf(
                ChanceDieResult(
                    id = ChanceResultId(index, 0),
                    result = d3,
                ),
            ),
            scope = ChanceObservationScope.fromState(state, team),
            success = true,
            outcome = ChanceOutcome(
                category = ChanceOutcomeCategory.AT_LEAST,
                successProbability = OutcomeRatio(4 - d3.value, 3),
            ),
            finalized = true,
        )
    }
}
