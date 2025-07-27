package com.jervisffb.ui.menu

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.jervisffb.engine.serialize.JervisSerialization
import com.jervisffb.ui.game.dialogs.DialogSize
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.view.JervisTheme.buttonTextColor
import com.jervisffb.ui.game.view.utils.JervisButton
import com.jervisffb.ui.menu.components.JervisDialog
import com.jervisffb.ui.menu.utils.JervisLogo
import com.jervisffb.ui.utils.saveFile

@Composable
fun ExitGameDialogComponent(viewModel: GameScreenModel, onDismissRequest: () -> Unit) {
    val navigator = LocalNavigator.currentOrThrow
    val isHotseat = viewModel.uiState.uiMode == TeamActionMode.ALL_TEAMS
    val isHost = viewModel.uiState.uiMode == TeamActionMode.HOME_TEAM
    val isDone = viewModel.uiState.gameController.stack.isEmpty()
    val dialogText = when {
        isHotseat && isDone -> "Game is over. It is safe to exit the game."
        isHotseat && !isDone -> "Game is not over. If you exit the game before saving it, all progress is lost. Are you sure you want to exit?"
        isHost && isDone -> "Game is over. It is safe to exit the game."
        !isHost && isDone -> "Game is over. It is safe to exit the game."
        else -> "Game is not over. Are you sure you want to exit?"
    }
    JervisDialog(
        title = "Exit Game?",
        icon = { JervisLogo() },
        width = DialogSize.MEDIUM,
        draggable = true,
        centerOnField = viewModel,
        backgroundScrim = true,
        content = { textFieldColors, textColor ->
            Box(
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(dialogText, color = textColor)
            }
        },
        buttons = {
            JervisButton(
                text = "Cancel",
                onClick = { onDismissRequest() },
                buttonColor = JervisTheme.rulebookBlue,
                textColor = buttonTextColor
            )
            Spacer(modifier = Modifier.weight(1f))
            JervisButton(
                text = "Save Game",
                onClick = {
                    saveFile(
                        "Save Game",
                        JervisSerialization.getGameFileName(viewModel.uiState.gameController),
                        JervisSerialization.serializeGameState(viewModel.uiState.gameController),
                    )
                },
                buttonColor = JervisTheme.rulebookBlue,
                textColor = buttonTextColor
            )
            Spacer(modifier = Modifier.width(16.dp))
            JervisButton(
                text = "Exit",
                onClick = {
                    viewModel.stopGame()
                    navigator.pop()
                },
                buttonColor = JervisTheme.rulebookBlue,
                textColor = buttonTextColor
            )
        },
        onDismissRequest = onDismissRequest,
    )
}
