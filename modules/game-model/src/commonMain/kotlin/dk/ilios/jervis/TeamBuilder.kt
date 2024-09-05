package dk.ilios.jervis

import dk.ilios.jervis.model.Coach
import dk.ilios.jervis.model.PlayerId
import dk.ilios.jervis.model.PlayerNo
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.roster.Position
import dk.ilios.jervis.rules.roster.bb2020.BB2020Roster
import dk.ilios.jervis.rules.roster.bb2020.SpecialRules

private data class PlayerData(val id: PlayerId, val name: String, val number: PlayerNo, val type: Position)

class TeamBuilder(val rules: Rules, val roster: BB2020Roster) {
    private val players: MutableMap<PlayerNo, PlayerData> = mutableMapOf()
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
    val specialRules = mutableListOf<SpecialRules>()

    var apothecaries: Int = 0
        set(value) {
            if (!roster.allowApothecary && value > 0) {
                throw IllegalArgumentException("This team does not allow an apothecary")
            }
            field = value
        }

    fun addPlayer(
        id: PlayerId,
        name: String,
        number: PlayerNo,
        type: Position,
    ) {
        if (players.containsKey(number)) {
            throw IllegalArgumentException("Player with number $number already exits: ${players[number]}")
        }
        val allowedOnTeam = type.quantity
        if (players.values.count { it.type == type } == allowedOnTeam) {
            throw IllegalArgumentException("Max number of $type are already on the team.")
        }
        players[number] = PlayerData(id, name, number, type)
    }

    fun build(): Team {
        return Team(name, roster, coach!!).apply {
            this@TeamBuilder.players.forEach {
                val data: PlayerData = it.value
                add(data.type.createPlayer(
                    rules,
                    this@apply,
                    data.id,
                    data.name,
                    data.number
                ))
            }
            this.rerollsCountOnRoster = this@TeamBuilder.reRolls
            this.teamCheerleaders = this@TeamBuilder.cheerLeaders
            this.teamAssistentCoaches = this@TeamBuilder.assistentCoaches
            this.dedicatedFans = this@TeamBuilder.dedicatedFans
            this.teamValue = this@TeamBuilder.teamValue
            notifyDogoutChange()
        }
    }
}

fun teamBuilder(
    rules: Rules,
    roster: BB2020Roster,
    action: TeamBuilder.() -> Unit,
): Team {
    val builder = TeamBuilder(rules, roster)
    action(builder)
    return builder.build()
}
