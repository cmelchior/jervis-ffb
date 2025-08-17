package com.jervisffb.ui.menu.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.jervisffb.ui.game.view.JervisTheme


@Composable
fun JervisCircularProgressIndicator(progress: Float) {
    CircularProgressIndicator(
        progress = { progress },
        modifier = Modifier.size(60.dp),
        trackColor = JervisTheme.rulebookPaperDark.copy(alpha = 0.2f),
        color = JervisTheme.rulebookRed,
        strokeWidth = 8.dp,
        strokeCap = StrokeCap.Square,
        gapSize = 0.dp
    )
}
