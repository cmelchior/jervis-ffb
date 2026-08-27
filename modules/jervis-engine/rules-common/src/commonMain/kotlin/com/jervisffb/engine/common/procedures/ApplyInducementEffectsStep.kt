package com.jervisffb.engine.common.procedures

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.InducementEffectSelected
import com.jervisffb.engine.actions.SelectInducementEffect
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.common.context.ApplyInducementEffectsContext
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.inducements.InducementEffect
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * Responsible for selecting and applying inducement effects at a given timing.
 *
 * Developer's Commentary:
 * The order between Away and Home teams is not defined in the rulebook, so for
 * now we use the somewhat arbitrary order of first away team, then home team.
 */
object ApplyInducementEffectsStep: Procedure() {
    override val initialNode: Node = SelectAwayTeamInducement
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<ApplyInducementEffectsContext>()

    object DecideOnStartingTeam: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<ApplyInducementEffectsContext>()
            return when (context.team?.isHomeTeam() == true) {
                true -> GotoNode(SelectHomeTeamInducement)
                false -> GotoNode(SelectAwayTeamInducement)
            }
        }
    }

    object SelectAwayTeamInducement: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.awayTeam
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val availableInducements = findAvailableInducements(state.awayTeam)
            return when (availableInducements.isNotEmpty()) {
                true -> listOf(SelectInducementEffect(availableInducements), CancelWhenReady)
                false -> listOf(ContinueWhenReady)
            }
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                Continue,
                Cancel -> GotoNode(SelectHomeTeamInducement)
                is InducementEffectSelected -> {
                    val context = state.getContext<ApplyInducementEffectsContext>()
                    val inducement = action.getEffect(state.awayTeam)
                    compositeCommandOf(
                        UpdateContext(context.copy(
                            selectedTeam = state.awayTeam,
                            selectedInducement = inducement
                        )),
                        // If the `team` is set, we know that we already processed it, so can just exit here.
                        when (context.team == null) {
                            true -> GotoNode(ApplyAwayTeamInducement)
                            false -> ExitProcedure()
                        }
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object ApplyAwayTeamInducement: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            val context = state.getContext<ApplyInducementEffectsContext>()
            return context.selectedInducement?.procedure ?: INVALID_GAME_STATE("Missing procedure: $context")
        }
        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(SelectAwayTeamInducement)
        }
    }

    object SelectHomeTeamInducement: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.homeTeam
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val availableInducements = findAvailableInducements(state.homeTeam)
            return when (availableInducements.isNotEmpty()) {
                true -> listOf(SelectInducementEffect(availableInducements), CancelWhenReady)
                false -> listOf(ContinueWhenReady)
            }
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                Continue,
                Cancel -> ExitProcedure()
                is InducementEffectSelected -> {
                    val context = state.getContext<ApplyInducementEffectsContext>()
                    val inducement = action.getEffect(state.homeTeam)
                    compositeCommandOf(
                        UpdateContext(context.copy(
                            selectedTeam = state.homeTeam,
                            selectedInducement = inducement
                        )),
                        ExitProcedure()
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object ApplyHomeTeamInducement: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            val context = state.getContext<ApplyInducementEffectsContext>()
            return context.selectedInducement?.procedure ?: INVALID_GAME_STATE("Missing procedure: $context")
        }
        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(SelectHomeTeamInducement)
        }

    }

    //
    // HELPER FUNCTIONS
    //

    private fun findAvailableInducements(team: Team): List<InducementEffect> {
        val inducements = mutableListOf<InducementEffect>()
        team.wizards.forEach { wizard ->
            wizard.spells.forEach { spell ->
                if (!spell.used) {
                    inducements.add(spell)
                }
            }
        }
        team.specialPlayCards.forEach { card ->
            if (!card.used) {
                inducements.add(card)
            }
        }

        return inducements
    }
}
