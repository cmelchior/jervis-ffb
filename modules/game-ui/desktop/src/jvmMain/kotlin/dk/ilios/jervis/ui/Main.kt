package dk.ilios.jervis.ui

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.fumbbl.FumbblReplayAdapter
import dk.ilios.jervis.model.Coach
import dk.ilios.jervis.model.CoachId
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.PlayerId
import dk.ilios.jervis.model.PlayerNo
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.rules.BB2020Rules
import dk.ilios.jervis.rules.roster.bb2020.HumanTeam
import dk.ilios.jervis.teamBuilder
import dk.ilios.jervis.utils.createRandomAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import okio.Path.Companion.toPath
import java.io.File

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
    val controller = GameController(rules, fumbbl.getGame())
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
