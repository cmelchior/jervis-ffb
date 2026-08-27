package com.jervisffb.engine.bb2020.inducements.wizards

import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.inducements.wizard.Wizard
import com.jervisffb.engine.model.inducements.wizard.WizardType
import kotlinx.serialization.Serializable

@Serializable
enum class WizardType2020(override val label: String): WizardType {
    HIRELING_SPORTS_WIZARD("Hireling Sports-Wizard") {
        override fun create(team: Team): Wizard = HirelingSportsWizard(team)
    }
}
