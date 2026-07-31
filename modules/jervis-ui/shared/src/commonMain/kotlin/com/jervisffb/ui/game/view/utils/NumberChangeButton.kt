package com.jervisffb.ui.game.view.utils

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.view.JervisTheme.rulebookBlue
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * Button used to control number changes. Either up (+) or down (-). This is
 * used when buying inducements or editing player stats.
 */
@Composable
fun NumberChangeButton(
    icon: DrawableResource,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    buttonColor: Color = rulebookBlue,
    shape: Shape = RoundedCornerShape(4.dp),
) {
//    Button(
//        modifier = modifier
//            .size(48.dp)
//            .alpha(if (enabled) 1f else 0.1f)
//            .applyIf(enabled) {
//                onClickWithSmallDragControl(onClick = onClick)
//            }
//        ,
//        colors = ButtonDefaults.buttonColors(containerColor = buttonColor, disabledContainerColor = buttonColor),
//        contentPadding = PaddingValues(0.dp),
//        shape = shape,
//        onClick = { /* Do nothing */ },
//        enabled = enabled,
//    ) {
    Image(
        modifier = Modifier
            .padding(12.dp)
            .size(36.dp)
            .clickable(onClick = onClick)
        ,
        painter = painterResource(icon),
        contentDescription = description,
        contentScale = ContentScale.FillBounds,
        colorFilter = ColorFilter.tint(JervisTheme.white),
    )
//    }
}
