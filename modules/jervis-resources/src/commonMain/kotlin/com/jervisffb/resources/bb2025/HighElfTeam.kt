package com.jervisffb.resources.bb2025

import com.jervisffb.engine.model.PlayerKeyword
import com.jervisffb.engine.model.PlayerSize
import com.jervisffb.engine.model.PositionId
import com.jervisffb.engine.model.RosterId
import com.jervisffb.engine.rules.common.roster.RegionalSpecialRule
import com.jervisffb.engine.rules.common.roster.Roster
import com.jervisffb.engine.rules.common.roster.RosterPosition
import com.jervisffb.engine.rules.common.skills.SkillCategory.AGILITY
import com.jervisffb.engine.rules.common.skills.SkillCategory.GENERAL
import com.jervisffb.engine.rules.common.skills.SkillCategory.PASSING
import com.jervisffb.engine.rules.common.skills.SkillCategory.STRENGTH
import com.jervisffb.engine.rules.common.skills.SkillType.BLOCK
import com.jervisffb.engine.rules.common.skills.SkillType.CLAWS
import com.jervisffb.engine.rules.common.skills.SkillType.CLOUD_BURSTER
import com.jervisffb.engine.rules.common.skills.SkillType.MY_BALL
import com.jervisffb.engine.rules.common.skills.SkillType.PASS
import com.jervisffb.engine.rules.common.skills.SkillType.SAFE_PASS
import com.jervisffb.engine.rules.common.skills.SkillType.STEADY_FOOTING
import com.jervisffb.engine.rules.common.skills.SkillType.WRESTLE
import com.jervisffb.engine.sprites.RosterLogo
import com.jervisffb.engine.sprites.SingleSprite
import com.jervisffb.engine.sprites.SpriteSheet
import com.jervisffb.resources.iconRootPath
import com.jervisffb.resources.portraitRootPath
import kotlinx.serialization.Serializable

val HIGH_ELF_LINEMAN =
    RosterPosition(
        PositionId("high-elf-lineman"),
        16,
        "Linemen",
        "Lineman",
        "L",
        65_000,
        6, 3, 2, 3, 9,
        emptyList(),
        listOf(AGILITY, GENERAL),
        listOf(STRENGTH),
        emptyList(),
        listOf(PlayerKeyword.ELF, PlayerKeyword.LINEMAN),
        PlayerSize.STANDARD,
        SpriteSheet.ini("${iconRootPath}/highelf_lineman.png", 8),
        SingleSprite.ini("${portraitRootPath}/highelf_lineman.png")
    )

val PHOENIX_WARRIOR =
    RosterPosition(
        PositionId("high-elf-phoenix-warrior"),
        2,
        "Phoenix Warriors",
        "Phoenix Warrior",
        "P",
        90_000,
        6, 3, 2, 2, 9,
        listOf(CLOUD_BURSTER.id(), PASS.id(), SAFE_PASS.id()),
        listOf(AGILITY, GENERAL, PASSING),
        listOf(STRENGTH),
        emptyList(),
        listOf(PlayerKeyword.ELF, PlayerKeyword.THROWER),
        PlayerSize.STANDARD,
        SpriteSheet.ini("${iconRootPath}/highelf_thrower.png", 2),
        SingleSprite.ini("${portraitRootPath}/highelf_thrower.png")
    )

val WHITE_LION =
    RosterPosition(
        PositionId("high-elf-white-lion"),
        2,
        "White Lions",
        "White Lion",
        "W",
        110_000,
        7, 3, 2, 3, 9,
        listOf(CLAWS.id(), WRESTLE.id()),
        listOf(AGILITY, GENERAL),
        listOf(PASSING, STRENGTH),
        emptyList(),
        listOf(PlayerKeyword.ELF, PlayerKeyword.BLITZER),
        PlayerSize.STANDARD,
        SpriteSheet.ini("${iconRootPath}/highelf_catcher.png", 4),
        SingleSprite.ini("${portraitRootPath}/highelf_catcher.png")
    )

val DRAGON_PRINCE =
    RosterPosition(
        PositionId("high-elf-dragon-prince"),
        2,
        "Dragon Princes",
        "Dragon Prince",
        "D",
        110_000,
        8, 3, 2, 4, 9,
        listOf(BLOCK.id(), STEADY_FOOTING.id(), MY_BALL.id()),
        listOf(AGILITY, GENERAL),
        listOf(STRENGTH),
        emptyList(),
        listOf(PlayerKeyword.ELF, PlayerKeyword.BLITZER, PlayerKeyword.RUNNER),
        PlayerSize.STANDARD,
        SpriteSheet.ini("${iconRootPath}/highelf_blitzer.png", 2),
        SingleSprite.ini("${portraitRootPath}/highelf_blitzer.png")
    )

@Serializable
val HIGH_ELF_TEAM_BB2025 = Roster(
    id = RosterId("jervis-high-elf"),
    name = "High Elf",
    tier = 1,
    numberOfRerolls = 8,
    rerollCost = 50_000,
    allowApothecary = true,
    positions = listOf(
        HIGH_ELF_LINEMAN,
        PHOENIX_WARRIOR,
        WHITE_LION,
        DRAGON_PRINCE,
    ),
    leagues = listOf(RegionalSpecialRule.ELVEN_KINGDOMS_LEAGUE),
    specialRules = emptyList(),
    logo = RosterLogo.NONE
)
