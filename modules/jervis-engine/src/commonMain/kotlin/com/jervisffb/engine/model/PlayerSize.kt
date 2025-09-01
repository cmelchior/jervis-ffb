package com.jervisffb.engine.model

/**
 * While BB2020 only has a rules distinction between Giants and standard player
 * sizes. Big Guys have visible larger figures on the field as well.
 *
 * So to simplify things we tracker player size on the Rules side.
 */
enum class PlayerSize {
    // Normal sized players, this also includes hobbits and goblins.
    // Their smaller size is just reflected in the UI by having a smaller icon
    // inside the standard player area.
    STANDARD,
    // These are not a concept in the BB2020 rules, but do have an 33% increased size
    // in the UI.
    BIG_GUY,
    // Giant-sized creatures have special rules and take up 4 squares on the field.
    GIANT,
}
