package com.jervisffb.ui.game.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import com.jervisffb.ui.utils.jdp
import com.jervisffb.ui.utils.jsp

/**
 * Render a team feature using an image icon (normally inducements).
 */
@Composable
fun TeamFeature(
    value: Int?,
    icon: ImageBitmap,
    // If false, it means it has been used, but will return.
    // Inducements used that are 1-time should just disappear.
    available: Boolean = true
) {
    TeamFeature(
        value,
        {
            Image(
                modifier = Modifier, //.dropShadow(blurRadius = 4.dp),
                bitmap = icon,
                contentDescription = "",
                contentScale = ContentScale.Fit,
                filterQuality = FilterQuality.None,
            )
        },
        available
    )
}

/**
 * Render a team feature icon using a custom Composable.
 */
@Composable
fun TeamFeature(
    value: Int?,
    content: @Composable () -> Unit,
    // If false, it means it has been used, but will return.
    // Inducements used that are 1-time should just disappear.
    available: Boolean = true,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        content()
        if (value != null && value > 1) {
            Text(
                modifier = Modifier.padding(start = 6.jdp).alpha(if (available) 1f else 0.3f),
                fontSize = 22.jsp,
                text = value.toString(),
                color = Color.White,
                lineHeight = 1.em,
                fontWeight = FontWeight.Bold,
                fontFamily = JervisTheme.fontFamily(),
                style = MaterialTheme.typography.bodySmall.copy(
                    shadow = Shadow(
                        color = Color.Black,
                        offset = Offset(2f, 2f),
                    )
                )
            )
        }
    }
}
