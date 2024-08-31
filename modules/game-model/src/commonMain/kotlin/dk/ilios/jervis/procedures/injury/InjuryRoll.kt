package dk.ilios.jervis.procedures.injury

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.actions.Dice
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.commands.SetOldContext
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.modifiers.DiceModifier
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.utils.assert

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

    override fun onEnterProcedure(state: Game, rules: Rules): Command? {
        assert(state.riskingInjuryRollsContext != null)
        return null
    }

    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object RollDice : ActionNode() {
        override fun getAvailableActions(
            state: Game,
            rules: Rules,
        ): List<ActionDescriptor> = listOf(dk.ilios.jervis.actions.RollDice(Dice.D6, Dice.D6))

        override fun applyAction(
            action: GameAction,
            state: Game,
            rules: Rules,
        ): Command {
            return checkDiceRoll<D6Result, D6Result>(action) { die1, die2 ->
                val context = state.riskingInjuryRollsContext!!

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
                    SetOldContext(Game::riskingInjuryRollsContext, updatedContext),
                    ExitProcedure()
                )
            }
        }
    }
}
