package com.jervisffb.engine.model.inducements

import kotlinx.serialization.Serializable

/**
 * This file is just being used for prototyping how to model inducements
 * It probably needs a few iterations
 */
enum class ApothecaryType(val description: String) {
    STANDARD("Apothecary"), // See page 62 in the rulebook
    WANDERING("Wandering Apothecary"), // See page 91 in the rulebook
    MORTUARY_ASSISTANT("Mortuary Assistant"), // See page 91 in the rulebook
    PLAGUE_DOCTOR("Plague Doctor") // See page 91 in the rulebook
}

@Serializable
data class Apothecary(
    var used: Boolean,
    val type: ApothecaryType,
)
