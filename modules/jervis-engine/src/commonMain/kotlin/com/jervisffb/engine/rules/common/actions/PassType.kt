package com.jervisffb.engine.rules.common.actions

import kotlinx.serialization.Serializable

@Serializable
enum class PassType {
    STANDARD,
    // Hail Mary is technically not a separate special action, but it changes
    // the behavior of a Pass enough that we are splitting it out into its own
    // Procedure. This is also true for the UI, where Hail Mary is selected just
    // like other special actions.
    HAIL_MARY_PASS
}
