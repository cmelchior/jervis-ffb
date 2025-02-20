package com.jervisffb.ui.game.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.jervisffb.ui.menu.GameScreenModel

@Composable
fun LoadingScreen(
    screenModel: GameScreenModel,
    content: @Composable () -> Unit,
) {
    val loadingMessage: String by screenModel.loadingMessages.collectAsState()
    val isLoaded: Boolean by screenModel.isLoaded.collectAsState()

    LaunchedEffect(Unit) {
        screenModel.initialize()
    }

    if (!isLoaded) {
        Box(
            modifier = Modifier.background(JervisTheme.white),
            contentAlignment = Alignment.BottomEnd,
        ) {
            Text(
                text = loadingMessage,
            )
        }

    } else {
        content()
    }
}

