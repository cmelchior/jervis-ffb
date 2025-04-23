package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.model.locations.Location

class SetPlayerLocation(val player: Player, val location: Location) : Command {
    lateinit var originalPlayerLocation: Location
    private var originalPlayerOnField: Player? = null

    override fun execute(state: Game) {
        this.originalPlayerLocation = player.location
        if (originalPlayerLocation is FieldCoordinate) {
            val currentLocation = player.location as FieldCoordinate
            if (player.location == FieldCoordinate.UNKNOWN || player.location == FieldCoordinate.OUT_OF_BOUNDS) {
                this.originalPlayerOnField = null
            } else {
                this.originalPlayerOnField = state.field[player.location as FieldCoordinate].player
            }
        }

        // Remove from old location
        val oldLocation = originalPlayerLocation
        if (oldLocation is FieldCoordinate && oldLocation != FieldCoordinate.UNKNOWN && oldLocation != FieldCoordinate.OUT_OF_BOUNDS) {
            state.field[oldLocation].apply {
                // In some cases, players are in an intermediate state, where
                // field.location doesn't match player.location In that case,
                // do not remove the player from the field
                if (player == this@SetPlayerLocation.player) {
                    player = null
                }
            }
        }

        // Add to new location
        player.location = location
        if (location is FieldCoordinate && location != FieldCoordinate.UNKNOWN && location != FieldCoordinate.OUT_OF_BOUNDS) {
            state.field[location].apply {
                player = this@SetPlayerLocation.player
            }
        }
    }

    override fun undo(state: Game) {
        if (location is FieldCoordinate) {
            state.field[location].apply {
                player = null
            }
        }
        player.location = originalPlayerLocation
        val originalLoc = originalPlayerLocation
        if (originalLoc is FieldCoordinate) {
            state.field[originalLoc].apply {
                player = originalPlayerOnField
            }
        }
    }
}
