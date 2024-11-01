package com.jervisffb.engine.rules.bb2020.procedures

import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetContext
import com.jervisffb.engine.commands.SetSuddenDeathGoals
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.checkDiceRoll
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.reports.LogCategory
import com.jervisffb.engine.reports.ReportDiceRoll
import com.jervisffb.engine.reports.SimpleLogEntry
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.skills.DiceRollType
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import com.jervisffb.engine.commands.compositeCommandOf

data class SuddenDeathContext(
    val homeRolls: List<D6Result> = emptyList(),
    val awayRolls: List<D6Result> = emptyList(),
    val rollOffs: Int = 0 // How many roll offs has happened. Roll offs with the same result are not counted.
): ProcedureContext

/**
 * Procedure responsible for handling Sudden Death as described on page 67 in the rulebook.
 */
object SuddenDeath : Procedure() {
    override val initialNode: Node = HomeTeamRoll
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        return SetContext(SuddenDeathContext())
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object HomeTeamRoll: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.homeTeam
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(RollDice(Dice.D6))
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkDiceRoll<D6Result>(action) { d6 ->
                val context = state.getContext<SuddenDeathContext>()
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.SUDDEN_DEATH, d6),
                    SetContext(context.copy(homeRolls = context.homeRolls + d6)),
                    GotoNode(AwayTeamRoll)
                )
            }
        }
    }

    object AwayTeamRoll: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.awayTeam
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(RollDice(Dice.D6))
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkDiceRoll<D6Result>(action) { d6 ->
                val context = state.getContext<SuddenDeathContext>()
                val homeResult = context.homeRolls.last()
                val rollOffs = context.rollOffs + if (homeResult != d6) 1 else 0
                val (rollOffWinner, goals) = when {
                    homeResult == d6 -> null to 0
                    homeResult.value > d6.value -> state.homeTeam to state.homeSuddenDeathGoals + 1
                    homeResult.value < d6.value -> state.awayTeam to state.awaySuddenDeathGoals + 1
                    else -> INVALID_GAME_STATE("Unsupported state")
                }
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.SUDDEN_DEATH, d6),
                    if (rollOffWinner == null) SimpleLogEntry("Roll-off is a draw", category = LogCategory.GAME_PROGRESS) else null,
                    if (rollOffWinner != null) SimpleLogEntry("${rollOffWinner.name} wins ${rollOffs}. roll-off", category = LogCategory.GAME_PROGRESS) else null,
                    if (rollOffWinner != null) SetSuddenDeathGoals(rollOffWinner, goals) else null,
                    SetContext(context.copy(
                        awayRolls = context.awayRolls + d6,
                        rollOffs = rollOffs
                    )),
                    if (rollOffs == 5) ExitProcedure() else GotoNode(HomeTeamRoll)
                )
            }
        }
    }
}
