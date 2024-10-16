package com.jervisffb.engine.rules.bb2020.procedures

import com.jervisffb.engine.actions.ActionDescriptor
import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.CoinSideSelected
import com.jervisffb.engine.actions.CoinTossResult
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.ConfirmWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.TossCoin
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetContext
import com.jervisffb.engine.commands.SetKickingTeam
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.checkType
import com.jervisffb.engine.model.Coin
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.reports.ReportKickingTeamResult
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.commands.compositeCommandOf

data class CoinTossContext(
    val sideSelected: Coin,
    val coinToss: CoinTossResult? = null,
    val winner: Team? = null,
): ProcedureContext

/**
 * Select the kicking team automatically by using a coin toss.
 *
 * See page 38 of the rulebook.
 */
object DetermineKickingTeam : Procedure() {
    override val initialNode: Node = SelectCoinSide
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object SelectCoinSide : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules) = state.receivingTeam
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            return listOf(com.jervisffb.engine.actions.SelectCoinSide)
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkType<CoinSideSelected>(action) {
                compositeCommandOf(
                    SetContext(CoinTossContext(sideSelected = it.side)),
                    GotoNode(CoinToss),
                )
            }
        }
    }

    object CoinToss : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules) = state.kickingTeam
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> = listOf(TossCoin)
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkType<CoinTossResult>(action) { coinToss ->
                val context = state.getContext<CoinTossContext>()
                // It was the receiving team that selected the excepted coin result,
                // so if it lands there, they get to choose first.
                val winner = if (context.sideSelected == coinToss.result) state.receivingTeam else state.kickingTeam
                compositeCommandOf(
                    SetContext(state.getContext<CoinTossContext>().copy(coinToss = coinToss, winner = winner)),
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
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> =
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
}
