package com.jervisffb.ui.game.model

import com.jervisffb.engine.model.Availability
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.PlayerNo
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.PlayerType
import com.jervisffb.engine.model.TeamId
import com.jervisffb.engine.model.isOnHomeTeam
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.common.roster.Position

/**
 * Contains all the information needed to display a player on the field.
 * Squares themselves are represented by [UiFieldSquare].
 */
data class UiFieldPlayer(
    val id: PlayerId,
    val coordinate: FieldCoordinate,
    val number: PlayerNo,
    val team: TeamId,
    val selectedAction: (() -> Unit)?,
    val carriesBall: Boolean,
    val state: PlayerState,
    val isOnHomeTeam: Boolean,
    val position: Position,
    val isActive: Boolean,
    val isGoingDown: Boolean,
    val hasActivated: Boolean,
    val dice: Int = 0, // Show block dice decorator
    val isBlocked: Boolean = false, // Show "blocked" indicator
) {
    constructor(model: Player, selectAction: (() -> Unit)? = null) : this(
        id = model.id,
        coordinate = model.coordinates,
        number = model.number,
        team = model.team.id,
        selectedAction = selectAction,
        carriesBall = model.hasBall(),
        state = model.state,
        isOnHomeTeam = model.isOnHomeTeam(),
        position = model.position,
        isActive = (model.available == Availability.IS_ACTIVE),
        isGoingDown = (
            model.state == PlayerState.KNOCKED_DOWN
                || model.state == PlayerState.FALLEN_OVER
                || model.team.game.rules.isInjuried(model)
        ),
        hasActivated = (model.available == Availability.HAS_ACTIVATED || model.available == Availability.UNAVAILABLE) && model.location.isOnField(model.team.game.rules)
    )

    val isSelectable = (selectedAction != null)
    val isProne: Boolean = (state == PlayerState.PRONE)
    val isStunned: Boolean = (state == PlayerState.STUNNED || state == PlayerState.STUNNED_OWN_TURN)
}
