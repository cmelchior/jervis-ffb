package dk.ilios.jervis.rules.roster.bb2020

import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.PlayerNo
import dk.ilios.jervis.rules.bb2020.Agility
import dk.ilios.jervis.rules.bb2020.BB2020SkillCategory
import dk.ilios.jervis.rules.bb2020.General
import dk.ilios.jervis.rules.bb2020.Passing
import dk.ilios.jervis.rules.bb2020.Strength
import dk.ilios.jervis.rules.roster.Position
import dk.ilios.jervis.rules.roster.Roster
import dk.ilios.jervis.rules.skills.Skill

interface BB2020Roster: Roster {
    val tier: Int
    val specialRules: List<RegionalSpecialRules>
}

class BB2020Position(
    override val roster: BB2020Roster,
    override val quantity: Int,
    override val position: String,
    override val cost: Int,
    override var move: Int,
    override var strenght: Int,
    override var agility: Int,
    var passing: Int,
    override var armorValue: Int,
    override val skills: List<Skill>,
    primary: List<BB2020SkillCategory>,
    secondary: List<BB2020SkillCategory>,
): Position {
    override fun createPlayer(name: String, number: PlayerNo): Player {
        return Player().apply {
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

// Page 116 in the rulebook
data object HumanTeam: BB2020Roster {

    val LINEMAN = BB2020Position(
        HumanTeam,
        16,
        "Human Lineman",
        50_000,
        6,3,3,4,9,
        emptyList(),
        listOf(General),
        listOf(Agility, Strength)
    )
    val THROWER = BB2020Position(
        HumanTeam,
        2,
        "Throwers",
        80_000,
        6,3,3,2,9,
        emptyList(),
        listOf(General, Passing),
        listOf(Agility, Strength)
    )
    val CATCHER = BB2020Position(
        HumanTeam,
        4,
        "Catchers",
        65_000,
        8,2,3,5,8,
        emptyList(),
        listOf(Agility, General),
        listOf(Strength, Passing)
    )
    val BLITZER = BB2020Position(
        HumanTeam,
        4,
        "Blitzers",
        85_000,
        7,3,3,4,9,
        emptyList(),
        listOf(General, Strength),
        listOf(Agility, Passing)
    )
    val HALFLING_HOPEFUL = BB2020Position(
        HumanTeam,
        3,
        "Halfing Hopefuls",
        30_000,
        5,2,3,4,7,
        emptyList(),
        listOf(Agility),
        listOf(General, Strength)
    )
    val OGRE = BB2020Position(
        HumanTeam,
        1,
        "Ogre",
        140_000,
        5,5,4,5,10,
        emptyList(),
        listOf(Strength),
        listOf(Agility, General)
    )

    override val tier: Int = 1
    override val specialRules: List<RegionalSpecialRules> = listOf(OldWorldClassic)
    override val name: String = "Human Team"
    override val numberOfRerolls: Int = 8
    override val apothecary: Boolean = true
    override val positions = listOf(
        LINEMAN,
        THROWER,
        CATCHER,
        BLITZER,
        HALFLING_HOPEFUL,
        OGRE
    )
}