package com.jervisffb.engine.bb2025.inducements.wizards

import com.jervisffb.engine.model.inducements.wizard.Wizard

/**
 * Interface capturing all Wizards available in BB2025.
 */
sealed interface Wizard2025: Wizard {
    override val type: WizardType2025
}
