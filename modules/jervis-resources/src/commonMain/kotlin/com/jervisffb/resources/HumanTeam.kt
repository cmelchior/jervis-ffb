package com.jervisffb.resources

import com.jervisffb.engine.model.PositionId
import com.jervisffb.engine.model.RosterId
import com.jervisffb.engine.rules.bb2020.roster.BB2020Roster
import com.jervisffb.engine.rules.bb2020.roster.RegionalSpecialRule
import com.jervisffb.engine.rules.bb2020.roster.RosterPosition
import com.jervisffb.engine.rules.bb2020.skills.SkillCategory.AGILITY
import com.jervisffb.engine.rules.bb2020.skills.SkillCategory.GENERAL
import com.jervisffb.engine.rules.bb2020.skills.SkillCategory.PASSING
import com.jervisffb.engine.rules.bb2020.skills.SkillCategory.STRENGTH
import com.jervisffb.engine.rules.bb2020.skills.SkillType
import com.jervisffb.engine.rules.bb2020.skills.SkillType.CATCH
import com.jervisffb.engine.rules.bb2020.skills.SkillType.BLOCK
import com.jervisffb.engine.rules.bb2020.skills.SkillType.DODGE
import com.jervisffb.engine.rules.bb2020.skills.SkillType.PASS
import com.jervisffb.engine.rules.bb2020.skills.SkillType.SURE_HANDS
import com.jervisffb.engine.rules.bb2020.skills.SkillType.BONE_HEAD
import com.jervisffb.engine.rules.bb2020.skills.SkillType.LONER
import com.jervisffb.engine.rules.bb2020.skills.SkillType.MIGHTY_BLOW
import com.jervisffb.engine.rules.bb2020.skills.SkillType.THICK_SKULL
import com.jervisffb.engine.rules.bb2020.skills.SkillType.THROW_TEAMMATE
import com.jervisffb.engine.rules.bb2020.skills.SkillType.RIGHT_STUFF
import com.jervisffb.engine.rules.bb2020.skills.SkillType.STUNTY
import com.jervisffb.engine.serialize.RosterLogo
import com.jervisffb.engine.serialize.SingleSprite
import com.jervisffb.engine.serialize.SpriteSheet

/**
 * Human Teams
 *
 * See page 116 in the rulebook.
 */
val HUMAN_LINEMAN =
    RosterPosition(
        PositionId("human-lineman"),
        16,
        "Human Lineman",
        "Human Lineman",
        "L",
        50_000,
        6, 3, 3, 4, 9,
        emptyList(),
        listOf(GENERAL),
        listOf(AGILITY, STRENGTH),
        SpriteSheet.embedded("$iconRootPath/human_lineman.png",8),
        SingleSprite.embedded("$portraitRootPath/human_lineman.png")

    )
val HUMAN_THROWER =
    RosterPosition(
        PositionId("human-thrower"),
        2,
        "Throwers",
        "Thrower",
        "T",
        80_000,
        6, 3, 3, 2, 9,
        listOf(
            PASS.id(),
            SURE_HANDS.id()
        ),
        listOf(GENERAL, PASSING),
        listOf(AGILITY, STRENGTH),
        SpriteSheet.embedded("$iconRootPath/human_thrower.png",2),
        SingleSprite.embedded("$portraitRootPath/human_thrower.png")
    )
val HUMAN_CATCHER =
    RosterPosition(
        PositionId("human-catcher"),
        4,
        "Catchers",
        "Catcher",
        "C",
        65_000,
        8, 2, 3, 5, 8,
        listOf(
            CATCH.id(),
            DODGE.id()
        ),
        listOf(AGILITY, GENERAL),
        listOf(STRENGTH, PASSING),
        SpriteSheet.embedded("$iconRootPath/human_catcher.png", 4),
        SingleSprite.embedded("$portraitRootPath/human_catcher.png")
    )
val HUMAN_BLITZER =
    RosterPosition(
        PositionId("human-blitzer"),
        4,
        "Blitzers",
        "Blitzer",
        "B",
        85_000,
        7, 3, 3, 4, 9,
        listOf(BLOCK.id()),
        listOf(GENERAL, STRENGTH),
        listOf(AGILITY, PASSING),
        SpriteSheet.embedded("$iconRootPath/human_blitzer.png", 4),
        SingleSprite.embedded("$portraitRootPath/human_blitzer.png")
    )
val HALFLING_HOPEFUL =
    RosterPosition(
        PositionId("human-hafling-hopeful"),
        3,
        "Halfling Hopefuls",
        "Halfling Hopeful",
        "H",
        30_000,
        5, 2, 3, 4, 7,
        listOf(
            DODGE.id(),
            RIGHT_STUFF.id(),
            STUNTY.id()
        ),
        listOf(AGILITY),
        listOf(GENERAL, STRENGTH),
        SpriteSheet.embedded("$iconRootPath/human_halflinghopeful.png", 8),
        SingleSprite.embedded("$portraitRootPath/human_halflinghopeful.png")
    )
val OGRE =
    RosterPosition(
        PositionId("human-ogre"),
        1,
        "Ogre",
        "Ogre",
        "O",
        140_000,
        5, 5, 4, 5, 10,
        listOf(
            BONE_HEAD.id(),
            LONER.id(4),
            MIGHTY_BLOW.id(1),
            THICK_SKULL.id(),
            THROW_TEAMMATE.id()
        ),
        listOf(STRENGTH),
        listOf(AGILITY, GENERAL),
        SpriteSheet.embedded("$iconRootPath/human_ogre.png", 8),
        SingleSprite.embedded("$portraitRootPath/human_ogre.png")
    )

val HUMAN_TEAM = BB2020Roster(
    id = RosterId("jervis-human"),
    name = "Human Team",
    tier = 1,
    numberOfRerolls = 8,
    rerollCost = 50_000,
    allowApothecary = true,
    specialRules = listOf(RegionalSpecialRule.OLD_WORLD_CLASSIC),
    positions = listOf(
        HUMAN_LINEMAN,
        HUMAN_THROWER,
        HUMAN_CATCHER,
        HUMAN_BLITZER,
        HALFLING_HOPEFUL,
        OGRE,
    ),
    logo = RosterLogo(
        large = SingleSprite.embedded("roster/logo/roster_logo_jervis_human_large.png"),
        small = SingleSprite.embedded("roster/logo/roster_logo_jervis_human_small.png")
    )
)
