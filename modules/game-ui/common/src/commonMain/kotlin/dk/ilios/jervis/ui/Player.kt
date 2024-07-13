package dk.ilios.jervis.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import dk.ilios.jervis.model.PlayerState
import dk.ilios.jervis.ui.images.IconFactory
import dk.ilios.jervis.ui.model.UiPlayer

@Composable
fun Player(modifier: Modifier, player: UiPlayer) {
    val backgroundColor = when {
        player.state == PlayerState.STUNNED -> Color.White
        player.isSelectable -> Color.Red
        else -> Color.Transparent
    }
    Box(modifier = modifier.aspectRatio(1f).background(color = backgroundColor).clickable {
        if (player.isSelectable) {
            player.selectAction!!()
        }
    }) {
        val playerImage = remember(player) { IconFactory.getImage(player).toComposeImageBitmap() }
        val ballImage = remember(player) { IconFactory.getHeldBallOverlay().toComposeImageBitmap() }
        Image(
            bitmap = playerImage,
            contentDescription = null,
            alignment = Alignment.Center,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
        if (player.hasBall()) {
            Image(
                bitmap = ballImage,
                contentDescription = null,
                alignment = Alignment.Center,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}