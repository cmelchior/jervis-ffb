package com.jervisffb.engine.model.inducements.settings

/**
 * Interface for creating "builders" for inducements. This is used by the UI
 * when editing inducement properties.
 */
sealed interface InducementBuilder {
    val type: InducementType
    val name: String
    var max: Int
    var enabled: Boolean
    // We cannot have `build()` in the interface because we end up with recursive type definitions
    fun build(): Inducement<*>
}

// Top-level interface for inducements that are selected "as-is".
// Examples: Extra Training, Wandering Apothecaries, Blitzer's Keg.
interface SingleInducementBuilder: InducementBuilder {
    var price: Int
}

// Top-level interface for inducements that are a collection of different types
// Examples: Biased Referees, Infamous Coaching Staff, Star Players and Wizards
interface InducementGroupBuilder: InducementBuilder

// Top-level interface for inducements that are "normal" team players.
// Examples: Standard Mercenaries, Expanded Mercenaries
interface TeamPlayerInducementBuilder: InducementBuilder


