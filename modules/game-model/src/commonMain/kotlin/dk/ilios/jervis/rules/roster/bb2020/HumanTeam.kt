package dk.ilios.jervis.rules.roster.bb2020

import dk.ilios.jervis.rules.bb2020.Agility
import dk.ilios.jervis.rules.bb2020.General
import dk.ilios.jervis.rules.bb2020.Passing
import dk.ilios.jervis.rules.bb2020.Strength
import dk.ilios.jervis.rules.roster.RosterId
import kotlinx.serialization.Serializable

// Page 116 in the rulebook
@Serializable
data object HumanTeam: BB2020Roster {
    val LINEMAN = BB2020Position(
        HumanTeam,
        16,
        "Human Lineman",
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
        "Thrower",
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
        "Catcher",
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
        "Blitzer",
        85_000,
        7,3,3,4,9,
        emptyList(),
        listOf(General, Strength),
        listOf(Agility, Passing)
    )
    val HALFLING_HOPEFUL = BB2020Position(
        HumanTeam,
        3,
        "Halfling Hopefuls",
        "Halfling Hopeful",
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
        "Ogre",
        140_000,
        5,5,4,5,10,
        emptyList(),
        listOf(Strength),
        listOf(Agility, General)
    )
    override val id: RosterId = RosterId("jervis-human")
    override val tier: Int = 1
    override val specialRules: List<SpecialRules> = listOf(OldWorldClassic)
    override val name: String = "Human Team"
    override val numberOfRerolls: Int = 8
    override val rerollCost: Int = 50_000
    override val allowApothecary: Boolean = true
    override val positions = listOf(
        LINEMAN,
        THROWER,
        CATCHER,
        BLITZER,
        HALFLING_HOPEFUL,
        OGRE
    )
}