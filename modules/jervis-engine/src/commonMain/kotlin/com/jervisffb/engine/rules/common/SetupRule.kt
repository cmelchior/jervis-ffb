package com.jervisffb.engine.rules.common

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerId

/**
 * Enum describing restrictions on setting up Players. These are reported by
 * [com.jervisffb.engine.rules.Rules.isSetupValid] in case any invariant is
 * broken.
 *
 * See page 47 in the BB2025 rulebook.
 */
sealed interface SetupRule
data class OwnHalf(
    /** Players that are _not_ on the teams half. */
    val invalidPlayers: List<Player>
): SetupRule
data class MissingPlayersOnLoS(
    /** How many players are on the LoS */
    val players: Int,
    /** How many players are required on the Center Field LoS */
    val requiredPlayers: Int
): SetupRule
data class TooManyPlayersInWideZone(
    /** `true` if this is the top widezone, `false` if bottom. */
    val top: Boolean,
    /** How many players are in the Wide Zone */
    val players: Int,
    /** How many players are allowed in the Wide Zone */
    val maxPlayers: Int
): SetupRule
data class WrongAmountOfPlayersOnPitch(
    /** How many available players are available. */
    val availablePlayers: Int,
    /** How many players are actually on the pitch */
    val playersOnPitch: Int
): SetupRule
data class TeamCaptainNotOnPitch(
    // Which players with Team Captain status are available
    val availablePlayers: List<PlayerId>
): SetupRule
