package dk.ilios.jervis.rules.roster.bb2020

import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.PlayerId
import dk.ilios.jervis.model.PlayerNo
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.rules.bb2020.BB2020SkillCategory
import dk.ilios.jervis.rules.roster.Position
import dk.ilios.jervis.rules.roster.Roster
import dk.ilios.jervis.rules.skills.SkillFactory
import kotlinx.serialization.Serializable

interface BB2020Roster : Roster {
    val tier: Int
    val specialRules: List<SpecialRules>
    override val positions: List<BB2020Position>
}

@Serializable
data class BB2020Position(
    override val roster: BB2020Roster,
    override val quantity: Int,
    override val position: String,
    override val positionSingular: String,
    override val cost: Int,
    override val move: Int,
    override val strenght: Int,
    override val agility: Int,
    val passing: Int?,
    override var armorValue: Int,
    override val skills: List<SkillFactory>,
    val primary: List<BB2020SkillCategory>,
    val secondary: List<BB2020SkillCategory>,
) : Position {
    override fun createPlayer(
        team: Team,
        id: PlayerId,
        name: String,
        number: PlayerNo,
    ): Player {
        return Player(id, this).apply {
            this.team = team
            this.name = name
            this.number = number
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
