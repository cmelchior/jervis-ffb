package com.jervisffb.engine.rules.common.procedures.rerolls

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
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.AddContext
import com.jervisffb.engine.commands.context.RemoveContext
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
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.UseRerollContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.reports.ReportDiceRoll
import com.jervisffb.engine.reports.ReportRerollUsed
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.D6DieRoll
import com.jervisffb.engine.rules.common.procedures.actions.dicerolls.D6WithRerollProcedure
import com.jervisffb.engine.rules.common.rerolls.DiceRerollOption
import com.jervisffb.engine.rules.common.rerolls.TeamMascotReroll
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.INVALID_GAME_STATE

data class MascotRollContext(
    val team: Team,
    val roll: D6DieRoll? = null,
    val isSuccess: Boolean = false,
): ProcedureContext

/**
 * Procedure controlling the Mascot roll, i.e., when a Team checks to see if
 * Team Mascot reroll works.
 *
 * While this class is conceptually similar to [D6WithRerollProcedure] it
 * is slightly different in the sense that the Mascot works at a team, not
 * player level.
 *
 * For this reason, this procedure is self-contained, but follows the same
 * "shape" as [D6WithRerollProcedure]. This means that any change to this class
 * should most likely be mirrored there (and vice versa).
 */
object TeamMascotRoll: Procedure() {
    override val initialNode: Node = RollDie
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        val context = state.getContext<MascotRollContext>()
        val rerollContext = UseRerollContext(
            type = DiceRollType.TEAM_MASCOT,
            team = context.team,
        )
        return AddContext(rerollContext)
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        val context = state.getRerollContext()
        return RemoveContext(context)
    }
    override fun isValid(state: Game, rules: Rules) = state.assertContext<MascotRollContext>()

    object RollDie: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<MascotRollContext>().team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(RollDice(Dice.D6))
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return castDiceRoll<D6Result>(action) { d6 ->
                val context = state.getContext<MascotRollContext>()
                val success = isSuccess(d6)
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.TEAM_MASCOT, d6),
                    UpdateContext(context.copy(
                        roll = D6DieRoll.create(state, d6),
                        isSuccess = success,
                    )),
                    GotoNode(ChooseReRollSource)
                )
            }
        }
    }

    object ChooseReRollSource : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<MascotRollContext>().team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<MascotRollContext>()
            // Team re-rolls are not supported on Mascot rolls, and it is uncler how
            // player skills could affect it, so for now, no rerolls are allowed.
            val availableRerolls = emptyList<DiceRerollOption>()
            return when (availableRerolls.isEmpty()) {
                true -> listOf(ContinueWhenReady)
                false -> listOf(SelectNoReroll(context.isSuccess), SelectRerollOption(availableRerolls))
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                Continue -> ExitProcedure()
                is NoRerollSelected -> ExitProcedure()
                is RerollOptionSelected -> {
                    val context = state.getContext<MascotRollContext>()
                    val rerollContext = state.getRerollContext()
                    compositeCommandOf(
                        UpdateContext(rerollContext.copy(source = action.getRerollSource(state))),
                        ReportRerollUsed(action.getRerollSource(state)),
                        GotoNode(UseRerollSource),
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object UseRerollSource : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            val context = state.getRerollContext()
            return context.source?.rerollProcedure ?: INVALID_GAME_STATE("Missing reroll source: $context")
        }
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getRerollContext()
            return if (context.rerollAllowed) {
                GotoNode(ReRollDie)
            } else {
                ExitProcedure()
            }
        }
    }

    object ReRollDie : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<MascotRollContext>().team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> = listOf(RollDice(Dice.D6))
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return castDiceRoll<D6Result>(action) { d6 ->
                val rollContext = state.getContext<MascotRollContext>()
                val rerollContext = state.getRerollContext()
                val rerollResult = rollContext.copy(
                    roll = rollContext.roll!!.copyReroll(
                        rerollSource = rerollContext.source,
                        rerolledResult = d6,
                    ),
                    isSuccess = isSuccess(d6),
                )
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.TEAM_MASCOT, d6),
                    UpdateContext(rerollResult),
                    ExitProcedure(),
                )
            }
        }
    }

    private fun isSuccess(roll: D6Result): Boolean {
        val target = TeamMascotReroll.TARGET
        return roll.value >= target
    }
}
