package dk.ilios.jervis

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Coach
import dk.ilios.jervis.model.Field
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.PlayerNo
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.rules.BB2020Rules
import dk.ilios.jervis.rules.roster.Position
import dk.ilios.jervis.rules.roster.Roster
import dk.ilios.jervis.rules.roster.bb2020.HumanTeam
import dk.ilios.jervis.ui.App
import dk.ilios.jervis.utils.createRandomAction
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import java.lang.IllegalArgumentException

@Preview
@Composable
fun AppPreview() {
    val rules = BB2020Rules
    val team1: Team = teamBuilder(HumanTeam) {
        coach = Coach("HomeCoach")
        name = "HomeTeam"
        addPlayer("Lineman-1", PlayerNo(1), HumanTeam.LINEMAN)
        addPlayer("Lineman-2", PlayerNo(2), HumanTeam.LINEMAN)
        addPlayer("Lineman-3", PlayerNo(3), HumanTeam.LINEMAN)
        addPlayer("Lineman-4", PlayerNo(4), HumanTeam.LINEMAN)
        addPlayer("Thrower-1", PlayerNo(5), HumanTeam.THROWER)
        addPlayer("Catcher-1", PlayerNo(6), HumanTeam.CATCHER)
        addPlayer("Catcher-2", PlayerNo(7), HumanTeam.CATCHER)
        addPlayer("Blitzer-1", PlayerNo(8), HumanTeam.BLITZER)
        addPlayer("Blitzer-2", PlayerNo(9), HumanTeam.BLITZER)
        addPlayer("Blitzer-3", PlayerNo(10), HumanTeam.BLITZER)
        addPlayer("Blitzer-4", PlayerNo(11), HumanTeam.BLITZER)
        reRolls = 4
        apothecary = true
    }
    val p1 = team1
    val p2 = team1
    val field = Field.createForRuleset(rules)
    val state = Game(p1, p2, field)
    val actionRequestChannel = Channel<Pair<GameController, List<ActionDescriptor>>>(capacity = 1, onBufferOverflow = BufferOverflow.SUSPEND)
    val actionSelectedChannel = Channel<GameAction>(1, onBufferOverflow = BufferOverflow.SUSPEND)
    val actionProvider = { controller: GameController, availableActions: List<ActionDescriptor> ->
        createRandomAction(controller.state, availableActions)
    }
    val controller = GameController(rules, state, actionProvider)
    App(controller, actionRequestChannel, actionSelectedChannel)
}