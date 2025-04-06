package com.jervisffb.engine.rules.bb2020.procedures

import com.jervisffb.engine.actions.D3Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetFanFactor
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.checkDiceRoll
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.reports.ReportDiceRoll
import com.jervisffb.engine.reports.ReportFanFactor
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules

/**
 * This procedure controls rolling for "The Fans" as described on page
 * 37 in the rulebook.
 */
object FanFactorRolls : Procedure() {
    override val initialNode: Node = SetFanFactorForHomeTeam
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object SetFanFactorForHomeTeam : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.homeTeam

        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
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

        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
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
