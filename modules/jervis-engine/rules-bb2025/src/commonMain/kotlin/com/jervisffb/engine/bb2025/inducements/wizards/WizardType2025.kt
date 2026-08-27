package com.jervisffb.engine.bb2025.inducements.wizards

import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.inducements.wizard.Wizard
import com.jervisffb.engine.model.inducements.wizard.WizardType
import kotlinx.serialization.Serializable

@Serializable
enum class WizardType2025(override val label: String): WizardType {
    SPORTS_WIZARD("Sports Wizard") {
        override fun create(team: Team): Wizard = SportsWizard(team)
    }
}
