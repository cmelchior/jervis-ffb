package dk.ilios.jervis.procedures

import compositeCommandOf
import dk.ilios.jervis.actions.Action
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.D2Result
import dk.ilios.jervis.actions.Dice
import dk.ilios.jervis.actions.RollDice
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.commands.SetKickingTeam
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.reports.ReportKickingTeamResult
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.utils.INVALID_GAME_STATE

/**
 * Select the kicking team automatically by using a coin toss.
 * TODO Extend this with the winner choosing whether to kick or receive.
 */
object DetermineKickingTeam: Procedure() {
    override val initialNode: Node = CoinToss
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object CoinToss: ActionNode() {
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> = listOf(RollDice(Dice.D2))
        override fun applyAction(action: Action, state: Game, rules: Rules): Command {
            return checkType<D2Result>(action) {
                when(it.result) {
                    1 -> {
                        return compositeCommandOf(
                            SetKickingTeam(state.homeTeam),
                            ReportKickingTeamResult(1, state.homeTeam),
                            ExitProcedure()
                        )
                    }
                    2 -> {
                        return compositeCommandOf(
                            SetKickingTeam(state.awayTeam),
                            ReportKickingTeamResult(2, state.awayTeam),
                            ExitProcedure()
                        )
                    }
                    else -> INVALID_GAME_STATE()
                }
            }
        }
    }
}