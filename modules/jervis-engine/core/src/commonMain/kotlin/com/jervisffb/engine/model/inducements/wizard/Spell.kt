package com.jervisffb.engine.model.inducements.wizard

import com.jervisffb.engine.model.inducements.InducementEffect

/**
 * Interface describing spells owned by Wizards,
 */
interface Spell: InducementEffect {
    // This causes a circular reference crashing serialization
    // val wizard: Wizard
}
