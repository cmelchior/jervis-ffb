package com.jervisffb.ui.game.model

import com.jervisffb.engine.model.Availability
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.isOnHomeTeam

class UiPlayer(
    val model: Player,
    var selectAction: (() -> Unit)? = null,
    var onHover: (() -> Unit)? = null,
    var onHoverExit: (() -> Unit)? = null
) {
    val carriesBall: Boolean = model.hasBall()
    val state: PlayerState = model.state
    val isOnHomeTeam = model.isOnHomeTeam()
    val isProne = (model.state == PlayerState.PRONE)
    val isStunned = (model.state == PlayerState.STUNNED || model.state == PlayerState.STUNNED_OWN_TURN)
    val position = model.position
    val isActive = (model.available == Availability.IS_ACTIVE)
    val isSelectable get() = (selectAction != null)
    val hasActivated = (model.available == Availability.HAS_ACTIVATED || model.available == Availability.UNAVAILABLE) && model.location.isOnField(model.team.game.rules)
}
