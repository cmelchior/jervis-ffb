package com.jervisffb.ui.model

import com.jervisffb.engine.model.Availability
import com.jervisffb.engine.model.FieldSquare
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.isOnHomeTeam
import com.jervisffb.engine.model.Direction
import com.jervisffb.ui.ContextMenuOption

/**
 * Represents all information needed to render a single square
 */
data class UiFieldSquare(
    override val model: FieldSquare,
    val isBallOnGround: Boolean = false,
    val isBallExiting: Boolean = false,
    val isBallCarried: Boolean = false,
    val player: UiPlayer? = null,
    val direction: Direction? = null,
    val onSelected: (() -> Unit)? = null,
    val onMenuHidden: (() -> Unit?)? = null,
    val requiresRoll: Boolean = false,
    val contextMenuOptions: List<ContextMenuOption> = emptyList(),
    val showContextMenu: Boolean = false,
) : UiModel<FieldSquare>

class UiPlayer(
    override val model: Player,
    val selectAction: (() -> Unit)?,
    val onHover: (() -> Unit)? = null,
    val onHoverExit: (() -> Unit)? = null
) : UiModel<Player> {
    val carriesBall: Boolean = model.hasBall()
    val state: PlayerState = model.state
    val isOnHomeTeam = model.isOnHomeTeam()
    val isProne = (model.state == PlayerState.PRONE)
    val isStunned = (model.state == PlayerState.STUNNED || model.state == PlayerState.STUNNED_OWN_TURN)
    val position = model.position
    val isActive = (model.available == Availability.IS_ACTIVE)
    val isSelectable = (selectAction != null)
    val hasActivated = (model.available == Availability.HAS_ACTIVATED || model.available == Availability.UNAVAILABLE)
}

class UiPlayerCard(
    override val model: Player,
) : UiModel<Player>
