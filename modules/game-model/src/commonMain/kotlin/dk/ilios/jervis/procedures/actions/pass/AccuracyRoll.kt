package dk.ilios.jervis.procedures.actions.pass

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.Continue
import dk.ilios.jervis.actions.ContinueWhenReady
import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.actions.Dice
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.NoRerollSelected
import dk.ilios.jervis.actions.RerollOptionSelected
import dk.ilios.jervis.actions.RollDice
import dk.ilios.jervis.actions.SelectNoReroll
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.commands.SetOldContext
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.commands.fsm.GotoNode
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.model.context.UseRerollContext
import dk.ilios.jervis.model.context.assertContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.model.modifiers.AccuracyModifier
import dk.ilios.jervis.model.modifiers.DiceModifier
import dk.ilios.jervis.procedures.D6DieRoll
import dk.ilios.jervis.reports.ReportDiceRoll
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.skills.DiceRollType
import dk.ilios.jervis.rules.tables.Range
import dk.ilios.jervis.rules.tables.Weather
import dk.ilios.jervis.utils.INVALID_ACTION
import dk.ilios.jervis.utils.INVALID_GAME_STATE
import dk.ilios.jervis.utils.calculateAvailableRerollsFor

/**
 * Implement the Accuracy Roll as described on page 49 in the rulebook.
 *
 * The result is stored in [Game.passContext] and it is up
 * to the caller to determine what to do with the result.
 */
object AccuracyRoll: Procedure() {
    override val initialNode: Node = RollDice
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<PassContext>()

    object RollDice : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<PassContext>().thrower.team
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> = listOf(RollDice(Dice.D6))
        override fun applyAction(
            action: GameAction,
            state: Game,
            rules: Rules,
        ): Command {
            return checkType<D6Result>(action) { d6 ->
                val updatedContext = updatePassContext(state, rules, d6, false)
                return compositeCommandOf(
                    ReportDiceRoll(DiceRollType.ACCURACY, d6),
                    SetContext(updatedContext),
                    GotoNode(ChooseReRollSource),
                )
            }
        }
    }

    // Team Reroll, Pro, Catch (only if failed), other skills
    object ChooseReRollSource : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules) = state.getContext<PassContext>().thrower.team
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            val context = state.getContext<PassContext>()
            val availableRerolls = calculateAvailableRerollsFor(
                rules,
                context.thrower,
                DiceRollType.ACCURACY,
                context.passingRoll!!,
                null
            )
            return if (availableRerolls.isEmpty()) {
                listOf(ContinueWhenReady)
            } else {
                listOf(SelectNoReroll(null)) + availableRerolls
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                Continue -> ExitProcedure()
                is NoRerollSelected -> ExitProcedure()
                is RerollOptionSelected -> {
                    val rerollContext = UseRerollContext(DiceRollType.ACCURACY, action.getRerollSource(state))
                    compositeCommandOf(
                        SetOldContext(Game::rerollContext, rerollContext),
                        GotoNode(UseRerollSource),
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object UseRerollSource : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            return state.rerollContext!!.source.rerollProcedure
        }
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.rerollContext!!
            return if (context.rerollAllowed) {
                GotoNode(ReRollDie)
            } else {
                ExitProcedure()
            }
        }
    }

    object ReRollDie : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules) = state.getContext<PassContext>().thrower.team
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> = listOf(RollDice(Dice.D6))
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkDiceRoll<D6Result>(action) { d6 ->
                val updatedContext = updatePassContext(state, rules, d6, true)
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.ACCURACY, d6),
                    SetContext(updatedContext),
                    ExitProcedure(),
                )
            }
        }
    }

    private fun updatePassContext(state: Game, rules: Rules, d6: D6Result, reroll: Boolean): PassContext {
        val context = state.getContext<PassContext>()
        val modifiers = mutableListOf<DiceModifier>()

        // Range modifier
        when (context.range) {
            Range.QUICK_PASS -> null
            Range.SHORT_PASS -> AccuracyModifier.SHORT_PASS
            Range.LONG_PASS -> AccuracyModifier.LONG_PASS
            Range.LONG_BOMB -> AccuracyModifier.LONG_BOMB
            else -> INVALID_GAME_STATE("Unsupported range: ${context.range}")
        }?.let { modifiers.add(it) }

        // Marked modifiers for square where ball is landing
        rules.addMarkedModifiers(
            state,
            context.thrower.team,
            state.field[state.currentBall().location],
            modifiers,
            AccuracyModifier.MARKED
        )

        // Weather
        if (state.weather == Weather.VERY_SUNNY) {
            modifiers.add(AccuracyModifier.VERY_SUNNY)
        }

        // Are there other accuracy modifiers? (Like disturbing presence)
        // TODO

        // Calculate result
        val passingStat = context.thrower.passing ?: Int.MAX_VALUE
        val modifierTotal = modifiers.sumOf { it.modifier }
        val result = when {
            context.thrower.passing == null -> PassingType.FUMBLED
            d6.value == 6 -> PassingType.ACCURATE
            d6.value == 1 -> PassingType.FUMBLED
            // Designers commentary: Rolling 1 after modifiers with PA 1+ is an accurate pass.
            // Designers commentary: Rolling 1 or less after modifiers is Wildly Inaccurate, not
            // just a result of 1.
            d6.value + modifierTotal <= 1 && passingStat != 1 -> PassingType.WILDLY_INACCURATE
            d6.value + modifierTotal >= passingStat -> PassingType.ACCURATE
            d6.value + modifierTotal < passingStat -> PassingType.INACCURATE
            else -> INVALID_GAME_STATE("Unsupported result: ${d6.value}, target: $passingStat, modifierTotal: $modifierTotal")
        }

        return if (reroll) {
            context.copy(
                passingRoll = context.passingRoll!!.copy(
                    rerollSource = state.rerollContext!!.source,
                    rerolledResult = d6
                ),
                passingModifiers = modifiers,
                passingResult = result
            )
        } else {
            context.copy(
                passingRoll = D6DieRoll(d6),
                passingModifiers = modifiers,
                passingResult = result
            )
        }
    }
}
