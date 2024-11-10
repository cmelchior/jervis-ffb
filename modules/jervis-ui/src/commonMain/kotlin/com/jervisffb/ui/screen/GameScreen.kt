package com.jervisffb.ui.screen

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import com.jervisffb.engine.GameController
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.rules.StandardBB2020Rules
import com.jervisffb.engine.utils.createDefaultGameState
import com.jervisffb.engine.utils.lizardMenAwayTeam
import com.jervisffb.fumbbl.net.adapter.FumbblReplayAdapter
import com.jervisffb.ui.UiGameController
import com.jervisffb.ui.icons.IconFactory
import com.jervisffb.ui.state.ManualActionProvider
import com.jervisffb.ui.state.RandomActionProvider
import com.jervisffb.ui.state.ReplayActionProvider
import com.jervisffb.ui.view.Screen
import com.jervisffb.ui.viewmodel.ActionSelectorViewModel
import com.jervisffb.ui.viewmodel.DialogsViewModel
import com.jervisffb.ui.viewmodel.FieldViewModel
import com.jervisffb.ui.viewmodel.GameStatusViewModel
import com.jervisffb.ui.viewmodel.LogViewModel
import com.jervisffb.ui.viewmodel.MenuViewModel
import com.jervisffb.ui.viewmodel.RandomActionsControllerViewModel
import com.jervisffb.ui.viewmodel.ReplayControllerViewModel
import com.jervisffb.ui.viewmodel.SidebarViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow

class GameScreenModel(
    val mode: GameMode,
    val menuViewModel: MenuViewModel,
    private val injectedController: GameController? = null,
    private val actions: List<GameAction> = emptyList(),
) : ScreenModel {

    val hoverPlayerFlow = MutableSharedFlow<Player?>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    lateinit var uiState: UiGameController
    lateinit var controller: GameController
    var fumbbl: FumbblReplayAdapter? = null
    val rules: StandardBB2020Rules = StandardBB2020Rules

    suspend fun initialize() {
        if (injectedController != null) {
            this.controller = injectedController
            fumbbl = null
        } else {
            when (mode) {
                Manual -> {
                    fumbbl = null
                    this.controller = GameController(rules, createDefaultGameState(rules, awayTeam = lizardMenAwayTeam()))
                }

                Random -> {
                    fumbbl = null
                    this.controller = GameController(rules, createDefaultGameState(rules))
                }

                is Replay -> {
                    fumbbl = FumbblReplayAdapter(mode.file, checkCommandsWhenLoading = false)
                    fumbbl!!.loadCommands()
                    this.controller = GameController(rules, fumbbl!!.getGame())
                }
            }
        }

        menuViewModel.controller = this.controller
        IconFactory.initialize(controller.state.homeTeam, controller.state.awayTeam)
        uiState = UiGameController(mode, controller, menuViewModel, actions)
        val uiActionFactory =
            when (mode) {
                Manual -> ManualActionProvider(uiState, menuViewModel)
                Random -> RandomActionProvider(uiState)
                is Replay -> ReplayActionProvider(uiState, fumbbl)
            }

        // Setup references and start action listener
        menuViewModel.uiState = uiState
        uiState.startGameEventLoop(uiActionFactory)
    }
}

class GameScreen(val screenModel: GameScreenModel) : Screen {
    override val key: ScreenKey = "GameScreen"
    val controller = screenModel.controller

    @Composable
    override fun Content() {
        Screen(
            FieldViewModel(
                screenModel.uiState,
                screenModel.hoverPlayerFlow,
            ),
            SidebarViewModel(
                screenModel.uiState,
                controller.state.homeTeam,
                screenModel.hoverPlayerFlow
            ),
            SidebarViewModel(
                screenModel.uiState,
                controller.state.awayTeam,
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
