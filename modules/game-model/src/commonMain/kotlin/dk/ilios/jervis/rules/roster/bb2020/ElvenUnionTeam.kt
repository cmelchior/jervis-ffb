package dk.ilios.jervis.rules.roster.bb2020

import dk.ilios.jervis.rules.bb2020.Agility
import dk.ilios.jervis.rules.bb2020.General
import dk.ilios.jervis.rules.bb2020.Passing
import dk.ilios.jervis.rules.bb2020.Strength
import dk.ilios.jervis.rules.roster.RosterId
import dk.ilios.jervis.rules.skills.CatchSkill
import kotlinx.serialization.Serializable

@Serializable
data object ElvenUnionTeam : BB2020Roster {
    val LINEMAN =
        BB2020Position(
            ElvenUnionTeam,
            12,
            "Lineman",
            "Lineman",
            60_000,
            6, 3, 2, 4, 8,
            emptyList(),
            listOf(Agility, General),
            listOf(Strength),
        )
    val THROWER =
        BB2020Position(
            ElvenUnionTeam,
            2,
            "Throwers",
            "Thrower",
            75_000,
            6, 3, 2, 2, 8,
            listOf(/* Pass */),
            listOf(Agility, General, Passing),
            listOf(Strength),
        )
    val CATCHER =
        BB2020Position(
            ElvenUnionTeam,
            4,
            "Catchers",
            "Catcher",
            100_000,
            8, 3, 3, 4, 8,
            listOf(CatchSkill.Factory, /* Nerves Of Steel */),
            listOf(Agility, General),
            listOf(Strength),
        )
    val BLITZER =
        BB2020Position(
            HumanTeam,
            2,
            "Blitzers",
            "Blitzer",
            115_000,
            7, 3, 2, 3, 9,
            emptyList(),
            listOf(General, Strength),
            listOf(Agility, Passing),
        )
    override val id: RosterId = RosterId("jervis-elvish-union")
    override val tier: Int = 2
    override val specialRules: List<SpecialRules> = emptyList()
    override val name: String = "Elven Union"
    override val numberOfRerolls: Int = 8
    override val rerollCost: Int = 50_000
    override val allowApothecary: Boolean = true
    override val positions =
        listOf(
            LINEMAN,
            THROWER,
            CATCHER,
            BLITZER
        )
}
