package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.FieldCoordinate
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Location
import dk.ilios.jervis.model.Player

class SetPlayerLocation(private val player: Player, val location: Location) : Command {
    private lateinit var originalLocation: Location
    override fun execute(state: Game, controller: GameController) {
        this.originalLocation = player.location

        // Remove from old location
        val oldLocation = originalLocation
        if (oldLocation is FieldCoordinate) {
            state.field[oldLocation].apply {
                player = null
            }
        }

        // Add to new location
        player.location = location
        if (location is FieldCoordinate) {
            state.field[location].apply {
                player = this@SetPlayerLocation.player
            }
        }

        // Only run notifications after all changes are applied
        if (oldLocation is FieldCoordinate) {
            state.field[oldLocation].notifyUpdate()
        }
        player.notifyUpdate()
        player.team.notifyDogoutChange()
        player.location.let {
            if (it is FieldCoordinate) {
                state.field[it].notifyUpdate()
            }
        }
    }

    override fun undo(state: Game, controller: GameController) {
        if (location is FieldCoordinate) {
            state.field[location].apply {
                player = null
            }
        }
        player.location = originalLocation
        val originalLoc = originalLocation
        if (originalLoc is FieldCoordinate) {
            state.field[originalLoc].apply {
                player = this@SetPlayerLocation.player
            }
        }
        // Only run notifications after all changes are applied
        if (location is FieldCoordinate) {
            state.field[location].notifyUpdate()
        }
        player.notifyUpdate()
        player.team.notifyDogoutChange()
        originalLoc.let {
            if (it is FieldCoordinate) {
                state.field[it].notifyUpdate()
            }
        }
    }
}
