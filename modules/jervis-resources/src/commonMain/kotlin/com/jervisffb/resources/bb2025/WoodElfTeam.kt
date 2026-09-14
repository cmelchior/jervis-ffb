package com.jervisffb.resources.bb2025

import com.jervisffb.engine.model.PlayerKeyword
import com.jervisffb.engine.model.PlayerSize
import com.jervisffb.engine.model.PositionId
import com.jervisffb.engine.model.RosterId
import com.jervisffb.engine.rules.common.roster.RegionalSpecialRule
import com.jervisffb.engine.rules.common.roster.RegionalSpecialRule.ELVEN_KINGDOMS_LEAGUE
import com.jervisffb.engine.rules.common.roster.RegionalSpecialRule.WOODLAND_LEAGUE
import com.jervisffb.engine.rules.common.roster.Roster
import com.jervisffb.engine.rules.common.roster.RosterPosition
import com.jervisffb.engine.rules.common.skills.SkillCategory.AGILITY
import com.jervisffb.engine.rules.common.skills.SkillCategory.GENERAL
import com.jervisffb.engine.rules.common.skills.SkillCategory.PASSING
import com.jervisffb.engine.rules.common.skills.SkillCategory.STRENGTH
import com.jervisffb.engine.rules.common.skills.SkillType.BLOCK
import com.jervisffb.engine.rules.common.skills.SkillType.CATCH
import com.jervisffb.engine.rules.common.skills.SkillType.DODGE
import com.jervisffb.engine.rules.common.skills.SkillType.LEAP
import com.jervisffb.engine.rules.common.skills.SkillType.LONER
import com.jervisffb.engine.rules.common.skills.SkillType.MIGHTY_BLOW
import com.jervisffb.engine.rules.common.skills.SkillType.PASS
import com.jervisffb.engine.rules.common.skills.SkillType.SAFE_PAIR_OF_HANDS
import com.jervisffb.engine.rules.common.skills.SkillType.SPRINT
import com.jervisffb.engine.rules.common.skills.SkillType.STAND_FIRM
import com.jervisffb.engine.rules.common.skills.SkillType.STRONG_ARM
import com.jervisffb.engine.rules.common.skills.SkillType.TAKE_ROOT
import com.jervisffb.engine.rules.common.skills.SkillType.THICK_SKULL
import com.jervisffb.engine.rules.common.skills.SkillType.THROW_TEAMMATE
import com.jervisffb.engine.sprites.RosterLogo
import com.jervisffb.engine.sprites.SingleSprite
import com.jervisffb.engine.sprites.SpriteSheet
import com.jervisffb.resources.iconRootPath
import com.jervisffb.resources.portraitRootPath
import kotlinx.serialization.Serializable

val WOOD_ELF_WOOD_ELF_LINEMAN = RosterPosition(
    PositionId("wood_elf-wood-elf-lineman"),
    16,
    "Wood Elf Linemans",
    "Wood Elf Lineman",
    "L",
    65000,
    7, 3, 2, 3, 8,
    listOf(),
    listOf(GENERAL, AGILITY),
    listOf(STRENGTH),
    emptyList(),
    listOf(PlayerKeyword.ELF, PlayerKeyword.LINEMAN),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/woodelf_lineman.png"),
    SingleSprite.ini("${portraitRootPath}/woodelf_lineman.png")
)

val WOOD_ELF_WOOD_ELF_THROWER = RosterPosition(
    PositionId("wood_elf-wood-elf-thrower"),
    2,
    "Wood Elf Throwers",
    "Wood Elf Thrower",
    "T",
    85000,
    7, 3, 2, 2, 8,
    listOf(SAFE_PAIR_OF_HANDS.id(), PASS.id()),
    listOf(GENERAL, AGILITY, PASSING),
    listOf(STRENGTH),
    emptyList(),
    listOf(PlayerKeyword.ELF, PlayerKeyword.THROWER),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/woodelf_thrower.png"),
    SingleSprite.ini("${portraitRootPath}/woodelf_thrower.png")
)

val WOOD_ELF_WOOD_ELF_CATCHER = RosterPosition(
    PositionId("wood_elf-wood-elf-catcher"),
    2,
    "Wood Elf Catchers",
    "Wood Elf Catcher",
    "C",
    90000,
    8, 2, 2, 3, 8,
    listOf(CATCH.id(), DODGE.id(), SPRINT.id()),
    listOf(GENERAL, AGILITY),
    listOf(STRENGTH, PASSING),
    emptyList(),
    listOf(PlayerKeyword.ELF, PlayerKeyword.CATCHER),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/woodelf_catcher.png"),
    SingleSprite.ini("${portraitRootPath}/woodelf_catcher.png")
)

val WOOD_ELF_WARDANCER = RosterPosition(
    PositionId("wood_elf-wardancer"),
    2,
    "Wardancers",
    "Wardancer",
    "W",
    130000,
    8, 3, 2, 3, 8,
    listOf(DODGE.id(), LEAP.id(), BLOCK.id()),
    listOf(GENERAL, AGILITY),
    listOf(STRENGTH, PASSING),
    emptyList(),
    listOf(PlayerKeyword.ELF, PlayerKeyword.BLITZER),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/woodelf_wardancer.png"),
    SingleSprite.ini("${portraitRootPath}/woodelf_wardancer.png")
)

val WOOD_ELF_LOREN_FOREST_TREEMAN = RosterPosition(
    PositionId("wood_elf-loren-forest-treeman"),
    1,
    "Loren Forest Treemans",
    "Loren Forest Treeman",
    "Tr",
    120000,
    2, 6, 5, 5, 11,
    listOf(MIGHTY_BLOW.idAdjustment(1), STAND_FIRM.id(), STRONG_ARM.id(), THICK_SKULL.id(), LONER.idTarget(4), TAKE_ROOT.id(), THROW_TEAMMATE.id()),
    listOf(STRENGTH),
    listOf(GENERAL, AGILITY, PASSING),
    emptyList(),
    listOf(PlayerKeyword.ELF, PlayerKeyword.BIG_GUY),
    PlayerSize.BIG_GUY,
    SpriteSheet.ini("${iconRootPath}/woodelf_lorenforesttreeman.png"),
    SingleSprite.ini("${portraitRootPath}/woodelf_treeman.png")
)

@Serializable
val WOOD_ELF_TEAM_BB2025 = Roster(
    id = RosterId("jervis-wood-elf"),
    name = "Wood Elf",
    tier = 1,
    numberOfRerolls = 8,
    rerollCost = 50000,
    allowApothecary = true,
    positions = listOf(WOOD_ELF_WOOD_ELF_LINEMAN, WOOD_ELF_WOOD_ELF_THROWER, WOOD_ELF_WOOD_ELF_CATCHER, WOOD_ELF_WARDANCER, WOOD_ELF_LOREN_FOREST_TREEMAN),
    leagues = listOf(RegionalSpecialRule.ELVEN_KINGDOMS_LEAGUE, RegionalSpecialRule.WOODLAND_LEAGUE),
    specialRules = listOf(),
    logo = RosterLogo(
        large = SingleSprite.fumbbl("486361"),
        small = SingleSprite.fumbbl("486361")
    )
)
