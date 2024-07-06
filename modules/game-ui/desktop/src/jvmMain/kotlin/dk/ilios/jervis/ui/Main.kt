package dk.ilios.jervis.ui

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.fumbbl.FumbblReplayAdapter
import dk.ilios.jervis.rules.BB2020Rules
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import okio.Path.Companion.toPath
import dk.ilios.jervis.ui.viewmodel.MenuViewModel

fun main() = application {
    val rules = BB2020Rules
    val actionRequestChannel = Channel<Pair<GameController, List<ActionDescriptor>>>(capacity = 2, onBufferOverflow = BufferOverflow.SUSPEND)
    val actionSelectedChannel = Channel<GameAction>(capacity = 2, onBufferOverflow = BufferOverflow.SUSPEND)

//    val replayFromFileActionProvider = { controller: GameController, availableActions: List<ActionDescriptor> ->
//        val action: GameAction = runBlocking(Dispatchers.Default) {
//            actionRequestChannel.send(Pair(controller, availableActions))
//            actionSelectedChannel.receive()
//        }
//        action
//    }
//
//    val userActionProvider = { controller: GameController, availableActions: List<ActionDescriptor> ->
//        val action: GameAction = runBlocking {
//            actionRequestChannel.send(Pair(controller, availableActions))
//            actionSelectedChannel.receive()
//        }
//        action
//    }
//
//    val randomActionProvider = { controller: GameController, availableActions: List<ActionDescriptor> ->
//        createRandomAction(controller.state, availableActions)
//    }

    val actionProvider = { controller: GameController, availableActions: List<ActionDescriptor> ->
        val action: GameAction = runBlocking(Dispatchers.Default) {
            actionRequestChannel.send(Pair(controller, availableActions))
            actionSelectedChannel.receive()
        }
        action
    }

    val fumbbl = FumbblReplayAdapter("../../../replays/game-1624379.json".toPath())
    runBlocking {
        fumbbl.loadCommands()
    }
//    val controller = GameController(rules, fumbbl.getGame())
    val menuViewModel = MenuViewModel()
    Window(onCloseRequest = ::exitApplication) {
        WindowMenuBar(menuViewModel)
        App(menuViewModel)
    }
}
