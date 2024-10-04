package dk.ilios.jervis.procedures.tables.prayers

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.Continue
import dk.ilios.jervis.actions.ContinueWhenReady
import dk.ilios.jervis.actions.D3Result
import dk.ilios.jervis.actions.Dice
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.RandomPlayersSelected
import dk.ilios.jervis.actions.RollDice
import dk.ilios.jervis.actions.SelectRandomPlayers
import dk.ilios.jervis.commands.AddPlayerSkill
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.RemoveContext
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.commands.fsm.GotoNode
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.PlayerState
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.model.context.BadHabitsContext
import dk.ilios.jervis.model.context.assertContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.model.hasSkill
import dk.ilios.jervis.procedures.PrayersToNuffleRollContext
import dk.ilios.jervis.reports.ReportDiceRoll
import dk.ilios.jervis.reports.ReportGameProgress
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.skills.DiceRollType
import dk.ilios.jervis.rules.skills.Duration
import dk.ilios.jervis.rules.skills.Loner
import dk.ilios.jervis.utils.INVALID_ACTION
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
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
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
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
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
                                AddPlayerSkill(player, Loner(2, isTemporary = true, expiresAt = Duration.END_OF_DRIVE))
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
