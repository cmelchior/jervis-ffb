package dk.ilios.jervis.rules.roster.bb2020

import dk.ilios.jervis.rules.bb2020.BB2020SkillCategory.AGILITY
import dk.ilios.jervis.rules.bb2020.BB2020SkillCategory.GENERAL
import dk.ilios.jervis.rules.bb2020.BB2020SkillCategory.PASSING
import dk.ilios.jervis.rules.bb2020.BB2020SkillCategory.STRENGTH
import dk.ilios.jervis.rules.roster.RosterId
import dk.ilios.jervis.rules.skills.Block
import dk.ilios.jervis.rules.skills.CatchSkill
import dk.ilios.jervis.rules.skills.Dodge
import dk.ilios.jervis.rules.skills.SureHands
import kotlinx.serialization.Serializable

/**
 * Human Teams
 *
 * See page 116 in the rulebook.
 */
@Serializable
data object HumanTeam : BB2020Roster {
    val LINEMAN =
        BB2020Position(
            HumanTeam,
            16,
            "Human Lineman",
            "Human Lineman",
            50_000,
            6, 3, 3, 4, 9,
            emptyList(),
            listOf(GENERAL),
            listOf(AGILITY, STRENGTH),
        )
    val THROWER =
        BB2020Position(
            HumanTeam,
            2,
            "Throwers",
            "Thrower",
            80_000,
            6, 3, 3, 2, 9,
            listOf(/* Pass */ SureHands.Factory),
            listOf(GENERAL, PASSING),
            listOf(AGILITY, STRENGTH),
        )
    val CATCHER =
        BB2020Position(
            HumanTeam,
            4,
            "Catchers",
            "Catcher",
            65_000,
            8, 2, 3, 5, 8,
            listOf(CatchSkill.Factory, Dodge.Factory),
            listOf(AGILITY, GENERAL),
            listOf(STRENGTH, PASSING),
        )
    val BLITZER =
        BB2020Position(
            HumanTeam,
            4,
            "Blitzers",
            "Blitzer",
            85_000,
            7, 3, 3, 4, 9,
            listOf(Block.Factory),
            listOf(GENERAL, STRENGTH),
            listOf(AGILITY, PASSING),
        )
    val HALFLING_HOPEFUL =
        BB2020Position(
            HumanTeam,
            3,
            "Halfling Hopefuls",
            "Halfling Hopeful",
            30_000,
            5, 2, 3, 4, 7,
            emptyList(),
            listOf(AGILITY),
            listOf(GENERAL, STRENGTH),
        )
    val OGRE =
        BB2020Position(
            HumanTeam,
            1,
            "Ogre",
            "Ogre",
            140_000,
            5, 5, 4, 5, 10,
            emptyList(),
            listOf(STRENGTH),
            listOf(AGILITY, GENERAL),
        )
    override val id: RosterId = RosterId("jervis-human")
    override val tier: Int = 1
    override val specialRules: List<SpecialRules> = listOf(RegionalSpecialRule.OLD_WORLD_CLASSIC)
    override val name: String = "Human Team"
    override val numberOfRerolls: Int = 8
    override val rerollCost: Int = 50_000
    override val allowApothecary: Boolean = true
    override val positions =
        listOf(
            LINEMAN,
            THROWER,
            CATCHER,
            BLITZER,
            HALFLING_HOPEFUL,
            OGRE,
        )
}
