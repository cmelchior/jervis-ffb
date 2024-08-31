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
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.utils.assert

/**
 * Implement the lasting injury roll as described on page 61 in the rulebook.
 *
 * The result is stored in [Game.injuryRollResultContext] and it is up
 * to the caller to determine what to do with the result.
 */
object LastingInjuryRoll: Procedure() {
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
            return checkDiceRoll<D6Result>(action) { d6 ->
                val context = state.riskingInjuryRollsContext!!

                val result = rules.lastingInjuryTable.roll(d6)
                val updatedContext = context.copy(
                    lastingInjuryRoll = d6,
                    lastingInjuryResult = result,
                    lastingInjuryModifiers = emptyList(),
                )

                compositeCommandOf(
                    SetOldContext(Game::riskingInjuryRollsContext, updatedContext),
                    ExitProcedure()
                )
            }
        }
    }
}
