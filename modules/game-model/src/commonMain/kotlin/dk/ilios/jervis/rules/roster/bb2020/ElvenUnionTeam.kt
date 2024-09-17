package dk.ilios.jervis.rules.roster.bb2020

import dk.ilios.jervis.rules.bb2020.BB2020SkillCategory.AGILITY
import dk.ilios.jervis.rules.bb2020.BB2020SkillCategory.GENERAL
import dk.ilios.jervis.rules.bb2020.BB2020SkillCategory.PASSING
import dk.ilios.jervis.rules.roster.RosterId
import dk.ilios.jervis.rules.skills.Block
import dk.ilios.jervis.rules.skills.CatchSkill
import dk.ilios.jervis.rules.skills.Pass
import dk.ilios.jervis.rules.skills.SideStep
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
            listOf(AGILITY, GENERAL),
            listOf(GENERAL),
        )
    val THROWER =
        BB2020Position(
            ElvenUnionTeam,
            2,
            "Throwers",
            "Thrower",
            75_000,
            6, 3, 2, 2, 8,
            listOf(Pass.Factory),
            listOf(AGILITY, GENERAL, PASSING),
            listOf(GENERAL),
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
            listOf(AGILITY, GENERAL),
            listOf(GENERAL),
        )
    val BLITZER =
        BB2020Position(
            HumanTeam,
            2,
            "Blitzers",
            "Blitzer",
            115_000,
            7, 3, 2, 3, 9,
            listOf(Block.Factory, SideStep.Factory),
            listOf(GENERAL, GENERAL),
            listOf(AGILITY, PASSING),
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
