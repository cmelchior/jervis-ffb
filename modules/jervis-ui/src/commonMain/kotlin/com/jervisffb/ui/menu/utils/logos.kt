package com.jervisffb.ui.menu.utils

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jervisffb.ui.game.view.JervisTheme

// Composable for the Jervis Logo used in dialogs
@Composable
fun JervisLogo() {
    Text(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        text = "J",
        fontFamily = JervisTheme.fontFamily(),
        color = JervisTheme.white,
        textAlign = TextAlign.Center,
        fontSize = 100.sp,
        fontWeight = FontWeight.Bold,
    )
}
