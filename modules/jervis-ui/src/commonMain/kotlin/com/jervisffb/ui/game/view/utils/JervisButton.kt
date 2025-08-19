package com.jervisffb.ui.game.view.utils

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.utils.applyIf
import com.jervisffb.ui.utils.onClickWithSmallDragControl

@Composable
fun JervisButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    textModifier: Modifier = Modifier,
    textUppercase: Boolean = true,
    enabled: Boolean = true,
    fillWidth: Boolean = false,
    buttonColor: Color = JervisTheme.rulebookBlue,
    textColor: Color = JervisTheme.white,
    border: BorderStroke? = null,
    shape: Shape = RoundedCornerShape(4.dp)
) {
    Button(
        modifier = modifier
            .onClickWithSmallDragControl(onClick = onClick)
            .applyIf(fillWidth) {
                fillMaxWidth()
            }
        ,
        colors = ButtonDefaults.buttonColors(containerColor = buttonColor, disabledContainerColor = JervisTheme.rulebookPaperMediumDark),
        shape = shape,
        onClick = { /* Do nothing */ },
        border = border,
        enabled = enabled,
    ) {
        Text(
            modifier = textModifier,
            text = if (textUppercase) text.uppercase() else text,
            fontSize = 14.sp,
            lineHeight = 1.em,
            fontWeight = FontWeight.Medium,
            color = if (enabled) textColor else JervisTheme.contentTextColor.copy(alpha = 0.5f),
        )
    }
}
