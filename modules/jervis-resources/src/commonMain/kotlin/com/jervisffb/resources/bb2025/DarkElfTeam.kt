package com.jervisffb.resources.bb2025

import com.jervisffb.engine.model.PlayerKeyword
import com.jervisffb.engine.model.PlayerSize
import com.jervisffb.engine.model.PositionId
import com.jervisffb.engine.model.RosterId
import com.jervisffb.engine.rules.common.roster.RegionalSpecialRule
import com.jervisffb.engine.rules.common.roster.RegionalSpecialRule.ELVEN_KINGDOMS_LEAGUE
import com.jervisffb.engine.rules.common.roster.Roster
import com.jervisffb.engine.rules.common.roster.RosterPosition
import com.jervisffb.engine.rules.common.skills.SkillCategory.AGILITY
import com.jervisffb.engine.rules.common.skills.SkillCategory.DEVIOUS
import com.jervisffb.engine.rules.common.skills.SkillCategory.GENERAL
import com.jervisffb.engine.rules.common.skills.SkillCategory.PASSING
import com.jervisffb.engine.rules.common.skills.SkillCategory.STRENGTH
import com.jervisffb.engine.rules.common.skills.SkillType.BLOCK
import com.jervisffb.engine.rules.common.skills.SkillType.DODGE
import com.jervisffb.engine.rules.common.skills.SkillType.DUMP_OFF
import com.jervisffb.engine.rules.common.skills.SkillType.FRENZY
import com.jervisffb.engine.rules.common.skills.SkillType.HIT_AND_RUN
import com.jervisffb.engine.rules.common.skills.SkillType.JUMP_UP
import com.jervisffb.engine.rules.common.skills.SkillType.PUNT
import com.jervisffb.engine.rules.common.skills.SkillType.SHADOWING
import com.jervisffb.engine.rules.common.skills.SkillType.STAB
import com.jervisffb.engine.sprites.RosterLogo
import com.jervisffb.engine.sprites.SingleSprite
import com.jervisffb.engine.sprites.SpriteSheet
import com.jervisffb.resources.iconRootPath
import com.jervisffb.resources.portraitRootPath
import kotlinx.serialization.Serializable

val DARK_ELF_DARK_ELF_LINEMAN = RosterPosition(
    PositionId("dark_elf-dark-elf-lineman"),
    16,
    "Dark Elf Linemans",
    "Dark Elf Lineman",
    "L",
    65000,
    6, 3, 2, 3, 9,
    listOf(),
    listOf(GENERAL, AGILITY),
    listOf(STRENGTH, DEVIOUS),
    emptyList(),
    listOf(PlayerKeyword.ELF, PlayerKeyword.LINEMAN),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/darkelf_darkelflineman.png"),
    SingleSprite.ini("${portraitRootPath}/darkelf_lineman.png")
)

val DARK_ELF_DARK_ELF_RUNNER = RosterPosition(
    PositionId("dark_elf-dark-elf-runner"),
    2,
    "Dark Elf Runners",
    "Dark Elf Runner",
    "R",
    80000,
    7, 3, 2, 3, 8,
    listOf(DUMP_OFF.id(), PUNT.id()),
    listOf(GENERAL, AGILITY, PASSING),
    listOf(STRENGTH, DEVIOUS),
    emptyList(),
    listOf(PlayerKeyword.ELF, PlayerKeyword.RUNNER),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/darkelf_runner.png"),
    SingleSprite.ini("${portraitRootPath}/darkelf_runner.png")
)

val DARK_ELF_DARK_ELF_BLITZER = RosterPosition(
    PositionId("dark_elf-dark-elf-blitzer"),
    2,
    "Dark Elf Blitzers",
    "Dark Elf Blitzer",
    "Z",
    105000,
    7, 3, 2, 3, 9,
    listOf(BLOCK.id()),
    listOf(GENERAL, AGILITY),
    listOf(STRENGTH, PASSING, DEVIOUS),
    emptyList(),
    listOf(PlayerKeyword.ELF, PlayerKeyword.BLITZER),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/darkelf_blitzer.png"),
    SingleSprite.ini("${portraitRootPath}/darkelf_blitzer.png")
)

val DARK_ELF_DARK_ELF_ASSASSIN = RosterPosition(
    PositionId("dark_elf-dark-elf-assassin"),
    2,
    "Dark Elf Assassins",
    "Dark Elf Assassin",
    "A",
    90000,
    7, 3, 2, 4, 8,
    listOf(SHADOWING.id(), STAB.id(), HIT_AND_RUN.id()),
    listOf(AGILITY, DEVIOUS),
    listOf(GENERAL, STRENGTH),
    emptyList(),
    listOf(PlayerKeyword.ELF),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/darkelf_assassin.png"),
    SingleSprite.ini("${portraitRootPath}/darkelf_assassin.png")
)

val DARK_ELF_WITCH_ELF = RosterPosition(
    PositionId("dark_elf-witch-elf"),
    2,
    "Witches",
    "Witch Elf",
    "WE",
    110000,
    7, 3, 2, 4, 8,
    listOf(DODGE.id(), JUMP_UP.id(), FRENZY.id()),
    listOf(GENERAL, AGILITY),
    listOf(STRENGTH, DEVIOUS),
    emptyList(),
    listOf(),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/darkelf_witchelf.png"),
    SingleSprite.ini("${portraitRootPath}/darkelf_witchelf.png")
)

@Serializable
val DARK_ELF_TEAM_BB2025 = Roster(
    id = RosterId("jervis-dark-elf"),
    name = "Dark Elf",
    tier = 1,
    numberOfRerolls = 8,
    rerollCost = 50000,
    allowApothecary = true,
    positions = listOf(DARK_ELF_DARK_ELF_LINEMAN, DARK_ELF_DARK_ELF_RUNNER, DARK_ELF_DARK_ELF_BLITZER, DARK_ELF_DARK_ELF_ASSASSIN, DARK_ELF_WITCH_ELF),
    leagues = listOf(RegionalSpecialRule.ELVEN_KINGDOMS_LEAGUE),
    specialRules = listOf(),
    logo = RosterLogo(
        large = SingleSprite.fumbbl("486259"),
        small = SingleSprite.fumbbl("486259")
    )
)
