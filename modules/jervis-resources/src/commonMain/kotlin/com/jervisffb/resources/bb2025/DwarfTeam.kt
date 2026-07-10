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
import com.jervisffb.engine.rules.common.skills.SkillType.BREAK_TACKLE
import com.jervisffb.engine.rules.common.skills.SkillType.DAUNTLESS
import com.jervisffb.engine.rules.common.skills.SkillType.DEFENSIVE
import com.jervisffb.engine.rules.common.skills.SkillType.DIRTY_PLAYER
import com.jervisffb.engine.rules.common.skills.SkillType.DIVING_TACKLE
import com.jervisffb.engine.rules.common.skills.SkillType.FRENZY
import com.jervisffb.engine.rules.common.skills.SkillType.HATRED
import com.jervisffb.engine.rules.common.skills.SkillType.JUGGERNAUT
import com.jervisffb.engine.rules.common.skills.SkillType.LONER
import com.jervisffb.engine.rules.common.skills.SkillType.MIGHTY_BLOW
import com.jervisffb.engine.rules.common.skills.SkillType.NO_BALL
import com.jervisffb.engine.rules.common.skills.SkillType.SECRET_WEAPON
import com.jervisffb.engine.rules.common.skills.SkillType.SPRINT
import com.jervisffb.engine.rules.common.skills.SkillType.STAND_FIRM
import com.jervisffb.engine.rules.common.skills.SkillType.SURE_HANDS
import com.jervisffb.engine.rules.common.skills.SkillType.TACKLE
import com.jervisffb.engine.rules.common.skills.SkillType.THICK_SKULL
import com.jervisffb.engine.serialize.RosterLogo
import com.jervisffb.engine.serialize.SingleSprite
import com.jervisffb.engine.serialize.SpriteSheet
import com.jervisffb.resources.iconRootPath
import com.jervisffb.resources.portraitRootPath
import kotlinx.serialization.Serializable

/**
 * Dwarf team
 */
val DWARF_LINEMAN =
    RosterPosition(
        PositionId("dwarf-lineman"),
        16,
        "Dwarf Linemen",
        "Dwarf Lineman",
        "L",
        70_000,
        4, 3, 4, 5, 10,
        listOf(
            BLOCK.id(),
            DEFENSIVE.id(),
            THICK_SKULL.id()
        ),
        listOf(DEVIOUS, GENERAL),
        listOf(STRENGTH),
        emptyList(),
        listOf(PlayerKeyword.DWARF, PlayerKeyword.LINEMAN),
        PlayerSize.STANDARD,
        SpriteSheet.ini("${iconRootPath}/dwarf_blocker.png",6),
        SingleSprite.ini("${portraitRootPath}/dwarf_blocker.png")
    )

val DWARF_RUNNER =
    RosterPosition(
        PositionId("dwarf-runner"),
        2,
        "Dwarf Runner",
        "Dwarf Runner",
        "R",
        80_000,
        6, 3, 3, 4, 9,
        listOf(
            SPRINT.id(),
            SURE_HANDS.id(),
            THICK_SKULL.id()
        ),
        listOf(GENERAL, PASSING),
        listOf(STRENGTH, AGILITY),
        emptyList(),
        listOf(PlayerKeyword.DWARF, PlayerKeyword.RUNNER),
        PlayerSize.STANDARD,
        SpriteSheet.ini("${iconRootPath}/dwarf_runner.png",3),
        SingleSprite.ini("${portraitRootPath}/dwarf_runner.png")
    )

val DWARF_BLITZER =
    RosterPosition(
        PositionId("dwarf-blitzer"),
        2,
        "Dwarf Blitzer",
        "Dwarf Blitzer",
        "B",
        100_000,
        5, 3, 4, 4, 10,
        listOf(
            BLOCK.id(),
            DIVING_TACKLE.id(),
            TACKLE.id(),
            THICK_SKULL.id()
        ),
        listOf(GENERAL, STRENGTH),
        listOf(PASSING),
        emptyList(),
        listOf(PlayerKeyword.DWARF, PlayerKeyword.BLITZER),
        PlayerSize.STANDARD,
        SpriteSheet.ini("${iconRootPath}/dwarf_blitzer.png",2),
        SingleSprite.ini("${portraitRootPath}/dwarf_blitzer.png")
    )

val TROLL_SLAYER =
    RosterPosition(
        PositionId("dwarf-slayer"),
        2,
        "Troll Slayer",
        "Troll Slayer",
        "Ts",
        95_000,
        5, 3, 4, 5, 9,
        listOf(
            BLOCK.id(),
            DAUNTLESS.id(),
            FRENZY.id(),
            HATRED.id(PlayerKeyword.TROLL),
            THICK_SKULL.id()
        ),
        listOf(GENERAL, STRENGTH),
        listOf(DEVIOUS),
        emptyList(),
        listOf(PlayerKeyword.DWARF, PlayerKeyword.SPECIAL),
        PlayerSize.STANDARD,
        SpriteSheet.ini("${iconRootPath}/dwarf_trollslayer.png",1),
        SingleSprite.ini("${portraitRootPath}/dwarf_trollslayer.png")
    )

val DEATHROLLER =
    RosterPosition(
        PositionId("dwarf-deathroller"),
        1,
        "Deathroller",
        "Deathroller",
        "D",
        170_000,
        5, 7, 5, null, 11,
        listOf(
            BREAK_TACKLE.id(),
            DIRTY_PLAYER.id(),
            JUGGERNAUT.id(),
            LONER.id(4),
            MIGHTY_BLOW.id(),
            NO_BALL.id(),
            SECRET_WEAPON.id(),
            STAND_FIRM.id(),
        ),
        listOf(DEVIOUS, STRENGTH),
        listOf(GENERAL),
        emptyList(),
        listOf(PlayerKeyword.BIG_GUY, PlayerKeyword.DWARF, PlayerKeyword.SPECIAL),
        PlayerSize.BIG_GUY,
        SpriteSheet.ini("${iconRootPath}/dwarf_deathroller.png",1),
        SingleSprite.ini("${portraitRootPath}/dwarf_deathroller.png")
    )

@Serializable
val DWARF_TEAM_BB2025 = Roster(
    id = RosterId("jervis-dwarf"),
    name = "Dwarf",
    tier = 2,
    numberOfRerolls = 8,
    rerollCost = 60_000,
    allowApothecary = true,
    positions = listOf(
        DWARF_LINEMAN,
        DWARF_RUNNER,
        DWARF_BLITZER,
        TROLL_SLAYER,
        DEATHROLLER,
    ),
    leagues = listOf(RegionalSpecialRule.WORLDS_EDGE_SUPERLEAGUE),
    specialRules = listOf(TeamSpecialRule.BRAWLIN_BRUTES, TeamSpecialRule.BRIBERY_AND_CORRUPTION),
    logo = RosterLogo(
        large = SingleSprite.embedded("jervis/roster/logo_dwarf_large.png"),
        small = SingleSprite.embedded("jervis/roster/logo_dwarf_small.png")
    )
)
