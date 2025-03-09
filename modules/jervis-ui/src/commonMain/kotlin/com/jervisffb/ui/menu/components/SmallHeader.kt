package com.jervisffb.ui.menu.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.jervisffb.ui.game.view.JervisTheme

@Composable
fun SmallHeader(title: String, topPadding: Dp = 0.dp, bottomPadding: Dp = 0.dp) {
    Text(
        modifier = Modifier.fillMaxWidth().padding(bottom = bottomPadding, top = topPadding).background(JervisTheme.rulebookRed).padding(4.dp),
        text = title.uppercase(),
        fontSize = 14.sp,
        lineHeight = 1.0.em,
        fontWeight = FontWeight.Medium,
        color = JervisTheme.white,
    )
}
