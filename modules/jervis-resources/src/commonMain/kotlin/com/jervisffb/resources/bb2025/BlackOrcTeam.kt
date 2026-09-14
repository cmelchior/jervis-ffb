package com.jervisffb.resources.bb2025

import com.jervisffb.engine.model.PlayerKeyword
import com.jervisffb.engine.model.PlayerSize
import com.jervisffb.engine.model.PositionId
import com.jervisffb.engine.model.RosterId
import com.jervisffb.engine.rules.common.roster.RegionalSpecialRule
import com.jervisffb.engine.rules.common.roster.RegionalSpecialRule.BADLANDS_BRAWL
import com.jervisffb.engine.rules.common.roster.Roster
import com.jervisffb.engine.rules.common.roster.RosterPosition
import com.jervisffb.engine.rules.common.roster.TeamSpecialRule
import com.jervisffb.engine.rules.common.roster.TeamSpecialRule.BRAWLIN_BRUTES
import com.jervisffb.engine.rules.common.roster.TeamSpecialRule.BRIBERY_AND_CORRUPTION
import com.jervisffb.engine.rules.common.skills.SkillCategory.AGILITY
import com.jervisffb.engine.rules.common.skills.SkillCategory.DEVIOUS
import com.jervisffb.engine.rules.common.skills.SkillCategory.GENERAL
import com.jervisffb.engine.rules.common.skills.SkillCategory.PASSING
import com.jervisffb.engine.rules.common.skills.SkillCategory.STRENGTH
import com.jervisffb.engine.rules.common.skills.SkillType.ALWAYS_HUNGRY
import com.jervisffb.engine.rules.common.skills.SkillType.BRAWLER
import com.jervisffb.engine.rules.common.skills.SkillType.DODGE
import com.jervisffb.engine.rules.common.skills.SkillType.GRAB
import com.jervisffb.engine.rules.common.skills.SkillType.MIGHTY_BLOW
import com.jervisffb.engine.rules.common.skills.SkillType.PROJECTILE_VOMIT
import com.jervisffb.engine.rules.common.skills.SkillType.REALLY_STUPID
import com.jervisffb.engine.rules.common.skills.SkillType.REGENERATION
import com.jervisffb.engine.rules.common.skills.SkillType.RIGHT_STUFF
import com.jervisffb.engine.rules.common.skills.SkillType.STUNTY
import com.jervisffb.engine.rules.common.skills.SkillType.THICK_SKULL
import com.jervisffb.engine.rules.common.skills.SkillType.THROW_TEAMMATE
import com.jervisffb.engine.sprites.RosterLogo
import com.jervisffb.engine.sprites.SingleSprite
import com.jervisffb.engine.sprites.SpriteSheet
import com.jervisffb.resources.iconRootPath
import com.jervisffb.resources.portraitRootPath
import kotlinx.serialization.Serializable

val BLACK_ORC_GOBLIN_BRUISER = RosterPosition(
    PositionId("black_orc-goblin-bruiser"),
    16,
    "Goblin Bruisers",
    "Goblin Bruiser",
    "L",
    45000,
    6, 2, 3, 4, 8,
    listOf(DODGE.id(), THICK_SKULL.id(), RIGHT_STUFF.id(), STUNTY.id()),
    listOf(AGILITY, DEVIOUS),
    listOf(GENERAL, STRENGTH, PASSING),
    emptyList(),
    listOf(PlayerKeyword.GOBLIN),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/blackorc_goblinbruiserlineman.png"),
    SingleSprite.ini("${portraitRootPath}/blackorc_goblinbruiserlineman.png")
)

val BLACK_ORC_BLACK_ORC = RosterPosition(
    PositionId("black_orc-black-orc"),
    6,
    "Black Orcs",
    "Black Orc",
    "B",
    90000,
    4, 4, 4, 5, 10,
    listOf(BRAWLER.id(), GRAB.id()),
    listOf(GENERAL, STRENGTH),
    listOf(AGILITY, DEVIOUS),
    emptyList(),
    listOf(PlayerKeyword.ORC),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/blackorc_blackorc.png"),
    SingleSprite.ini("${portraitRootPath}/blackorc_blackorc.png")
)

val BLACK_ORC_TRAINED_TROLL = RosterPosition(
    PositionId("black_orc-trained-troll"),
    1,
    "Trained Trolls",
    "Trained Troll",
    "T",
    115000,
    4, 5, 5, 5, 10,
    listOf(MIGHTY_BLOW.idAdjustment(1), ALWAYS_HUNGRY.id(), PROJECTILE_VOMIT.id(), REALLY_STUPID.id(), REGENERATION.id(), THROW_TEAMMATE.id()),
    listOf(STRENGTH),
    listOf(GENERAL, AGILITY, PASSING),
    emptyList(),
    listOf(PlayerKeyword.TROLL, PlayerKeyword.BIG_GUY),
    PlayerSize.BIG_GUY,
    SpriteSheet.ini("${iconRootPath}/blackorc_trainedtroll.png"),
    SingleSprite.ini("${portraitRootPath}/blackorc_trainedtroll.png")
)

@Serializable
val BLACK_ORC_TEAM_BB2025 = Roster(
    id = RosterId("jervis-black-orc"),
    name = "Black Orc",
    tier = 2,
    numberOfRerolls = 8,
    rerollCost = 60000,
    allowApothecary = true,
    positions = listOf(BLACK_ORC_GOBLIN_BRUISER, BLACK_ORC_BLACK_ORC, BLACK_ORC_TRAINED_TROLL),
    leagues = listOf(RegionalSpecialRule.BADLANDS_BRAWL),
    specialRules = listOf(TeamSpecialRule.BRIBERY_AND_CORRUPTION, TeamSpecialRule.BRAWLIN_BRUTES),
    logo = RosterLogo(
        large = SingleSprite.fumbbl("641574"),
        small = SingleSprite.fumbbl("641574")
    )
)
