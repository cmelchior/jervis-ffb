package dk.ilios.jervis.rules.roster.bb2020

import dk.ilios.jervis.rules.bb2020.Agility
import dk.ilios.jervis.rules.bb2020.General
import dk.ilios.jervis.rules.bb2020.Passing
import dk.ilios.jervis.rules.bb2020.Strength
import dk.ilios.jervis.rules.roster.RosterId
import dk.ilios.jervis.rules.skills.Dodge
import dk.ilios.jervis.rules.skills.PrehensileTail
import dk.ilios.jervis.rules.skills.Stunty
import kotlinx.serialization.Serializable

/**
 * Lizardmen Team
 *
 * See page 118 in the rulebook
 */
@Serializable
data object LizardmenTeam : BB2020Roster {
    val SKINK_RUNNER_LINEMEN =
        BB2020Position(
            LizardmenTeam,
            12,
            "Skink Runner Linemen",
            "Skink Runner Lineman",
            60_000,
            8, 2, 3, 4, 8,
            listOf(Dodge.Factory, Stunty.Factory),
            listOf(Agility),
            listOf(General, Passing, Strength),
        )
    val CHAMELEON_SKINKS =
        BB2020Position(
            LizardmenTeam,
            2,
            "Chameleon Skinks",
            "Chameleon Skink",
            70_000,
            7, 2, 3, 3, 8,
            listOf(Dodge.Factory, /* On the Ball, Shadowing */ Stunty.Factory),
            listOf(Agility),
            listOf(General, Passing, Strength),
        )
    val SAURUS_BLOCKERS =
        BB2020Position(
            LizardmenTeam,
            6,
            "Saurus Blockers",
            "Saurus Blocker",
            85_000,
            6, 4, 5, 6, 10,
            emptyList(),
            listOf(General, Strength),
            listOf(Agility),
        )
    val KROXIGOR =
        BB2020Position(
            LizardmenTeam,
            1,
            "Kroxigor",
            "Kroxigor",
            140_000,
            6, 5, 5, null, 10,
            listOf(/* Bone Head, Loner (4+), Might Blow (1+), Thick Skull */ PrehensileTail.Factory),
            listOf(Strength),
            listOf(Agility, General),
        )

    override val id: RosterId = RosterId("jervis-lizardmen")
    override val tier: Int = 1
    override val specialRules: List<SpecialRules> = listOf(LustrianSuperLeague)
    override val name: String = "Lizardmen Team"
    override val numberOfRerolls: Int = 8
    override val rerollCost: Int = 70_000
    override val allowApothecary: Boolean = true
    override val positions =
        listOf(
            SKINK_RUNNER_LINEMEN,
            CHAMELEON_SKINKS,
            SAURUS_BLOCKERS,
            KROXIGOR,
        )
}
