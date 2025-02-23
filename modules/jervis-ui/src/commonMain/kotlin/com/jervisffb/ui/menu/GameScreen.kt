package com.jervisffb.ui.menu

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.BB2020Rules
import com.jervisffb.engine.rules.StandardBB2020Rules
import com.jervisffb.fumbbl.net.adapter.FumbblReplayAdapter
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
    val rules: BB2020Rules = StandardBB2020Rules()

//    init {
//        if (injectedGameRunner != null) {
//            this.gameRunner = injectedGameRunner
//            fumbbl = null
//        } else {
//            TODO()
//            when (mode) {
//                is Manual -> {
//                    fumbbl = null
//                    homeTeam = StandaloneTeams.defaultTeams["human-starter-team.jrt"]!!.team
//                    awayTeam = StandaloneTeams.defaultTeams["lizardmen-starter-team.jrt"]!!.team
//                    TODO()
//                    this.gameRunner = HotSeatGameRunner(rules, homeTeam!!, awayTeam!!)
//                }
//
//                Random -> {
//                    fumbbl = null
//                    this.gameRunner = GameEngineController(createDefaultGameState(rules))
//                }
//
//                is Replay -> {
//                    fumbbl = FumbblReplayAdapter(mode.file, checkCommandsWhenLoading = false)
//                    fumbbl!!.loadCommands()
//                    this.gameRunner = GameEngineController(fumbbl!!.getGame())
//                }
//                else -> TODO()
//            }
//        }
//    }

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
//        val homeActionProvider =
//            when (mode) {
//                is Manual -> ManualActionProvider(menuViewModel, mode.actionMode)
//                Random -> TODO() // RandomActionProvider(uiState)
//                is Replay -> TODO() // ReplayActionProvider(uiState, fumbbl)
//            }
//
//        val awayActionProvider =
//            when (mode) {
//                is Manual -> ManualActionProvider(menuViewModel, mode.actionMode)
//                Random -> TODO() // RandomActionProvider(uiState)
//                is Replay -> TODO() // ReplayActionProvider(uiState, fumbbl)
//            }
        uiState = UiGameController(
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

class GameScreen(val screenModel: GameScreenModel) : Screen {
    override val key: ScreenKey = "GameScreen"

    @Composable
    override fun Content() {
        LoadingScreen(screenModel) {
            com.jervisffb.ui.game.view.Screen(
                FieldViewModel(
                    screenModel.uiState,
                    screenModel.hoverPlayerFlow,
                ),
                SidebarViewModel(
                    screenModel.uiState,
                    screenModel.homeTeam,
                    screenModel.hoverPlayerFlow
                ),
                SidebarViewModel(
                    screenModel.uiState,
                    screenModel.awayTeam,
                    screenModel.hoverPlayerFlow
                ),
                GameStatusViewModel(screenModel.uiState),
                if (screenModel.mode is Replay) ReplayControllerViewModel(screenModel.uiState, screenModel) else null,
                if (screenModel.mode is Random) RandomActionsControllerViewModel(screenModel.uiState, screenModel) else null,
                ActionSelectorViewModel(screenModel.uiState),
                LogViewModel(screenModel.uiState),
                DialogsViewModel(screenModel.uiState),
            )
        }
    }
}
