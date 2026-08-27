package com.jervisffb.engine.bb2020.inducements.infamouscoach

import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.InducementEffectId
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.TeamId
import com.jervisffb.engine.model.inducements.Timing
import com.jervisffb.engine.model.inducements.infamouscoach.InfamousCoachAbility
import com.jervisffb.engine.rules.common.procedures.DummyProcedure
import kotlinx.serialization.Serializable

// See page 16 in DeathZone
@Serializable
class ByThePowerOfTheGoods(private val team: TeamId): InfamousCoachAbility {
    constructor(team: Team): this(team.id)
    override val id: InducementEffectId = InducementEffectId("${team.value}-by-the-power-of-the-gods")
    override val name: String = "By The Power Of The Gods!"
    override var used: Boolean = false
    override val triggers: List<Timing> = listOf(Timing.START_OF_DRIVE)
    override val procedure: Procedure = DummyProcedure
}
