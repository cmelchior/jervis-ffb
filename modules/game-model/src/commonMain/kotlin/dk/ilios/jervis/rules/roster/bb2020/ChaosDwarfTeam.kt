package dk.ilios.jervis.rules.roster.bb2020

import dk.ilios.jervis.rules.bb2020.BB2020SkillCategory.AGILITY
import dk.ilios.jervis.rules.bb2020.BB2020SkillCategory.GENERAL
import dk.ilios.jervis.rules.bb2020.BB2020SkillCategory.MUTATIONS
import dk.ilios.jervis.rules.bb2020.BB2020SkillCategory.PASSING
import dk.ilios.jervis.rules.bb2020.BB2020SkillCategory.STRENGTH
import dk.ilios.jervis.rules.roster.RosterId
import kotlinx.serialization.Serializable

// See Teams of Legend: https://www.warhammer-community.com/wp-content/uploads/2020/11/lFZy1SIuNmWvxPj1.pdf
@Serializable
data object ChaosDwarfTeam : BB2020Roster {
    val HOBGOBLIN_LINEMEN =
        BB2020Position(
            ChaosDwarfTeam,
            16,
            "Hobgoblin Linemen",
            "Hobgoblin Lineman",
            40_000,
            6, 3, 3, 4, 8,
            emptyList(),
            listOf(GENERAL),
            listOf(AGILITY, STRENGTH),
        )
    val CHAOS_DWARF_BLOCKERS =
        BB2020Position(
            ChaosDwarfTeam,
            6,
            "Chaos Dwarf Blockers",
            "Chaos Dwarf Blocker",
            70_000,
            4, 3, 4, 6, 10,
            emptyList(), // Block, Tackle, Thick Skull
            listOf(GENERAL, STRENGTH),
            listOf(AGILITY, MUTATIONS),
        )
    val BULL_CENTAUR_BLITZERS =
        BB2020Position(
            ChaosDwarfTeam,
            2,
            "Bull Centaur Blitzers",
            "Bull Centaur Blitzer",
            130_000,
            6, 4, 4, 6, 10,
            emptyList(), // Sprint, Sure Feet, Thick Skull
            listOf(GENERAL, STRENGTH),
            listOf(AGILITY),
        )
    val ENSLAVED_MINOTAUR =
        BB2020Position(
            ChaosDwarfTeam,
            1,
            "Enslaved Minotaur",
            "Enslaved Minotaur",
            150_000,
            5, 5, 4, 0, 9,
            emptyList(),
            listOf(AGILITY, GENERAL),
            listOf(STRENGTH, PASSING),
        )
    override val id: RosterId = RosterId("jervis-chaos-dwarf")
    override val tier: Int = 1

    // Only select one of Favoured of
    override val specialRules: List<SpecialRules> =
        listOf(
            RegionalSpecialRule.BADLANDS_BRAWL,
            RegionalSpecialRule.WORLDS_EDGE_SUPERLEAGUE,
            TeamSpecialRule.FAVOURED_OF_CHAOS_UNDIVIDED,
            TeamSpecialRule.FAVOURED_OF_KHORNE,
            TeamSpecialRule.FAVOURED_OF_NURGLE,
            TeamSpecialRule.FAVOURED_OF_TZEENTCH,
            TeamSpecialRule.FAVOURED_OF_SLAANESH,
        )
    override val name: String = "Chaos Dwarf"
    override val numberOfRerolls: Int = 8
    override val rerollCost: Int = 70_000
    override val allowApothecary: Boolean = true
    override val positions =
        listOf(
            HOBGOBLIN_LINEMEN,
            CHAOS_DWARF_BLOCKERS,
            BULL_CENTAUR_BLITZERS,
            ENSLAVED_MINOTAUR,
        )
}
