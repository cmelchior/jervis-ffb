package com.jervisffb.ui.view.utils

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.jervisffb.ui.view.JervisTheme


@Composable
fun TitleBorder(color: Color = JervisTheme.rulebookRed, alpha: Float = 1f) {
    Divider(
        modifier = Modifier
            .alpha(alpha)
            .wrapContentWidth()
            .height(3.dp)
        ,
        color = color
    )
}

@Composable
fun OrangeTitleBorder(alpha: Float = 1f) {
    TitleBorder(JervisTheme.rulebookOrange, alpha)
}
