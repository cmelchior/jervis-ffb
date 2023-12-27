package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.FieldCoordinate
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Location
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.PlayerState

class SetPlayerState(private val player: Player, val state: PlayerState) : Command {
    private lateinit var originalState: PlayerState
    override fun execute(state: Game, controller: GameController) {
        this.originalState = player.state
        player.state = this.state
    }

    override fun undo(state: Game, controller: GameController) {
        player.state = originalState
    }
}
