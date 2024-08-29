package dk.ilios.jervis.procedures.actions.pass

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
import dk.ilios.jervis.model.DiceModifier
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.tables.Range
import dk.ilios.jervis.utils.INVALID_GAME_STATE
import dk.ilios.jervis.utils.assert

enum class AccuracyModifier(override val modifier: Int, override val description: String) : DiceModifier {
    MARKED(-1, "Marked"),
    SHORT_PASS(-1, "Short Pass"),
    LONG_PASS(-2, "Long Pass"),
    LONG_BOMB(-3, "Long Bomb")
}

/**
 * Implement the Accuracy Roll as described on page 49 in the rulebook.
 *
 * The result is stored in [Game.passContext] and it is up
 * to the caller to determine what to do with the result.
 */
object AccuracyRoll: Procedure() {
    override val initialNode: Node = RollDice

    override fun onEnterProcedure(state: Game, rules: Rules): Command? {
        assert(state.passContext != null)
        return null
    }

    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    // TODO Add support for rerolls
    object RollDice : ActionNode() {
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> = listOf(RollDice(Dice.D6))
        override fun applyAction(
            action: GameAction,
            state: Game,
            rules: Rules,
        ): Command {
            return checkType<D6Result>(action) { d6 ->
                val context = state.passContext!!

                val modifiers = mutableListOf<DiceModifier>()

                // Range modifier
                when (context.range) {
                    Range.QUICK_PASS -> null
                    Range.SHORT_PASS -> AccuracyModifier.SHORT_PASS
                    Range.LONG_PASS -> AccuracyModifier.LONG_PASS
                    Range.LONG_BOMB -> AccuracyModifier.LONG_BOMB
                    else -> INVALID_GAME_STATE("Unsupported range: ${context.range}")
                }?.let { modifiers.add(it) }

                // Marked modifiers
                rules.addMarkedModifiers(
                    state,
                    context.thrower.team,
                    state.ballSquare,
                    modifiers,
                    AccuracyModifier.MARKED
                )

                // Are there other accuracy modifiers? (Like disturbing presence)
                // TODO

                // Calculate result
                val passingStat = context.thrower.passing ?: Int.MAX_VALUE
                val modifierTotal = modifiers.sumOf { it.modifier }
                val result = when {
                    context.thrower.passing == null -> PassingType.FUMBLED
                    d6.result == 6 -> PassingType.ACCURATE
                    d6.result == 1 -> PassingType.FUMBLED
                    // Designers commentary: Rolling 1 after modifiers with PA 1+ is an accurate pass.
                    // Designers commentary: Rolling 1 or less after modifiers is Wildly Inaccurate, not
                    // just a result of 1.
                    d6.result + modifierTotal <= 1 && passingStat != 1 -> PassingType.WILDLY_INACCURATE
                    d6.result + modifierTotal >= passingStat -> PassingType.ACCURATE
                    d6.result + modifierTotal < passingStat -> PassingType.INACCURATE
                    else -> INVALID_GAME_STATE("Unsupported result: ${d6.result}, target: $passingStat, modifierTotal: $modifierTotal")
                }

                return compositeCommandOf(
                    SetContext(Game::passContext, context.copy(
                        passingRoll =  d6,
                        passingModifiers = modifiers,
                        passingResult = result
                    )),
                    ExitProcedure()
                )
            }
        }
    }
}
