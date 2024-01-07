package dk.ilios.jervis.procedures

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.D3Result
import dk.ilios.jervis.actions.Dice
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.RollDice
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.commands.GotoNode
import dk.ilios.jervis.commands.SetActiveTeam
import dk.ilios.jervis.commands.SetFanFactor
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.reports.ReportFanFactor
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.utils.INVALID_GAME_STATE

object RollForStartingFanFactor: Procedure() {
    override val initialNode: Node = SetFanFactorForHomeTeam
    override fun onEnterProcedure(state: Game, rules: Rules): Command? {
        if (!state.activeTeam.isHomeTeam()) {
            INVALID_GAME_STATE("Expected active team to be the home team.")
        }
        return null
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command? {
        return SetActiveTeam(state.homeTeam)
    }

    object SetFanFactorForHomeTeam: ActionNode() {
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            return listOf(RollDice(Dice.D3))
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val dedicatedFans = state.homeTeam.dedicatedFans
            return checkDiceRoll<D3Result>(action) {
                val total = it.result + dedicatedFans
                compositeCommandOf(
                    SetFanFactor(state.homeTeam, total),
                    ReportFanFactor(state.homeTeam, it.result, dedicatedFans),
                    GotoNode(SetFanFactorForAwayTeam)
                )
            }
        }
    }

    object SetFanFactorForAwayTeam: ActionNode() {
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            return listOf(RollDice(Dice.D3))
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val dedicatedFans = state.awayTeam.dedicatedFans
            return checkDiceRoll<D3Result>(action) {
                val total = it.result + dedicatedFans
                compositeCommandOf(
                    SetFanFactor(state.awayTeam, total),
                    ReportFanFactor(state.awayTeam, it.result, dedicatedFans),
                    ExitProcedure()
                )
            }
        }
    }


}