package com.jervisffb.test

import com.jervisffb.engine.model.PlayerSize
import com.jervisffb.engine.model.PositionId
import com.jervisffb.engine.model.RosterId
import com.jervisffb.engine.rules.bb2020.roster.BB2020Roster
import com.jervisffb.engine.rules.bb2020.roster.RegionalSpecialRule
import com.jervisffb.engine.rules.bb2020.roster.RosterPosition
import com.jervisffb.engine.rules.common.skills.SkillCategory
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.serialize.RosterLogo

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
        listOf(SkillCategory.GENERAL),
        listOf(SkillCategory.AGILITY, SkillCategory.STRENGTH),
        PlayerSize.STANDARD,
        null,
        null
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
        listOf(/* Pass */ SkillType.SURE_HANDS.id()),
        listOf(SkillCategory.GENERAL, SkillCategory.PASSING),
        listOf(SkillCategory.AGILITY, SkillCategory.STRENGTH),
        PlayerSize.STANDARD,
        null,
        null
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
        listOf(SkillType.CATCH.id(), SkillType.DODGE.id()),
        listOf(SkillCategory.AGILITY, SkillCategory.GENERAL),
        listOf(SkillCategory.STRENGTH, SkillCategory.PASSING),
        PlayerSize.STANDARD,
        null,
        null,
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
        listOf(SkillType.BLOCK.id()),
        listOf(SkillCategory.GENERAL, SkillCategory.STRENGTH),
        listOf(SkillCategory.AGILITY, SkillCategory.PASSING),
        PlayerSize.STANDARD,
        null,
        null
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
        emptyList(),
        listOf(SkillCategory.AGILITY),
        listOf(SkillCategory.GENERAL, SkillCategory.STRENGTH),
        PlayerSize.STANDARD,
        null,
        null
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
        emptyList(),
        listOf(SkillCategory.STRENGTH),
        listOf(SkillCategory.AGILITY, SkillCategory.GENERAL),
        PlayerSize.BIG_GUY,
        null,
        null
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
    logo = RosterLogo.NONE
)
