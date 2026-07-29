package com.jervisffb.tourplay.api

// Auto-generate the shorthand letters we use for this position if no player
// sprite exists.
val LineUpMaster.positionShortHand: String
    get() {
        // TODO This doesn't work with full Unicode. Revisit it, if it turns out to be a problem
        return this.position.trim()
            .split(Regex("\\s+"))
            .filter { it.isNotEmpty() }
            .map { it.first().uppercaseChar() }
            .joinToString("")
            .lowercase()
            .replaceFirstChar { it.uppercase() }
    }
