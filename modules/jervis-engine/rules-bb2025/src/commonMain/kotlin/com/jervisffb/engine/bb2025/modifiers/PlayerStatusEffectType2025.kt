package com.jervisffb.engine.bb2025.modifiers

import com.jervisffb.engine.model.modifiers.PlayerStatusEffectType
import kotlinx.serialization.Serializable

@Serializable
enum class PlayerStatusEffectType2025(
    override val label: String
): PlayerStatusEffectType  {
    DISTRACTED("Distracted"),
    CHOMPED("Chomped"),
    EYE_GOUGE("Eye Gouge"),
    // This is just a marker that determines if the player has to skip the drive.
    // Stat decreases are tracked separately
    DODGY_SNACK("Dodgy Snack"),

    HANGOVER("Hangover"), // Marker for Desperate Measures "Hangover"
    DOPED("Doped"), // Marker for a failed Desperate Measures "You Dope!"
    GRUDGE_MATCH("Grudge Match"), // Marker for the active player when activating Desperate Measure "Grudge Match"
    SET_PIECE("Set Piece") // Marker for the Desperate Measure "Set Piece"

}
