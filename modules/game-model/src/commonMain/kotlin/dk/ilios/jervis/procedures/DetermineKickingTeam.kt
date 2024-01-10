package dk.ilios.jervis.procedures

import compositeCommandOf
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.Cancel
import dk.ilios.jervis.actions.CancelWhenReady
import dk.ilios.jervis.actions.CoinSideSelected
import dk.ilios.jervis.actions.CoinTossResult
import dk.ilios.jervis.actions.Confirm
import dk.ilios.jervis.actions.ConfirmWhenReady
import dk.ilios.jervis.actions.Continue
import dk.ilios.jervis.actions.D2Result
import dk.ilios.jervis.actions.Dice
import dk.ilios.jervis.actions.DiceResults
import dk.ilios.jervis.actions.DieResult
import dk.ilios.jervis.actions.DogoutSelected
import dk.ilios.jervis.actions.EndAction
import dk.ilios.jervis.actions.EndSetup
import dk.ilios.jervis.actions.EndTurn
import dk.ilios.jervis.actions.FieldSquareSelected
import dk.ilios.jervis.actions.PlayerActionSelected
import dk.ilios.jervis.actions.PlayerDeselected
import dk.ilios.jervis.actions.PlayerSelected
import dk.ilios.jervis.actions.RollDice
import dk.ilios.jervis.actions.SelectCoinSide
import dk.ilios.jervis.actions.TossCoin
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.commands.GotoNode
import dk.ilios.jervis.commands.SetActiveTeam
import dk.ilios.jervis.commands.SetCoinSideSelected
import dk.ilios.jervis.commands.SetCoinTossResult
import dk.ilios.jervis.commands.SetKickingTeam
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.reports.ReportKickingTeamResult
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.utils.INVALID_ACTION
import dk.ilios.jervis.utils.INVALID_GAME_STATE




/**
 * Select the kicking team automatically by using a coin toss.
 *
 * See page 38 of the rulebook.
 */
object DetermineKickingTeam: Procedure() {
    override val initialNode: Node = SelectCoinSide
    override fun onEnterProcedure(state: Game, rules: Rules): Command? {
        if (!state.activeTeam.isHomeTeam()) {
            INVALID_GAME_STATE("Home team should be active at this stage.")
        }
        return null
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object SelectCoinSide: ActionNode() {
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            return listOf(dk.ilios.jervis.actions.SelectCoinSide)
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkType<CoinSideSelected>(action) {
                val result = it.side
                compositeCommandOf(
                    SetCoinSideSelected(result),
                    GotoNode(CoinToss)
                )
            }
        }
    }

    object CoinToss: ActionNode() {
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> = listOf(TossCoin)
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkType<CoinTossResult>(action) {
                val result = it.result
                if (result == state.coinSideSelected) {
                    return compositeCommandOf(
                        SetCoinTossResult(result),
                        GotoNode(ChooseKickingTeam)
                    )
                } else {
                    return compositeCommandOf(
                        SetCoinTossResult(result),
                        SetActiveTeam(state.awayTeam),
                        GotoNode(ChooseKickingTeam)
                    )
                }
            }
        }
    }

    object ChooseKickingTeam: ActionNode() {
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> = listOf(
            ConfirmWhenReady,
            CancelWhenReady
        )

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when(action) {
                Cancel -> {
                    compositeCommandOf(
                        SetKickingTeam(state.awayTeam),
                        ReportKickingTeamResult(state.coinResult!!, state.awayTeam),
                        SetActiveTeam(state.homeTeam),
                        ExitProcedure()
                    )
                }
                Confirm -> {
                    compositeCommandOf(
                        SetKickingTeam(state.homeTeam),
                        ReportKickingTeamResult(state.coinResult!!, state.homeTeam),
                        SetActiveTeam(state.homeTeam),
                        ExitProcedure()
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }
}