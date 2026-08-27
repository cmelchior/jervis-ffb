package com.jervisffb.engine.bb2025.inducements.wizards

import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.WizardId
import com.jervisffb.engine.model.inducements.wizard.Spell
import kotlinx.serialization.Serializable

// See page 149 in the BB2025 rulebook
@Serializable
class SportsWizard(private val isHomeTeam: Boolean, private val index: Int): Wizard2025 {
    constructor(team: Team): this(team.isHomeTeam(), team.wizards.size)
    override val id: WizardId = run {
        val prefix = if (isHomeTeam) "home" else "away"
        WizardId("$prefix-$index-SportsWizard")
    }
    override val type = WizardType2025.SPORTS_WIZARD
    override val name: String = type.label
    override val spells: List<Spell> = listOf(
        Fireball(id),
        Zap(id),
    )
}
