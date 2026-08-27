package com.jervisffb.engine.bb2020.inducements.wizards

import com.jervisffb.engine.common.procedures.inducements.spells.ZapProcedure
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.InducementEffectId
import com.jervisffb.engine.model.WizardId
import com.jervisffb.engine.model.inducements.Timing
import com.jervisffb.engine.model.inducements.wizard.Spell
import kotlinx.serialization.Serializable

// Zap! spell - See page 94 in the BB2020 rulebook
@Serializable
class Zap(val wizard: WizardId) : Spell {
    override val id: InducementEffectId = InducementEffectId("${wizard.value}-zap")
    override val name: String = "Zap!"
    override var used: Boolean = false
    override val triggers = listOf(
        Timing.START_OF_OPPONENT_TURN,
        Timing.END_OF_OPPONENT_TURN
    )
    override val procedure: Procedure = ZapProcedure
}
