package dk.ilios.jervis.rules.roster

import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.PlayerId
import dk.ilios.jervis.model.PlayerNo
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.rules.skills.SkillFactory

interface Position {
    val roster: Roster
    val quantity: Int
    val position: String
    val positionSingular: String
    val cost: Int

    val move: Int
    val strenght: Int
    val agility: Int
    val armorValue: Int

    val skills: List<SkillFactory>

    fun createPlayer(
        team: Team,
        id: PlayerId,
        name: String,
        number: PlayerNo,
    ): Player
}
