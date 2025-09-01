package com.jervisffb.ui.game.view.field

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import com.jervisffb.engine.model.Field
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.ui.game.UiGameSnapshot
import com.jervisffb.ui.game.icons.IconFactory
import com.jervisffb.ui.game.model.UiFieldPlayer
import com.jervisffb.ui.game.model.UiFieldSquare
import com.jervisffb.ui.game.view.Player
import com.jervisffb.ui.game.viewmodel.FieldViewModel
import com.jervisffb.ui.game.viewmodel.UiPlayerTransientData
import com.jervisffb.ui.utils.pixelSize
import org.jetbrains.compose.resources.painterResource
import kotlin.math.roundToInt

/**
 * Layer 8: Player Layer.
 *
 * This layer is responsible for placing players on the field (and not dogout) including any indicators that
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
    val fieldSizeData = LocalFieldData.current.size
    val playersFlow = remember { vm.observeSnapshot() }
    val snapshot: UiGameSnapshot? by playersFlow.collectAsState(null)
    if (snapshot == null) return
    // Needed to keep player zIndexes from creating problems on other layers
    Box(modifier = Modifier.fillMaxSize()) {
        snapshot!!.players.forEach { (id, player) ->
            if (player.location !is FieldCoordinate) return@forEach

            val playerSize = fieldSizeData.getPlayerSquareSize(player.size)
            val coordinates = player.location
            val xDiff = playerSize.width - fieldSizeData.squareSize.width
            val yDiff = playerSize.height - fieldSizeData.squareSize.height

            // We want players at the bottom-right to be above players on the top-left.
            // So we can just enumerate the zIndexes using the player coordinates.
            val zIndex = (coordinates.x + 1) * coordinates.y
            Box(
                modifier = Modifier
                    .zIndex(zIndex.toFloat())
                    .pixelSize(playerSize)
                    .offset {
                        IntOffset(
                            x = (coordinates.x * fieldSizeData.squareSize.width - xDiff / 2f).roundToInt(),
                            y = (coordinates.y * fieldSizeData.squareSize.height - yDiff / 2f).roundToInt()
                        )
                    }
            ) {
                PlayerWithIndicators(
                    Modifier,
                    snapshot!!.squares[coordinates]!!,
                    player,
                    null,  // TODO We need to also pass that in here. Not really. This is handled by the general hover channel...This design needs to be revisited
                )
            }
        }
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
            if (it.dice != 0) {
                BlockDiceIndicatorImage(it.dice)
            }
            if (it.isBlocked) {
                PlayerBlockedIndicator()
            }
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
