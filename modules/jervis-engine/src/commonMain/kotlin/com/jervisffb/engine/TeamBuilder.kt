package com.jervisffb.engine

import com.jervisffb.engine.model.Coach
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.PlayerNo
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.inducements.Apothecary
import com.jervisffb.engine.model.inducements.ApothecaryType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.roster.BB2020Roster
import com.jervisffb.engine.rules.bb2020.skills.RegularTeamReroll
import com.jervisffb.engine.rules.bb2020.skills.Skill
import com.jervisffb.engine.rules.bb2020.roster.SpecialRules
import com.jervisffb.engine.rules.common.roster.Position

private data class PlayerData(
    val id: PlayerId,
    val name: String,
    val number: PlayerNo,
    val type: Position,
    val extraSkills: List<Skill> = emptyList())

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
        skills: List<Skill> = emptyList()
    ) {
        if (players.containsKey(number)) {
            throw IllegalArgumentException("Player with number $number already exits: ${players[number]}")
        }
        val allowedOnTeam = type.quantity
        if (players.values.count { it.type == type } == allowedOnTeam) {
            throw IllegalArgumentException("Max number of $type are already on the team.")
        }
        players[number] = PlayerData(id, name, number, type, skills)
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
                ).also { player ->
                    player.extraSkills.addAll(data.extraSkills)
                })
            }
            this.rerolls.addAll((0 ..<this@TeamBuilder.reRolls).map {
                RegularTeamReroll(
                    it
                )
            })
            this.teamApothecaries.addAll((0 until this@TeamBuilder.apothecaries).map { Apothecary(false, ApothecaryType.STANDARD) })
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
