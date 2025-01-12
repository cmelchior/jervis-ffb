package com.jervisffb.ui.view.utils

import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.jervisffb.ui.view.JervisTheme

@Composable
fun JervisButton(text: String, onClick: () -> Unit, enabled: Boolean = true) {
    Button(
        colors = ButtonDefaults.buttonColors(backgroundColor = JervisTheme.rulebookBlue, contentColor = JervisTheme.white),
        onClick = onClick,
        enabled = enabled,
    ) {
        Text(
            text = text.uppercase()
        )
    }
}
