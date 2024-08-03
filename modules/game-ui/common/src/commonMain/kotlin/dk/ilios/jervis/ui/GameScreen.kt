package dk.ilios.jervis.ui

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.screen.Screen
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.fumbbl.FumbblReplayAdapter
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.rules.BB2020Rules
import dk.ilios.jervis.ui.viewmodel.ActionSelectorViewModel
import dk.ilios.jervis.ui.viewmodel.DialogsViewModel
import dk.ilios.jervis.ui.viewmodel.FieldViewModel
import dk.ilios.jervis.ui.viewmodel.GameStatusViewModel
import dk.ilios.jervis.ui.viewmodel.LogViewModel
import dk.ilios.jervis.ui.viewmodel.ManualModeUiActionFactory
import dk.ilios.jervis.ui.viewmodel.MenuViewModel
import dk.ilios.jervis.ui.viewmodel.RandomModeUiActionFactory
import dk.ilios.jervis.ui.viewmodel.ReplayModeUiActionFactory
import dk.ilios.jervis.ui.viewmodel.ReplayViewModel
import dk.ilios.jervis.ui.viewmodel.SidebarViewModel
import dk.ilios.jervis.utils.createDefaultGameState
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.runBlocking
import okio.Path.Companion.toOkioPath

class GameScreenModel(
    val mode: GameMode,
    val menuViewModel: MenuViewModel,
    controller: GameController? = null
): ScreenModel {
    val actionRequestChannel = Channel<Pair<GameController, List<ActionDescriptor>>>(capacity = 2, onBufferOverflow = BufferOverflow.SUSPEND)
    val actionSelectedChannel = Channel<GameAction>(capacity = Channel.Factory.RENDEZVOUS, onBufferOverflow = BufferOverflow.SUSPEND)
    val hoverPlayerFlow = MutableSharedFlow<Player?>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    val controller: GameController
    val fumbbl: FumbblReplayAdapter?
    val rules: BB2020Rules = BB2020Rules

    init {
        if (controller != null) {
            this.controller = controller
            fumbbl = null
        } else {
            when (mode) {
                Manual -> {
                    fumbbl = null
                    this.controller = GameController(rules, createDefaultGameState(rules))
                }

                Random -> {
                    fumbbl = null
                    this.controller = GameController(rules, createDefaultGameState(rules))
                }

                is Replay -> {
                    fumbbl = FumbblReplayAdapter(mode.file.toOkioPath()).also { adapter ->
                        runBlocking {
                            adapter.loadCommands()
                        }
                        this.controller = GameController(rules, adapter.getGame())
                    }
                }
            }
        }
        menuViewModel.controller = this.controller
    }
}

class GameScreen(val screenModel: GameScreenModel, private val actions: List<GameAction>): Screen {
    @Composable
    override fun Content() {
        val uiActionFactory = when(screenModel.mode) {
            Manual -> ManualModeUiActionFactory(screenModel, actions)
            Random -> RandomModeUiActionFactory(screenModel)
            is Replay -> ReplayModeUiActionFactory(screenModel)
        }
        screenModel.menuViewModel.uiActionFactory = uiActionFactory
        Screen(
            FieldViewModel(screenModel.controller, uiActionFactory, screenModel.controller.state.field, screenModel.hoverPlayerFlow),
            SidebarViewModel(uiActionFactory, screenModel.controller.state.homeTeam, screenModel.hoverPlayerFlow),
            SidebarViewModel(uiActionFactory, screenModel.controller.state.awayTeam, screenModel.hoverPlayerFlow),
            GameStatusViewModel(screenModel.controller),
            ReplayViewModel(uiActionFactory, screenModel),
            ActionSelectorViewModel(uiActionFactory),
            LogViewModel(screenModel.controller),
            DialogsViewModel(uiActionFactory)
        )
    }
}