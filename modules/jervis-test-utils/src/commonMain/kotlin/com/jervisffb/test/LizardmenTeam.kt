package com.jervisffb.test

import com.jervisffb.engine.model.PlayerSize
import com.jervisffb.engine.model.PositionId
import com.jervisffb.engine.model.RosterId
import com.jervisffb.engine.rules.bb2020.roster.BB2020Roster
import com.jervisffb.engine.rules.bb2020.roster.RegionalSpecialRule
import com.jervisffb.engine.rules.bb2020.roster.RosterPosition
import com.jervisffb.engine.rules.bb2020.skills.SkillCategory
import com.jervisffb.engine.rules.bb2020.skills.SkillType
import com.jervisffb.engine.serialize.RosterLogo

val SKINK_RUNNER_LINEMEN =
    RosterPosition(
        PositionId("lizardmen-skink-runner-lineman"),
        12,
        "Skink Runner Linemen",
        "Skink Runner Lineman",
        "Sk",
        60_000,
        8, 2, 3, 4, 8,
        listOf(SkillType.DODGE.id(), SkillType.STUNTY.id()),
        listOf(SkillCategory.AGILITY),
        listOf(SkillCategory.GENERAL, SkillCategory.PASSING, SkillCategory.STRENGTH),
        PlayerSize.STANDARD,
        null,
        null
    )
val CHAMELEON_SKINKS =
    RosterPosition(
        PositionId("lizardmen-chameleon-skink"),
        2,
        "Chameleon Skinks",
        "Chameleon Skink",
        "Cs",
        70_000,
        7, 2, 3, 3, 8,
        listOf(SkillType.DODGE.id(), /* On the Ball, Shadowing */ SkillType.STUNTY.id()),
        listOf(SkillCategory.AGILITY),
        listOf(SkillCategory.GENERAL, SkillCategory.PASSING, SkillCategory.STRENGTH),
        PlayerSize.STANDARD,
        null,
        null
    )
val SAURUS_BLOCKERS =
    RosterPosition(
        PositionId("lizardmen-saurus-blocker"),
        6,
        "Saurus Blockers",
        "Saurus Blocker",
        "S",
        85_000,
        6, 4, 5, 6, 10,
        emptyList(),
        listOf(SkillCategory.GENERAL, SkillCategory.STRENGTH),
        listOf(SkillCategory.AGILITY),
        PlayerSize.STANDARD,
        null,
        null
    )
val KROXIGOR =
    RosterPosition(
        PositionId("lizardmen-kroxigor"),
        1,
        "Kroxigor",
        "Kroxigor",
        "K",
        140_000,
        6, 5, 5, null, 10,
        listOf(
            SkillType.BONE_HEAD.id(),
            SkillType.LONER.id(4),
            SkillType.MIGHTY_BLOW.id(1),
            SkillType.PREHENSILE_TAIL.id(),
            SkillType.THICK_SKULL.id()
        ),
        listOf(SkillCategory.STRENGTH),
        listOf(SkillCategory.AGILITY, SkillCategory.GENERAL),
        PlayerSize.BIG_GUY,
        null,
        null
    )

/**
 * Lizardmen Team
 *
 * See page 118 in the rulebook
 */
val LIZARDMEN_TEAM = BB2020Roster(
    id = RosterId("jervis-lizardmen"),
    name = "Lizardmen Team",
    tier = 1,
    numberOfRerolls = 8,
    rerollCost = 70_000,
    allowApothecary = true,
    specialRules = listOf(RegionalSpecialRule.LUSTRIAN_SUPERLEAGUE),
    positions = listOf(
        SKINK_RUNNER_LINEMEN,
        CHAMELEON_SKINKS,
        SAURUS_BLOCKERS,
        KROXIGOR,
    ),
    logo = RosterLogo.NONE
)
