package com.jervisffb.engine.model.inducements.specialplays

import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.inducements.MiscellaneousMayhem
import com.jervisffb.engine.model.inducements.Timing
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.inducements.ActivateInducementContext
import com.jervisffb.engine.rules.bb2020.procedures.inducements.dirtytricks.SpotTheSneakProcedure
import com.jervisffb.engine.rules.bb2020.skills.Duration

// Miscellaneous Mayhem: Assassination Attempt - See Special Plays Card Pack
class AssassinationAttempt: MiscellaneousMayhem() {
    override val name: String = "Assassination Attempt"
    override val duration: Duration = Duration.IMMEDIATE
    override val triggers: List<Timing> = listOf(Timing.END_OF_OPPONENT_TURN)
    override val procedure: Procedure = SpotTheSneakProcedure

    override fun isApplicable(state: Game, rules: Rules): Boolean {
        // This card is only available if a player on the opponents team was stalling
        // during the turn.
        val context = state.getContext<ActivateInducementContext>()
        return context.team.otherTeam().count { it.isStalling } > 0
    }
}
