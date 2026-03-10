package com.jervisffb.engine.rules.common.procedures.actions.block

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
import com.jervisffb.engine.model.context.UseRerollContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.reports.ReportDiceRoll
import com.jervisffb.engine.reports.ReportRerollUsed
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.D6DieRoll
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.calculateAvailableRerollsFor

/**
 * Implement the Projectile Vomit Roll as described on page 133 in the BB2025
 * rulebook.
 *
 * The result is stored in [ProjectileVomitContext] and it is up to the caller to
 * determine what to do with the result.
 */
object ProjectileVomitRoll: Procedure() {
    override val initialNode: Node = RollDie
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<ProjectileVomitContext>()

    object RollDie : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<ProjectileVomitContext>().attacker.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(RollDice(Dice.D6))
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return castDiceRoll<D6Result>(action) { d6 ->
                val context = state.getContext<ProjectileVomitContext>()
                val updatedContext = context.copy(
                    vomitRoll = D6DieRoll.create(state, d6),
                    isSuccess = isSuccess(d6),
                )
                return compositeCommandOf(
                    UpdateContext(updatedContext),
                    ReportDiceRoll(DiceRollType.PROJECTILE_VOMIT, d6),
                    GotoNode(ChooseReRollSource),
                )
            }
        }
    }

    object ChooseReRollSource : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<ProjectileVomitContext>().attacker.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<ProjectileVomitContext>()
            val player = context.attacker
            val availableRerolls = calculateAvailableRerollsFor(
                rules,
                player,
                DiceRollType.PROJECTILE_VOMIT,
                context.vomitRoll!!,
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
                    val rerollContext = UseRerollContext(DiceRollType.PROJECTILE_VOMIT, action.getRerollSource(state))
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
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = state.rerollContext!!.source.rerollProcedure
        override fun onExitNode(state: Game, rules: Rules): Command {
            return if (state.rerollContext!!.rerollAllowed) {
                GotoNode(ReRollDie)
            } else {
                ExitProcedure()
            }
        }
    }

    object ReRollDie : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<ProjectileVomitContext>().attacker.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> = listOf(RollDice(Dice.D6))
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return castDiceRoll<D6Result>(action) { d6 ->
                val context = state.getContext<ProjectileVomitContext>()
                val updatedContext = context.copy(
                    vomitRoll = context.vomitRoll!!.copyReroll(
                        rerollSource = state.rerollContext!!.source,
                        rerolledResult = d6,
                    ),
                    isSuccess = isSuccess(d6),
                )
                compositeCommandOf(
                    UpdateContext(updatedContext),
                    ReportDiceRoll(DiceRollType.PROJECTILE_VOMIT, d6),
                    ExitProcedure(),
                )
            }
        }
    }

    // -- HELPER METHODS --
    private fun isSuccess(d6: D6Result): Boolean {
        return d6.value >= 2
    }
}
