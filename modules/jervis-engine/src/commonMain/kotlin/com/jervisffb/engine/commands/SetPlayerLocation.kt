package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.model.locations.Location

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
    private var originalPlayerOnField: Player? = null
    private var originalThrownPlayerOnField: Player? = null

    override fun execute(state: Game) {
        // Save original state
        this.originalPlayerLocation = player.location
        this.originalPlayerBeingThrown = player.isBeingThrown
        if (originalPlayerLocation is FieldCoordinate) {
            if (player.location == FieldCoordinate.UNKNOWN || player.location.isOutOfBounds(state.rules)) {
                this.originalPlayerOnField = null
                this.originalThrownPlayerOnField = null
            } else {
                this.originalPlayerOnField = state.field[player.location as FieldCoordinate].player
                this.originalThrownPlayerOnField = state.field[player.location as FieldCoordinate].thrownPlayer
            }
        }

        // Remove from old location
        val oldLocation = originalPlayerLocation
        if (oldLocation is FieldCoordinate && oldLocation != FieldCoordinate.UNKNOWN && !oldLocation.isOutOfBounds(state.rules)) {
            state.field[oldLocation].apply {
                // In some cases, players are in an intermediate state, where
                // field.location doesn't match player.location. E.g. when
                // setting up a pushback chain. In that case, do not remove the
                // player from the field.
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
        if (location is FieldCoordinate && location != FieldCoordinate.UNKNOWN && !location.isOutOfBounds(state.rules)) {
            state.field[location].apply {
                if (isThrown) {
                    thrownPlayer = this@SetPlayerLocation.player
                } else {
                    player = this@SetPlayerLocation.player
                }
            }
        }
    }

    override fun undo(state: Game) {
        if (location is FieldCoordinate && location != FieldCoordinate.UNKNOWN && !location.isOutOfBounds(state.rules)) {
            state.field[location].apply {
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
        if (originalLoc is FieldCoordinate && originalLoc != FieldCoordinate.UNKNOWN && !originalLoc.isOutOfBounds(state.rules)) {
            state.field[originalLoc].apply {
                player = originalPlayerOnField
                thrownPlayer = originalThrownPlayerOnField
            }
        }
    }
}
