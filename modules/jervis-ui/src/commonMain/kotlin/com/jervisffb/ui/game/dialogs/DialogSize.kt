package com.jervisffb.ui.game.dialogs

import androidx.compose.ui.unit.dp
import com.jervisffb.ui.game.view.JervisTheme

/**
 * Standard sizes for [com.jervisffb.ui.menu.components.JervisDialog]
 */
object DialogSize {
    val SMALL = 450.dp
    val MEDIUM = 650.dp
    val LARGE = 850.dp
    val D6_SELECTOR = 514.dp // 24 + 130 + 24 + 32 + (6*48 + 5*8)
    val D8_SELECTOR = 626.dp // 24 + 130 + 24 + 32 + (6*48 + 5*8)
    val P90 = JervisTheme.windowSizeDp.width * 0.9f // 90% of the current screen size

}

