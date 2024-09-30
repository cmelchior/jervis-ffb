package dk.ilios.jervis.procedures

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.D16Result
import dk.ilios.jervis.actions.Dice
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.RollDice
import dk.ilios.jervis.commands.AddPrayersToNuffle
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.RemoveContext
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.commands.fsm.GotoNode
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.model.context.assertContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.reports.ReportDiceRoll
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.skills.DiceRollType
import dk.ilios.jervis.rules.tables.PrayerToNuffle

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

        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            return listOf(RollDice(Dice.D16))
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkType<D16Result>(action) { d16 ->
                val context = state.getContext<PrayersToNuffleRollContext>()
                val result: PrayerToNuffle = rules.prayersToNuffleTable.roll(d16)

                // Multiple instances of the same prayer is not allowed across both teams.
                // Neither as inducement nor as a kick-off table result
                if (
                    context.team.activePrayersToNuffle.contains(result) ||
                    context.team.otherTeam().activePrayersToNuffle.contains(result)
                ) {
                    compositeCommandOf(
                        ReportDiceRoll(DiceRollType.PRAYERS_TO_NUFFLE, d16),
                        GotoNode(RollDie)
                    )
                } else {
                    compositeCommandOf(
                        SetContext(context.copy(
                            rollsRemaining = context.rollsRemaining - 1,
                            result = result,
                            resultApplied = false
                        )),
                        ReportDiceRoll(DiceRollType.PRAYERS_TO_NUFFLE, d16),
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
            // Currently we do not grant another roll if the Prayer was not applied.
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
