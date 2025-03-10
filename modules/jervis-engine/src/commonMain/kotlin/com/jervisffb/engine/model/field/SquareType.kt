package com.jervisffb.engine.model.field

/**
 * This enum describes more loosely what kind of square it is. It is mostly used
 * in Dungeon Bowl to describe the various rooms at a high level.
 *
 * All other features of the square are placed on top of it.
 *
 * Note there is a difference between [SquareType] and [SquareSubstrateType] as it
 * allows us to combine their effects.
 */
enum class SquareType {

    // Standard square with no special properties.
    // Mostly used by Blood Bowl Standard and BB7
    STANDARD,

    // Dungeon Bowl
    BEDROCK, // Used to describe the space between rooms and corridors.
    CORRIDOR, // No effect, but tracked for UI purposes.
    ARMOURY_ROOM, // See Dungeon Bowl page 26
    BONE_PIT_ROOM, // See Dungeon Bowl page 26
    CURSED_ROOM, // See Dungeon Bowl page 26
    FLOODED_ROOM, // See Dungeon Bowl page page 27
    FORGOTTEN_JAIL_ROOM, // See Dungeon Bowl page page 27
    KITCHEN_ROOM, // See Dungeon Bowl page page 27
    SEWER_ROOM, // See Dungeon Bowl page page 28
    TREASURE_ROOM, // See Dungeon Bowl page page 28
    CHAOTIC_IDOL_ROOM, // See Dungeon Bowl page page 29
    CRYPT_ROOM, // See Dungeon Bowl page page 29
    DRAGON_YOUNGLINGS_LAIR_ROOM, // See Dungeon Bowl page page 30
    FIERY_CHASM_ROOM, // See Dungeon Bowl page page 30

    // Gutter Bowl
    MARKET_STALL,
    SEWER
}
