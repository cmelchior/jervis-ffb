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
import com.jervisffb.engine.commands.context.AddContext
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.common.context.ResolveInducementEffectsContext
import com.jervisffb.engine.common.context.SelectInducementEffectsContext
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
import com.jervisffb.engine.model.inducements.Timing
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.utils.INVALID_ACTION

/**
 * Responsible for selecting and applying inducement effects at a given timing.
 *
 * Developer's Commentary:
 * The order between Away and Home teams is not defined in the rulebook, so for
 * now we use the somewhat arbitrary order of first away team, then home team.
 */
object ActivateInducementEffectsStep: Procedure() {
    override val initialNode: Node = DecideOnStartingTeam
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<SelectInducementEffectsContext>()

    object DecideOnStartingTeam: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<SelectInducementEffectsContext>()
            return when (context.team?.isHomeTeam() == true) {
                true -> GotoNode(SelectHomeTeamInducement)
                false -> GotoNode(SelectAwayTeamInducement)
            }
        }
    }

    object SelectAwayTeamInducement: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.awayTeam
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<SelectInducementEffectsContext>()
            val availableInducements = findAvailableInducements(state.awayTeam, context.phase)
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
                    val context = state.getContext<SelectInducementEffectsContext>()
                    val inducement = action.getEffect(state.awayTeam)
                    compositeCommandOf(
                        AddContext(ResolveInducementEffectsContext(
                            team = state.awayTeam,
                            inducement = inducement
                        )),
                        GotoNode(ApplyAwayTeamInducement)
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object ApplyAwayTeamInducement: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            val context = state.getContext<ResolveInducementEffectsContext>()
            return context.inducement.procedure
        }
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                RemoveContext<ResolveInducementEffectsContext>(),
                GotoNode(SelectAwayTeamInducement)
            )
        }
    }

    object SelectHomeTeamInducement: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.homeTeam
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<SelectInducementEffectsContext>()
            val availableInducements = findAvailableInducements(state.homeTeam, context.phase)
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
                    val inducement = action.getEffect(state.homeTeam)
                    compositeCommandOf(
                        AddContext(ResolveInducementEffectsContext(
                            team = state.homeTeam,
                            inducement = inducement
                        )),
                        GotoNode(ApplyHomeTeamInducement)
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object ApplyHomeTeamInducement: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            val context = state.getContext<ResolveInducementEffectsContext>()
            return context.inducement.procedure
        }
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                RemoveContext<ResolveInducementEffectsContext>(),
                GotoNode(SelectHomeTeamInducement)
            )
        }
    }

    //
    // HELPER FUNCTIONS
    //

    private fun findAvailableInducements(team: Team, timing: Timing): List<InducementEffect> {
        val inducements = mutableListOf<InducementEffect>()
        team.wizards.forEach { wizard ->
            inducements.addAll(wizard.getAvailableSpells(timing))
        }
        team.specialPlayCards.forEach { card ->
            if (card.triggers.contains(timing) && !card.used && card.isApplicable(team.game, team.game.rules)) {
                inducements.add(card)
            }
        }

        team.infamousCoachingStaff.forEach { coachingStaff ->
            coachingStaff.specialAbilities.forEach { ability ->
                if (ability.triggers.contains(timing) && !ability.used && ability.isApplicable(team.game, team.game.rules)) {
                    inducements.add(ability)
                }
            }
        }

        return inducements
    }
}
