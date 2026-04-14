package com.jervisffb.ui.game.viewmodel

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.positionInRoot
import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CompositeGameAction
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.actions.PitchSquareSelected
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.hasSkill
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.utils.safeTryEmit
import com.jervisffb.ui.game.UiGameController
import com.jervisffb.ui.game.UiGameSnapshot
import com.jervisffb.ui.game.animations.JervisAnimation
import com.jervisffb.ui.game.dialogs.PrimaryActionWheelViewModel
import com.jervisffb.ui.game.dialogs.SecondaryActionWheelViewModel
import com.jervisffb.ui.game.model.UiPitchPlayer
import com.jervisffb.ui.game.model.UiPitchSquare
import com.jervisffb.ui.game.state.ManualActionProvider
import com.jervisffb.ui.game.state.QueuedActionsResult
import com.jervisffb.ui.menu.GameScreenModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

data class SquareLayoutCoordinates(
    val coordinate: PitchCoordinate,
    val positionInRoot: Offset,
    val boundsInRoot: Rect,
)

data class PitchLayoutCoordinates(
    val positionInRoot: Offset,
)

/**
 * Expand player data with extra callbacks related to UI hover effects.
 * TODO I think this is only used for the Player Stat Card, so we can probably
 *  find a way to remove this.
 */
data class UiPlayerTransientData(
    val onHover: (() -> Unit)?,
    val onHoverExit: (() -> Unit)?,
    val onSecondaryClick: (() -> Unit)? = null,
)

/**
 * Information needed to render PathFinder information on the board.
 */
data class UiPathFinderData(
    val coordinate: PitchCoordinate,
    // Indicate the amount of move used to reach a potential target square.
    // This number will override `moveUsed` if present
    val futureMoveDistance: Int,
    // Action triggered if this square is selected.
    val hoverAction: () -> Unit,
)

/**
 * This class collects all the information needed to render the pitch. This includes all information needed for
 * each single square on the pitch.
 */
