package com.jervisffb.engine.common.procedures

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.CoinSideSelected
import com.jervisffb.engine.actions.CoinTossResult
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.ConfirmWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.TossCoin
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.AddContext
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.commands.probabiliy.AddChanceObservation
import com.jervisffb.engine.common.commands.SetKickingTeam
import com.jervisffb.engine.common.context.CoinTossContext
import com.jervisffb.engine.common.reports.ReportKickingTeamResult
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.castAction
import com.jervisffb.engine.model.Coin
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.statistics.probability.event.ChanceOutcomeCategory
import com.jervisffb.engine.statistics.probability.event.OutcomeRatio
import com.jervisffb.engine.statistics.probability.observation.ChanceDieResult
import com.jervisffb.engine.statistics.probability.observation.ChanceObservation
import com.jervisffb.engine.statistics.probability.observation.ChanceObservationHandler
import com.jervisffb.engine.statistics.probability.observation.ChanceObservationScope
import com.jervisffb.engine.statistics.probability.observation.ChanceOutcome
import com.jervisffb.engine.statistics.probability.observation.ChanceResultId
import com.jervisffb.engine.utils.INVALID_ACTION

/**
 * Select the kicking team automatically by using a coin toss.
 *
 * See page 38 of the BB2020 rulebook.
 * See page 46 of the BB2025 rulebook.
 */
object DetermineKickingTeamStep : Procedure(), ChanceObservationHandler {
    override val initialNode: Node = SelectCoinSide
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        return RemoveContext<CoinTossContext>()
    }

    object SelectCoinSide : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules) = state.receivingTeam
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(com.jervisffb.engine.actions.SelectCoinSide)
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return castAction<CoinSideSelected>(action) {
                compositeCommandOf(
                    AddContext(CoinTossContext(sideSelected = it.side)),
                    GotoNode(CoinToss),
                )
            }
        }
    }

    object CoinToss : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules) = state.kickingTeam
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> = listOf(TossCoin)
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return castAction<CoinTossResult>(action) { coinToss ->
                val context = state.getContext<CoinTossContext>()
                val chanceObservation = createChanceObservation(state, coinToss)
                // It was the receiving team that selected the excepted coin result,
                // so if it lands there, they get to choose first.
                val winner = if (context.sideSelected == coinToss.result) state.receivingTeam else state.kickingTeam
                compositeCommandOf(
                    UpdateContext(context.copy(coinToss = coinToss, winner = winner)),
                    chanceObservation?.let(::AddChanceObservation),
                    GotoNode(ChooseKickingTeam),
                )
            }
        }
    }

    object ChooseKickingTeam : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team {
            val context = state.getContext<CoinTossContext>()
            return context.winner!!
        }
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> =
            listOf(
                ConfirmWhenReady, /* Chooser becomes kicker */
                CancelWhenReady, /* Chooser becomes receiver */
            )

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<CoinTossContext>()
            val winner = context.winner!!
            return when (action) {
                Cancel -> {
                    compositeCommandOf(
                        SetKickingTeam(winner.otherTeam()),
                        ReportKickingTeamResult(context.coinToss!!.result, winner.otherTeam()),
                        ExitProcedure(),
                    )
                }
                Confirm -> {
                    compositeCommandOf(
                        SetKickingTeam(winner),
                        ReportKickingTeamResult(context.coinToss!!.result, winner),
                        ExitProcedure(),
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    private fun createChanceObservation(
        state: Game,
        coinToss: CoinTossResult,
    ): ChanceObservation.DiceRoll? {
        if (!state.collectChanceData) return null

        val index = state.nextAvailableChanceObservationIndex
        val team = state.kickingTeam
        return ChanceObservation.DiceRoll(
            index = index,
            rollType = DiceRollType.COIN_TOSS,
            teamId = team.id,
            dice = listOf(
                ChanceDieResult(
                    id = ChanceResultId(index, 0),
                    result = coinToss.result.d2,
                ),
            ),
            scope = ChanceObservationScope.fromState(state, team),
            success = true,
            outcome = ChanceOutcome(
                category = ChanceOutcomeCategory.TARGET_SET,
                successProbability = OutcomeRatio(1, Coin.entries.size),
            ),
            finalized = true,
        )
    }
}
