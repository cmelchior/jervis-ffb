package dk.ilios.jervis.procedures

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.actions.Dice
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.RollDice
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.commands.SetSuddenDeathGoals
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.commands.fsm.GotoNode
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.model.context.ProcedureContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.reports.LogCategory
import dk.ilios.jervis.reports.ReportDiceRoll
import dk.ilios.jervis.reports.SimpleLogEntry
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.skills.DiceRollType
import dk.ilios.jervis.utils.INVALID_GAME_STATE

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
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
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
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
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
