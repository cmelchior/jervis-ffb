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
import com.jervisffb.engine.rules.common.skills.SkillType.DECAY
import com.jervisffb.engine.rules.common.skills.SkillType.DISTURBING_PRESENCE
import com.jervisffb.engine.rules.common.skills.SkillType.FOUL_APPEARANCE
import com.jervisffb.engine.rules.common.skills.SkillType.HORNS
import com.jervisffb.engine.rules.common.skills.SkillType.LONER
import com.jervisffb.engine.rules.common.skills.SkillType.MIGHTY_BLOW
import com.jervisffb.engine.rules.common.skills.SkillType.PICK_ME_UP
import com.jervisffb.engine.rules.common.skills.SkillType.PLAGUE_RIDDEN
import com.jervisffb.engine.rules.common.skills.SkillType.REALLY_STUPID
import com.jervisffb.engine.rules.common.skills.SkillType.REGENERATION
import com.jervisffb.engine.rules.common.skills.SkillType.STAND_FIRM
import com.jervisffb.engine.rules.common.skills.SkillType.STEADY_FOOTING
import com.jervisffb.engine.rules.common.skills.SkillType.TENTACLES
import com.jervisffb.engine.rules.common.skills.SkillType.THICK_SKULL
import com.jervisffb.engine.rules.common.skills.SkillType.UNSTEADY
import com.jervisffb.engine.sprites.RosterLogo
import com.jervisffb.engine.sprites.SingleSprite
import com.jervisffb.engine.sprites.SpriteSheet
import com.jervisffb.resources.iconRootPath
import com.jervisffb.resources.portraitRootPath
import kotlinx.serialization.Serializable

val ROTTER_LINEMEN =
    RosterPosition(
        PositionId("nurgle-rotter-lineman"),
        16,
        "Rotter Linemen",
        "Rotter Lineman",
        "R",
        40_000,
        5, 3, 4, 6, 9,
        listOf(DECAY.id(), PLAGUE_RIDDEN.id()),
        listOf(DEVIOUS, GENERAL, MUTATIONS),
        listOf(AGILITY, STRENGTH),
        emptyList(),
        listOf(PlayerKeyword.HUMAN, PlayerKeyword.LINEMAN),
        PlayerSize.STANDARD,
        SpriteSheet.ini("$iconRootPath/nurgle_rotter.png", 6),
        SingleSprite.ini("$portraitRootPath/nurgle_rotter.png")
    )

val PESTIGORS =
    RosterPosition(
        PositionId("nurgle-pestigor"),
        4,
        "Pestigors",
        "Pestigor",
        "P",
        70_000,
        6, 3, 3, 4, 9,
        listOf(
            HORNS.id(),
            STEADY_FOOTING.id(),
            THICK_SKULL.id(),
            PLAGUE_RIDDEN.id(),
            REGENERATION.id(),
        ),
        listOf(GENERAL, MUTATIONS, STRENGTH),
        listOf(AGILITY, DEVIOUS, PASSING),
        emptyList(),
        listOf(PlayerKeyword.BEASTMAN, PlayerKeyword.RUNNER),
        PlayerSize.STANDARD,
        SpriteSheet.ini("$iconRootPath/nurgle_pestigor.png", 4),
        SingleSprite.ini("$portraitRootPath/nurgle_pestigor.png")
    )

val BLOATERS =
    RosterPosition(
        PositionId("nurgle-bloater"),
        4,
        "Bloaters",
        "Bloater",
        "B",
        110_000,
        4, 4, 4, 6, 10,
        listOf(
            DISTURBING_PRESENCE.id(),
            FOUL_APPEARANCE.id(),
            PLAGUE_RIDDEN.id(),
            REGENERATION.id(),
            STAND_FIRM.id(),
            UNSTEADY.id(),
        ),
        listOf(GENERAL, MUTATIONS, STRENGTH),
        listOf(AGILITY, DEVIOUS),
        emptyList(),
        listOf(PlayerKeyword.BLOCKER, PlayerKeyword.HUMAN),
        PlayerSize.STANDARD,
        SpriteSheet.ini("$iconRootPath/nurgle_bloater.png", 4),
        SingleSprite.ini("$portraitRootPath/nurgle_bloater.png")
    )

val ROTSPAWN =
    RosterPosition(
        PositionId("nurgle-rotspawn"),
        1,
        "Rotspawn",
        "Rotspawn",
        "R",
        140_000,
        4, 5, 5, 6, 10,
        listOf(
            DISTURBING_PRESENCE.id(),
            FOUL_APPEARANCE.id(),
            LONER.idTarget(4),
            MIGHTY_BLOW.id(),
            PLAGUE_RIDDEN.id(),
            REALLY_STUPID.id(),
            REGENERATION.id(),
            TENTACLES.id(),
            PICK_ME_UP.id(),
        ),
        listOf(STRENGTH),
        listOf(DEVIOUS, GENERAL, MUTATIONS),
        emptyList(),
        listOf(PlayerKeyword.BIG_GUY, PlayerKeyword.SPAWN),
        PlayerSize.BIG_GUY,
        SpriteSheet.ini("$iconRootPath/nurgle_rotspawn.png", 1),
        SingleSprite.ini("$portraitRootPath/nurgle_rotspawn.png")
    )

@Serializable
val NURGLE_TEAM_BB2025 = Roster(
    id = RosterId("jervis-nurgle"),
    name = "Nurgle",
    tier = 3,
    numberOfRerolls = 8,
    rerollCost = 60_000,
    allowApothecary = false,
    positions = listOf(
        ROTTER_LINEMEN,
        PESTIGORS,
        BLOATERS,
        ROTSPAWN,
    ),
    leagues = listOf(RegionalSpecialRule.CHAOS_CLASH),
    specialRules = listOf(
        TeamSpecialRule.FAVOURED_OF_NURGLE,
        TeamSpecialRule.BRAWLIN_BRUTES,
    ),
    logo = RosterLogo.NONE
)
