package com.jervisffb.ui.game.view.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

/**
 * Animated trailing dots for a label describing an ongoing operation, e.g. "Waiting For Opponent".
 * The dot count cycles between 1 and [maxDots], so the label reads as in-progress rather than
 * frozen while we wait for something slow, like the game server releasing its port.
 */
@Composable
fun animatedDots(maxDots: Int = 3, intervalMs: Long = 500L): String {
    var dotCount by remember { mutableStateOf(1) }
    LaunchedEffect(maxDots, intervalMs) {
        while (true) {
            delay(intervalMs)
            dotCount = (dotCount % maxDots) + 1
        }
    }
    return ".".repeat(dotCount)
}
