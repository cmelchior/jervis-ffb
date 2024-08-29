package dk.ilios.jervis.procedures.actions.foul

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.actions.Dice
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.RollDice
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.utils.assert

/**
 * Implement the Argue The Call roll as described on page 63 in the rulebook.
 *
 * The result is stored in [Game.foulContext] and it is up
 * to the caller to determine what to do with the result.
 */
object ArgueTheCallRoll: Procedure() {
    override val initialNode: Node = RollDice

    override fun onEnterProcedure(state: Game, rules: Rules): Command? {
        assert(state.foulContext != null)
        return null
    }

    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object RollDice : ActionNode() {
        override fun getAvailableActions(
            state: Game,
            rules: Rules,
        ): List<ActionDescriptor> = listOf(RollDice(Dice.D6))

        override fun applyAction(
            action: GameAction,
            state: Game,
            rules: Rules,
        ): Command {
            return checkType<D6Result>(action) { d6 ->
                val context = state.foulContext!!
                val result = rules.argueTheCallTable.roll(d6)
                val updatedContext = context.copy(
                    argueTheCallRoll = d6,
                    argueTheCallResult = result
                )
                return compositeCommandOf(
                    SetContext(Game::foulContext, updatedContext),
                    ExitProcedure()
                )
            }
        }
    }
}
