package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.locations.Location
import com.jervisffb.engine.model.locations.PitchCoordinate

/**
 * Set player location. This also include state changes related to being thrown or not.
 */
class SetPlayerLocation(
    val player: Player,
    val location: Location,
    val isThrown: Boolean = false, // If true, the player is thrown to the new location rather than being placed
) : Command {
    private var originalPlayerBeingThrown = false
    lateinit var originalPlayerLocation: Location
    private var originalPlayerOnPitch: Player? = null
    private var originalThrownPlayerOnPitch: Player? = null

    override fun execute(state: Game) {
        // Save original state
        this.originalPlayerLocation = player.location
        this.originalPlayerBeingThrown = player.isBeingThrown
        if (originalPlayerLocation is PitchCoordinate) {
            if (player.location == PitchCoordinate.UNKNOWN || player.location.isOutOfBounds(state.rules)) {
                this.originalPlayerOnPitch = null
                this.originalThrownPlayerOnPitch = null
            } else {
                this.originalPlayerOnPitch = state.pitch[player.location as PitchCoordinate].player
                this.originalThrownPlayerOnPitch = state.pitch[player.location as PitchCoordinate].thrownPlayer
            }
        }

        // Remove from old location
        val oldLocation = originalPlayerLocation
        if (oldLocation is PitchCoordinate && oldLocation != PitchCoordinate.UNKNOWN && !oldLocation.isOutOfBounds(state.rules)) {
            state.pitch[oldLocation].apply {
                // In some cases, players are in an intermediate state, where
                // square.location doesn't match player.location. E.g. when
                // setting up a pushback chain. In that case, do not remove the
                // player from the pitch.
                if (player == this@SetPlayerLocation.player) {
                    player = null
                }
                if (thrownPlayer == this@SetPlayerLocation.player) {
                    thrownPlayer = null
                }
            }
        }

        // Add to new location
        player.location = location
        player.isBeingThrown = isThrown
        if (location is PitchCoordinate && location != PitchCoordinate.UNKNOWN && !location.isOutOfBounds(state.rules)) {
            state.pitch[location].apply {
                if (isThrown) {
                    thrownPlayer = this@SetPlayerLocation.player
                } else {
                    player = this@SetPlayerLocation.player
                }
            }
        }
    }

    override fun undo(state: Game) {
        if (location is PitchCoordinate && location != PitchCoordinate.UNKNOWN && !location.isOutOfBounds(state.rules)) {
            state.pitch[location].apply {
                if (isThrown) {
                    thrownPlayer = null
                } else {
                    player = null
                }
            }
        }
        player.location = originalPlayerLocation
        player.isBeingThrown = originalPlayerBeingThrown
        val originalLoc = originalPlayerLocation
        if (originalLoc is PitchCoordinate && originalLoc != PitchCoordinate.UNKNOWN && !originalLoc.isOutOfBounds(state.rules)) {
            state.pitch[originalLoc].apply {
                player = originalPlayerOnPitch
                thrownPlayer = originalThrownPlayerOnPitch
            }
        }
    }
}
