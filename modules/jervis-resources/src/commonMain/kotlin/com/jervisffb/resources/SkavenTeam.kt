package com.jervisffb.resources

import com.jervisffb.engine.model.PositionId
import com.jervisffb.engine.model.RosterId
import com.jervisffb.engine.rules.bb2020.roster.BB2020Roster
import com.jervisffb.engine.rules.bb2020.roster.RegionalSpecialRule
import com.jervisffb.engine.rules.bb2020.roster.RosterPosition
import com.jervisffb.engine.rules.bb2020.skills.SkillCategory
import com.jervisffb.engine.rules.bb2020.skills.SkillType.ANIMAL_SAVAGERY
import com.jervisffb.engine.rules.bb2020.skills.SkillType.BLOCK
import com.jervisffb.engine.rules.bb2020.skills.SkillType.DODGE
import com.jervisffb.engine.rules.bb2020.skills.SkillType.FRENZY
import com.jervisffb.engine.rules.bb2020.skills.SkillType.LONER
import com.jervisffb.engine.rules.bb2020.skills.SkillType.MIGHTY_BLOW
import com.jervisffb.engine.rules.bb2020.skills.SkillType.PASS
import com.jervisffb.engine.rules.bb2020.skills.SkillType.PREHENSILE_TAIL
import com.jervisffb.engine.rules.bb2020.skills.SkillType.SURE_HANDS
import com.jervisffb.engine.serialize.RosterLogo
import com.jervisffb.engine.serialize.SingleSprite
import com.jervisffb.engine.serialize.SpriteSheet
import kotlinx.serialization.Serializable

val SKAVEN_LINEMAN =
    RosterPosition(
        PositionId("skaven-lineman"),
        16,
        "Skaven Clanrat Linemen",
        "Skaven Clanrat Lineman",
        "L",
        50_000,
        7, 3, 3, 4, 8,
        emptyList(),
        listOf(SkillCategory.GENERAL),
        listOf(SkillCategory.AGILITY, SkillCategory.MUTATIONS, SkillCategory.STRENGTH),
        SpriteSheet.embedded("$iconRootPath/skaven_lineman.png", 9),
        SingleSprite.embedded("$portraitRootPath/skaven_lineman.png")

    )
val SKAVEN_THROWER =
    RosterPosition(
        PositionId("skaven-thrower"),
        2,
        "Throwers",
        "Thrower",
        "T",
        85_000,
        7, 3, 3, 2, 8,
        listOf(PASS.id(), SURE_HANDS.id()),
        listOf(SkillCategory.GENERAL, SkillCategory.PASSING),
        listOf(SkillCategory.AGILITY, SkillCategory.MUTATIONS, SkillCategory.STRENGTH),
        SpriteSheet.embedded("$iconRootPath/skaven_thrower.png", 2),
        SingleSprite.embedded("$portraitRootPath/skaven_thrower.png")
    )
val GUTTER_RUNNER =
    RosterPosition(
        PositionId("skaven-gutter-runner"),
        4,
        "Gutter Runners",
        "Gutter Runner",
        "Gr",
        85_000,
        9, 2, 2, 4, 8,
        listOf(DODGE.id()),
        listOf(SkillCategory.AGILITY, SkillCategory.GENERAL),
        listOf(SkillCategory.MUTATIONS, SkillCategory.PASSING, SkillCategory.STRENGTH),
        SpriteSheet.embedded("$iconRootPath/skaven_gutterrunner.png", 4),
        SingleSprite.embedded("$portraitRootPath/skaven_gutterrunner.png")
    )
val SKAVEN_BLITZER =
    RosterPosition(
        PositionId("skaven-blitzer"),
        4,
        "Blitzers",
        "Blitzer",
        "B",
        90_000,
        7, 3, 3, 5, 9,
        listOf(BLOCK.id()),
        listOf(SkillCategory.GENERAL, SkillCategory.STRENGTH),
        listOf(SkillCategory.AGILITY, SkillCategory.MUTATIONS, SkillCategory.PASSING),
        SpriteSheet.embedded("$iconRootPath/skaven_blitzer.png", 2),
        SingleSprite.embedded("$portraitRootPath/skaven_blitzer.png")
    )
val RAT_OGRE =
    RosterPosition(
        PositionId("skaven-rat-ogre"),
        1,
        "Rat Ogre",
        "Rat Ogre",
        "Ros",
        150_000,
        6, 5, 4, null, 9,
        listOf(
            ANIMAL_SAVAGERY.id(),
            FRENZY.id(),
            LONER.id(4),
            MIGHTY_BLOW.id(1),
            PREHENSILE_TAIL.id()
        ),
        listOf(SkillCategory.STRENGTH),
        listOf(SkillCategory.AGILITY, SkillCategory.GENERAL, SkillCategory.MUTATIONS),
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
