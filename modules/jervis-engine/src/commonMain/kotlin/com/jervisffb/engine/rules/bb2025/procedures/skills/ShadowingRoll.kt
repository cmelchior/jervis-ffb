package com.jervisffb.engine.rules.bb2025.procedures.skills

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
import com.jervisffb.engine.commands.SetOldContext
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.castDiceRoll
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ShadowingRollContext
import com.jervisffb.engine.model.context.UseRerollContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.reports.ReportDiceRoll
import com.jervisffb.engine.reports.ReportRerollUsed
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.D6DieRoll
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.calculateAvailableRerollsFor
import com.jervisffb.engine.utils.sum

/**
 * Procedure for handling a Shadowing roll.
 *
 * The result is stored in [ShadowingRollContext]] and it is up to the caller of the procedure to
 * choose the appropriate action depending on the outcome.
 */
object ShadowingRoll : Procedure() {
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<ShadowingRollContext>()
    }
    override val initialNode: Node = RollDie
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object RollDie : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<ShadowingRollContext>().player.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(RollDice(Dice.D6))
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return castDiceRoll<D6Result>(action) { d6 ->
                val context = state.getContext<ShadowingRollContext>()
                val updatedContext = context.copy(
                    roll = D6DieRoll.create(state, d6),
                    isSuccess = isShadowingSuccessful(d6, context.modifiers),
                )
                return compositeCommandOf(
                    UpdateContext(updatedContext),
                    ReportDiceRoll(DiceRollType.SHADOWING, d6),
                    GotoNode(ChooseReRollSource),
                )
            }
        }
    }

    // We expect all team reroll types, but not Sure Hands
    object ChooseReRollSource : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<ShadowingRollContext>().player.team

        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<ShadowingRollContext>()
            val pickupPlayer = context.player
            val availableRerolls = calculateAvailableRerollsFor(
                rules,
                pickupPlayer,
                DiceRollType.SHADOWING,
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
                    val rerollContext = UseRerollContext(DiceRollType.SHADOWING, action.getRerollSource(state))
                    compositeCommandOf(
                        SetOldContext(Game::rerollContext, rerollContext),
                        ReportRerollUsed(action.getRerollSource(state)),
                        GotoNode(UseRerollSource),
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object UseRerollSource : ParentNode() {
        override fun getChildProcedure(
            state: Game,
            rules: Rules,
        ): Procedure = state.rerollContext!!.source.rerollProcedure

        override fun onExitNode(
            state: Game,
            rules: Rules,
        ): Command {
            return if (state.rerollContext!!.rerollAllowed) {
                GotoNode(ReRollDie)
            } else {
                ExitProcedure()
            }
        }
    }

    object ReRollDie : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<ShadowingRollContext>().player.team

        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> = listOf(RollDice(Dice.D6))

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return castDiceRoll<D6Result>(action) { d6 ->
                val context = state.getContext<ShadowingRollContext>()
                val updatedContext = context.copy(
                    roll = context.roll!!.copyReroll(
                        rerollSource = state.rerollContext!!.source,
                        rerolledResult = d6,
                    ),
                    isSuccess = isShadowingSuccessful(d6, context.modifiers)
                )
                compositeCommandOf(
                    UpdateContext(updatedContext),
                    ReportDiceRoll(DiceRollType.SHADOWING, d6),
                    ExitProcedure(),
                )
            }
        }
    }

    private fun isShadowingSuccessful(roll: D6Result, modifiers: List<DiceModifier>): Boolean {
        val target = 4
        return when (roll.value) {
            1 -> false
            in 2..5 -> (target <= roll.value + modifiers.sum())
            6 -> true
            else -> error("Invalid value: ${roll.value}")
        }
    }
}
