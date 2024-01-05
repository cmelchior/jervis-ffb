package dk.ilios.jervis.rules.roster.bb2020

import dk.ilios.jervis.rules.bb2020.Agility
import dk.ilios.jervis.rules.bb2020.General
import dk.ilios.jervis.rules.bb2020.Mutations
import dk.ilios.jervis.rules.bb2020.Passing
import dk.ilios.jervis.rules.bb2020.Strength
import dk.ilios.jervis.rules.bb2020.Traits
import dk.ilios.jervis.rules.roster.RosterId
import dk.ilios.jervis.rules.roster.bb2020.ChaosDwarfTeam.BULL_CENTAUR_BLITZERS
import dk.ilios.jervis.rules.roster.bb2020.ChaosDwarfTeam.CHAOS_DWARF_BLOCKERS
import dk.ilios.jervis.rules.roster.bb2020.ChaosDwarfTeam.ENSLAVED_MINOTAUR
import dk.ilios.jervis.rules.roster.bb2020.ChaosDwarfTeam.HOBGOBLIN_LINEMEN
import dk.ilios.jervis.rules.roster.bb2020.HumanTeam.BLITZER
import dk.ilios.jervis.rules.roster.bb2020.HumanTeam.CATCHER
import dk.ilios.jervis.rules.roster.bb2020.HumanTeam.HALFLING_HOPEFUL
import dk.ilios.jervis.rules.roster.bb2020.HumanTeam.LINEMAN
import dk.ilios.jervis.rules.roster.bb2020.HumanTeam.OGRE
import dk.ilios.jervis.rules.roster.bb2020.HumanTeam.THROWER

// See Spike! Journal Issue 13
data object KhorneTeam: BB2020Roster {
    val BLOODBORN_MARAUDER_LINEMEN = BB2020Position(
        KhorneTeam,
        16,
        "Bloodborn Marauder Linemen",
        "Bloodborn Marauder Lineman",
        50_000,
        6,3,3,4,8,
        emptyList(), // Frenzy
        listOf(General, Mutations),
        listOf(Agility, Strength)
    )
    val KHORNGORS = BB2020Position(
        KhorneTeam,
        4,
        "Khorngors",
        "Khorngor",
        70_000,
        6,3,4,4,9,
        emptyList(), // Horns, Juggernaut
        listOf(General, Mutations, Strength),
        listOf(Agility, Passing)
    )
    val BLOODSEEKERS = BB2020Position(
        KhorneTeam,
        4,
        "Bloodseekers",
        "Bloodseeker",
        110_000,
        5,4,4,6,10,
        emptyList(), // Frenzy
        listOf(General, Mutations, Strength),
        listOf(Agility)
    )
    val BLOODSPAWN = BB2020Position(
        KhorneTeam,
        1,
        "Bloodspawn",
        "Bloodspawn",
        160_000,
        5,5,4,null,9,
        emptyList(), // Claws, Frenzy, Loner(4+), Might Blow (+1), Unchanelled Fury
        listOf(Mutations, Strength),
        listOf(Agility, General)
    )
    override val id: RosterId = RosterId("jervis-khorne")
    override val tier: Int = 2
    override val specialRules: List<SpecialRules> = listOf(FavouredOfKhorne)
    override val name: String = "Chaos Dwarf"
    override val numberOfRerolls: Int = 8
    override val rerollCost: Int = 60_000
    override val allowApothecary: Boolean = true
    override val positions = listOf(
        BLOODBORN_MARAUDER_LINEMEN,
        KHORNGORS,
        BLOODSEEKERS,
        BLOODSPAWN
    )
}