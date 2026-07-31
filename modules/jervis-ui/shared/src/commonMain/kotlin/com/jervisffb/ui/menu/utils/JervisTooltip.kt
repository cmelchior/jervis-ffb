package com.jervisffb.ui.menu.utils

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.menu.components.JervisTooltipArea
import com.jervisffb.ui.menu.components.JervisTooltipPlacement

/**
 * Default implementation of tooltips across all menus in Jervis.
 */
@Composable
fun JervisTooltip(
    // The string to display in the tooltip.
    tooltip: String,
    // Modifier to apply to the tooltip area.
    modifier: Modifier = Modifier,
    // The content that will show the tooltip when hovered over.
    owner: @Composable () -> Unit,
) {
    @OptIn(ExperimentalFoundationApi::class)
    JervisTooltipArea(
        tooltip = {
            Surface(
                tonalElevation = 4.dp,
                shadowElevation = 4.dp,
                shape = RoundedCornerShape(8.dp),
                color = JervisTheme.white.copy(alpha = 0.95f),
            ) {
                Text(
                    text = tooltip,
                    Modifier.padding(horizontal = 8.dp).padding(top = 5.dp, bottom = 3.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = JervisTheme.extendedDefaultFontFamily(),
//                        baselineShift = BaselineShift(0.05f)
                    ),
                )
            }
        },
        modifier = modifier,
        delayMillis = 100,
        tooltipPlacement = JervisTooltipPlacement.CursorPoint(offset = DpOffset((-16).dp, 16.dp))
    ) {
        owner()
    }
}