class PitchViewModel(
    val screenModel: GameScreenModel,
    private val uiState: UiGameController,
    private val hoverPlayerChannel: MutableSharedFlow<Player?>,
) {
    val rules = uiState.rules
    val game = uiState.state
    val width = rules.pitchWidth
    val height = rules.pitchHeight
    val sharedPitchData = screenModel.sharedPitchData

    val pitchViewData: MutableStateFlow<PitchViewData> = screenModel.pitchViewData
    val pitchBackground: Flow<PitchDetails> = screenModel.pitchBackground

    val actionWheelViewModel = PrimaryActionWheelViewModel(
        eventFlow = uiState.uiActionWheelFlow,
        team = uiState.state.homeTeam,
        sharedPitchData = sharedPitchData,
    )
    val contextActionWheelViewModel = SecondaryActionWheelViewModel(
        pitchViewModel = this,
        eventFlow = uiState.uiContextWheelFlow,
        sharedPitchData = sharedPitchData,
    )

    val highlights: StateFlow<PitchCoordinate?>
        field = MutableStateFlow<PitchCoordinate?>(null)

    // Pitch layout coordinates (inside the border)
    var pitchCoordinates: PitchLayoutCoordinates = PitchLayoutCoordinates(Offset.Zero)
    var borderSize: Float = 0f
    // Track offsets of pitch squares (so we can use them to animate things between squares)
    // Square offset is from inside the pitch border.
    val squareOffsets: MutableMap<PitchCoordinate, SquareLayoutCoordinates?> = mutableMapOf()

    fun observeAnimation(): Flow<Pair<UiGameController, JervisAnimation>?> {
        return uiState.animationFlow.map { if (it != null) Pair(uiState, it) else null }
    }

    fun triggerHoverEnter(square: PitchCoordinate) {
        if (!square.isOnPitch(game.rules)) {
            error("Square is not on pitch: $square")
        }
        game.pitch[square].player.let { player: Player? ->
            hoverPlayerChannel.safeTryEmit(player)
        }
        highlights.value = square
    }

    fun triggerHoverExit() {
        highlights.value = null
    }

    fun triggerShowPlayerContextMenu(player: PlayerId) {
        // Right now, right-clicking a player immediately show the "Edit Player" dialog.
        // The rules control this behavior. In the future the Player Context Menu
        // will probably contain more customizations (like markers), so should always be
        // available, but for now, the behavior to disable it is controlled through the rules.
        if (rules.allowPlayerEditsDuringGame) {
            screenModel.showPlayerContextMenu(player)
        }
    }

    /**
     * This flow exposes path finder data on the pitch (if relevant). I.e., future
     * moves are written on each square of the pitch.
     */
    fun observePathFinder(): Flow<Map<PitchCoordinate, UiPathFinderData>> {
        return combine(highlights, uiState.uiStateFlow) { mouseEnter, uiSnapshot ->
            // If a highlighted square exists, we are going to calculate the shortest path to that
            // square and annotate the path towards it as well. These decorations take precedence
            // over already existing move decorations.
            val activePlayer: Player? = uiSnapshot.game.activePlayer
            val requiresStandingUp = (activePlayer?.state == PlayerState.PRONE)
            val standingUpIsFree = (activePlayer?.hasSkill(SkillType.JUMP_UP) == true)

            // Use path finder
            val pathList = uiSnapshot.pathFinder?.let { pathFinder ->
                if (showPathFinder(activePlayer, mouseEnter, screenModel.actionProvider.currentProvider as? ManualActionProvider)) {
                    val standingUpPenalty = when {
                        requiresStandingUp && !standingUpIsFree -> rules.moveRequiredForStandingUp
                        else -> 0
                    }
                    val maxMoves = (activePlayer!!.movesLeft - standingUpPenalty)
                    val path: List<PitchCoordinate> = when (maxMoves > 0) {
                        true -> uiSnapshot.pathFinder.getClosestPathTo(mouseEnter!!, maxMoves)
                        else -> emptyList()
                    }

                    // Create the action triggered if clicking the mouse-over square.
                    val action = {
                        val actionProvider = (uiState.actionProvider)
                        fun getQueuedActionsForPath(): QueuedActionsResult {
                            // If the player is using Fumblerooski we have disabled the Pathfinder
                            // This means that if they _haven't_ used it, we need to _not_ use it
                            // across all moves triggered by the PathFinder.
                            val isFumblerooskiAvailable = activePlayer.isSkillAvailable(SkillType.FUMBLEROOSKI) && activePlayer.hasBall()
                            val selectedSquares = path.map {
                                CompositeGameAction(
                                    listOfNotNull(
                                        MoveTypeSelected(MoveType.STANDARD),
                                        PitchSquareSelected(it),
                                        if (isFumblerooskiAvailable) Cancel else null
                                    )
                                )
                            }
                            return if (selectedSquares.size == 1) {
                                QueuedActionsResult(selectedSquares.first())
                            } else {
                                QueuedActionsResult(selectedSquares, true)
                            }
                        }

                        if (requiresStandingUp) {
                            // If the player is about to stand up, there is room for uncertainty
                            // in which game actions queue up, as the player might have to roll
                            // and reroll for it. In that case, we just stand up first, and queue
                            // up all move actions up until the player can actually move.
                            actionProvider.registerQueuedActionGenerator { controller ->
                                if (controller.getAvailableActions().contains(MoveType.STANDARD)) {
                                    getQueuedActionsForPath()
                                } else {
                                    null
                                }
                            }
                            // Trigger Stand-up
                            actionProvider.userActionSelected(MoveTypeSelected(MoveType.STAND_UP))
                        } else {
                            // Nothing should prevent the player from moving straight away, so
                            // just queue up all pathfinder data directly.
                            actionProvider.userMultipleActionsSelected(getQueuedActionsForPath().actions)
                        }
                    }

                    // Annotate all squares with "Move Used" amount + add action on the square
                    // that is currently being hovered over.
                    val standingUpModifier = if (requiresStandingUp && !standingUpIsFree) rules.moveRequiredForStandingUp else 0
                    val currentMovesLeft = activePlayer.move - activePlayer.movesLeft
                    path.mapIndexed { index, pathSquare ->
                        val shownMoveValue = when (index) {
                            0 -> currentMovesLeft + index + 1 + standingUpModifier
                            else -> currentMovesLeft + index + 1 + standingUpModifier
                        }
                        UiPathFinderData(
                            coordinate = pathSquare,
                            futureMoveDistance = shownMoveValue,
                            hoverAction = action
                        )
                    }
                } else {
                    emptyList()
                }
            }
            pathList?.associate { it.coordinate to it } ?: emptyMap()
        }
    }

    fun observeSnapshot(): Flow<UiGameSnapshot>  = uiState.uiStateFlow

    fun observePitch(): Flow<Map<PitchCoordinate, Pair<UiPitchSquare, UiPitchPlayer?>>> {
        return combine(highlights, uiState.uiStateFlow) { mouseEnter, uiSnapshot ->
            uiSnapshot.squares.map {
                it.key to Pair(it.value, uiSnapshot.players[it.value.player])
            }.toMap()
        }
    }
    private fun showPathFinder(
        activePlayer: Player?,
        mouseEnter: PitchCoordinate?,
        actionProvider: ManualActionProvider?,
    ): Boolean {
        return activePlayer != null
            && mouseEnter != null
            && activePlayer.coordinates != mouseEnter
            && activePlayer.movesLeft > 0
            && rules.calculateMarks(game, activePlayer.team, activePlayer.coordinates) <= 0
            && (actionProvider != null && actionProvider.nextFumblerooskiCommand != Confirm)
    }

    fun notifyAnimationFinished() {
        uiState.notifyAnimationDone()
    }

    fun updateOffset(coordinate: PitchCoordinate, layoutCoords: LayoutCoordinates) {
        squareOffsets[coordinate] = SquareLayoutCoordinates(
            coordinate = coordinate,
            positionInRoot = layoutCoords.positionInRoot(),
            boundsInRoot = layoutCoords.boundsInRoot()
        )
    }

    fun updatePitchOffSet(fieldLayoutCoordinates: LayoutCoordinates, borderSize: Float) {
        pitchCoordinates = PitchLayoutCoordinates(fieldLayoutCoordinates.positionInRoot())
        this@PitchViewModel.borderSize = borderSize
        screenModel.updateFieldViewData(fieldLayoutCoordinates, borderSize)
    }
}
