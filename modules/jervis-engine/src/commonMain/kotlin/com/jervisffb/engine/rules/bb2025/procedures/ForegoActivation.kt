package com.jervisffb.engine.rules.bb2025.procedures

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetPlayerAvailability
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Availability
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.ForegoActivationContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.reports.ReportForegoActivation
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * Procedure controlling a player choosing Forego Activation.
 * It is up to callers of this procedure to remove the [ForegoActivationContext]
 * after use.
 *
 * See page 52 in the BB2025 Rulebook.
 *
 * TODO Check for stalling.
 */
object ForegoActivation: Procedure() {
    override val initialNode: Node = ResolveEndOfActivation
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        val context = state.getContext<ForegoActivationContext>()
        if (context.player.available != Availability.AVAILABLE) {
            INVALID_GAME_STATE("Player ${context.player.name} must be available: ${context.player.state}")
        }
    }
    object ResolveEndOfActivation: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<ForegoActivationContext>()
            return compositeCommandOf(
                // Only report foregoing an activation if it was chosen manually, otherwise we
                // the logs get flooded with messages that doesn't really provide any value.
                if (!context.isEndingTurn) ReportForegoActivation(context.player) else null,
                SetPlayerAvailability(context.player, Availability.HAS_ACTIVATED),
                ExitProcedure()
            )
        }
    }
}
