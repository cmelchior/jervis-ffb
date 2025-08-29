package com.jervisffb.ui.game.view.field

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.ui.game.icons.IconFactory
import com.jervisffb.ui.game.model.UiFieldPlayer
import com.jervisffb.ui.game.model.UiFieldSquare
import com.jervisffb.ui.game.view.Player
import com.jervisffb.ui.game.viewmodel.FieldViewModel
import com.jervisffb.ui.game.viewmodel.UiPlayerTransientData
import org.jetbrains.compose.resources.painterResource

/**
 * Layer 8: Player Layer.
 *
 * This layer is responsible for placing players including any indicators that
 * are relevant to them, like "block dice count", "blocked" or "prone".
 *
 * Loose balls and bombs are NOT handled by this layer.
 *
 * See [Field] for more details about layer ordering.
 */
@Composable
fun PlayerLayer(
    vm: FieldViewModel,
) {
    val fieldDataFlow = remember { vm.observeField() }
    val fieldData: Map<FieldCoordinate, Pair<UiFieldSquare, UiFieldPlayer?>> by fieldDataFlow.collectAsState(emptyMap())

    // TODO Refactor this so we do not use squares, but manually place players
    FieldSquares(vm) { modifier, x, y ->
        val squareData: UiFieldSquare? = fieldData[FieldCoordinate(x, y)]?.first
        val playerData: UiFieldPlayer? = fieldData[FieldCoordinate(x, y)]?.second
        PlayerWithIndicators(
            modifier,
            squareData ?: UiFieldSquare(FieldCoordinate.UNKNOWN),
            playerData,
            null,  // TODO We need to also pass that in here. Not really. This is handled by the general hover channel...This design needs to be revisited
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun PlayerWithIndicators(
    boxModifier: Modifier,
    square: UiFieldSquare,
    player: UiFieldPlayer? = null,
    playerTransientData: UiPlayerTransientData? = null,
) {
    val modifier = boxModifier.fillMaxSize()

    Box(modifier = modifier) {
        player?.let {
            Player(
                boxModifier,
                player,
                playerTransientData,
                true,
                square.showContextMenu // && !square.useActionWheel
            )
        }
        if (square.dice != 0) {
            BlockDiceIndicatorImage(square.dice)
        }
        if (square.isBlocked) {
            PlayerBlockedIndicator()
        }
    }
}

@Composable
private fun PlayerBlockedIndicator() {
    val imageRes = IconFactory.getBlockedDecoration()
    Image(
        modifier = Modifier.fillMaxSize(),
        painter = painterResource(imageRes),
        contentDescription = null,
    )
}

/**
 * @param dice If negative, it means the defender has more strength. If positive,
 * it means the attacker has more strength.
 */
@Composable
private fun BlockDiceIndicatorImage(dice: Int) {
//    val interactionSource = remember { MutableInteractionSource() }
//    val isHovered by interactionSource.collectIsHoveredAsState()
    val imageRes = remember(dice) { IconFactory.getBlockDiceRolledIndicator(dice) }
    Image(
        modifier = Modifier.fillMaxSize() /* .hoverable(interactionSource = interactionSource) */,
        painter = painterResource(imageRes),
        // painter = if (isHovered) painterResource(imageRes) else ColorPainter(Color.Transparent),
        contentDescription = null,
    )
}
