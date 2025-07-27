package com.jervisffb.ui.game.view.utils

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.jervisffb.ui.game.view.JervisTheme

@Composable
fun JervisButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    fillWidth: Boolean = false,
    buttonColor: Color = JervisTheme.rulebookBlue,
    textColor: Color = JervisTheme.white
) {
    Button(
        modifier = if (fillWidth) modifier.fillMaxWidth() else modifier,
        colors = ButtonDefaults.buttonColors(backgroundColor = buttonColor, disabledBackgroundColor = JervisTheme.rulebookPaperMediumDark),
        onClick = onClick,
        enabled = enabled,
    ) {
        Text(
            text = text.uppercase(),
            fontSize = 14.sp,
            lineHeight = 1.em,
            fontWeight = FontWeight.Medium,
            color = if (enabled) textColor else JervisTheme.contentTextColor.copy(alpha = 0.5f),
        )
    }
}
