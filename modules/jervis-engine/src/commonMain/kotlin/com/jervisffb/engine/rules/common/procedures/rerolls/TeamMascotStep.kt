package com.jervisffb.engine.rules.common.procedures.rerolls

import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.RerollOptionSelected
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
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.UseRerollContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.reports.ReportRerollUsed
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.D6DieRoll
import com.jervisffb.engine.rules.common.rerolls.DiceRerollOption
import com.jervisffb.engine.rules.common.rerolls.TeamMascotReroll
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.calculateAvailableRerollsForTeam

data class MascotContext(
    val team: Team,
    val reroll: TeamMascotReroll,
    val roll: D6DieRoll? = null,
    // Whether the mascot roll was successful,
    val isSuccessful: Boolean = false,
    // If Mascot failed, this tracks any alternative reroll selected
    val alternativeRerollSelected: DiceRerollOption? = null,
    // Track whether Mascot or another re-roll was use successfully, allowing
    // the caller procedure to continue with their reroll.
    val isRerollAllowed: Boolean = false,
): ProcedureContext

/**
 * Procedure responsible for handling checking if a Team Mascot works, and
 * if not, possibly replace it with another reroll.
 */
object TeamMascotStep: Procedure() {
    override val initialNode: Node = RollDie
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<MascotContext>()

    object RollDie: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val rerollContext = state.getRerollContext()
            val rollContext = MascotRollContext(rerollContext.team)
            return AddContext(rollContext)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = TeamMascotRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val mascotContext = state.getContext<MascotContext>()
            val rollContext = state.getContext<MascotRollContext>()
            return compositeCommandOf(
                RemoveContext(rollContext),
                UpdateContext(mascotContext.copy(
                    roll = rollContext.roll,
                    isSuccessful = rollContext.isSuccess,
                    isRerollAllowed = rollContext.isSuccess,
                )),
                when (rollContext.isSuccess) {
                    true -> ExitProcedure()
                    false -> GotoNode(ChooseAnotherReroll)
                }
            )
        }
    }

    object ChooseAnotherReroll: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<MascotContext>().team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<MascotContext>()
            // Find all available rerolls
            val availableTeamRerolls = calculateAvailableRerollsForTeam(
                team = context.team,
                type = DiceRollType.TEAM_MASCOT,
                roll = listOf(context.roll!!),
                firstRollWasSuccess = false
            )
            return when (availableTeamRerolls.isEmpty()) {
                true -> listOf(ContinueWhenReady)
                false -> listOf(
                    SelectNoReroll(rollSuccessful = false),
                    SelectRerollOption(availableTeamRerolls)
                )
            }
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                Continue -> ExitProcedure() // No rerolls are available, keep original Mascot roll
                NoRerollSelected -> ExitProcedure() // Coach decides to keep original Mascot roll
                is RerollOptionSelected -> { // Another reroll was used
                    val context = state.getContext<MascotContext>()
                    compositeCommandOf(
                        UpdateContext(context.copy(
                            alternativeRerollSelected = action.option
                        )),
                        GotoNode(UseAlternativeReroll)
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object UseAlternativeReroll: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command? {
            val context = state.getContext<MascotContext>()
            val rerollContext = UseRerollContext(
                type = DiceRollType.TEAM_MASCOT,
                team = context.team,
                source = context.alternativeRerollSelected?.getRerollSource(state)
            )
            return compositeCommandOf(
                ReportRerollUsed(rerollContext.source!!),
                AddContext(rerollContext)
            )
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            val context = state.getContext<MascotContext>()
            val reroll = context.alternativeRerollSelected!!.getRerollSource(state)
            return reroll.rerollProcedure
        }
        override fun onExitNode(state: Game, rules: Rules): Command {
            val rerollContext = state.getRerollContext()
            val context = state.getContext<MascotContext>()
            return compositeCommandOf(
                RemoveContext(rerollContext),
                UpdateContext(context.copy(
                    isRerollAllowed = rerollContext.rerollAllowed
                )),
                ExitProcedure()
            )
        }
    }
}
