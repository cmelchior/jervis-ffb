package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Coin
import dk.ilios.jervis.model.Game

class SetCoinTossResult(private val coin: Coin) : Command {
    private var originalCoin: Coin? = null
    override fun execute(state: Game, controller: GameController) {
        this.originalCoin = state.coinSideSelected
        state.coinResult = coin
    }

    override fun undo(state: Game, controller: GameController) {
        state.coinResult = originalCoin
    }
}
