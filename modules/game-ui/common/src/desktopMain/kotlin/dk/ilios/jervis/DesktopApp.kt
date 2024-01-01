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

class TeamBuilder(val roster: Roster) {
    private val players: MutableMap<PlayerNo, Player> = mutableMapOf()
    var coach: Coach? = null
    var name: String = ""
    var reRolls: Int = 0
        set(value) {
            if (roster.numberOfRerolls < value || value < 0) {
                throw IllegalArgumentException("This team only allows ${roster.numberOfRerolls}, not $value")
            }
            field = value
        }
    var cheerLeaders: Int = 0
    var assistentCoaches: Int = 0
    var fanFactor: Int = 0
    var teamValue: Int = 0
    var treasury: Int = 0
    var dedicatedFans: Int = 0

    var apothecary: Boolean = false
        set(value) {
            if (!roster.apothecary && value) {
                throw IllegalArgumentException("This team does not allow an apothecary")
            }
            field = value
        }

    fun addPlayer(name: String, number: PlayerNo, type: Position) {
        val player = type.createPlayer(name, number)
        if (players.containsKey(number)) {
            throw IllegalArgumentException("Player with number $number already exits: ${players[number]}")
        }
        val allowedOnTeam = type.quantity
        if (players.values.count { it.position == type } == allowedOnTeam) {
            throw IllegalArgumentException("Max number of $type are already on the team.")
        }
        players[number] = player
    }
    fun build(): Team {
        return Team(name, roster, coach!!).apply {
            this@TeamBuilder.players.forEach {
                add(it.value)
            }
            notifyDogoutChange()
        }
    }
}

fun teamBuilder(roster: Roster, action: TeamBuilder.() -> Unit): Team {
    val builder = TeamBuilder(roster)
    action(builder)
    return builder.build()
}

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