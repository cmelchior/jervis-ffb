package dk.ilios.jervis

import dk.ilios.jervis.model.Coach
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.PlayerNo
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.rules.roster.Position
import dk.ilios.jervis.rules.roster.Roster

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
