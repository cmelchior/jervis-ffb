package com.jervisffb.ui.game.view.field

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import com.jervisffb.ui.game.icons.IconFactory
import com.jervisffb.ui.game.viewmodel.FieldDetails

/**
 * Layer 1: Background Image Layer.
 *
 * This is the most bottom layer when rendering the field. For normal games
 * of Blood Bowl, this just contains the field. For Dungeon Bowl, it contains
 * the background images required to build up the dungeon (but not room features).
 *
 * See [Field] for more details about layer ordering.
 */
@Composable
fun BoxScope.BackgroundImageLayer(field: FieldDetails) {
    Image(
        painter = BitmapPainter(IconFactory.getField(field)),
        contentDescription = field.description,
        // We can live with something of the field being cropped for now.
        // Avoiding it across game types is hard, and if we want something
        // that also works across custom field sizes, it turns borderline
        // impossible.
        contentScale = ContentScale.Crop,
        alignment = Alignment.Center,
        modifier =
            Modifier
                .fillMaxSize()
                .align(Alignment.TopStart),
    )
}
