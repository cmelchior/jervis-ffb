package com.jervisffb.resources.bb2025

import com.jervisffb.engine.model.PlayerKeyword
import com.jervisffb.engine.model.PlayerSize
import com.jervisffb.engine.model.PositionId
import com.jervisffb.engine.model.RosterId
import com.jervisffb.engine.rules.common.roster.RegionalSpecialRule
import com.jervisffb.engine.rules.common.roster.RegionalSpecialRule.SYLVANIAN_SPOTLIGHT
import com.jervisffb.engine.rules.common.roster.Roster
import com.jervisffb.engine.rules.common.roster.RosterPosition
import com.jervisffb.engine.rules.common.roster.TeamSpecialRule
import com.jervisffb.engine.rules.common.roster.TeamSpecialRule.MASTERS_OF_UNDEATH
import com.jervisffb.engine.rules.common.skills.SkillCategory.AGILITY
import com.jervisffb.engine.rules.common.skills.SkillCategory.DEVIOUS
import com.jervisffb.engine.rules.common.skills.SkillCategory.GENERAL
import com.jervisffb.engine.rules.common.skills.SkillCategory.PASSING
import com.jervisffb.engine.rules.common.skills.SkillCategory.STRENGTH
import com.jervisffb.engine.rules.common.skills.SkillType.BLOCK
import com.jervisffb.engine.rules.common.skills.SkillType.DODGE
import com.jervisffb.engine.rules.common.skills.SkillType.EYE_GOUGE
import com.jervisffb.engine.rules.common.skills.SkillType.MIGHTY_BLOW
import com.jervisffb.engine.rules.common.skills.SkillType.REGENERATION
import com.jervisffb.engine.rules.common.skills.SkillType.TACKLE
import com.jervisffb.engine.rules.common.skills.SkillType.THICK_SKULL
import com.jervisffb.engine.rules.common.skills.SkillType.UNSTEADY
import com.jervisffb.engine.sprites.RosterLogo
import com.jervisffb.engine.sprites.SingleSprite
import com.jervisffb.engine.sprites.SpriteSheet
import com.jervisffb.resources.iconRootPath
import com.jervisffb.resources.portraitRootPath
import kotlinx.serialization.Serializable

val SHAMBLING_UNDEAD_SKELETON_LINEMAN = RosterPosition(
    PositionId("shambling_undead-skeleton-lineman"),
    16,
    "Skeleton Linemans",
    "Skeleton Lineman",
    "S",
    40000,
    5, 3, 4, 6, 8,
    listOf(THICK_SKULL.id(), REGENERATION.id()),
    listOf(GENERAL),
    listOf(AGILITY, STRENGTH, DEVIOUS),
    emptyList(),
    listOf(PlayerKeyword.UNDEAD, PlayerKeyword.LINEMAN),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/shamblingundead_skeleton.png"),
    SingleSprite.ini("${portraitRootPath}/shamblingundead_skeleton.png")
)

val SHAMBLING_UNDEAD_ZOMBIE_LINEMAN = RosterPosition(
    PositionId("shambling_undead-zombie-lineman"),
    16,
    "Zombie Linemans",
    "Zombie Lineman",
    "Z",
    40000,
    4, 3, 4, 6, 9,
    listOf(REGENERATION.id(), EYE_GOUGE.id(), UNSTEADY.id()),
    listOf(GENERAL, DEVIOUS),
    listOf(AGILITY, STRENGTH),
    emptyList(),
    listOf(PlayerKeyword.ZOMBIE, PlayerKeyword.LINEMAN),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/shamblingundead_zombie.png"),
    SingleSprite.ini("${portraitRootPath}/shamblingundead_zombie.png")
)

val SHAMBLING_UNDEAD_GHOUL_RUNNER = RosterPosition(
    PositionId("shambling_undead-ghoul-runner"),
    2,
    "Ghoul Runners",
    "Ghoul Runner",
    "G",
    75000,
    7, 3, 3, 3, 8,
    listOf(DODGE.id(), REGENERATION.id()),
    listOf(GENERAL, AGILITY),
    listOf(STRENGTH, PASSING, DEVIOUS),
    emptyList(),
    listOf(PlayerKeyword.UNDEAD, PlayerKeyword.RUNNER),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/shamblingundead_ghoulrunner.png"),
    SingleSprite.ini("${portraitRootPath}/shamblingundead_ghoulrunner.png")
)

val SHAMBLING_UNDEAD_WIGHT_BLITZER = RosterPosition(
    PositionId("shambling_undead-wight-blitzer"),
    2,
    "Wight Blitzers",
    "Wight Blitzer",
    "W",
    95000,
    6, 3, 3, 5, 9,
    listOf(BLOCK.id(), TACKLE.id(), THICK_SKULL.id(), REGENERATION.id()),
    listOf(GENERAL, STRENGTH),
    listOf(AGILITY, DEVIOUS),
    emptyList(),
    listOf(PlayerKeyword.UNDEAD, PlayerKeyword.BLITZER),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/shamblingundead_wightblitzer.png"),
    SingleSprite.ini("${portraitRootPath}/shamblingundead_wightblitzer.png")
)

val SHAMBLING_UNDEAD_MUMMY = RosterPosition(
    PositionId("shambling_undead-mummy"),
    2,
    "Mummies",
    "Mummy",
    "M",
    125000,
    3, 5, 5, 6, 10,
    listOf(MIGHTY_BLOW.idAdjustment(1), REGENERATION.id()),
    listOf(STRENGTH),
    listOf(GENERAL, AGILITY),
    emptyList(),
    listOf(PlayerKeyword.UNDEAD, PlayerKeyword.BIG_GUY),
    PlayerSize.BIG_GUY,
    SpriteSheet.ini("${iconRootPath}/shamblingundead_mummy.png"),
    SingleSprite.ini("${portraitRootPath}/shamblingundead_mummy.png")
)

@Serializable
val SHAMBLING_UNDEAD_TEAM_BB2025 = Roster(
    id = RosterId("jervis-shambling-undead"),
    name = "Shambling Undead",
    tier = 2,
    numberOfRerolls = 8,
    rerollCost = 70000,
    allowApothecary = false,
    positions = listOf(SHAMBLING_UNDEAD_SKELETON_LINEMAN, SHAMBLING_UNDEAD_ZOMBIE_LINEMAN, SHAMBLING_UNDEAD_GHOUL_RUNNER, SHAMBLING_UNDEAD_WIGHT_BLITZER, SHAMBLING_UNDEAD_MUMMY),
    leagues = listOf(RegionalSpecialRule.SYLVANIAN_SPOTLIGHT),
    specialRules = listOf(TeamSpecialRule.MASTERS_OF_UNDEATH),
    logo = RosterLogo(
        large = SingleSprite.fumbbl("486349"),
        small = SingleSprite.fumbbl("486349")
    )
)
