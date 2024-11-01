package com.jervisffb.engine.rules.bb2020.procedures.tables.prayers

import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.D3Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.RandomPlayersSelected
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.actions.SelectRandomPlayers
import com.jervisffb.engine.commands.AddPlayerSkill
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.RemoveContext
import com.jervisffb.engine.commands.SetContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.checkDiceRoll
import com.jervisffb.engine.fsm.checkType
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.BadHabitsContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.hasSkill
import com.jervisffb.engine.reports.ReportDiceRoll
import com.jervisffb.engine.reports.ReportGameProgress
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.PrayersToNuffleRollContext
import com.jervisffb.engine.rules.bb2020.skills.DiceRollType
import com.jervisffb.engine.rules.bb2020.skills.Duration
import com.jervisffb.engine.rules.bb2020.skills.Loner
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.commands.compositeCommandOf
import kotlin.math.min

/**
 * Procedure for handling the Prayer to Nuffle "Bad Habits" as described on page 39
 * of the rulebook.
 */
object BadHabits : Procedure() {
    override val initialNode: Node = RollDie
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        return RemoveContext<BadHabitsContext>()
    }
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<PrayersToNuffleRollContext>()
    }

    object RollDie: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<PrayersToNuffleRollContext>().team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(RollDice(Dice.D3))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkDiceRoll<D3Result>(action) { d3 ->
                // Figure out how many players match the roll, if less players are available,
                // Use the lower of the dice roll or number of players available
                val prayerContext = state.getContext<PrayersToNuffleRollContext>()
                val availablePlayers = getEligiblePlayers(prayerContext, rules)
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.BAD_HABITS, d3),
                    SetContext(BadHabitsContext(roll = d3, mustSelectPlayers = min(availablePlayers.size, d3.value))),
                    GotoNode(SelectPlayers)
                )
            }
        }
    }

    object SelectPlayers: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team? = null
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val prayerContext = state.getContext<PrayersToNuffleRollContext>()
            val badHabitsContext = state.getContext<BadHabitsContext>()
            val availablePlayers = getEligiblePlayers(prayerContext, rules).map { it.id }

            return if (badHabitsContext.mustSelectPlayers == 0) {
                listOf(ContinueWhenReady)
            } else {
                listOf(SelectRandomPlayers(badHabitsContext.roll.value, availablePlayers))
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when(action) {
                is Continue -> {
                    compositeCommandOf(
                        ReportGameProgress("No players are able to receive Bad Habits"),
                        ExitProcedure(),
                    )
                }
                else -> {
                    checkType<RandomPlayersSelected>(action) {
                        val prayerContext = state.getContext<PrayersToNuffleRollContext>()
                        val badHabitsContext = state.getContext<BadHabitsContext>()
                        if (it.players.size != badHabitsContext.mustSelectPlayers) {
                            INVALID_ACTION(action,"Wrong number of players selected: ${it.players.size} vs. ${badHabitsContext.mustSelectPlayers}")
                        }

                        val addLonerCommands = it.getPlayers(state).flatMap { player ->
                            listOf(
                                ReportGameProgress("${player.name} received Loner (2+)"),
                                AddPlayerSkill(player,
                                    Loner(
                                        2,
                                        isTemporary = true,
                                        expiresAt = Duration.END_OF_DRIVE
                                    )
                                )
                            )
                        }.toTypedArray()

                        compositeCommandOf(
                            *addLonerCommands,
                            SetContext(prayerContext.copy(resultApplied = true)),
                            ExitProcedure()
                        )
                    }
                }
            }
        }
    }

    // Helper functions below

    private fun getEligiblePlayers(context: PrayersToNuffleRollContext, rules: Rules): List<Player> {
        return context.team.otherTeam().filter {
            (it.state == PlayerState.RESERVE || it.location.isOnField(rules)) && !it.hasSkill<Loner>()
        }
    }

}
