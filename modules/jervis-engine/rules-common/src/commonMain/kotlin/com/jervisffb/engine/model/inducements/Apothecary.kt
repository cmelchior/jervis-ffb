package com.jervisffb.engine.model.inducements

/** Interface capturing all apothecaries available across BB2020 and BB20205 */
sealed interface CommonApothecary: Apothecary

/**
 * Class representing the standard apothecary that can be bought as part of a
 * team roster.
 *
 * See page 62 in the BB2020 rulebook.
 */
data class StandardApothecary(
    override var used: Boolean,
): CommonApothecary {
    override val name: String = "Apothecary"
}

/**
 * Class representing a Wandering Apothecary Inducement.
 *
 * See page 91 in the BB2020 rulebook.
 * See page 145 in the BB2025 rulebook.
 */
data class WanderingApothecary(
    override var used: Boolean,
): CommonApothecary {
    override val name: String = "Wandering Apothecary"
}

/**
 * Class representing a Plague Doctor Inducement.
 *
 * See page 91 in the BB2020 rulebook.
 */
data class PlagueDoctor(
    override var used: Boolean = false,
): CommonApothecary {
    override val name: String = "Plague Doctor"
}
