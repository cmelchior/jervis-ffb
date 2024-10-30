package com.jervisffb.ui.view

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

object JervisTheme {
    val awayTeamColor = Color(0xFF4588c4)
    val homeTeamColor = Color(0xFFcc0102)
    val fieldSquareTextStyle = TextStyle(
        color = Color.Cyan.copy(alpha = 0.75f),
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        textAlign = TextAlign.Center,
        shadow = Shadow(
            color = Color.Black.copy(alpha = 0.75f),
            offset = Offset(2f, 2f),
            blurRadius = 2f
        )
    )
}


