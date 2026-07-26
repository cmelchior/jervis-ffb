package com.jervisffb.resources.bb2025

import com.jervisffb.engine.model.PlayerKeyword
import com.jervisffb.engine.model.PlayerSize
import com.jervisffb.engine.model.PositionId
import com.jervisffb.engine.model.RosterId
import com.jervisffb.engine.rules.common.roster.RegionalSpecialRule
import com.jervisffb.engine.rules.common.roster.Roster
import com.jervisffb.engine.rules.common.roster.RosterPosition
import com.jervisffb.engine.rules.common.roster.TeamSpecialRule
import com.jervisffb.engine.rules.common.skills.SkillCategory.AGILITY
import com.jervisffb.engine.rules.common.skills.SkillCategory.DEVIOUS
import com.jervisffb.engine.rules.common.skills.SkillCategory.GENERAL
import com.jervisffb.engine.rules.common.skills.SkillCategory.MUTATIONS
import com.jervisffb.engine.rules.common.skills.SkillCategory.PASSING
import com.jervisffb.engine.rules.common.skills.SkillCategory.STRENGTH
import com.jervisffb.engine.rules.common.skills.SkillType.CLAWS
import com.jervisffb.engine.rules.common.skills.SkillType.FRENZY
import com.jervisffb.engine.rules.common.skills.SkillType.HORNS
import com.jervisffb.engine.rules.common.skills.SkillType.JUGGERNAUT
import com.jervisffb.engine.rules.common.skills.SkillType.JUMP_UP
import com.jervisffb.engine.rules.common.skills.SkillType.LONER
import com.jervisffb.engine.rules.common.skills.SkillType.MIGHTY_BLOW
import com.jervisffb.engine.rules.common.skills.SkillType.THICK_SKULL
import com.jervisffb.engine.rules.common.skills.SkillType.UNCHANNELLED_FURY
import com.jervisffb.engine.sprites.RosterLogo
import com.jervisffb.engine.sprites.SingleSprite
import com.jervisffb.engine.sprites.SpriteSheet
import com.jervisffb.resources.iconRootPath
import com.jervisffb.resources.portraitRootPath

val BLOODBORN_MARAUDER_LINEMEN =
    RosterPosition(
        PositionId("khorne-bloodborn-marauder-lineman"),
        16,
        "Bloodborn Marauder Linemen",
        "Bloodborn Marauder Lineman",
        "L",
        50_000,
        6, 3, 3, 4, 8,
        listOf(FRENZY.id()),
        listOf(GENERAL, MUTATIONS),
        listOf(AGILITY, STRENGTH, DEVIOUS),
        emptyList(),
        listOf(PlayerKeyword.HUMAN, PlayerKeyword.LINEMAN),
        PlayerSize.STANDARD,
        SpriteSheet.ini("${iconRootPath}/khorne_bloodbornmarauderlineman.png",7),
        SingleSprite.ini("${portraitRootPath}/khorne_bloodbornmarauderlineman.png")
    )
val KHORNGORS =
    RosterPosition(
        PositionId("khorne-khorngor"),
        2,
        "Khorngors",
        "Khorngor",
        "K",
        70_000,
        6, 3, 3, 4, 9,
        listOf(HORNS.id(), JUMP_UP.id(), JUGGERNAUT.id(), THICK_SKULL.id()),
        listOf(GENERAL, MUTATIONS, STRENGTH),
        listOf(AGILITY, PASSING, DEVIOUS),
        emptyList(),
        listOf(PlayerKeyword.BEASTMAN, PlayerKeyword.RUNNER),
        PlayerSize.STANDARD,
        SpriteSheet.ini("${iconRootPath}/khorne_khorngor.png",2),
        SingleSprite.ini("${portraitRootPath}/khorne_khorngor.png")
    )
val BLOODSEEKERS =
    RosterPosition(
        PositionId("khorne-bloodseeker"),
        4,
        "Bloodseekers",
        "Bloodseeker",
        "Bs",
        105_000,
        5, 4, 4, 6, 10,
        listOf(FRENZY.id()),
        listOf(GENERAL, MUTATIONS, STRENGTH),
        listOf(AGILITY, DEVIOUS),
        emptyList(),
        listOf(PlayerKeyword.HUMAN, PlayerKeyword.BLOCKER),
        PlayerSize.STANDARD,
        SpriteSheet.ini("${iconRootPath}/khorne_bloodseeker.png", 4),
        SingleSprite.ini("${portraitRootPath}/khorne_bloodseeker.png")
    )
val BLOODSPAWN =
    RosterPosition(
        PositionId("khorne-bloodspawn"),
        1,
        "Bloodspawn",
        "Bloodspawn",
        "B",
        160_000,
        5, 5, 4, 6, 9,
        listOf(
            CLAWS.id(),
            FRENZY.id(),
            LONER.idTarget(4),
            MIGHTY_BLOW.idAdjustment(1),
            UNCHANNELLED_FURY.id()
        ),
        listOf(MUTATIONS, STRENGTH),
        listOf(AGILITY, GENERAL),
        emptyList(),
        listOf(PlayerKeyword.SPAWN, PlayerKeyword.BIG_GUY),
        PlayerSize.BIG_GUY,
        SpriteSheet.ini("${iconRootPath}/khorne_bloodspawn.png", 1),
        SingleSprite.ini("${portraitRootPath}/khorne_bloodspawn.png")
    )

// See Spike! Journal Issue 13
val KHORNE_TEAM_BB2025 = Roster(
    id = RosterId("jervis-khorne"),
    tier = 3,
    name = "Khorne Team",
    numberOfRerolls = 8,
    rerollCost = 60_000,
    allowApothecary = true,
    positions = listOf(
        BLOODBORN_MARAUDER_LINEMEN,
        KHORNGORS,
        BLOODSEEKERS,
        BLOODSPAWN,
    ),
    leagues = listOf(RegionalSpecialRule.CHAOS_CLASH),
    specialRules = listOf(TeamSpecialRule.BRAWLIN_BRUTES, TeamSpecialRule.FAVOURED_OF_KHORNE),
    logo = RosterLogo(
        large = SingleSprite.embedded("jervis/roster/logo_khorne_large.png"),
        small = SingleSprite.embedded("jervis/roster/logo_khorne_small.png")
    )
)
