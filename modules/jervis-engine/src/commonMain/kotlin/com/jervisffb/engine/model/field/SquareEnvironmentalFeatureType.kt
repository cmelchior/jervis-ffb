package com.jervisffb.engine.model.field

/**
 * Enum describing environmental features that might affect the square.
 * This is used to describe things like statues or furniture that fills the space.
 * Things like Chests, Teleports and Trapdoors are tracked elsewhere.
 */
enum class SquareEnvironmentalFeatureType {
    NONE, // No feature on this square
    IDOL, // See Chaotic Idol on page 29 in the Dungeon Bowl rulebook
    CRYPT, // See The Crypt on page 29 in the Dungeon Bowl rulebook
    DRAGON, // See The Dragon Youngling's Lair on page 30 in the Dungeon Bowl rulebook.
    // Suspension bridge over some kind of environmental hazard. It is treated as
    // a standard floor tile and is mostly tracked here, so the UI knows where to
    // render it.
    SUSPENSION_BRIDGE,
}
