package com.jervisffb.engine.bb2025.inducements.wizards

import com.jervisffb.engine.common.inducements.wizards.Fireball
import com.jervisffb.engine.common.inducements.wizards.Zap
import com.jervisffb.engine.model.WizardId
import com.jervisffb.engine.model.inducements.wizard.Spell
import kotlinx.serialization.Serializable

// See page 149 in the BB2025 rulebook
@Serializable
class SportsWizard: Wizard2025 {
    override val id: WizardId = WizardId("SportsWizard")
    override val name: String = "Sports Wizard"
    override val spells: List<Spell> = listOf(
        Fireball(/*this*/),
        Zap(/*this*/),
    )
}
