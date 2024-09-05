package dk.ilios.jervis.model.inducements.specialplays

import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.model.inducements.MiscellaneousMayhem
import dk.ilios.jervis.model.inducements.Timing
import dk.ilios.jervis.procedures.inducements.ActivateInducementContext
import dk.ilios.jervis.procedures.inducements.dirtytricks.SpotTheSneakProcedure
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.skills.Duration

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
