package com.jervisffb.engine.rules.bb2020.roster

import com.jervisffb.engine.model.PositionId
import com.jervisffb.engine.model.RosterId
import com.jervisffb.engine.rules.bb2020.BB2020SkillCategory.AGILITY
import com.jervisffb.engine.rules.bb2020.BB2020SkillCategory.GENERAL
import com.jervisffb.engine.rules.bb2020.BB2020SkillCategory.PASSING
import com.jervisffb.engine.rules.bb2020.BB2020SkillCategory.STRENGTH
import com.jervisffb.engine.rules.bb2020.skills.Block
import com.jervisffb.engine.rules.bb2020.skills.CatchSkill
import com.jervisffb.engine.rules.bb2020.skills.Dodge
import com.jervisffb.engine.rules.bb2020.skills.SureHands

/**
 * Human Teams
 *
 * See page 116 in the rulebook.
 */
val HUMAN_LINEMAN =
    BB2020Position(
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
    )
val HUMAN_THROWER =
    BB2020Position(
        PositionId("human-thrower"),
        2,
        "Throwers",
        "Thrower",
        "T",
        80_000,
        6, 3, 3, 2, 9,
        listOf(/* Pass */ SureHands.Factory),
        listOf(GENERAL, PASSING),
        listOf(AGILITY, STRENGTH),
    )
val HUMAN_CATCHER =
    BB2020Position(
        PositionId("human-catcher"),
        4,
        "Catchers",
        "Catcher",
        "C",
        65_000,
        8, 2, 3, 5, 8,
        listOf(CatchSkill.Factory, Dodge.Factory),
        listOf(AGILITY, GENERAL),
        listOf(STRENGTH, PASSING),
    )
val HUMAN_BLITZER =
    BB2020Position(
        PositionId("human-blitzer"),
        4,
        "Blitzers",
        "Blitzer",
        "B",
        85_000,
        7, 3, 3, 4, 9,
        listOf(Block.Factory),
        listOf(GENERAL, STRENGTH),
        listOf(AGILITY, PASSING),
    )
val HALFLING_HOPEFUL =
    BB2020Position(
        PositionId("human-hafling-hopeful"),
        3,
        "Halfling Hopefuls",
        "Halfling Hopeful",
        "H",
        30_000,
        5, 2, 3, 4, 7,
        emptyList(),
        listOf(AGILITY),
        listOf(GENERAL, STRENGTH),
    )
val OGRE =
    BB2020Position(
        PositionId("human-ogre"),
        1,
        "Ogre",
        "Ogre",
        "O",
        140_000,
        5, 5, 4, 5, 10,
        emptyList(),
        listOf(STRENGTH),
        listOf(AGILITY, GENERAL),
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
    )
)
