package com.jervisffb.ui.menu

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.Rules
import com.jervisffb.fumbbl.net.adapter.FumbblReplayAdapter
import com.jervisffb.ui.SoundManager
import com.jervisffb.ui.game.UiGameController
import com.jervisffb.ui.game.icons.IconFactory
import com.jervisffb.ui.game.state.UiActionProvider
import com.jervisffb.ui.game.view.LoadingScreen
import com.jervisffb.ui.game.viewmodel.ActionSelectorViewModel
import com.jervisffb.ui.game.viewmodel.DialogsViewModel
import com.jervisffb.ui.game.viewmodel.FieldViewModel
import com.jervisffb.ui.game.viewmodel.GameStatusViewModel
import com.jervisffb.ui.game.viewmodel.LogViewModel
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.game.viewmodel.RandomActionsControllerViewModel
import com.jervisffb.ui.game.viewmodel.ReplayControllerViewModel
import com.jervisffb.ui.game.viewmodel.SidebarViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class GameScreenModel(
    private val uiMode: TeamActionMode,
    private val gameController: GameEngineController,
    val homeTeam: Team,
    var awayTeam: Team,
    val actionProvider: UiActionProvider,
    val mode: GameMode,
    val menuViewModel: MenuViewModel,
    private val actions: List<GameAction> = emptyList(),
    private val onEngineInitialized: () -> Unit = { },
) : ScreenModel {

    val hoverPlayerFlow = MutableSharedFlow<Player?>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    lateinit var uiState: UiGameController
    var fumbbl: FumbblReplayAdapter? = null
    val rules: Rules = gameController.rules
    val _loadingMessages = MutableStateFlow<String>("")
    val loadingMessages: StateFlow<String> = _loadingMessages
    val _isLoaded = MutableStateFlow<Boolean>(false)
    val isLoaded: StateFlow<Boolean> = _isLoaded

    /**
     * Initialize icons
     */
    suspend fun initialize() {
        _loadingMessages.value = "Initializing icons..."
        IconFactory.initialize(homeTeam, awayTeam)
        _loadingMessages.value = "Initializing sounds..."
        SoundManager.initialize()
        uiState = UiGameController(
            uiMode,
            gameController,
            actionProvider,
            menuViewModel,
            actions)

        // Setup references and start action listener
        menuViewModel.uiState = uiState
        uiState.startGameEventLoop()
        onEngineInitialized()
        _loadingMessages.value = ""
        _isLoaded.value = true
    }
}

class GameScreen(val viewModel: GameScreenModel) : Screen {
    override val key: ScreenKey = "GameScreen"

    @Composable
    override fun Content() {
        LoadingScreen(viewModel) {
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
    }
}
