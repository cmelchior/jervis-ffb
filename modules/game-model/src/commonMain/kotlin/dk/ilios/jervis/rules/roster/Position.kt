package dk.ilios.jervis.rules.roster

import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.PlayerNo
import dk.ilios.jervis.rules.skills.Skill

interface Position {
    val roster: Roster
    val quantity: Int
    val position: String
    val cost: Int

    val move: Int
    val strenght: Int
    val agility: Int
    val armorValue: Int

    val skills: List<Skill>

    fun createPlayer(name: String, number: PlayerNo): Player
}