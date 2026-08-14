package com.jervisffb.ui.game.view.pitch

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.ui.game.icons.IconFactory
import com.jervisffb.ui.game.model.UiPitchPlayer
import com.jervisffb.ui.game.model.UiPitchSquare
import com.jervisffb.ui.game.view.ContextPopupMenu
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.viewmodel.PitchViewModel
import com.jervisffb.ui.game.viewmodel.UiPathFinderData
import com.jervisffb.ui.utils.jsp

/**
 * Stand-in for a coordinate that is missing from the pitch data.
 */
private val EMPTY_SQUARE = UiPitchSquare(PitchCoordinate.UNKNOWN)

/**
 * Layer 4: Pitch Actions and Underlays:
 *
 * This layer is responsible for two things:
 * 1. Actions: This layer is responsible for attaching click handlers
 *    for events that trigger when selecting a square
 * 2. Underlay: This layer controls background highlights for squares with
 *    actions, tackle zones, squares that require a roll to enter or move counters.
 *
 * See [Pitch] for more details about layer ordering.
 */
@Composable
fun PitchActionsAndUnderlaysLayers(
    vm: PitchViewModel,
) {
    val pathFinderFlow = remember { vm.observePathFinder() }
    val pitchDataFlow = remember { vm.observePitch() }
    val playerToMove by vm.screenModel.playerToMove.collectAsState(null)
    val isMovePlayersFreelyMode by vm.screenModel.isMovePlayersFreely.collectAsState()

    val pitchDataData: Map<PitchCoordinate, Pair<UiPitchSquare, UiPitchPlayer?>> by pitchDataFlow.collectAsState(emptyMap())
    val pathFinderData by pathFinderFlow.collectAsState(null)
    PitchSquares(vm) { modifier: Modifier, x, y ->
        val coordinate = PitchCoordinate(x, y)
        val squareData: UiPitchSquare? = pitchDataData[coordinate]?.first
        val playerData: UiPitchPlayer? = pitchDataData[coordinate]?.second
        SquareHighlightAndAction(
            modifier,
            vm,
            squareData ?: EMPTY_SQUARE,
            playerData,
            pathFinderData?.let { it[coordinate] },
            playerToMove?.id,
            isMovePlayersFreelyMode,
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun SquareHighlightAndAction(
    boxModifier: Modifier,
    vm: PitchViewModel,
    square: UiPitchSquare,
    player: UiPitchPlayer? = null,
    pathfinderData: UiPathFinderData?,
    playerToMove: PlayerId?,
    isMovePlayersFreelyMode: Boolean,
    fontSize: TextUnit = 16.jsp // Size of PathFinder and "Moves Used" indicators
) {
    val sharedPitchData = vm.sharedPitchData
    val bgColor = remember(square, player, playerToMove) {
        when {
            square.selectedAction != null && square.requiresRoll -> Color.Yellow.copy(alpha = 0.25f)
            // Hide square color when diretion arrows are shown
            square.selectableDirection != null || square.directionSelected != null -> Color.Transparent
            player?.isSelectable == true -> Color.Transparent
            playerToMove != null && square.isEmpty() -> JervisTheme.availableActionBackground
            square.selectedAction != null -> JervisTheme.availableActionBackground // Fallback for active squares
            else -> Color.Transparent
        }
    }

    // Setup onHover events
    val modifier = boxModifier
        .fillMaxSize()
        // Deferred read - the Action Wheel's visibility is a single piece of global state, but this
        // composable runs once per square, so reading it during composition subscribed all ~390 of
        // them and every show/hide of the wheel recomposed the whole pitch. Reading it inside
        // `drawBehind` moves the subscription to the draw phase: a toggle now repaints the squares
        // instead of recomposing them. Do not reintroduce a `by`/`.value` read in the function body.
        .drawBehind {
            if (bgColor != Color.Transparent && !sharedPitchData.isActionWheelVisible.value) {
                drawRect(bgColor)
            }
        }
        .jervisPointerEvent(SquarePointerEventType.EnterSquare, square.coordinates) {
            vm.triggerHoverEnter(square.coordinates)
        }

    // TODO Move some of this to the Dialog Layer?
    val boxWrapperModifier =
        if (playerToMove != null && (square.isEmpty() || player?.id == playerToMove) || square.contextMenuOptions.isNotEmpty() || player?.selectedAction != null || square.selectedAction != null || pathfinderData?.hoverAction != null) {
            modifier
                .jervisPointerEvent(SquarePointerEventType.PrimaryClickSquare, square.coordinates) {
                    // In that case, it is the square that should handle opening it again, after which control transfers
                    // back to the ActionWheelLayer
                    if (square.contextMenuOptions.isNotEmpty() && !vm.sharedPitchData.isActionWheelVisible.value) {
                        val menuOptions = square.createActionWheelContextMenu(vm, sharedPitchData)
                        vm.contextActionWheelViewModel.showWheel(menuOptions)
                    }

                    // Toggling the Action Wheel should take precedence over triggering square/player actions.
                    // Ideally, none should be configured anyway, but just in case.
                    if (square.contextMenuOptions.isEmpty()) {
                        if (playerToMove != null && (square.isEmpty() || player?.id == playerToMove)) {
                            vm.screenModel.movePlayerFreelyTo(square.coordinates)
                        } else if (isMovePlayersFreelyMode && playerToMove != null) {
                            // Do not select another player while choosing a destination.
                        } else pathfinderData?.hoverAction()
                            ?: player?.selectedAction?.invoke(vm.screenModel, player)
                            ?: square.selectedAction?.invoke()
                    }
                }
        } else {
            modifier
        }


    Box(
        modifier = boxWrapperModifier
            .jervisPointerEvent(SquarePointerEventType.SecondaryClickSquare, square.coordinates) {
                if (player != null) {
                    vm.triggerShowPlayerContextMenu(player.id)
                }
            }
    ) {
        // `isActionWheelVisible` is read inline rather than hoisted into a local, and the cheap
        // per-square conditions are tested first, so that `&&` short-circuits before the read
        // happens. `Box` with content is inline, so a read here lands in this composable's own
        // restart scope - hoisting it would subscribe all ~390 squares to the wheel's visibility
        // and undo the deferral above. `useActionWheel` is a constant `true`, so the branch below
        // never runs and never subscribes; only squares showing a pathfinder distance do.
        // TODO Move this to the Dialog Layer?
        if (!square.useActionWheel && sharedPitchData.isActionWheelVisible.value) {
            ContextPopupMenu(
                hidePopup = { dismissed ->
                    sharedPitchData.setContextActionWheelVisibility(false)
                    if (dismissed) {
                        square.onMenuHidden?.invoke()
                    }
                },
                commands = square.contextMenuOptions,
            )
        } else if (square.isBallExiting) {
            Box(modifier = Modifier.fillMaxSize().background(JervisTheme.ballExitColor)) {
                Image(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(2.dp)
                    ,
                    alignment = Alignment.Center,
                    contentScale = ContentScale.FillBounds,
                    bitmap = IconFactory.getBall(),
                    contentDescription = "Ball left pitch at ${square.coordinates}",
                )
            }
        } else if (pathfinderData?.futureMoveDistance != null && square.isEmpty() && !sharedPitchData.isActionWheelVisible.value) {
            val moveValue = pathfinderData.futureMoveDistance.toString()
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = moveValue,
                    fontSize = fontSize,
                    style = JervisTheme.pitchSquareTextStyle.copy(
                        color = Color.White.copy(0.75f)
                    ),
                )
            }
        } else if (square.moveUsed != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = square.moveUsed.toString(),
                    fontSize = fontSize,
                    style = JervisTheme.pitchSquareTextStyle.copy(
                        color = Color.Cyan.copy(0.75f)
                    )
                )
            }
        }
    }
}
