package com.jervisffb.ui.game.view.field

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
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.ui.game.icons.IconFactory
import com.jervisffb.ui.game.model.UiFieldPlayer
import com.jervisffb.ui.game.model.UiFieldSquare
import com.jervisffb.ui.game.viewmodel.FieldViewModel
import org.jetbrains.compose.resources.painterResource

/**
 * Layer 10: Direction Arrows.
 *
 * This layer is responsible for drawing "direction" arrows like those used
 * during pushbacks.
 *
 * See [Field] for more details about layer ordering.
 */
@Composable
fun DirectionArrowsLayer(
    vm: FieldViewModel,
) {
    val fieldSizeData = LocalFieldData.current.size
    val fieldDataFlow = remember { vm.observeField() }
    val fieldData: Map<FieldCoordinate, Pair<UiFieldSquare, UiFieldPlayer?>> by fieldDataFlow.collectAsState(emptyMap())

    fieldData.filter { it.value.first.hasDirectionArrow() }.forEach {
        val square = it.value.first
        val coordinate = it.key
        var isHover by remember { mutableStateOf(false) }
        Box(
            modifier = Modifier
                .jervisSquare(fieldSizeData, coordinate)
                .jervisPointerEvent(FieldPointerEventType.EnterSquare, coordinate) {
                    isHover = true
                }
                .jervisPointerEvent(FieldPointerEventType.ExitSquare, coordinate) {
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

