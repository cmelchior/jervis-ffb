package dk.ilios.jervis.rules.roster.bb2020

import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.PlayerId
import dk.ilios.jervis.model.PlayerNo
import dk.ilios.jervis.rules.bb2020.Agility
import dk.ilios.jervis.rules.bb2020.BB2020SkillCategory
import dk.ilios.jervis.rules.bb2020.General
import dk.ilios.jervis.rules.bb2020.Passing
import dk.ilios.jervis.rules.bb2020.Strength
import dk.ilios.jervis.rules.roster.Position
import dk.ilios.jervis.rules.roster.Roster
import dk.ilios.jervis.rules.roster.RosterId
import dk.ilios.jervis.rules.skills.Skill

interface BB2020Roster: Roster {
    val tier: Int
    val specialRules: List<SpecialRules>
}

class BB2020Position(
    override val roster: BB2020Roster,
    override val quantity: Int,
    override val position: String,
    override val positionSingular: String,
    override val cost: Int,
    override var move: Int,
    override var strenght: Int,
    override var agility: Int,
    var passing: Int?,
    override var armorValue: Int,
    override val skills: List<Skill>,
    primary: List<BB2020SkillCategory>,
    secondary: List<BB2020SkillCategory>,
): Position {
    override fun createPlayer(id: PlayerId, name: String, number: PlayerNo): Player {
        return Player(id).apply {
            this.name = name
            this.number = number
            position = this@BB2020Position
            baseMove = position.move
            baseStrenght = position.strenght
            baseAgility = position.agility
            basePassing = this@BB2020Position.passing
            baseArmorValue = position.armorValue
            // TODO Skills etc
        }
    }

    override fun toString(): String {
        return "BB2020Position(position='$position')"
    }
}
