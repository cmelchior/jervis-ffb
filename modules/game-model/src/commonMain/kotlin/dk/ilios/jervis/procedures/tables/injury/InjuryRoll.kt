package dk.ilios.jervis.procedures.tables.injury

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.actions.Dice
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.model.context.assertContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.model.modifiers.DiceModifier
import dk.ilios.jervis.reports.ReportDiceRoll
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.skills.DiceRollType

/**
 * Implement the injury roll as described on page 60 in the rulebook.
 *
 * The result is stored in [Game.injuryRollResultContext] and it is up
 * to the caller to determine what to do with the result.
 *
 * TODO Note, Mighty Blow specifically say "When an opposition player is knocked
 *  down" (page 80) and "Pushed into the Crows" (page 58) says "A player that
 *  is pushed into the crowd is immediately removed from play". So this would
 *  mean that any effect that requires a "Knocked Down" player doesn't apply.
 */
object InjuryRoll: Procedure() {
    override val initialNode: Node = RollDice
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<RiskingInjuryContext>()

    object RollDice : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<RiskingInjuryContext>().player.team.otherTeam()
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> = listOf(dk.ilios.jervis.actions.RollDice(Dice.D6, Dice.D6))

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkDiceRoll<D6Result, D6Result>(action) { die1, die2 ->
                val context = state.getContext<RiskingInjuryContext>()

                // Determine result of injury roll
                // TODO This logic needs to be expanded to support things like Mighty Blow and others.
                val roll = listOf(die1, die2)
                val modifiers = emptyList<DiceModifier>()
                val result = rules.injuryTable.roll(die1, die2, modifiers.sumOf { it.modifier })

                val updatedContext = context.copy(
                    injuryRoll = roll,
                    injuryResult = result,
                    injuryModifiers = modifiers,
                )

                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.INJURY, roll),
                    SetContext(updatedContext),
                    ExitProcedure()
                )
            }
        }
    }
}
