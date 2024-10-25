package com.jervisffb.ui.view

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
import androidx.compose.ui.layout.ContentScale
import com.jervisffb.ui.icons.IconFactory
import com.jervisffb.ui.model.UiPlayer

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Player(
    modifier: Modifier,
    player: UiPlayer,
    parentHandleClick: Boolean,
) {
    val playerImage = remember(player) { IconFactory.getImage(player) }
    val ballImage = IconFactory.getHeldBallOverlay()

    var playerModifier: Modifier = modifier.aspectRatio(1f)

    if (player.isSelectable && !parentHandleClick) {
        playerModifier = playerModifier.clickable {
            player.selectAction!!()
        }
    }
//    if (player.onHover != null) {
//        playerModifier =
//            playerModifier.onPointerEvent(eventType = PointerEventType.Enter) {
//                player.onHover.invoke()
//            }
//    }
//    if (player.onHoverExit != null) {
//        playerModifier =
//            playerModifier.onPointerEvent(eventType = PointerEventType.Exit) {
//                player.onHoverExit.invoke()
//            }
//    }

    Box(modifier = playerModifier) {
        Image(
            bitmap = playerImage,
            alpha = if (player.hasActivated || player.isStunned) 0.5f else 1.0f,
            contentDescription = null,
            alignment = Alignment.Center,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
        )
        if (player.carriesBall) {
            Image(
                bitmap = ballImage,
                contentDescription = null,
                alignment = Alignment.Center,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )
        }
        if (player.isProne) {
            Image(
                bitmap = IconFactory.getProneDecoration(),
                contentDescription = null,
                alignment = Alignment.Center,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )
        }
        if (player.isStunned) {
            Image(
                bitmap = IconFactory.getStunnedDecoration(),
                contentDescription = null,
                alignment = Alignment.Center,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
