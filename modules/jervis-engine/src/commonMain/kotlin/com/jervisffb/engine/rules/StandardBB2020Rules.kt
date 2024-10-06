package com.jervisffb.engine.rules

import kotlinx.serialization.Serializable

open class BB2020Rules : Rules {
    override val name: String
        get() = "Blood Bowl 2020 Rules"
}

@Serializable
object StandardBB2020Rules : BB2020Rules() {
    override val name: String
        get() = "Blood Bowl 2020 Rules (Strict)"
}

/**
 * Ruleset that is compatible with the way FUMBBL organizes its rules.
 * While it generally follows the rules as written, there are minor differences.
 *
 * - KickOff: No need to select the kicking player
 * - Foul: Player is not selected when starting the action.
 */
@Serializable
object FumbblBB2020Rules : Rules {
    override val name: String
        get() = "Blood Bowl 2020 Rules (FUMBBL Compatible)"
}
