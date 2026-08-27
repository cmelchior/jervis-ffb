package com.jervisffb.engine.bb2020.inducements.wizards

import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.WizardId
import com.jervisffb.engine.model.inducements.wizard.Spell
import kotlinx.serialization.Serializable

// See page 94 in the BB2020 rulebook
@Serializable
class HirelingSportsWizard(private val isHomeTeam: Boolean, private val index: Int): Wizard2020 {
    constructor(team: Team): this(team.isHomeTeam(), team.wizards.size)
    override val id: WizardId = run {
        val prefix = if (isHomeTeam) "home" else "away"
        WizardId("$prefix-$index-HirelingSportsWizard")
    }
    override val type: WizardType2020 = WizardType2020.HIRELING_SPORTS_WIZARD
    override val name: String = type.label
    override val spells: List<Spell> = listOf(
        Fireball(id),
        Zap(id)
    )
}
