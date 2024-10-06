package com.jervisffb.engine.rules.bb2020.roster

import com.jervisffb.engine.rules.bb2020.BB2020SkillCategory.AGILITY
import com.jervisffb.engine.rules.bb2020.BB2020SkillCategory.GENERAL
import com.jervisffb.engine.rules.bb2020.BB2020SkillCategory.MUTATIONS
import com.jervisffb.engine.rules.bb2020.BB2020SkillCategory.PASSING
import com.jervisffb.engine.rules.bb2020.BB2020SkillCategory.STRENGTH
import com.jervisffb.engine.rules.bb2020.skills.SureHands
import com.jervisffb.engine.rules.common.roster.RosterId
import kotlinx.serialization.Serializable

// Page 116 in the rulebook
@Serializable
data object SkavenTeam : BB2020Roster {
    val LINEMAN =
        BB2020Position(
            SkavenTeam,
            16,
            "Skaven Clanrat Linemen",
            "Skaven Clanrat Lineman",
            50_000,
            7, 3, 3, 4, 8,
            emptyList(),
            listOf(GENERAL),
            listOf(AGILITY, MUTATIONS, STRENGTH),
        )
    val THROWER =
        BB2020Position(
            SkavenTeam,
            2,
            "Throwers",
            "Thrower",
            85_000,
            7, 3, 3, 2, 8,
            listOf(/* Pass, */ SureHands.Factory),
            listOf(GENERAL, PASSING),
            listOf(AGILITY, MUTATIONS, STRENGTH),
        )
    val GUTTER_RUNNER =
        BB2020Position(
            SkavenTeam,
            4,
            "Gutter Runners",
            "Gutter Runner",
            85_000,
            9, 2, 2, 4, 8,
            listOf(/* Dodge */),
            listOf(AGILITY, GENERAL),
            listOf(MUTATIONS, PASSING, STRENGTH),
        )
    val BLITZER =
        BB2020Position(
            SkavenTeam,
            4,
            "Blitzers",
            "Blitzer",
            90_000,
            7, 3, 3, 5, 9,
            emptyList(/* Block */),
            listOf(GENERAL, STRENGTH),
            listOf(AGILITY, MUTATIONS, PASSING),
        )
    val RAT_OGRE =
        BB2020Position(
            SkavenTeam,
            1,
            "Rat Ogre",
            "Rat Ogre",
            150_000,
            6, 5, 4, null, 9,
            listOf(/* AnimalSavagery, Frenzy, Loner(4), MightyBlow(1), PrehensileTail */),
            listOf(STRENGTH),
            listOf(AGILITY, GENERAL, MUTATIONS),
        )
    override val id: RosterId = RosterId("jervis-skaven")
    override val tier: Int = 1
    override val specialRules: List<SpecialRules> = listOf(RegionalSpecialRule.UNDERWORLD_CHALLENGE)
    override val name: String = "Skaven Team"
    override val numberOfRerolls: Int = 8
    override val rerollCost: Int = 50_000
    override val allowApothecary: Boolean = true
    override val positions =
        listOf(
            LINEMAN,
            THROWER,
            GUTTER_RUNNER,
            BLITZER,
            RAT_OGRE,
        )
}
