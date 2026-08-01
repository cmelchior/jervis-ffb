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
import com.jervisffb.engine.rules.common.skills.SkillType.ALWAYS_HUNGRY
import com.jervisffb.engine.rules.common.skills.SkillType.BLOCK
import com.jervisffb.engine.rules.common.skills.SkillType.BREAK_TACKLE
import com.jervisffb.engine.rules.common.skills.SkillType.DODGE
import com.jervisffb.engine.rules.common.skills.SkillType.LONER
import com.jervisffb.engine.rules.common.skills.SkillType.MIGHTY_BLOW
import com.jervisffb.engine.rules.common.skills.SkillType.PASS
import com.jervisffb.engine.rules.common.skills.SkillType.PROJECTILE_VOMIT
import com.jervisffb.engine.rules.common.skills.SkillType.REALLY_STUPID
import com.jervisffb.engine.rules.common.skills.SkillType.REGENERATION
import com.jervisffb.engine.rules.common.skills.SkillType.RIGHT_STUFF
import com.jervisffb.engine.rules.common.skills.SkillType.STUNTY
import com.jervisffb.engine.rules.common.skills.SkillType.SURE_HANDS
import com.jervisffb.engine.rules.common.skills.SkillType.TAUNT
import com.jervisffb.engine.rules.common.skills.SkillType.THICK_SKULL
import com.jervisffb.engine.rules.common.skills.SkillType.THROW_TEAMMATE
import com.jervisffb.engine.rules.common.skills.SkillType.UNSTEADY
import com.jervisffb.engine.sprites.RosterLogo
import com.jervisffb.engine.sprites.SingleSprite
import com.jervisffb.engine.sprites.SpriteSheet
import com.jervisffb.resources.iconRootPath
import com.jervisffb.resources.portraitRootPath
import kotlinx.serialization.Serializable

/**
 * Orc Team
 */

val ORC_LINEMEN =
    RosterPosition(
        PositionId("orc-lineman"),
        16,
        "Orc Linemen",
        "Orc Lineman",
        "L",
        50_000,
        5, 3, 3, 4, 10,
        emptyList(),
        listOf(GENERAL, STRENGTH),
        listOf(AGILITY, DEVIOUS),
        emptyList(),
        listOf(PlayerKeyword.ORC, PlayerKeyword.LINEMAN),
        PlayerSize.STANDARD,
        SpriteSheet.ini("${iconRootPath}/orc_lineman.png",6),
        SingleSprite.ini("${portraitRootPath}/orc_lineman.png")
    )
val ORC_THROWER =
    RosterPosition(
        PositionId("orc-thrower"),
        2,
        "Orc Throwers",
        "Orc Thrower",
        "Tr",
        75_000,
        6, 3, 3, 3, 9,
        listOf(
            PASS.id(),
            SURE_HANDS.id()
        ),
        listOf(GENERAL, PASSING),
        listOf(AGILITY, STRENGTH, DEVIOUS),
        emptyList(),
        listOf(PlayerKeyword.ORC, PlayerKeyword.THROWER),
        PlayerSize.STANDARD,
        SpriteSheet.ini("${iconRootPath}/orc_thrower.png",2),
        SingleSprite.ini("${portraitRootPath}/orc_thrower.png")
    )
val ORC_BLITZER =
    RosterPosition(
        PositionId("orc-blitzer"),
        2,
        "Orc Blitzers",
        "Orc Blitzer",
        "B",
        85_000,
        6, 3, 3, 4, 10,
        listOf(
            BLOCK.id(),
            BREAK_TACKLE.id()
        ),
        listOf(GENERAL, STRENGTH),
        listOf(AGILITY, DEVIOUS),
        emptyList(),
        listOf(PlayerKeyword.ORC, PlayerKeyword.BLITZER),
        PlayerSize.STANDARD,
        SpriteSheet.ini("${iconRootPath}/orc_blitzer.png", 4),
        SingleSprite.ini("${portraitRootPath}/orc_blitzer.png")
    )
val BIG_UN_BLOCKERS =
    RosterPosition(
        PositionId("orc-bigunblocker"),
        2,
        "Big Un Blockers",
        "Big Un Blocker",
        "Bu",
        95_000,
        5, 4, 4, 6, 10,
        listOf(
            MIGHTY_BLOW.idAdjustment(1),
            THICK_SKULL.id(),
            TAUNT.id(),
            UNSTEADY.id()
        ),
        listOf(GENERAL, STRENGTH),
        listOf(AGILITY, DEVIOUS),
        emptyList(),
        listOf(PlayerKeyword.ORC, PlayerKeyword.BLOCKER),
        PlayerSize.STANDARD,
        SpriteSheet.ini("${iconRootPath}/orc_bigunblocker.png", 4),
        SingleSprite.ini("${portraitRootPath}/orc_bigunblocker.png")
    )
val GOBLIN_LINEMEN =
    RosterPosition(
        PositionId("orc-goblin"),
        4,
        "Goblin Linemen",
        "Goblin Lineman",
        "G",
        40_000,
        6, 2, 3, 4, 8,
        listOf(
            DODGE.id(),
            RIGHT_STUFF.id(),
            STUNTY.id()
        ),
        listOf(AGILITY, DEVIOUS),
        listOf(GENERAL, STRENGTH, PASSING),
        emptyList(),
        listOf(PlayerKeyword.GOBLIN, PlayerKeyword.LINEMAN),
        PlayerSize.STANDARD,
        SpriteSheet.ini("${iconRootPath}/orc_goblin.png", 4),
        SingleSprite.ini("${portraitRootPath}/orc_goblin.png")
    )
val UNTRAINED_TROLL =
    RosterPosition(
        PositionId("orc-troll"),
        1,
        "Untrained Troll",
        "Untrained Troll",
        "T",
        115_000,
        4, 5, 5, 5, 10,
        listOf(
            ALWAYS_HUNGRY.id(),
            LONER.idTarget(4),
            MIGHTY_BLOW.idAdjustment(1),
            PROJECTILE_VOMIT.id(),
            REALLY_STUPID.id(),
            REGENERATION.id(),
            THROW_TEAMMATE.id()
        ),
        listOf(STRENGTH),
        listOf(AGILITY, GENERAL, PASSING),
        emptyList(),
        listOf(PlayerKeyword.TROLL, PlayerKeyword.BIG_GUY),
        PlayerSize.BIG_GUY,
        SpriteSheet.ini("${iconRootPath}/orc_troll.png", 1),
        SingleSprite.ini("${portraitRootPath}/orc_troll.png")
    )

@Serializable
val ORC_TEAM_BB2025 = Roster(
    id = RosterId("jervis-orc"),
    name = "Orc",
    tier = 2,
    numberOfRerolls = 8,
    rerollCost = 60_000,
    allowApothecary = true,
    positions = listOf(
        ORC_LINEMEN,
        ORC_THROWER,
        ORC_BLITZER,
        BIG_UN_BLOCKERS,
        GOBLIN_LINEMEN,
        UNTRAINED_TROLL,
    ),
    leagues = listOf(RegionalSpecialRule.BADLANDS_BRAWL),
    specialRules = listOf(TeamSpecialRule.BRAWLIN_BRUTES, TeamSpecialRule.TEAM_CAPTAIN),
    logo = RosterLogo(
        large = SingleSprite.embedded("jervis/roster/logo_orc_large.png"),
        small = SingleSprite.embedded("jervis/roster/logo_orc_small.png")
    )
)
