package com.jervisffb.ui.game.view.pitch

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import com.jervisffb.ui.game.icons.IconFactory
import com.jervisffb.ui.game.viewmodel.PitchDetails

/**
 * Layer 1: Background Image Layer.
 *
 * This is the most bottom layer when rendering the pitch. For normal games
 * of Blood Bowl, this just contains the pitch. For Dungeon Bowl, it contains
 * the background images required to build up the dungeon (but not room features).
 *
 * See [Pitch] for more details about layer ordering.
 */
@Composable
fun BoxScope.BackgroundImageLayer(pitch: PitchDetails) {
    Image(
        painter = BitmapPainter(IconFactory.getPitch(pitch)),
        contentDescription = pitch.description,
        // We can live with something of the pitch being cropped for now.
        // Avoiding it across game types is hard, and if we want something
        // that also works across custom pitch sizes, it turns borderline
        // impossible.
        contentScale = ContentScale.Crop,
        alignment = Alignment.Center,
        modifier =
            Modifier
                .fillMaxSize()
                .align(Alignment.TopStart),
    )
}
