package com.jervisffb.ui.menu.utils

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.menu.components.JervisTooltipArea
import com.jervisffb.ui.menu.components.JervisTooltipPlacement
import com.jervisffb.ui.utils.applyIf

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
            // The default font is Noto Sans Symbols 2, which include the widest support for symbols
            // However, numbers in this font look slightly off when combined with other latin letters.
            // So in these cases, we fall back to Noto Sans Symbols.
            val useSymbols2 = remember(tooltip) {
                fun String.containsNonLetterAscii(): Boolean = any { it.code < 128 && it !in 'A'..'Z' && it !in 'a'..'z' }
                !tooltip.containsNonLetterAscii()
            }

            Surface(
                tonalElevation = 4.dp,
                shadowElevation = 4.dp,
                shape = RoundedCornerShape(8.dp),
                color = JervisTheme.white.copy(alpha = 0.95f),
            ) {
                Text(
                    text = tooltip,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .applyIf(useSymbols2) {
                            padding(top = 5.dp, bottom = 3.dp)
                        }
                    ,
                    lineHeight = 1.em,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = when (useSymbols2) {
                            true -> JervisTheme.extendedDefaultFontFamily()
                            false -> JervisTheme.defaultFontFamily()
                        }
                    )
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
