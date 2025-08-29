package com.jervisffb.ui.game.view.field

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.ui.game.icons.IconFactory
import com.jervisffb.ui.game.model.UiFieldPlayer
import com.jervisffb.ui.game.model.UiFieldSquare
import com.jervisffb.ui.game.view.ContextPopupMenu
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.viewmodel.FieldViewModel
import com.jervisffb.ui.game.viewmodel.UiPathFinderData

/**
 * Layer 4: Field Actions and Underlays:
 *
 * This layer is responsible for two things:
 * 1. Actions: This layer is responsible for attaching click handlers
 *    for events that trigger when selecting a square
 * 2. Underlay: This layer controls background highlights for squares with
 *    actions, tackle zones, fields that require a roll to enter or move counters.
 *
 * See [Field] for more details about layer ordering.
 */
@Composable
fun FieldActionsAndUnderlaysLayers(
    vm: FieldViewModel,
) {
    val pathFinderFlow = remember { vm.observePathFinder() }
    val fieldDataFlow = remember { vm.observeField() }

    val fieldData: Map<FieldCoordinate, Pair<UiFieldSquare, UiFieldPlayer?>> by fieldDataFlow.collectAsState(emptyMap())
    val pathFinderData by pathFinderFlow.collectAsState(null)

    FieldSquares(vm) { modifier: Modifier, x, y ->
        val coordinate = FieldCoordinate(x, y)
        val squareData: UiFieldSquare? = fieldData[coordinate]?.first
        val playerData: UiFieldPlayer? = fieldData[coordinate]?.second
        SquareHighlightAndAction(
            modifier,
            vm,
            squareData ?: UiFieldSquare(FieldCoordinate.UNKNOWN),
            playerData,
            pathFinderData?.let { it[coordinate] },
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun SquareHighlightAndAction(
    boxModifier: Modifier,
    vm: FieldViewModel,
    square: UiFieldSquare,
    player: UiFieldPlayer? = null,
    pathfinderData: UiPathFinderData?,
) {
    val sharedFieldData = vm.sharedFieldData

    val bgColor = when {
        sharedFieldData.isContentMenuVisible -> Color.Transparent
        square.selectedAction != null && square.requiresRoll -> Color.Yellow.copy(alpha = 0.25f)
        // Hide square color when diretion arrows are shown
        square.selectableDirection != null || square.directionSelected != null -> Color.Transparent
        player?.isSelectable == true -> Color.Transparent
        square.selectedAction != null -> JervisTheme.availableActionBackground // Fallback for active squares
        else -> Color.Transparent
    }

    // Setup onHover events
    val modifier = boxModifier
        .fillMaxSize()
        .background(color = bgColor)
        .jervisPointerEvent(FieldPointerEventType.EnterSquare, square.coordinates) {
            vm.triggerHoverEnter(square.coordinates)
        }

    // TODO Move some of this to the Dialog Layer?
    val boxWrapperModifier =
        if (square.contextMenuOptions.isNotEmpty() || player?.selectedAction != null || square.selectedAction != null || pathfinderData?.hoverAction != null) {
            modifier.jervisPointerEvent(FieldPointerEventType.ClickSquare, square.coordinates) {
                vm.sharedFieldData.isContentMenuVisible = !vm.sharedFieldData.isContentMenuVisible
                if (square.contextMenuOptions.isEmpty()) {
                    pathfinderData?.hoverAction()
                        ?: player?.selectedAction?.invoke()
                        ?: square.selectedAction?.invoke()
                }
            }
        } else {
            modifier
        }


    Box(modifier = boxWrapperModifier) {
        // TODO Move this to the Dialog Layer
        if (sharedFieldData.isContentMenuVisible && !square.useActionWheel) {
            ContextPopupMenu(
                hidePopup = { dismissed ->
                    sharedFieldData.isContentMenuVisible = false
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
                    contentDescription = "Ball left field at ${square.coordinates}",
                )
            }
        } else if (pathfinderData?.futureMoveDistance != null && square.isEmpty()) {
            val moveValue = pathfinderData.futureMoveDistance.toString()
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = moveValue,
                    style = JervisTheme.fieldSquareTextStyle.copy(
                        color = Color.White.copy(0.75f)
                    ),
                )
            }
        } else if (square.moveUsed != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = square.moveUsed.toString(),
                    style = JervisTheme.fieldSquareTextStyle.copy(
                        color = Color.Cyan.copy(0.75f)
                    )
                )
            }
        }
    }
}
