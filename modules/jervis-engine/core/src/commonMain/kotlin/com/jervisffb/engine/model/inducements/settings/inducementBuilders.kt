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

sealed interface SingleInducementBuilder: InducementBuilder {
    var price: Int
}

sealed interface InducementGroupBuilder: InducementBuilder

sealed interface TeamPlayerInducementBuilder: InducementBuilder


