package dk.ilios.jervis.ui.model

import dk.ilios.jervis.model.BallState
import dk.ilios.jervis.model.FieldSquare
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.PlayerState
import dk.ilios.jervis.model.isOnHomeTeam

/**
 * Represents all information needed to render a single square
 */
data class UiFieldSquare(
    override val model: FieldSquare,
    val ball: BallState? = null,
    val player: UiPlayer? = null,
    val onSelected: (() -> Unit)? = null,
): UiModel<FieldSquare>

data class UiPlayer(override val model: Player, val selectAction: (() -> Unit)?): UiModel<Player> {
    fun hasBall(): Boolean  = model.hasBall()
    val state: PlayerState = model.state
    val isSelectable = (selectAction != null)
    val isOnHomeTeam = model.isOnHomeTeam()
    val position = model.position
    val isActive = model.isActive
}

