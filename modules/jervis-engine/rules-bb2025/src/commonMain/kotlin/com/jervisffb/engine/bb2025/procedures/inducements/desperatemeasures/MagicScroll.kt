package com.jervisffb.engine.bb2025.procedures.inducements.desperatemeasures

import com.jervisffb.engine.bb2025.inducements.effects.MagicScrollCard
import com.jervisffb.engine.bb2025.inducements.wizards.SportsWizard
import com.jervisffb.engine.bb2025.reports.ReportUsingMagicScroll
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.common.commands.AddTeamWizard
import com.jervisffb.engine.common.commands.RemoveSpecialPlayCard
import com.jervisffb.engine.common.context.ResolveInducementEffectsContext
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import com.jervisffb.engine.utils.requireGameState

/**
 * Responsible for applying the "Magic Scroll" Desperate Measure.
 *
 * See page 15 in Spike 22.
 */
object MagicScroll: Procedure() {
    override val initialNode: Node = UseScroll
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        val context = state.getContext<ResolveInducementEffectsContext>()
        val team = context.team
        val card = team.specialPlayCards.find { it is MagicScrollCard } ?: INVALID_GAME_STATE("Missing hangover inducement")
        return RemoveSpecialPlayCard(team, card)
    }
    override fun isValid(state: Game, rules: Rules) {
        val context = state.getContext<ResolveInducementEffectsContext>()
        requireGameState(context.inducement is MagicScrollCard) { "Wrong inducement: $context" }
    }

    object UseScroll: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<ResolveInducementEffectsContext>()
            val team = context.team
            return compositeCommandOf(
                AddTeamWizard(team, SportsWizard(team)),
                ReportUsingMagicScroll(team),
                ExitProcedure()
            )
        }
    }
}
