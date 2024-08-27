package dk.ilios.jervis.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.ContentScale
import dk.ilios.jervis.ui.images.IconFactory
import dk.ilios.jervis.ui.model.UiPlayer

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Player(
    modifier: Modifier,
    player: UiPlayer,
    parentHandleClick: Boolean,
) {
    val playerImage = remember(player) { IconFactory.getImage(player) }
    val ballImage = remember { IconFactory.getHeldBallOverlay() }

//    val backgroundColor = when {
//        player.state == PlayerState.STUNNED -> Color.White
//        // player.isSelectable -> Color.Red
//        else -> Color.Transparent
//    }

    var playerModifier = modifier.aspectRatio(1f) // .background(color = backgroundColor)
    if (player.isSelectable && !parentHandleClick) {
        playerModifier = playerModifier.clickable {
            player.selectAction!!()
        }
    }
    if (player.onHover != null) {
        playerModifier =
            playerModifier.onPointerEvent(eventType = PointerEventType.Enter) {
                player.onHover.invoke()
            }
    }

    Box(modifier = playerModifier) {
        Image(
            bitmap = playerImage,
            alpha = if (player.hasActivated) 0.5f else 1.0f,
            contentDescription = null,
            alignment = Alignment.Center,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
        )
        if (player.hasBall()) {
            Image(
                bitmap = ballImage,
                contentDescription = null,
                alignment = Alignment.Center,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
