package com.jervisffb.ui.utils

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jervisffb.ui.game.view.JervisTheme

/**
 * A "Jervis Scale-independent Pixel" unit, this is similar to the [sp] unit, but
 * is also scaled based on the size of the game window.
 *
 * [jsp] values scale linearly with the size of the game window. A reference
 * window of 3456x2160 pixels (Macbook Pro screen size) is used to determine the
 * scale factor. This means that at this size [jsp] == [sp].
 *
 * This allows text to scale with the size of the game window.
 */
val Float.jsp: TextUnit
    get() = JervisTheme.getScaledValue(this).sp

val Int.jsp: TextUnit
    get() = JervisTheme.getScaledValue(this.toFloat()).sp

/**
 * A "Jervis Density Pixel" unit, this is similar to the [dp] unit, but is
 * also scaled based on the size of the game window.
 *
 * [jdp] values scale linearly with the size of the game window. A reference
 * window of 3456x2160 pixels (Macbook Pro screen size) is used to determine the
 * scale factor. This means that at this size [jdp] == [dp].
 *
 * This allows UI components to scale with the size of the game window.
 */
val Float.jdp: Dp
    get() = JervisTheme.getScaledValue(this).dp

val Int.jdp: Dp
    get() = JervisTheme.getScaledValue(this.toFloat()).dp
