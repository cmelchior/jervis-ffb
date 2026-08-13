package com.jervisffb.ui.game.model

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import com.jervisffb.engine.model.Availability
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.PlayerIntermediateState
import com.jervisffb.engine.model.PlayerNo
import com.jervisffb.engine.model.PlayerPitchState
import com.jervisffb.engine.model.PlayerSize
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.TeamId
import com.jervisffb.engine.model.isOnHomeTeam
import com.jervisffb.engine.model.locations.Location
import com.jervisffb.engine.rules.common.roster.Position
import com.jervisffb.ui.game.UiFocusStyle
import com.jervisffb.ui.menu.GameScreenModel

/**
 * Contains all the information needed to display a player on the pitch.
 * Squares themselves are represented by [UiPitchSquare].
 *
 * Right now, this class is used for both On-Pitch and Off-Pitch players.
 *
 * See [UiPitchSquare] for why the stability annotation is needed. [Stable]
 * rather than `@Immutable` because of [isTemporarySelected], which is genuinely
 * mutable - but it is a snapshot state, so mutating it notifies composition,
 * and being a body property it stays out of `equals`.
 */
@Stable
data class UiPitchPlayer(
    val id: PlayerId,
    val location: Location,
    val number: PlayerNo,
    val team: TeamId,
    val size: PlayerSize,
    val selectedAction: ((GameScreenModel, UiPitchPlayer) -> Unit)?,
    val carriesBall: Boolean,
    val state: PlayerState,
    val isOnHomeTeam: Boolean,
    val position: Position,
    val isActive: Boolean,
    val isGoingDown: Boolean,
    val hasActivated: Boolean,
    val dice: Int = 0, // Show block dice decorator
    val isBlocked: Boolean = false, // Show "blocked" indicator
    val isHighlighted: Boolean = false, // Show "highlighted" indicator. This is different from "selectable"
    val focusStyle: UiFocusStyle? = null,
) {
    constructor(
        model: Player,
        overrideLocation: Location = model.location,
        selectAction: ((GameScreenModel, UiPitchPlayer) -> Unit)? = null,
        focusStyle: UiFocusStyle? = null,
    ) : this(
        id = model.id,
        location = overrideLocation,
        number = model.number,
        team = model.team.id,
        size = model.position.size,
        selectedAction = selectAction,
        carriesBall = model.hasBall(),
        state = model.state,
        isOnHomeTeam = model.isOnHomeTeam(),
        position = model.position,
        isActive = (model.available == Availability.IS_ACTIVE),
        isGoingDown = (
            model.intermediateState == PlayerIntermediateState.KNOCKED_DOWN
                || model.intermediateState == PlayerIntermediateState.FALLEN_OVER
        ),
        hasActivated = (model.available == Availability.HAS_ACTIVATED || model.available == Availability.UNAVAILABLE) && model.location.isOnPitch(model.team.game.rules),
        focusStyle = focusStyle,
    )
    val isTemporarySelected = mutableStateOf(false)
    val isSelectable = (selectedAction != null)
    val isProne: Boolean = (state == PlayerPitchState.PRONE)
    val isStunned: Boolean = (state == PlayerPitchState.STUNNED || state == PlayerPitchState.STUNNED_OWN_TURN)
}
