package com.jervisffb.engine.rules.bb2020.procedures

import com.jervisffb.engine.actions.DieResult
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.commands.AddPrayersToNuffle
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.SetContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.checkType
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.reports.ReportDiceRoll
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.tables.PrayerToNuffle

/**
 * Roll on the Prayers to Nuffle table as many times as defined in [PrayersToNuffleRollContext].
 * If a result is already active, it will continue re-rolling until it succeeds.
 * See page 39 in the rulebook.
 */
object PrayersToNuffleRoll : Procedure() {
    override val initialNode: Node = RollDie
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command = RemoveContext<PrayersToNuffleRollContext>()
    override fun isValid(state: Game, rules: Rules) = state.assertContext<PrayersToNuffleRollContext>()

    object RollDie : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<PrayersToNuffleRollContext>().team

        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(RollDice(rules.prayersToNuffleTable.die))
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkType<DieResult>(action) { dieRoll ->
                val context = state.getContext<PrayersToNuffleRollContext>()
                val result: PrayerToNuffle = rules.prayersToNuffleTable.roll(dieRoll)

                // Multiple instances of the same prayer are not allowed on the same team.
                // Neither as an inducement nor as a kick-off table result
                if (context.team.activePrayersToNuffle.contains(result)) {
                    compositeCommandOf(
                        ReportDiceRoll(DiceRollType.PRAYERS_TO_NUFFLE, dieRoll),
                        GotoNode(RollDie)
                    )
                } else {
                    compositeCommandOf(
                        SetContext(context.copy(
                            rollsRemaining = context.rollsRemaining - 1,
                            result = result,
                            resultApplied = false
                        )),
                        ReportDiceRoll(DiceRollType.PRAYERS_TO_NUFFLE, dieRoll),
                        GotoNode(ApplyTableResult),
                    )
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
