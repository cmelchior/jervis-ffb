package com.jervisffb.engine.common.procedures

import com.jervisffb.engine.actions.D16Result
import com.jervisffb.engine.actions.D8Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.DieResult
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.commands.probabiliy.AddChanceObservation
import com.jervisffb.engine.common.commands.AddPrayersToNuffle
import com.jervisffb.engine.common.context.PrayersToNuffleRollContext
import com.jervisffb.engine.common.procedures.dicerolls.createFinalTableLookupObservation
import com.jervisffb.engine.common.reports.ReportDiceRoll
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
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
import com.jervisffb.engine.rules.common.tables.PrayerToNuffleEvent
import com.jervisffb.engine.statistics.probability.observation.ChanceObservationHandler

/**
 * Roll on the Prayers to Nuffle table as many times as defined in [PrayersToNuffleRollContext].
 * If a result is already active, it will continue re-rolling until it succeeds.
 * See page 39 in the rulebook.
 */
object PrayersToNuffleRoll : Procedure(), ChanceObservationHandler {
    override val initialNode: Node = RollDie
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<PrayersToNuffleRollContext>()

    object RollDie : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<PrayersToNuffleRollContext>().team

        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(RollDice(rules.prayersToNuffleTable.die, type = DiceRollType.PRAYERS_TO_NUFFLE))
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val table = rules.prayersToNuffleTable
            return castDiceRoll<DieResult>(action) { dieRoll ->
                val context = state.getContext<PrayersToNuffleRollContext>()
                val result: PrayerToNuffleEvent = table.roll(dieRoll)
                val chanceObservation = createFinalTableLookupObservation(
                    state = state,
                    team = context.team,
                    rollType = DiceRollType.PRAYERS_TO_NUFFLE,
                    dice = listOf(dieRoll),
                    favorableOutcomes = table.entries.values.count { it == result },
                    possibleOutcomes = when (table.die) {
                        Dice.D8 -> D8Result.SIDES
                        Dice.D16 -> D16Result.SIDES
                        else -> error("Unsupported Prayers to Nuffle die: ${table.die}")
                    },
                )

                // Multiple instances of the same prayer are not allowed on the same team.
                // Neither as an inducement nor as a kick-off table result
                return buildCompositeCommand {
                    add(ReportDiceRoll(DiceRollType.PRAYERS_TO_NUFFLE, dieRoll))
                    chanceObservation?.let { add(AddChanceObservation(it)) }
                    if (context.team.activePrayersToNuffle.contains(result)) {
                        addAll(
                            SimpleLogEntry("Prayer already active: ${result.description}. Roll again.", LogCategory.GAME_PROGRESS),
                            GotoNode(RollDie)
                        )
                    } else {
                        addAll(
                            UpdateContext(
                                context.copy(
                                    rollsRemaining = context.rollsRemaining - 1,
                                    result = result,
                                    resultApplied = false
                                )
                            ),
                            GotoNode(ApplyTableResult),
                        )
                    }
                }
            }
        }
    }

    object ApplyTableResult : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            return state.getContext<PrayersToNuffleRollContext>().result!!.procedure
        }
        override fun onExitNode(state: Game, rules: Rules): Command {
            // Currently, we do not grant another roll if the Prayer was not applied.
            // In that case, the roll is "wasted". It is unclear if that is the correct
            // rule interpretation.
            val context = state.getContext<PrayersToNuffleRollContext>()
            return compositeCommandOf(
                AddPrayersToNuffle(context.team, context.result!!),
                if (context.rollsRemaining >= 1) {
                    GotoNode(RollDie)
                } else {
                    ExitProcedure()
                }
            )
        }
    }
}
