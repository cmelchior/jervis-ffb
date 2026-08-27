package com.jervisffb.engine.bb2025.inducements.wizards

import com.jervisffb.engine.common.procedures.inducements.spells.FireBallProcedure
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.InducementEffectId
import com.jervisffb.engine.model.WizardId
import com.jervisffb.engine.model.inducements.Timing
import com.jervisffb.engine.model.inducements.wizard.Spell
import kotlinx.serialization.Serializable

// Fireball spell - See page 149 in the BB2025 rulebook
@Serializable
class Fireball(val wizard: WizardId) : Spell {
    override val id: InducementEffectId = InducementEffectId("${wizard.value}-fireball")
    override val name: String = "Fireball"
    override var used: Boolean = false
    override val triggers = listOf(
        Timing.START_OF_OPPONENT_TURN,
        Timing.END_OF_OPPONENT_TURN
    )
    override val procedure: Procedure = FireBallProcedure
}

