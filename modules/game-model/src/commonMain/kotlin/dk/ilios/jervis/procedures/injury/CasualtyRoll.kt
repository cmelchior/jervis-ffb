package dk.ilios.jervis.procedures.injury

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.actions.Dice
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.RollDice
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.commands.SetRollContext
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.DiceModifier
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.utils.assert
import dk.ilios.jervis.utils.sum

/**
 * Implement the Casualty Roll as described on page 61 in the rulebook.
 *
 * The result is stored in [Game.casultyRollResultContext] and it is up
 * to the caller to determine what to do with the result.
 *
 */
object CasualtyRoll: Procedure() {
    override val initialNode: Node = RollDie

    override fun onEnterProcedure(state: Game, rules: Rules): Command? {
        assert(state.riskingInjuryRollsContext != null)
        return null
    }

    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object RollDie : ActionNode() {
        override fun getAvailableActions(
            state: Game,
            rules: Rules,
        ): List<ActionDescriptor> = listOf(RollDice(Dice.D16))

        override fun applyAction(
            action: GameAction,
            state: Game,
            rules: Rules,
        ): Command {
            return checkDiceRoll<D6Result, D6Result>(action) { die1, die2 ->
                val context = state.riskingInjuryRollsContext!!

                // Determine result of casulty roll
                // TODO This logic needs to be expanded to support things like Niggling Injuries and others.
                val roll = listOf(die1, die2)
                val result = roll.sum()
                val modifiers = emptyList<DiceModifier>() // Just having skills here is not enough, we need more generic Modifier

                val updatedContext = state.riskingInjuryRollsContext!!.copy(
                    injuryRoll = roll,
                    injuryResult = result,
                    injuryModifiers = modifiers,
                )

                compositeCommandOf(
                    SetRollContext(Game::riskingInjuryRollsContext, updatedContext),
                    ExitProcedure()
                )
            }
        }
    }
}
