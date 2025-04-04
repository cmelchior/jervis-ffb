package com.jervisffb.engine.rules

import com.jervisffb.engine.rules.builder.FoulActionBehavior
import com.jervisffb.engine.rules.builder.GameType
import com.jervisffb.engine.rules.builder.KickingPlayerBehavior
import kotlinx.serialization.Serializable

abstract class BB2020Rules : Rules(
    name = "Blood Bowl 2020 Rules",
    gameType = GameType.STANDARD
) {
}

@Serializable
class StandardBB2020Rules : BB2020Rules() {
    override val name: String = "Blood Bowl 2020 Rules (Strict)"
}

/**
 * Ruleset that is compatible with the way FUMBBL organizes its rules.
 * While it generally follows the rules as written, there are minor differences.
 *
 * - KickOff: No need to select the kicking player
 * - Foul: Player is not selected when starting the action.
 * - A more lenient timing system, so the opponent must time out each other.
 */
@Serializable
class FumbblBB2020Rules : BB2020Rules() {
    override val name: String
        get() = "Blood Bowl 2020 Rules (FUMBBL Compatible)"
    override val kickingPlayerBehavior: KickingPlayerBehavior = KickingPlayerBehavior.FUMBBL
    override val foulActionBehavior: FoulActionBehavior = FoulActionBehavior.FUMBBL
}
