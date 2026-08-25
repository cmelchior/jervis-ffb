package com.jervisffb.engine.common.procedures

import com.jervisffb.engine.actions.D8Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.commands.probabiliy.AddChanceObservation
import com.jervisffb.engine.common.commands.AddSpecialPlayCard
import com.jervisffb.engine.common.context.DesperateMeasuresRollContext
import com.jervisffb.engine.common.procedures.dicerolls.createFinalTableLookupObservation
import com.jervisffb.engine.common.reports.ReportDiceRoll
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.castDiceRoll
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.reports.LogCategory
import com.jervisffb.engine.reports.SimpleLogEntry
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.tables.DesperateMeasuresEvent
import com.jervisffb.engine.statistics.probability.observation.ChanceObservationHandler

/**
 * Roll on the Desperate Measures table as many times as defined in [DesperateMeasuresRollContext].
 * If a result is already rolled, it will continue re-rolling until it succeeds.
 *
 * See page 15 in Spike 22.
 */
object DesperateMeasuresRoll : Procedure(), ChanceObservationHandler {
    override val initialNode: Node = RollDie
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<DesperateMeasuresRollContext>()

    object RollDie : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<DesperateMeasuresRollContext>().team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(RollDice(Dice.D8, type = DiceRollType.DESPERATE_MEASURES))
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val table = rules.desperateMeasuresTable
            return castDiceRoll<D8Result>(action) { d8 ->
                val context = state.getContext<DesperateMeasuresRollContext>()
                val result: DesperateMeasuresEvent = table.roll(d8)
                val chanceObservation = createFinalTableLookupObservation(
                    state = state,
                    team = context.team,
                    rollType = DiceRollType.DESPERATE_MEASURES,
                    dice = listOf(d8),
                    favorableOutcomes = table.entries.values.count { it == result },
                    possibleOutcomes = Dice.D8.sides,
                )

                // Multiple instances of the same desperate measure are not allowed on the same team.
                return buildCompositeCommand {
                    add(ReportDiceRoll(DiceRollType.DESPERATE_MEASURES, d8))
                    chanceObservation?.let { add(AddChanceObservation(it)) }
                    if (context.rolledEvents.contains(result)) {
                        addAll(
                            SimpleLogEntry("Desperate Measure was already rolled: ${result.label}. Roll again.", LogCategory.GAME_PROGRESS),
                            GotoNode(RollDie)
                        )
                    } else {
                        addAll(
                            UpdateContext(
                                context.copy(
                                    rollsRemaining = context.rollsRemaining - 1,
                                    result = result,
                                    resultApplied = false,
                                    rolledEvents = context.rolledEvents.add(result)
                                )
                            ),
                            AddSpecialPlayCard(context.team, result.createCard()),
                        )
                        when (context.rollsRemaining > 1) {
                            true -> add(GotoNode(RollDie))
                            false -> add(ExitProcedure())
                        }
                    }
                }
            }
        }
    }
}
