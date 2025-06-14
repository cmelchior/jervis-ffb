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
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.context.SetContext
import com.jervisffb.engine.commands.SetOldContext
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.checkDiceRoll
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.PassingInterferenceRollContext
import com.jervisffb.engine.model.context.UseRerollContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.reports.ReportDiceRoll
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.D6DieRoll
import com.jervisffb.engine.rules.bb2020.testAgainstAgility
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.calculateAvailableRerollsFor

/**
 * Procedure for handling rolling for Passing Interference as described on page 50
 * in the rulebook.
 *
 * It is only responsible for handling the actual dice roll. The result is stored
 * in [PassingInterferenceRollContext] and it is up to the caller of the procedure
 * to choose the appropriate action depending on the outcome.
 */
object PassingInterferenceRoll : Procedure() {
    override val initialNode: Node = RollDie
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<PassingInterferenceRollContext>()

    object RollDie : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules) = state.getContext<PassingInterferenceRollContext>().player.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> = listOf(RollDice(Dice.D6))
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkDiceRoll<D6Result>(action) { d6 ->
                val rollContext = state.getContext<PassingInterferenceRollContext>()
                val resultContext = rollContext.copy(
                    roll = D6DieRoll.Companion.create(state, d6),
                    isSuccess = testAgainstAgility(rollContext.player, d6, rollContext.modifiers)
                )
                return compositeCommandOf(
                    ReportDiceRoll(DiceRollType.PASSING_INTERFERENCE, d6),
                    SetContext(resultContext),
                    GotoNode(ChooseReRollSource),
                )
            }
        }
    }

    object ChooseReRollSource : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules) = state.getContext<PassingInterferenceRollContext>().player.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<PassingInterferenceRollContext>()
            val availableRerolls = calculateAvailableRerollsFor(
                rules,
                context.player,
                DiceRollType.PASSING_INTERFERENCE,
                context.roll!!,
                context.isSuccess
            )
            return if (availableRerolls == null) {
                listOf(ContinueWhenReady)
            } else {
                listOf(SelectNoReroll(context.isSuccess)) + availableRerolls
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                Continue -> ExitProcedure()
                is NoRerollSelected -> ExitProcedure()
                is RerollOptionSelected -> {
                    val rerollContext = UseRerollContext(DiceRollType.PASSING_INTERFERENCE, action.getRerollSource(state))
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
        override fun actionOwner(state: Game, rules: Rules) = state.getContext<PassingInterferenceRollContext>().player.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> = listOf(RollDice(Dice.D6))
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkDiceRoll<D6Result>(action) { d6 ->
                val context = state.getContext<PassingInterferenceRollContext>()
                val rerollResult = context.copy(
                    roll = context.roll!!.copyReroll(
                        rerollSource = state.rerollContext!!.source,
                        rerolledResult = d6,
                    ),
                    isSuccess = testAgainstAgility(context.player, d6, context.modifiers)
                )
                compositeCommandOf(
                    SetContext(rerollResult),
                    ExitProcedure(),
                )
            }
        }
    }
}
