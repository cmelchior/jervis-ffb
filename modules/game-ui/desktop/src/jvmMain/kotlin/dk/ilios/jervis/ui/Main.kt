package dk.ilios.jervis.ui

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.controller.GameController
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
import java.io.File

fun main() = application {
    val rules = BB2020Rules
    val team1: Team = teamBuilder(HumanTeam) {
        coach = Coach(CoachId("home-coach"), "HomeCoach")
        name = "HomeTeam"
        addPlayer(PlayerId("H1"), "Lineman-1-H", PlayerNo(1), HumanTeam.LINEMAN)
        addPlayer(PlayerId("H2"), "Lineman-2-H", PlayerNo(2), HumanTeam.LINEMAN)
        addPlayer(PlayerId("H3"), "Lineman-3-H", PlayerNo(3), HumanTeam.LINEMAN)
        addPlayer(PlayerId("H4"), "Lineman-4-H", PlayerNo(4), HumanTeam.LINEMAN)
        addPlayer(PlayerId("H5"), "Thrower-1-H", PlayerNo(5), HumanTeam.THROWER)
        addPlayer(PlayerId("H6"), "Catcher-1-H", PlayerNo(6), HumanTeam.CATCHER)
        addPlayer(PlayerId("H7"), "Catcher-2-H", PlayerNo(7), HumanTeam.CATCHER)
        addPlayer(PlayerId("H8"), "Blitzer-1-H", PlayerNo(8), HumanTeam.BLITZER)
        addPlayer(PlayerId("H9"), "Blitzer-2-H", PlayerNo(9), HumanTeam.BLITZER)
        addPlayer(PlayerId("H10"), "Blitzer-3-H", PlayerNo(10), HumanTeam.BLITZER)
        addPlayer(PlayerId("H11"), "Blitzer-4-H", PlayerNo(11), HumanTeam.BLITZER)
        reRolls = 4
        apothecaries = 1
    }
    val team2: Team = teamBuilder(HumanTeam) {
        coach = Coach(CoachId("away-coach"), "AwayCoach")
        name = "AwayTeam"
        addPlayer(PlayerId("A1"), "Lineman-1-A", PlayerNo(1), HumanTeam.LINEMAN)
        addPlayer(PlayerId("A2"), "Lineman-2-A", PlayerNo(2), HumanTeam.LINEMAN)
        addPlayer(PlayerId("A3"), "Lineman-3-A", PlayerNo(3), HumanTeam.LINEMAN)
        addPlayer(PlayerId("A4"), "Lineman-4-A", PlayerNo(4), HumanTeam.LINEMAN)
        addPlayer(PlayerId("A5"), "Thrower-1-A", PlayerNo(5), HumanTeam.THROWER)
        addPlayer(PlayerId("A6"), "Catcher-1-A", PlayerNo(6), HumanTeam.CATCHER)
        addPlayer(PlayerId("A7"), "Catcher-2-A", PlayerNo(7), HumanTeam.CATCHER)
        addPlayer(PlayerId("A8"), "Blitzer-1-A", PlayerNo(8), HumanTeam.BLITZER)
        addPlayer(PlayerId("A9"), "Blitzer-2-A", PlayerNo(9), HumanTeam.BLITZER)
        addPlayer(PlayerId("A10"), "Blitzer-3-A", PlayerNo(10), HumanTeam.BLITZER)
        addPlayer(PlayerId("A11"), "Blitzer-4-A", PlayerNo(11), HumanTeam.BLITZER)
        reRolls = 4
        apothecaries = 1
    }
    val field = dk.ilios.jervis.model.Field.createForRuleset(rules)
    val state = Game(team1, team2, field)
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


    val controller = GameController(rules, state)
    Window(onCloseRequest = ::exitApplication) {
        App(controller, actionRequestChannel, actionSelectedChannel)
    }
}

fun runReplayFromFile() {
    val replayFile = File("").path
//    val fumbblReplayAdapte = FumbblReplay(replayFile)

}
