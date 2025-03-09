package com.jervisffb.engine.model.inducements.wizards

import com.jervisffb.engine.model.inducements.Spell
import com.jervisffb.engine.model.inducements.Timing
import kotlinx.serialization.Serializable

/**
 * Interface describing a Wizard that has been assigned to a team.
 * Its purpose is to track the usage of the Wizard during a game,
 * and not how/when to purchase it.
 */
interface Wizard {
    val type: WizardType
    val name: String
    val used: Boolean
        get() = spells.firstOrNull { it.used } != null
    val spells: List<Spell>

    /**
     * Returns the available spell at a given timing event.
     */
    fun getAvailableSpells(timing: Timing): List<Spell> {
        return spells.filter { !it.used && it.triggers.contains(timing) }
    }
}

// See page 94 in the rulebook
@Serializable
class HirelingSportsWizard: Wizard {
    override val name: String get() = type.description
    override val type: WizardType = WizardType.HIRELING_SPORTS_WIZARD
    override val spells: List<Spell> = listOf(
        Fireball(/*this*/),
        Zap(/*this*/)
    )
}
