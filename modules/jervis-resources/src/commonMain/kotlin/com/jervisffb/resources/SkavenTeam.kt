package com.jervisffb.resources

import com.jervisffb.engine.model.PositionId
import com.jervisffb.engine.model.RosterId
import com.jervisffb.engine.rules.bb2020.BB2020SkillCategory.AGILITY
import com.jervisffb.engine.rules.bb2020.BB2020SkillCategory.GENERAL
import com.jervisffb.engine.rules.bb2020.BB2020SkillCategory.MUTATIONS
import com.jervisffb.engine.rules.bb2020.BB2020SkillCategory.PASSING
import com.jervisffb.engine.rules.bb2020.BB2020SkillCategory.STRENGTH
import com.jervisffb.engine.rules.bb2020.roster.BB2020Position
import com.jervisffb.engine.rules.bb2020.roster.BB2020Roster
import com.jervisffb.engine.rules.bb2020.roster.RegionalSpecialRule
import com.jervisffb.engine.rules.bb2020.skills.SureHands
import com.jervisffb.engine.serialize.RosterLogo
import com.jervisffb.engine.serialize.SingleSprite
import com.jervisffb.engine.serialize.SpriteSheet
import kotlinx.serialization.Serializable

val SKAVEN_LINEMAN =
    BB2020Position(
        PositionId("skaven-lineman"),
        16,
        "Skaven Clanrat Linemen",
        "Skaven Clanrat Lineman",
        "L",
        50_000,
        7, 3, 3, 4, 8,
        emptyList(),
        listOf(GENERAL),
        listOf(AGILITY, MUTATIONS, STRENGTH),
        SpriteSheet.embedded("$iconRootPath/skaven_lineman.png", 9),
        SingleSprite.embedded("$portraitRootPath/skaven_lineman.png")

    )
val SKAVEN_THROWER =
    BB2020Position(
        PositionId("skaven-thrower"),
        2,
        "Throwers",
        "Thrower",
        "T",
        85_000,
        7, 3, 3, 2, 8,
        listOf(/* Pass, */ SureHands.Factory),
        listOf(GENERAL, PASSING),
        listOf(AGILITY, MUTATIONS, STRENGTH),
        SpriteSheet.embedded("$iconRootPath/skaven_thrower.png", 2),
        SingleSprite.embedded("$portraitRootPath/skaven_thrower.png")
    )
val GUTTER_RUNNER =
    BB2020Position(
        PositionId("skaven-gutter-runner"),
        4,
        "Gutter Runners",
        "Gutter Runner",
        "Gr",
        85_000,
        9, 2, 2, 4, 8,
        listOf(/* Dodge */),
        listOf(AGILITY, GENERAL),
        listOf(MUTATIONS, PASSING, STRENGTH),
        SpriteSheet.embedded("$iconRootPath/skaven_gutterrunner.png", 4),
        SingleSprite.embedded("$portraitRootPath/skaven_gutterrunner.png")
    )
val SKAVEN_BLITZER =
    BB2020Position(
        PositionId("skaven-blitzer"),
        4,
        "Blitzers",
        "Blitzer",
        "B",
        90_000,
        7, 3, 3, 5, 9,
        emptyList(/* Block */),
        listOf(GENERAL, STRENGTH),
        listOf(AGILITY, MUTATIONS, PASSING),
        SpriteSheet.embedded("$iconRootPath/skaven_blitzer.png", 2),
        SingleSprite.embedded("$portraitRootPath/skaven_blitzer.png")
    )
val RAT_OGRE =
    BB2020Position(
        PositionId("skaven-rat-ogre"),
        1,
        "Rat Ogre",
        "Rat Ogre",
        "Ros",
        150_000,
        6, 5, 4, null, 9,
        listOf(/* AnimalSavagery, Frenzy, Loner(4), MightyBlow(1), PrehensileTail */),
        listOf(STRENGTH),
        listOf(AGILITY, GENERAL, MUTATIONS),
        SpriteSheet.embedded("$iconRootPath/skaven_ratogre.png", 1),
        SingleSprite.embedded("$portraitRootPath/skaven_ratogre.png")
    )

// Page 116 in the rulebook
@Serializable
val SKAVEN_TEAM = BB2020Roster(
    id = RosterId("jervis-skaven"),
    name = "Skaven Team",
    tier = 1,
    numberOfRerolls = 8,
    rerollCost = 50_000,
    allowApothecary = true,
    specialRules = listOf(RegionalSpecialRule.UNDERWORLD_CHALLENGE),
    positions = listOf(
        SKAVEN_LINEMAN,
        SKAVEN_THROWER,
        GUTTER_RUNNER,
        SKAVEN_BLITZER,
        RAT_OGRE,
    ),
    logo = RosterLogo(
        large = SingleSprite.embedded("roster/logo/roster_logo_jervis_skaven_large.png"),
        small = SingleSprite.embedded("roster/logo/roster_logo_jervis_skaven_small.png")
    )
)
