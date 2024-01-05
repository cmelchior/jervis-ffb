package dk.ilios.jervis.rules.roster.bb2020

import dk.ilios.jervis.rules.bb2020.Agility
import dk.ilios.jervis.rules.bb2020.General
import dk.ilios.jervis.rules.bb2020.Mutations
import dk.ilios.jervis.rules.bb2020.Passing
import dk.ilios.jervis.rules.bb2020.Strength
import dk.ilios.jervis.rules.bb2020.Traits
import dk.ilios.jervis.rules.roster.RosterId
import dk.ilios.jervis.rules.roster.bb2020.HumanTeam.BLITZER
import dk.ilios.jervis.rules.roster.bb2020.HumanTeam.CATCHER
import dk.ilios.jervis.rules.roster.bb2020.HumanTeam.HALFLING_HOPEFUL
import dk.ilios.jervis.rules.roster.bb2020.HumanTeam.LINEMAN
import dk.ilios.jervis.rules.roster.bb2020.HumanTeam.OGRE
import dk.ilios.jervis.rules.roster.bb2020.HumanTeam.THROWER

// See Teams of Legend: https://www.warhammer-community.com/wp-content/uploads/2020/11/lFZy1SIuNmWvxPj1.pdf
data object ChaosDwarfTeam: BB2020Roster {
    val HOBGOBLIN_LINEMEN = BB2020Position(
        ChaosDwarfTeam,
        16,
        "Hobgoblin Linemen",
        "Hobgoblin Lineman",
        40_000,
        6,3,3,4,8,
        emptyList(),
        listOf(General),
        listOf(Agility, Strength)
    )
    val CHAOS_DWARF_BLOCKERS = BB2020Position(
        ChaosDwarfTeam,
        6,
        "Chaos Dwarf Blockers",
        "Chaos Dwarf Blocker",
        70_000,
        4,3,4,6,10,
        emptyList(), // Block, Tackle, Thick Skull
        listOf(General, Strength),
        listOf(Agility, Mutations)
    )
    val BULL_CENTAUR_BLITZERS = BB2020Position(
        ChaosDwarfTeam,
        2,
        "Bull Centaur Blitzers",
        "Bull Centaur Blitzer",
        130_000,
        6,4,4,6,10,
        emptyList(), // Sprint, Sure Feet, Thick Skull
        listOf(General, Strength),
        listOf(Agility)
    )
    val ENSLAVED_MINOTAUR = BB2020Position(
        ChaosDwarfTeam,
        1,
        "Enslaved Minotaur",
        "Enslaved Minotaur",
        150_000,
        5,5,4,0,9,
        emptyList(),
        listOf(Agility, General),
        listOf(Strength, Passing)
    )
    override val id: RosterId = RosterId("jervis-chaos-dwarf")
    override val tier: Int = 1
    // Only select one of Favoured of
    override val specialRules: List<SpecialRules> = listOf(BadlandsBrawl, WorldsEdgeSuperLeague, FavouredOfChaosUndivided, FavouredOfKhorne, FavouredOfNurgle, FavouredOfTzeentch, FavouredOfSlaanesh)
    override val name: String = "Chaos Dwarf"
    override val numberOfRerolls: Int = 8
    override val rerollCost: Int = 70_000
    override val allowApothecary: Boolean = true
    override val positions = listOf(
        HOBGOBLIN_LINEMEN,
        CHAOS_DWARF_BLOCKERS,
        BULL_CENTAUR_BLITZERS,
        ENSLAVED_MINOTAUR
    )
}