package com.jervisffb.engine.model.field

/**
 * This enum describes what kind of surface the square has.
 *
 * Note there is a difference between [SquareType] and [SquareSubstrateType] as it
 * allows us to combine their effects.
 */
enum class SquareSubstrateType {
    // The surface is unspecified and has no special rules.
    // Normally used for squares, players cannot access.
    UNKNOWN,
    // Default kind of surface on top-level playing fields
    // Used on normal Blood Bowl pitches. It normally has no special effect.
    GRASS,
    // Default kind of surface on playing fields underground or in the streets.
    // Used by Dungeon Bowl and Gutter Bowl. It normally has no special effect.
    STONE,
    // Default surface when playing Gutter Bowl. Effects bounces and injury rolls.
    COBBLESTONE_FLOORS,
    // Chasm full of lava. See page 30 in Dungeon Bowl
    FIERY_CHASM,
}
