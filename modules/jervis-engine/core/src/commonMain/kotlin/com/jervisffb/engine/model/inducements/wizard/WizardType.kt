package com.jervisffb.engine.model.inducements.wizard

import com.jervisffb.engine.model.Team

/**
 * Interface describing the type of Wizard.
 * This is used to more easily identify the wizard when configuring
 * inducements available for the game.
 */
interface WizardType {
    val label: String
    fun create(team: Team): Wizard
}
