package com.jervisffb.resources.bb2025

import com.jervisffb.engine.model.PlayerKeyword
import com.jervisffb.engine.model.PlayerSize
import com.jervisffb.engine.model.PositionId
import com.jervisffb.engine.model.RosterId
import com.jervisffb.engine.rules.common.roster.RegionalSpecialRule
import com.jervisffb.engine.rules.common.roster.RegionalSpecialRule.HAFLING_THIMBLE_CUP
import com.jervisffb.engine.rules.common.roster.RegionalSpecialRule.WOODLAND_LEAGUE
import com.jervisffb.engine.rules.common.roster.Roster
import com.jervisffb.engine.rules.common.roster.RosterPosition
import com.jervisffb.engine.rules.common.skills.SkillCategory.AGILITY
import com.jervisffb.engine.rules.common.skills.SkillCategory.DEVIOUS
import com.jervisffb.engine.rules.common.skills.SkillCategory.GENERAL
import com.jervisffb.engine.rules.common.skills.SkillCategory.PASSING
import com.jervisffb.engine.rules.common.skills.SkillCategory.STRENGTH
import com.jervisffb.engine.rules.common.skills.SkillType.DODGE
import com.jervisffb.engine.rules.common.skills.SkillType.GUARD
import com.jervisffb.engine.rules.common.skills.SkillType.JUMP_UP
import com.jervisffb.engine.rules.common.skills.SkillType.MIGHTY_BLOW
import com.jervisffb.engine.rules.common.skills.SkillType.MY_BALL
import com.jervisffb.engine.rules.common.skills.SkillType.RIGHT_STUFF
import com.jervisffb.engine.rules.common.skills.SkillType.SIDESTEP
import com.jervisffb.engine.rules.common.skills.SkillType.STAND_FIRM
import com.jervisffb.engine.rules.common.skills.SkillType.STRONG_ARM
import com.jervisffb.engine.rules.common.skills.SkillType.STUNTY
import com.jervisffb.engine.rules.common.skills.SkillType.TAKE_ROOT
import com.jervisffb.engine.rules.common.skills.SkillType.THICK_SKULL
import com.jervisffb.engine.rules.common.skills.SkillType.THROW_TEAMMATE
import com.jervisffb.engine.rules.common.skills.SkillType.TIMMMBER
import com.jervisffb.engine.rules.common.skills.SkillType.TRICKSTER
import com.jervisffb.engine.rules.common.skills.SkillType.WRESTLE
import com.jervisffb.engine.sprites.RosterLogo
import com.jervisffb.engine.sprites.SingleSprite
import com.jervisffb.engine.sprites.SpriteSheet
import com.jervisffb.resources.iconRootPath
import com.jervisffb.resources.portraitRootPath
import kotlinx.serialization.Serializable

val GNOME_ALTERN_FOREST_TREEMAN = RosterPosition(
    PositionId("gnome-altern-forest-treeman"),
    2,
    "Altern Forest Treemans",
    "Altern Forest Treeman",
    "T",
    120000,
    2, 6, 5, 5, 11,
    listOf(MIGHTY_BLOW.idAdjustment(1), STAND_FIRM.id(), STRONG_ARM.id(), THICK_SKULL.id(), TAKE_ROOT.id(), TIMMMBER.id(), THROW_TEAMMATE.id()),
    listOf(STRENGTH),
    listOf(GENERAL, AGILITY, PASSING),
    emptyList(),
    listOf(PlayerKeyword.ELF, PlayerKeyword.BIG_GUY),
    PlayerSize.BIG_GUY,
    SpriteSheet.ini("${iconRootPath}/gnome_alternforesttreeman.png"),
    SingleSprite.ini("${portraitRootPath}/gnome_alternforesttreeman.png")
)

val GNOME_GNOME_BEASTMASTER = RosterPosition(
    PositionId("gnome-gnome-beastmaster"),
    2,
    "Gnome Beastmasters",
    "Gnome Beastmaster",
    "B",
    55000,
    5, 2, 3, 4, 8,
    listOf(JUMP_UP.id(), WRESTLE.id(), GUARD.id(), STUNTY.id()),
    listOf(AGILITY),
    listOf(GENERAL, STRENGTH, DEVIOUS),
    emptyList(),
    listOf(PlayerKeyword.GNOME),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/gnome_gnomebeastmaster.png"),
    SingleSprite.ini("${portraitRootPath}/gnome_gnomebeastmaster.png")
)

val GNOME_GNOME_ILLUSIONIST = RosterPosition(
    PositionId("gnome-gnome-illusionist"),
    2,
    "Gnome Illusionists",
    "Gnome Illusionist",
    "I",
    50000,
    5, 2, 3, 3, 7,
    listOf(JUMP_UP.id(), WRESTLE.id(), STUNTY.id(), TRICKSTER.id()),
    listOf(AGILITY, PASSING),
    listOf(GENERAL, DEVIOUS),
    emptyList(),
    listOf(PlayerKeyword.GNOME),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/gnome_gnomeillusionist.png"),
    SingleSprite.ini("${portraitRootPath}/gnome_gnomeillusionist.png")
)

val GNOME_WOODLAND_FOX = RosterPosition(
    PositionId("gnome-woodland-fox"),
    2,
    "Woodland Foxs",
    "Woodland Fox",
    "F",
    50000,
    7, 2, 2, null, 6,
    listOf(DODGE.id(), SIDESTEP.id(), STUNTY.id(), MY_BALL.id()),
    listOf(),
    listOf(AGILITY),
    emptyList(),
    listOf(PlayerKeyword.ELF),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/gnome_woodlandfox.png"),
    SingleSprite.ini("${portraitRootPath}/gnome_woodlandfox.png")
)

val GNOME_GNOME_LINEMAN = RosterPosition(
    PositionId("gnome-gnome-lineman"),
    16,
    "Gnome Linemans",
    "Gnome Lineman",
    "L",
    40000,
    5, 2, 3, 4, 7,
    listOf(JUMP_UP.id(), WRESTLE.id(), RIGHT_STUFF.id(), STUNTY.id()),
    listOf(AGILITY),
    listOf(GENERAL, STRENGTH, DEVIOUS),
    emptyList(),
    listOf(PlayerKeyword.GNOME, PlayerKeyword.LINEMAN),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/gnome_gnomelineman.png"),
    SingleSprite.ini("${portraitRootPath}/gnome_gnomelineman.png")
)

@Serializable
val GNOME_TEAM_BB2025 = Roster(
    id = RosterId("jervis-gnome"),
    name = "Gnome",
    tier = 3,
    numberOfRerolls = 8,
    rerollCost = 50000,
    allowApothecary = true,
    positions = listOf(GNOME_ALTERN_FOREST_TREEMAN, GNOME_GNOME_BEASTMASTER, GNOME_GNOME_ILLUSIONIST, GNOME_WOODLAND_FOX, GNOME_GNOME_LINEMAN),
    leagues = listOf(RegionalSpecialRule.HAFLING_THIMBLE_CUP, RegionalSpecialRule.WOODLAND_LEAGUE),
    specialRules = listOf(),
    logo = RosterLogo(
        large = SingleSprite.fumbbl("733575"),
        small = SingleSprite.fumbbl("733575")
    )
)
