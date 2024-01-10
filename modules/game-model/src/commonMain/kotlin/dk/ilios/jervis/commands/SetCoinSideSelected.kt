package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Coin
import dk.ilios.jervis.model.FieldCoordinate
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Location
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.PlayerState

class SetCoinSideSelected(private val coin: Coin) : Command {
    private var originalCoin: Coin? = null
    override fun execute(state: Game, controller: GameController) {
        this.originalCoin = state.coinSideSelected
        state.coinSideSelected = coin
    }

    override fun undo(state: Game, controller: GameController) {
        state.coinSideSelected = originalCoin
    }
}
