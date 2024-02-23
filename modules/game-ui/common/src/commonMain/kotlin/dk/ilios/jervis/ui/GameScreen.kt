package dk.ilios.jervis.ui

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.screen.Screen
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.fumbbl.FumbblReplayAdapter
import dk.ilios.jervis.rules.BB2020Rules
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.ui.model.*
import dk.ilios.jervis.utils.createDefaultGameState
import dk.ilios.jervis.utils.createRandomAction
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okio.Path.Companion.toOkioPath

class GameScreenModel(val mode: GameMode): ScreenModel {
    val actionRequestChannel = Channel<Pair<GameController, List<ActionDescriptor>>>(capacity = 2, onBufferOverflow = BufferOverflow.SUSPEND)
    val actionSelectedChannel = Channel<GameAction>(capacity = 2, onBufferOverflow = BufferOverflow.SUSPEND)
    val controller: GameController
    val fumbbl: FumbblReplayAdapter?
    val rules: BB2020Rules = BB2020Rules

    init {
        when(mode) {
            Manual -> {
                fumbbl = null
                controller = GameController(rules, createDefaultGameState(rules))
            }
            Random -> {
                fumbbl = null
                controller = GameController(rules, createDefaultGameState(rules))
            }
            is Replay -> {
                fumbbl = FumbblReplayAdapter(mode.file.toOkioPath()).also { adapter ->
                    runBlocking {
                        adapter.loadCommands()
                    }
                    controller = GameController(rules, adapter.getGame())
                }
            }
        }
    }
}

class GameScreen(val screenModel: GameScreenModel): Screen {
    @Composable
    override fun Content() {
//fun App(
//    controller: GameController,
//    actionRequestChannel: Channel<Pair<GameController, List<ActionDescriptor>>>,
//    actionSelectedChannel: Channel<GameAction>,
//    fumbbl: FumbblReplayAdapter? = null
//) {
    Screen(
        FieldViewModel(screenModel.controller.state.field),
        SidebarViewModel(screenModel.controller.state.homeTeam),
        SidebarViewModel(screenModel.controller.state.awayTeam),
        GameStatusViewModel(screenModel.controller),
        ReplayViewModel(screenModel.controller),
        ActionSelectorViewModel(screenModel.mode, screenModel.controller, screenModel.actionRequestChannel, screenModel.actionSelectedChannel, screenModel.fumbbl),
        LogViewModel(screenModel.controller),
    )
//}
    }
}