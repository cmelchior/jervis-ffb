package com.jervisffb.engine.bb2020.inducements.wizards

import com.jervisffb.engine.model.WizardId
import com.jervisffb.engine.model.inducements.Spell
import com.jervisffb.engine.model.inducements.wizards.Fireball
import com.jervisffb.engine.model.inducements.wizards.Zap
import kotlinx.serialization.Serializable

// See page 94 in the BB2020 rulebook
@Serializable
class HirelingSportsWizard: BB2020Wizard {
    override val id: WizardId = WizardId("HirelingSportsWizard")
    override val name: String = "Hireling Sports-Wizard"
    override val spells: List<Spell> = listOf(
        Fireball(/*this*/),
        Zap(/*this*/)
    )
}
