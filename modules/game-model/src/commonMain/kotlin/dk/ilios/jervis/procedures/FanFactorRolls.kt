package dk.ilios.jervis.procedures

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.D3Result
import dk.ilios.jervis.actions.Dice
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.RollDice
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.SetActiveTeam
import dk.ilios.jervis.commands.SetFanFactor
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.commands.fsm.GotoNode
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.reports.ReportDiceRoll
import dk.ilios.jervis.reports.ReportFanFactor
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.skills.DiceRollType
import dk.ilios.jervis.utils.INVALID_GAME_STATE

/**
 * This procedure controls rolling for "The Fans" as described on page
 * 37 in the rulebook.
 */
object FanFactorRolls : Procedure() {
    override val initialNode: Node = SetFanFactorForHomeTeam

    override fun onEnterProcedure(state: Game, rules: Rules): Command? {
        if (!state.activeTeam.isHomeTeam()) {
            INVALID_GAME_STATE("Expected active team to be the home team.")
        }
        return null
    }

    override fun onExitProcedure(state: Game, rules: Rules): Command {
        return SetActiveTeam(state.homeTeam)
    }

    object SetFanFactorForHomeTeam : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.homeTeam

        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            return listOf(RollDice(Dice.D3))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkDiceRoll<D3Result>(action) { d3 ->
                val dedicatedFans = state.homeTeam.dedicatedFans
                val total = d3.value + dedicatedFans
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.FAN_FACTOR, d3),
                    SetFanFactor(state.homeTeam, total),
                    ReportFanFactor(state.homeTeam, d3.value, dedicatedFans),
                    GotoNode(SetFanFactorForAwayTeam),
                )
            }
        }
    }

    object SetFanFactorForAwayTeam : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.awayTeam

        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            return listOf(RollDice(Dice.D3))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val dedicatedFans = state.awayTeam.dedicatedFans
            return checkDiceRoll<D3Result>(action) {
                val total = it.value + dedicatedFans
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.FAN_FACTOR, it),
                    SetFanFactor(state.awayTeam, total),
                    ReportFanFactor(state.awayTeam, it.value, dedicatedFans),
                    ExitProcedure(),
                )
            }
        }
    }
}
