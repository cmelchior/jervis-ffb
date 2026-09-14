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
import com.jervisffb.engine.rules.common.skills.SkillType.CATCH
import com.jervisffb.engine.rules.common.skills.SkillType.DODGE
import com.jervisffb.engine.rules.common.skills.SkillType.FEND
import com.jervisffb.engine.rules.common.skills.SkillType.MIGHTY_BLOW
import com.jervisffb.engine.rules.common.skills.SkillType.RIGHT_STUFF
import com.jervisffb.engine.rules.common.skills.SkillType.SPRINT
import com.jervisffb.engine.rules.common.skills.SkillType.STAND_FIRM
import com.jervisffb.engine.rules.common.skills.SkillType.STRONG_ARM
import com.jervisffb.engine.rules.common.skills.SkillType.STUNTY
import com.jervisffb.engine.rules.common.skills.SkillType.TAKE_ROOT
import com.jervisffb.engine.rules.common.skills.SkillType.THICK_SKULL
import com.jervisffb.engine.rules.common.skills.SkillType.THROW_TEAMMATE
import com.jervisffb.engine.rules.common.skills.SkillType.TIMMMBER
import com.jervisffb.engine.sprites.RosterLogo
import com.jervisffb.engine.sprites.SingleSprite
import com.jervisffb.engine.sprites.SpriteSheet
import com.jervisffb.resources.iconRootPath
import com.jervisffb.resources.portraitRootPath
import kotlinx.serialization.Serializable

val HALFLING_HALFLING_HOPEFUL = RosterPosition(
    PositionId("halfling-halfling-hopeful"),
    16,
    "Halfling Hopefuls",
    "Halfling Hopeful",
    "L",
    30000,
    5, 2, 3, 4, 7,
    listOf(DODGE.id(), RIGHT_STUFF.id(), STUNTY.id()),
    listOf(AGILITY),
    listOf(GENERAL, STRENGTH, DEVIOUS),
    emptyList(),
    listOf(PlayerKeyword.HALFLING),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/halfling_halflinghopeful.png"),
    SingleSprite.ini("${portraitRootPath}/halfling_halflinghopeful.png")
)

val HALFLING_HALFLING_HEFTY = RosterPosition(
    PositionId("halfling-halfling-hefty"),
    2,
    "Halfling Hefties",
    "Halfling Hefty",
    "H",
    50000,
    5, 2, 3, 3, 8,
    listOf(DODGE.id(), FEND.id(), STUNTY.id()),
    listOf(AGILITY, PASSING),
    listOf(GENERAL, STRENGTH, DEVIOUS),
    emptyList(),
    listOf(PlayerKeyword.HALFLING),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/halfling_halflinghefty.png"),
    SingleSprite.ini("${portraitRootPath}/halfling_halflinghefty.png")
)

val HALFLING_HALFLING_CATCHER = RosterPosition(
    PositionId("halfling-halfling-catcher"),
    2,
    "Halfling Catchers",
    "Halfling Catcher",
    "C",
    55000,
    5, 2, 3, 4, 7,
    listOf(CATCH.id(), DODGE.id(), SPRINT.id(), RIGHT_STUFF.id(), STUNTY.id()),
    listOf(AGILITY),
    listOf(GENERAL, STRENGTH, DEVIOUS),
    emptyList(),
    listOf(PlayerKeyword.HALFLING, PlayerKeyword.CATCHER),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/halfling_halflingcatcher.png"),
    SingleSprite.ini("${portraitRootPath}/halfling_halflingcatcher.png")
)

val HALFLING_ALTERN_FOREST_TREEMAN = RosterPosition(
    PositionId("halfling-altern-forest-treeman"),
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
    SpriteSheet.ini("${iconRootPath}/halfling_treeman.png"),
    SingleSprite.ini("${portraitRootPath}/halfling_treeman.png")
)

@Serializable
val HALFLING_TEAM_BB2025 = Roster(
    id = RosterId("jervis-halfling"),
    name = "Halfling",
    tier = 3,
    numberOfRerolls = 8,
    rerollCost = 60000,
    allowApothecary = true,
    positions = listOf(HALFLING_HALFLING_HOPEFUL, HALFLING_HALFLING_HEFTY, HALFLING_HALFLING_CATCHER, HALFLING_ALTERN_FOREST_TREEMAN),
    leagues = listOf(RegionalSpecialRule.HAFLING_THIMBLE_CUP, RegionalSpecialRule.WOODLAND_LEAGUE),
    specialRules = listOf(),
    logo = RosterLogo(
        large = SingleSprite.fumbbl("486283"),
        small = SingleSprite.fumbbl("486283")
    )
)
