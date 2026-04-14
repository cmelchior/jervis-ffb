package com.jervisffb.ui.game.view.pitch

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.ui.game.icons.IconFactory
import com.jervisffb.ui.game.model.UiPitchPlayer
import com.jervisffb.ui.game.model.UiPitchSquare
import com.jervisffb.ui.game.viewmodel.PitchViewModel
import org.jetbrains.compose.resources.painterResource

/**
 * Layer 10: Direction Arrows.
 *
 * This layer is responsible for drawing "direction" arrows like those used
 * during pushbacks.
 *
 * See [Pitch] for more details about layer ordering.
 */
@Composable
fun DirectionArrowsLayer(
    vm: PitchViewModel,
) {
    val pitchSizeData = LocalPitchData.current.size
    val pitchDataFlow = remember { vm.observePitch() }
    val pitchData: Map<PitchCoordinate, Pair<UiPitchSquare, UiPitchPlayer?>> by pitchDataFlow.collectAsState(emptyMap())

    pitchData.filter { it.value.first.hasDirectionArrow() }.forEach {
        val square = it.value.first
        val coordinate = it.key
        var isHover by remember { mutableStateOf(false) }
        Box(
            modifier = Modifier
                .jervisSquare(pitchSizeData, coordinate)
                .jervisPointerEvent(SquarePointerEventType.EnterSquare, coordinate) {
                    isHover = true
                }
                .jervisPointerEvent(SquarePointerEventType.ExitSquare, coordinate) {
                    isHover = false
                }
        ) {
            val image = when {
                square.directionSelected != null -> IconFactory.getDirection(square.directionSelected, true)
                square.selectableDirection != null -> IconFactory.getDirection(square.selectableDirection, isHover)
                else -> null
            }
            if (image != null) {
                Image(
                    modifier = Modifier.fillMaxSize(),
                    painter = painterResource(image),
                    contentDescription = null,
                )
            }
        }
    }
}

