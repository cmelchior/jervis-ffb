package dk.ilios.jervis.rules.roster.bb2020

import dk.ilios.jervis.rules.bb2020.BB2020SkillCategory.AGILITY
import dk.ilios.jervis.rules.bb2020.BB2020SkillCategory.GENERAL
import dk.ilios.jervis.rules.bb2020.BB2020SkillCategory.MUTATIONS
import dk.ilios.jervis.rules.bb2020.BB2020SkillCategory.PASSING
import dk.ilios.jervis.rules.bb2020.BB2020SkillCategory.STRENGTH
import dk.ilios.jervis.rules.roster.RosterId
import dk.ilios.jervis.rules.skills.Frenzy
import kotlinx.serialization.Serializable

// See Spike! Journal Issue 13
@Serializable
data object KhorneTeam : BB2020Roster {
    val BLOODBORN_MARAUDER_LINEMEN =
        BB2020Position(
            KhorneTeam,
            16,
            "Bloodborn Marauder Linemen",
            "Bloodborn Marauder Lineman",
            50_000,
            6, 3, 3, 4, 8,
            listOf(Frenzy.Factory),
            listOf(GENERAL, MUTATIONS),
            listOf(AGILITY, STRENGTH),
        )
    val KHORNGORS =
        BB2020Position(
            KhorneTeam,
            4,
            "Khorngors",
            "Khorngor",
            70_000,
            6, 3, 4, 4, 9,
            emptyList(), // Horns, Juggernaut
            listOf(GENERAL, MUTATIONS, STRENGTH),
            listOf(AGILITY, PASSING),
        )
    val BLOODSEEKERS =
        BB2020Position(
            KhorneTeam,
            4,
            "Bloodseekers",
            "Bloodseeker",
            110_000,
            5, 4, 4, 6, 10,
            listOf(Frenzy.Factory),
            listOf(GENERAL, MUTATIONS, STRENGTH),
            listOf(AGILITY),
        )
    val BLOODSPAWN =
        BB2020Position(
            KhorneTeam,
            1,
            "Bloodspawn",
            "Bloodspawn",
            160_000,
            5, 5, 4, null, 9,
            listOf(Frenzy.Factory), // Claws, Frenzy, Loner(4+), Might Blow (+1), Unchanelled Fury
            listOf(MUTATIONS, STRENGTH),
            listOf(AGILITY, GENERAL),
        )
    override val id: RosterId = RosterId("jervis-khorne")
    override val tier: Int = 2
    override val specialRules: List<SpecialRules> = listOf(TeamSpecialRule.FAVOURED_OF_KHORNE)
    override val name: String = "Chaos Dwarf"
    override val numberOfRerolls: Int = 8
    override val rerollCost: Int = 60_000
    override val allowApothecary: Boolean = true
    override val positions =
        listOf(
            BLOODBORN_MARAUDER_LINEMEN,
            KHORNGORS,
            BLOODSEEKERS,
            BLOODSPAWN,
        )
}
