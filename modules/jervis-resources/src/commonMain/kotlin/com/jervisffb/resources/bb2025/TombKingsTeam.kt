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
import com.jervisffb.engine.rules.common.skills.SkillCategory.PASSING
import com.jervisffb.engine.rules.common.skills.SkillCategory.STRENGTH
import com.jervisffb.engine.rules.common.skills.SkillType.BLOCK
import com.jervisffb.engine.rules.common.skills.SkillType.BRAWLER
import com.jervisffb.engine.rules.common.skills.SkillType.DECAY
import com.jervisffb.engine.rules.common.skills.SkillType.PASS
import com.jervisffb.engine.rules.common.skills.SkillType.REGENERATION
import com.jervisffb.engine.rules.common.skills.SkillType.SURE_HANDS
import com.jervisffb.engine.rules.common.skills.SkillType.THICK_SKULL
import com.jervisffb.engine.sprites.RosterLogo
import com.jervisffb.engine.sprites.SingleSprite
import com.jervisffb.engine.sprites.SpriteSheet
import com.jervisffb.resources.iconRootPath
import com.jervisffb.resources.portraitRootPath
import kotlinx.serialization.Serializable

val SKELETON_LINEMEN =
    RosterPosition(
        PositionId("tomb-kings-skeleton-lineman"),
        16,
        "Skeleton Linemen",
        "Skeleton Lineman",
        "S",
        40_000,
        5, 3, 4, 6, 8,
        listOf(REGENERATION.id(), THICK_SKULL.id()),
        listOf(GENERAL),
        listOf(AGILITY, DEVIOUS, STRENGTH),
        emptyList(),
        listOf(PlayerKeyword.HUMAN, PlayerKeyword.LINEMAN, PlayerKeyword.SKELETON, PlayerKeyword.UNDEAD),
        PlayerSize.STANDARD,
        SpriteSheet.ini("$iconRootPath/tombkings_skeletonlineman.png", 8),
        SingleSprite.ini("$portraitRootPath/tombkings_skeletonlineman.png")
    )

val TOMB_KINGS_THROWERS =
    RosterPosition(
        PositionId("tomb-kings-thrower"),
        2,
        "Tomb Kings Throwers",
        "Tomb Kings Thrower",
        "T",
        65_000,
        6, 3, 4, 3, 9,
        listOf(PASS.id(), REGENERATION.id(), SURE_HANDS.id(), THICK_SKULL.id()),
        listOf(GENERAL, PASSING),
        listOf(AGILITY, DEVIOUS, STRENGTH),
        emptyList(),
        listOf(PlayerKeyword.HUMAN, PlayerKeyword.SKELETON, PlayerKeyword.THROWER, PlayerKeyword.UNDEAD),
        PlayerSize.STANDARD,
        SpriteSheet.ini("$iconRootPath/tombkings_anointedthrower.png", 2),
        SingleSprite.ini("$portraitRootPath/tombkings_anointedthrower.png")
    )

val TOMB_KINGS_BLITZERS =
    RosterPosition(
        PositionId("tomb-kings-blitzer"),
        2,
        "Tomb Kings Blitzers",
        "Tomb Kings Blitzer",
        "B",
        85_000,
        6, 3, 4, 6, 9,
        listOf(BLOCK.id(), REGENERATION.id(), THICK_SKULL.id()),
        listOf(GENERAL, STRENGTH),
        listOf(AGILITY, DEVIOUS),
        emptyList(),
        listOf(PlayerKeyword.BLITZER, PlayerKeyword.HUMAN, PlayerKeyword.SKELETON, PlayerKeyword.UNDEAD),
        PlayerSize.STANDARD,
        SpriteSheet.ini("$iconRootPath/tombkings_anointedblitzer.png", 2),
        SingleSprite.ini("$portraitRootPath/tombkings_anointedblitzer.png")
    )

val TOMB_GUARDIANS =
    RosterPosition(
        PositionId("tomb-kings-tomb-guardian"),
        4,
        "Tomb Guardians",
        "Tomb Guardian",
        "G",
        115_000,
        4, 5, 5, 6, 10,
        listOf(BRAWLER.id(), DECAY.id(), REGENERATION.id()),
        listOf(STRENGTH),
        listOf(AGILITY),
        emptyList(),
        listOf(PlayerKeyword.BIG_GUY, PlayerKeyword.BLOCKER, PlayerKeyword.HUMAN, PlayerKeyword.UNDEAD),
        PlayerSize.BIG_GUY,
        SpriteSheet.ini("$iconRootPath/tombkings_tombguardian.png", 4),
        SingleSprite.ini("$portraitRootPath/tombkings_tombguardian.png")
    )

@Serializable
val TOMB_KINGS_TEAM_BB2025 = Roster(
    id = RosterId("jervis-tomb-kings"),
    name = "Tomb Kings",
    tier = 2,
    numberOfRerolls = 8,
    rerollCost = 60_000,
    allowApothecary = false,
    positions = listOf(
        SKELETON_LINEMEN,
        TOMB_KINGS_THROWERS,
        TOMB_KINGS_BLITZERS,
        TOMB_GUARDIANS,
    ),
    leagues = listOf(RegionalSpecialRule.SYLVANIAN_SPOTLIGHT),
    specialRules = listOf(TeamSpecialRule.MASTERS_OF_UNDEATH),
    logo = RosterLogo.NONE
)
