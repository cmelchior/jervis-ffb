package dk.ilios.jervis.ui

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.screen.Screen
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.fumbbl.FumbblReplayAdapter
import dk.ilios.jervis.rules.BB2020Rules
import dk.ilios.jervis.ui.viewmodel.*
import dk.ilios.jervis.utils.createDefaultGameState
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import okio.Path.Companion.toOkioPath

class GameScreenModel(
    val mode: GameMode,
    menuViewModel: MenuViewModel,
    controller: GameController? = null
): ScreenModel {
    val actionRequestChannel = Channel<Pair<GameController, List<ActionDescriptor>>>(capacity = 2, onBufferOverflow = BufferOverflow.SUSPEND)
    val actionSelectedChannel = Channel<GameAction>(capacity = 2, onBufferOverflow = BufferOverflow.SUSPEND)
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
        Screen(
            FieldViewModel(uiActionFactory, screenModel.controller.state.field),
            SidebarViewModel(uiActionFactory, screenModel.controller.state.homeTeam),
            SidebarViewModel(uiActionFactory, screenModel.controller.state.awayTeam),
            GameStatusViewModel(screenModel.controller),
            ReplayViewModel(screenModel.controller),
            ActionSelectorViewModel(uiActionFactory),
            LogViewModel(screenModel.controller),
            DialogsViewModel(uiActionFactory)
        )
    }
}