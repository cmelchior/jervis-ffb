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
        player.location = location
        if (location is FieldCoordinate) {
            state.field[location.x, location.y].player = player
        }
    }

    override fun undo(state: Game, controller: GameController) {
        if (location is FieldCoordinate) {
            state.field[location.x, location.y].player = null
        }
        player.location = originalLocation
    }
}
