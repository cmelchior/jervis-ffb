package com.jervisffb.engine.rules.common.procedures

import com.jervisffb.engine.actions.D3Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetFairWeatherFans
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.castDiceRoll
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.reports.ReportDiceRoll
import com.jervisffb.engine.reports.ReportFanFactor
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules

/**
 * This procedure controls rolling for "The Fans".
 *
 * See page 37 in the BB2020 rulebook.
 * See page 45 in the BB2025 rulebook.
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
            return castDiceRoll<D3Result>(action) { d3 ->
                val dedicatedFans = state.homeTeam.dedicatedFans
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.FAN_FACTOR, d3),
                    SetFairWeatherFans(state.homeTeam, d3.value),
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
            return castDiceRoll<D3Result>(action) { d3 ->
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.FAN_FACTOR, d3),
                    SetFairWeatherFans(state.awayTeam, d3.value),
                    ReportFanFactor(state.awayTeam, d3.value, dedicatedFans),
                    ExitProcedure(),
                )
            }
        }
    }
}
