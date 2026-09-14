package com.jervisffb.ui.menu.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color


@Composable
fun JervisLinearProgressIndicator(
    modifier: Modifier = Modifier.fillMaxWidth(),
) {
    LinearProgressIndicator(
        modifier = modifier,
        trackColor = Color.Transparent
    )
}
