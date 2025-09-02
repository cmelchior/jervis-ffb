package com.jervisffb.ui.game

import androidx.compose.runtime.Immutable
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.common.pathfinder.PathFinder
import com.jervisffb.engine.rules.common.tables.Weather
import com.jervisffb.ui.game.dialogs.UserInputDialog
import com.jervisffb.ui.game.model.UiFieldPlayer
import com.jervisffb.ui.game.model.UiFieldSquare
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentMap

/**
 * Class representing a snapshot of the current UI State as it should be shown for this "frame". This only
 * includes the model rules state, and shouldn't include transient state. Things like hover state
 * should be covered by individual view models.
 *
 * Note, the snapshot contains a few mutable references. Not is only a temporary work-around and they
 * should be removed as soon as possible. They should only be considered stable for the duration of a single
 * game loop.
 */
@Immutable
data class UiGameSnapshot(
    val actionOwner: Team?,
    val game: Game, // We should try to remove this since it is mutable
    val squares: PersistentMap<FieldCoordinate, UiFieldSquare>,
    val players: PersistentMap<PlayerId, UiFieldPlayer>,
    // Quick access to squares with known free balls and bombs
    val freeBalls: Map<FieldCoordinate, UiFieldSquare>,
    val status: UiGameStatusUpdate,
    val unknownActions: PersistentList<GameAction>,
    val homeDogoutOnClickAction: (() -> Unit)?,
    val awayDogoutOnClickAction: (() -> Unit)?,
    // If set, a dialog should be shown as a first priority
    val dialogInput: UserInputDialog?,
    // These are not used for rending, but to cache information we cannot extract from the rule engine
    val movesUsed: PersistentList<MoveUsed>,
    val weather: Weather,
    val homeTeamInfo: UiTeamInfoUpdate,
    val awayTeamInfo: UiTeamInfoUpdate,
    // If set, it means we are in the middle of a move action that allows the player
    // to move multiple squares.
    val pathFinder: PathFinder.AllPathsResult?,
) {
    val stack = game.stack
}
