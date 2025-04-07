package com.jervisffb.ui.menu

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import com.jervisffb.ui.game.view.LoadingScreen
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

class GameScreen(val menuViewModel: MenuViewModel, val viewModel: GameScreenModel) : Screen {
    override val key: ScreenKey = "GameScreen"

    @Composable
    override fun Content() {
        JervisScreen(menuViewModel) {
            LoadingScreen(viewModel) {
                Box(
                    modifier = Modifier.fillMaxSize().stoneBackground(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    GameScreenContent(viewModel)
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
