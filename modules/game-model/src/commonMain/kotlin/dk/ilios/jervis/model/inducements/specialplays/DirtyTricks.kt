package dk.ilios.jervis.model.inducements.specialplays

import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.inducements.DirtyTrick
import dk.ilios.jervis.model.inducements.Timing
import dk.ilios.jervis.procedures.inducements.dirtytricks.SpotTheSneakProcedure
import dk.ilios.jervis.rules.skills.Duration

// Dirty Trick: Spot the Sneak - See Special Plays Card Pack
class SpotTheSneak: DirtyTrick() {
    override val name: String = "Spot the Sneak"
    override val duration: Duration = Duration.END_OF_DRIVE
    override val triggers: List<Timing> = listOf(Timing.START_OF_TURN)
    override val procedure: Procedure = SpotTheSneakProcedure
}

