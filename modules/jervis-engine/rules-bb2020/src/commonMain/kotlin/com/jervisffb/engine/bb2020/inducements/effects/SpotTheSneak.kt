package com.jervisffb.engine.bb2020.inducements.effects

import com.jervisffb.engine.bb2020.procedures.inducements.dirtytricks.SpotTheSneakProcedure
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.InducementEffectId
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.TeamId
import com.jervisffb.engine.model.inducements.Timing
import com.jervisffb.engine.rules.common.skills.Duration
import kotlinx.serialization.Serializable

// Dirty Trick: Spot the Sneak - See Special Plays Card Pack
@Serializable
class SpotTheSneak(private val team: TeamId): DirtyTrick() {
    constructor(team: Team): this(team.id)
    override val id: InducementEffectId = InducementEffectId("${team.value}-spot-the-sneak")
    override val name: String = "Spot the Sneak"
    override val duration: Duration = Duration.END_OF_DRIVE
    override val triggers: List<Timing> = listOf(Timing.START_OF_OWN_TURN)
    override val procedure: Procedure = SpotTheSneakProcedure
}
