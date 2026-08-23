package com.jervisffb.engine.common.procedures.tables.prayers

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.common.commands.AddTeamFeature
import com.jervisffb.engine.common.context.PrayersToNuffleRollContext
import com.jervisffb.engine.common.reports.ReportGameProgress
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.modifiers.TeamFeature
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * Procedure for handling the Prayer to Nuffle "Under Scrutiny".
 *
 * See page 39 in the BB2020 rulebook.
 * See page 143 in the BB2025 rulebook.
 */
object UnderScrutiny : Procedure() {
    override val initialNode: Node = ApplyEvent
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<PrayersToNuffleRollContext>()
    }

    object ApplyEvent : ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<PrayersToNuffleRollContext>()
            val duration = context.result?.duration ?: INVALID_GAME_STATE("Missing result: $context")
            val targetTeam = context.team.otherTeam()
            return compositeCommandOf(
                UpdateContext(context.copy(resultApplied = true)),
                ReportGameProgress("${targetTeam.name} is Under Scrutiny"),
                AddTeamFeature(targetTeam, TeamFeature.underScrutiny(duration)),
                ExitProcedure(),
            )
        }
    }
}
