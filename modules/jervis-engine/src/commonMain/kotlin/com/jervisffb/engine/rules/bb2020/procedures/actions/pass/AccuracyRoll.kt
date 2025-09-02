package com.jervisffb.engine.rules.bb2020.procedures.actions.pass

import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.RerollOptionSelected
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.actions.SelectNoReroll
import com.jervisffb.engine.actions.SelectRerollOption
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetOldContext
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.SetContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.checkDiceRoll
import com.jervisffb.engine.fsm.checkType
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.UseRerollContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.modifiers.AccuracyModifier
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.reports.ReportDiceRoll
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.D6DieRoll
import com.jervisffb.engine.rules.common.tables.Range
import com.jervisffb.engine.rules.common.tables.Weather
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import com.jervisffb.engine.utils.calculateAvailableRerollsFor
import com.jervisffb.engine.utils.sum

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
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> = listOf(RollDice(Dice.D6))
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
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
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<PassContext>()
            val availableRerolls: SelectRerollOption? = calculateAvailableRerollsFor(
                rules,
                context.thrower,
                DiceRollType.ACCURACY,
                context.passingRoll!!,
                null
            )
            return if (availableRerolls == null) {
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
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> = listOf(RollDice(Dice.D6))
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

    // HELPER METHODS

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

        // Marked modifiers for thrower
        rules.addMarkedModifiers(
            state,
            context.thrower.team,
            context.thrower.coordinates,
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
        val modifierTotal = modifiers.sum()
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
                passingRoll = context.passingRoll!!.copyReroll(
                    rerollSource = state.rerollContext!!.source,
                    rerolledResult = d6
                ),
                passingModifiers = modifiers,
                passingResult = result
            )
        } else {
            context.copy(
                passingRoll = D6DieRoll.create(state, d6),
                passingModifiers = modifiers,
                passingResult = result
            )
        }
    }
}
