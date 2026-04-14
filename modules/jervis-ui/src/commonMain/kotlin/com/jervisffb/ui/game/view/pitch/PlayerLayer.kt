package com.jervisffb.ui.game.view.pitch

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
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import com.jervisffb.engine.model.Pitch
import com.jervisffb.engine.model.PlayerSize
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.jervis_ui.generated.resources.Res
import com.jervisffb.jervis_ui.generated.resources.icons_game_humanref1
import com.jervisffb.ui.game.UiGameSnapshot
import com.jervisffb.ui.game.icons.IconFactory
import com.jervisffb.ui.game.model.UiPitchPlayer
import com.jervisffb.ui.game.model.UiPitchSquare
import com.jervisffb.ui.game.view.Player
import com.jervisffb.ui.game.view.PlayerImage
import com.jervisffb.ui.game.viewmodel.PitchViewModel
import com.jervisffb.ui.game.viewmodel.UiPlayerTransientData
import com.jervisffb.ui.menu.GameScreenModel
import com.jervisffb.ui.utils.pixelSize
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.compose.resources.painterResource
import kotlin.math.roundToInt

/**
 * Layer 8: Player Layer.
 *
 * This layer is responsible for placing players on the pitch (and not dogout) including any indicators that
 * are relevant to them, like "block dice count", "blocked" or "prone".
 *
 * Loose balls and bombs are NOT handled by this layer.
 *
 * See [Pitch] for more details about layer ordering.
 */
@Composable
fun PlayerLayer(
    vm: PitchViewModel,
) {
    val pitchSizeData = LocalPitchData.current.size
    val playersFlow = remember { vm.observeSnapshot() }
    val snapshot: UiGameSnapshot? by playersFlow.collectAsState(null)
    if (snapshot == null) return
    // Needed to keep player zIndexes from creating problems on other layers
    Box(modifier = Modifier.fillMaxSize()) {
        snapshot!!.players.forEach { (id, player) ->
            if (player.location !is PitchCoordinate) return@forEach

            val playerSize = pitchSizeData.getPlayerSquareSize(player.size)
            val coordinates = player.location
            val xDiff = playerSize.width - pitchSizeData.squareSize.width
            val yDiff = playerSize.height - pitchSizeData.squareSize.height

            // We want players at the bottom-right to be above players on the top-left.
            // So we can just enumerate the zIndexes using the player coordinates.
            val zIndex = (coordinates.x + 1) * coordinates.y
            Box(
                modifier = Modifier
                    .zIndex(zIndex.toFloat())
                    .pixelSize(playerSize)
                    .offset {
                        IntOffset(
                            x = (coordinates.x * pitchSizeData.squareSize.width - xDiff / 2f).roundToInt(),
                            y = (coordinates.y * pitchSizeData.squareSize.height - yDiff / 2f).roundToInt()
                        )
                    }
            ) {
                if (coordinates.isOnPitch(snapshot!!.game.rules)) {
                    PlayerWithIndicators(
                        vm.screenModel,
                        Modifier,
                        snapshot!!.squares[coordinates]!!,
                        player,
                        null,  // TODO We need to also pass that in here. Not really. This is handled by the general hover channel...This design needs to be revisited
                    )
                }
            }
        }

        if (snapshot?.showReferee == true) {
            val refereeCoordinates = snapshot?.refereeCoordinates
            val refereeSize = pitchSizeData.getPlayerSquareSize(PlayerSize.STANDARD)
            val xDiff = refereeSize.width - pitchSizeData.squareSize.width
            val yDiff = refereeSize.height - pitchSizeData.squareSize.height
            val rules = vm.game.rules
            val (xOffset, yOffset) = when (refereeCoordinates != null) {
                true -> {
                    // Referee is placed inside a field coordinate
                    val x = (refereeCoordinates.x * pitchSizeData.squareSize.width - xDiff / 2f).roundToInt()
                    val y = (refereeCoordinates.y * pitchSizeData.squareSize.height - yDiff / 2f).roundToInt()
                    Pair(x, y)
                }
                false -> {
                    // Referee is place in the midle of the field, which doesn't match square coordinates
                    val x = (rules.pitchWidth / 2 * pitchSizeData.squareSize.width - pitchSizeData.squareSize.width/2f).roundToInt()
                    val y = (rules.pitchHeight / 2 * pitchSizeData.squareSize.height - yDiff / 2f).roundToInt()
                    Pair(x, y)
                }
            }
            Box(
                modifier = Modifier
                    .pixelSize(refereeSize)
                    .offset {
                        IntOffset(
                            x = xOffset,
                            y = yOffset,
                        )
                    }
            ) {
                PlayerImage(
                    bitmap = imageResource(Res.drawable.icons_game_humanref1),
                    isSelectable = false,
                    isTempSelected = false,
                    isActionWheelFocus = false,
                    isGoingDown = false,
                    alpha = 1f,
                    filterQuality = FilterQuality.None,
                )
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun PlayerWithIndicators(
    screenModel: GameScreenModel,
    boxModifier: Modifier,
    square: UiPitchSquare,
    player: UiPitchPlayer? = null,
    playerTransientData: UiPlayerTransientData? = null,
) {
    val modifier = boxModifier.fillMaxSize()

    Box(modifier = modifier) {
        player?.let {
            Player(
                boxModifier,
                screenModel,
                player,
                playerTransientData,
                true,
                square.showContextMenu
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
