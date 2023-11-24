package dk.ilios.bowlbot.procedures

import compositeCommandOf
import dk.ilios.bowlbot.actions.Action
import dk.ilios.bowlbot.actions.ActionDescriptor
import dk.ilios.bowlbot.actions.D2Result
import dk.ilios.bowlbot.actions.RollD2
import dk.ilios.bowlbot.commands.Command
import dk.ilios.bowlbot.commands.ExitProcedure
import dk.ilios.bowlbot.commands.SetKickingTeam
import dk.ilios.bowlbot.fsm.ActionNode
import dk.ilios.bowlbot.fsm.Node
import dk.ilios.bowlbot.fsm.Procedure
import dk.ilios.bowlbot.logs.ReportKickingTeamResult
import dk.ilios.bowlbot.model.Game
import dk.ilios.bowlbot.rules.Rules

/**
 * Select the kicking team automatically by using a coin toss.
 * TODO Extend this with the winner choosing whether to kick or receive.
 */
object DetermineKickingTeam: Procedure() {
    override val initialNode: Node = CoinToss
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object CoinToss: ActionNode() {
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> = listOf(RollD2)
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
                    else -> TODO()
                }
            }
        }
    }


}