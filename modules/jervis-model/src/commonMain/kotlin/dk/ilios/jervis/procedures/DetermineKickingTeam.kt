package dk.ilios.jervis.procedures

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.Cancel
import dk.ilios.jervis.actions.CancelWhenReady
import dk.ilios.jervis.actions.CoinSideSelected
import dk.ilios.jervis.actions.CoinTossResult
import dk.ilios.jervis.actions.Confirm
import dk.ilios.jervis.actions.ConfirmWhenReady
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.TossCoin
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.commands.SetKickingTeam
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.commands.fsm.GotoNode
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Coin
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.model.context.ProcedureContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.reports.ReportKickingTeamResult
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.utils.INVALID_ACTION

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
            return listOf(dk.ilios.jervis.actions.SelectCoinSide)
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
