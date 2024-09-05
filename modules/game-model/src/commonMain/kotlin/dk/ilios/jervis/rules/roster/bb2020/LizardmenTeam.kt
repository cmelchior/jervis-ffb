package dk.ilios.jervis.rules.roster.bb2020

import dk.ilios.jervis.rules.bb2020.BB2020SkillCategory.AGILITY
import dk.ilios.jervis.rules.bb2020.BB2020SkillCategory.GENERAL
import dk.ilios.jervis.rules.bb2020.BB2020SkillCategory.PASSING
import dk.ilios.jervis.rules.bb2020.BB2020SkillCategory.STRENGTH
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
            listOf(AGILITY),
            listOf(GENERAL, PASSING, STRENGTH),
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
            listOf(AGILITY),
            listOf(GENERAL, PASSING, STRENGTH),
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
            listOf(GENERAL, STRENGTH),
            listOf(AGILITY),
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
            listOf(STRENGTH),
            listOf(AGILITY, GENERAL),
        )

    override val id: RosterId = RosterId("jervis-lizardmen")
    override val tier: Int = 1
    override val specialRules: List<SpecialRules> = listOf(RegionalSpecialRule.LUSTRIAN_SUPERLEAGUE)
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
