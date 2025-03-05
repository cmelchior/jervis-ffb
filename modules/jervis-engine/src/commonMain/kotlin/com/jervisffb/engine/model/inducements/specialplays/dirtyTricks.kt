package com.jervisffb.engine.model.inducements.specialplays

import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.inducements.DirtyTrick
import com.jervisffb.engine.model.inducements.Timing
import com.jervisffb.engine.rules.bb2020.procedures.inducements.dirtytricks.SpotTheSneakProcedure
import com.jervisffb.engine.rules.bb2020.skills.Duration

// Dirty Trick: Spot the Sneak - See Special Plays Card Pack
class SpotTheSneak: DirtyTrick() {
    override val name: String = "Spot the Sneak"
    override val duration: Duration = Duration.END_OF_DRIVE
    override val triggers: List<Timing> = listOf(Timing.START_OF_TURN)
    override val procedure: Procedure = SpotTheSneakProcedure
}

