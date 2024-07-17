package dk.ilios.jervis.ui.model

import dk.ilios.jervis.model.FieldSquare
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.PlayerState
import dk.ilios.jervis.model.isOnHomeTeam
import dk.ilios.jervis.ui.ContextMenuOption

/**
 * Represents all information needed to render a single square
 */
data class UiFieldSquare(
    override val model: FieldSquare,
    val isBallOnGround: Boolean = false,
    val isBallCarried: Boolean = false,
    val player: UiPlayer? = null,
    val onSelected: (() -> Unit)? = null,
    val contextMenuOptions: List<ContextMenuOption> = emptyList(),
    val showContextMenu: Boolean = false,
): UiModel<FieldSquare>


data class UiPlayer(override val model: Player, val selectAction: (() -> Unit)?): UiModel<Player> {
    fun hasBall(): Boolean  = model.hasBall()
    val state: PlayerState = model.state
    val isOnHomeTeam = model.isOnHomeTeam()
    val position = model.position
    val isActive = model.isActive
    val isSelectable = (selectAction != null)
}
