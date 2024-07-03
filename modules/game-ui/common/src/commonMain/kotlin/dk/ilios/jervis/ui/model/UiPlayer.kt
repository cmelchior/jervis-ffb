package dk.ilios.jervis.ui.model

import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.PlayerState

class UiPlayer(override val model: Player, val selectAction: (() -> Unit)?): UiModel<Player> {
    fun hasBall(): Boolean  = model.hasBall()
    val state: PlayerState = model.state
    val isSelectable = (selectAction != null)
}