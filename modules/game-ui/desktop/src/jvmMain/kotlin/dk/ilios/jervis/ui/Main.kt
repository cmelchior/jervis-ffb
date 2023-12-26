package dk.ilios.jervis.ui

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Coach
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.PlayerNo
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.rules.BB2020Rules
import dk.ilios.jervis.rules.roster.bb2020.HumanTeam
import dk.ilios.jervis.rules.roster.bb2020.HumanTeam.apothecary
import dk.ilios.jervis.teamBuilder
import dk.ilios.jervis.utils.createRandomAction
import kotlinx.coroutines.channels.Channel

fun main() = application {
    val rules = BB2020Rules
    val team1: Team = teamBuilder(HumanTeam) {
        coach = Coach("HomeCoach")
        name = "HomeTeam"
        addPlayer("Lineman-1-H", PlayerNo(1), HumanTeam.LINEMAN)
        addPlayer("Lineman-2-H", PlayerNo(2), HumanTeam.LINEMAN)
        addPlayer("Lineman-3-H", PlayerNo(3), HumanTeam.LINEMAN)
        addPlayer("Lineman-4-H", PlayerNo(4), HumanTeam.LINEMAN)
        addPlayer("Thrower-1-H", PlayerNo(5), HumanTeam.THROWER)
        addPlayer("Catcher-1-H", PlayerNo(6), HumanTeam.CATCHER)
        addPlayer("Catcher-2-H", PlayerNo(7), HumanTeam.CATCHER)
        addPlayer("Blitzer-1-H", PlayerNo(8), HumanTeam.BLITZER)
        addPlayer("Blitzer-2-H", PlayerNo(9), HumanTeam.BLITZER)
        addPlayer("Blitzer-3-H", PlayerNo(10), HumanTeam.BLITZER)
        addPlayer("Blitzer-4-H", PlayerNo(11), HumanTeam.BLITZER)
        reRolls = 4
        apothecary = true
    }
    val team2: Team = teamBuilder(HumanTeam) {
        coach = Coach("AwayCoach")
        name = "AwayTeam"
        addPlayer("Lineman-1-A", PlayerNo(1), HumanTeam.LINEMAN)
        addPlayer("Lineman-2-A", PlayerNo(2), HumanTeam.LINEMAN)
        addPlayer("Lineman-3-A", PlayerNo(3), HumanTeam.LINEMAN)
        addPlayer("Lineman-4-A", PlayerNo(4), HumanTeam.LINEMAN)
        addPlayer("Thrower-1-A", PlayerNo(5), HumanTeam.THROWER)
        addPlayer("Catcher-1-A", PlayerNo(6), HumanTeam.CATCHER)
        addPlayer("Catcher-2-A", PlayerNo(7), HumanTeam.CATCHER)
        addPlayer("Blitzer-1-A", PlayerNo(8), HumanTeam.BLITZER)
        addPlayer("Blitzer-2-A", PlayerNo(9), HumanTeam.BLITZER)
        addPlayer("Blitzer-3-A", PlayerNo(10), HumanTeam.BLITZER)
        addPlayer("Blitzer-4-A", PlayerNo(11), HumanTeam.BLITZER)
        reRolls = 4
        apothecary = true
    }
    val field = dk.ilios.jervis.model.Field.createForRuleset(rules)
    val state = Game(team1, team2, field)
    val actionRequestChannel = Channel<Pair<Game, List<ActionDescriptor>>> {  }
    val actionProvider = { state: Game, availableActions: List<ActionDescriptor> ->
        createRandomAction(state, availableActions)
    }
    val controller = GameController(rules, state, actionProvider)
    Window(onCloseRequest = ::exitApplication) {
        App(controller)
    }
}
