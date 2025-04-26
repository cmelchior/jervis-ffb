package com.jervisffb.ui.menu

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.annotation.ExperimentalVoyagerApi
import cafe.adriel.voyager.core.lifecycle.LifecycleEffectOnce
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.jervisffb.engine.serialize.JervisSerialization
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.view.JervisTheme.buttonTextColor
import com.jervisffb.ui.game.view.LoadingScreen
import com.jervisffb.ui.game.view.utils.JervisButton
import com.jervisffb.ui.game.view.utils.stoneBackground
import com.jervisffb.ui.game.viewmodel.ActionSelectorViewModel
import com.jervisffb.ui.game.viewmodel.DialogsViewModel
import com.jervisffb.ui.game.viewmodel.FieldViewModel
import com.jervisffb.ui.game.viewmodel.GameStatusViewModel
import com.jervisffb.ui.game.viewmodel.LogViewModel
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.game.viewmodel.RandomActionsControllerViewModel
import com.jervisffb.ui.game.viewmodel.ReplayControllerViewModel
import com.jervisffb.ui.game.viewmodel.SidebarViewModel
import com.jervisffb.ui.menu.components.JervisDialog
import com.jervisffb.ui.utils.saveFile

class GameScreen(val menuViewModel: MenuViewModel, val viewModel: GameScreenModel) : Screen {
    override val key: ScreenKey = "GameScreen"

    @OptIn(ExperimentalVoyagerApi::class)
    @Composable
    override fun Content() {
        JervisScreen(menuViewModel) {
            LoadingScreen(viewModel) {
                var showExitDialog by remember { mutableStateOf(false) }
                LifecycleEffectOnce {
                    val callback = object: OnBackPress {
                        override fun onBackPressed(): Boolean {
                            showExitDialog = true
                            return true
                        }
                    }
                    BackNavigationHandler.register(callback)
                    onDispose {
                        BackNavigationHandler.unregister(callback)
                    }
                }
                Box(
                    modifier = Modifier.fillMaxSize().stoneBackground(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    val navigator = LocalNavigator.currentOrThrow
                    GameScreenContent(viewModel)
                }
                if (showExitDialog) {
                    ExitDialogComponent(viewModel, { showExitDialog = false })
                }
            }
        }
    }
}

@Composable
private fun GameScreenContent(viewModel: GameScreenModel) {
    com.jervisffb.ui.game.view.GameScreen(
        FieldViewModel(
            viewModel.uiState,
            viewModel.hoverPlayerFlow,
        ),
        SidebarViewModel(
            viewModel.menuViewModel,
            viewModel.uiState,
            viewModel.homeTeam,
            viewModel.hoverPlayerFlow
        ),
        SidebarViewModel(
            viewModel.menuViewModel,
            viewModel.uiState,
            viewModel.awayTeam,
            viewModel.hoverPlayerFlow
        ),
        GameStatusViewModel(viewModel.uiState),
        if (viewModel.mode is Replay) ReplayControllerViewModel(viewModel.uiState, viewModel) else null,
        if (viewModel.mode is Random) RandomActionsControllerViewModel(viewModel.uiState, viewModel) else null,
        ActionSelectorViewModel(viewModel.uiState),
        LogViewModel(viewModel.uiState),
        DialogsViewModel(viewModel.uiState),
    )
}

@Composable
fun ExitDialogComponent(viewModel: GameScreenModel, onDismissRequest: () -> Unit) {
    val dialogColor = JervisTheme.rulebookRed
    val textColor = JervisTheme.contentTextColor
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
        icon = {
            Text(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                text = "J",
                fontFamily = JervisTheme.fontFamily(),
                color = JervisTheme.white,
                textAlign = TextAlign.Center,
                fontSize = 100.sp,
                fontWeight = FontWeight.Bold,
            )
        },
        width = 650.dp,
        content = { textFieldColors, textColor ->
            Box(
                modifier = Modifier.weight(1f).padding(top = 8.dp, bottom = 24.dp),
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
