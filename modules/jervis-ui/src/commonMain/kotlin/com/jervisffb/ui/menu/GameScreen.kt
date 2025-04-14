package com.jervisffb.ui.menu

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import com.jervisffb.ui.game.view.utils.TitleBorder
import com.jervisffb.ui.game.view.utils.bannerBackground
import com.jervisffb.ui.game.view.utils.paperBackground
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
fun ExitDialogComponent(viewModel: GameScreenModel, onDismiss: () -> Unit) {
    val dialogColor = JervisTheme.rulebookRed
    val textColor = JervisTheme.contentTextColor
    val navigator = LocalNavigator.currentOrThrow
    val isHotseat = viewModel.uiState.uiMode == TeamActionMode.ALL_TEAMS
    val isHost = viewModel.uiState.uiMode == TeamActionMode.HOME_TEAM
    val isDone = viewModel.uiState.gameController.stack.isEmpty()
    val dialogText = when {
        isHotseat && isDone -> "Game is over. It is safe to exit the game."
        isHotseat && !isDone -> "Game is not over. Exiting the game without saving it first means it is lost."
        isHost && isDone -> "Game is over. It is safe to exit the game."
        !isHost && isDone -> "Game is over. It is safe to exit the game."
        else -> "Game is not over. Are you sure you want to exit?"
    }
    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        ),
    ) {
        Surface(
            elevation = 8.dp,
            modifier = Modifier
                .width(650.dp)
                .defaultMinSize(minHeight = 200.dp, minWidth = 650.dp)
                .paperBackground(color = JervisTheme.rulebookPaper)
            ,
            shape = MaterialTheme.shapes.medium,
            border = BorderStroke(8.dp, color = dialogColor),
            color = JervisTheme.rulebookPaper,
            contentColor = textColor,
        ) {
            Row(Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .paperBackground(JervisTheme.rulebookPaper)
            ) {
                Box(modifier = Modifier
                    .width(130.dp)
                    .fillMaxHeight()
                    .padding(start = 24.dp)
                    .bannerBackground()

                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        text = "",
                        fontFamily = JervisTheme.fontFamily(),
                        color = JervisTheme.white,
                        textAlign = TextAlign.Center,
                        fontSize = 100.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Column(modifier = Modifier
                    .padding(start  = 24.dp, top = 16.dp, end = 24.dp, bottom = 16.dp)
                    .wrapContentHeight()
                ) {
                    ExitDialogContent(
                        dialogText,
                        dialogColor,
                        textColor,
                        onCancel = {
                            onDismiss()
                        },
                        onSave = {
                            saveFile(
                                "Save Game",
                                JervisSerialization.getGameFileName(viewModel.uiState.gameController),
                                JervisSerialization.serializeGameState(viewModel.uiState.gameController),
                            )
                        },
                        onExit = {
                            navigator.pop()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.ExitDialogContent(
    dialogText: String,
    dialogColor: Color,
    textColor: Color,
    onCancel: () -> Unit = {},
    onSave: () -> Unit = {},
    onExit: () -> Boolean,
) {
    ExitDialogHeader("Exit Game?", dialogColor)
    TitleBorder(dialogColor)
    Box(
        modifier = Modifier.weight(1f).padding(top = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(dialogText, color = textColor)
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        JervisButton(
            text = "Cancel",
            onClick = { onCancel() },
            buttonColor = JervisTheme.rulebookBlue,
            textColor = buttonTextColor
        )
        Spacer(modifier = Modifier.weight(1f))
        JervisButton(
            text = "Save Game",
            onClick = { onSave() },
            buttonColor = JervisTheme.rulebookBlue,
            textColor = buttonTextColor
        )
        Spacer(modifier = Modifier.width(16.dp))
        JervisButton(
            text = "Exit",
            onClick = { onExit() },
            buttonColor = JervisTheme.rulebookBlue,
            textColor = buttonTextColor
        )
    }
}

@Composable
private fun ColumnScope.ExitDialogHeader(title: String, dialogColor: Color) {
    Box(
        modifier = Modifier.height(36.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            modifier = Modifier.padding(bottom = 2.dp),
            text = title.uppercase(),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = dialogColor
        )
    }
}

