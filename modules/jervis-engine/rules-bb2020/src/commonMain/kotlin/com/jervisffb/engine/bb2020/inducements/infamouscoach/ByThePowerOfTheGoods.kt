package com.jervisffb.engine.bb2020.inducements.infamouscoach

import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.inducements.Timing
import com.jervisffb.engine.model.inducements.infamouscoach.InfamousCoachAbility
import com.jervisffb.engine.rules.common.procedures.DummyProcedure
import kotlinx.serialization.Serializable

// See page 16 in DeathZone
@Serializable
class ByThePowerOfTheGoods: InfamousCoachAbility {
    override val name: String = "By The Power Of The Gods!"
    override var used: Boolean = false
    override val triggers: List<Timing> = listOf(Timing.START_OF_DRIVE)
    override val procedure: Procedure = DummyProcedure
}
