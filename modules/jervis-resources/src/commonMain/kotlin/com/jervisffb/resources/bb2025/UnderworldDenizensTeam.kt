package com.jervisffb.resources.bb2025

import com.jervisffb.engine.model.PlayerKeyword
import com.jervisffb.engine.model.PlayerSize
import com.jervisffb.engine.model.PositionId
import com.jervisffb.engine.model.RosterId
import com.jervisffb.engine.rules.common.roster.RegionalSpecialRule
import com.jervisffb.engine.rules.common.roster.RegionalSpecialRule.UNDERWORLD_CHALLENGE
import com.jervisffb.engine.rules.common.roster.Roster
import com.jervisffb.engine.rules.common.roster.RosterPosition
import com.jervisffb.engine.rules.common.roster.TeamSpecialRule
import com.jervisffb.engine.rules.common.roster.TeamSpecialRule.BRIBERY_AND_CORRUPTION
import com.jervisffb.engine.rules.common.skills.SkillCategory.AGILITY
import com.jervisffb.engine.rules.common.skills.SkillCategory.DEVIOUS
import com.jervisffb.engine.rules.common.skills.SkillCategory.GENERAL
import com.jervisffb.engine.rules.common.skills.SkillCategory.PASSING
import com.jervisffb.engine.rules.common.skills.SkillCategory.STRENGTH
import com.jervisffb.engine.rules.common.skills.SkillType.ALWAYS_HUNGRY
import com.jervisffb.engine.rules.common.skills.SkillType.ANIMAL_SAVAGERY
import com.jervisffb.engine.rules.common.skills.SkillType.ANIMOSITY
import com.jervisffb.engine.rules.common.skills.SkillType.BLOCK
import com.jervisffb.engine.rules.common.skills.SkillType.DODGE
import com.jervisffb.engine.rules.common.skills.SkillType.FRENZY
import com.jervisffb.engine.rules.common.skills.SkillType.INSIGNIFICANT
import com.jervisffb.engine.rules.common.skills.SkillType.LONER
import com.jervisffb.engine.rules.common.skills.SkillType.MIGHTY_BLOW
import com.jervisffb.engine.rules.common.skills.SkillType.PASS
import com.jervisffb.engine.rules.common.skills.SkillType.PREHENSILE_TAIL
import com.jervisffb.engine.rules.common.skills.SkillType.PROJECTILE_VOMIT
import com.jervisffb.engine.rules.common.skills.SkillType.REALLY_STUPID
import com.jervisffb.engine.rules.common.skills.SkillType.REGENERATION
import com.jervisffb.engine.rules.common.skills.SkillType.RIGHT_STUFF
import com.jervisffb.engine.rules.common.skills.SkillType.SIDESTEP
import com.jervisffb.engine.rules.common.skills.SkillType.STAB
import com.jervisffb.engine.rules.common.skills.SkillType.STRIP_BALL
import com.jervisffb.engine.rules.common.skills.SkillType.STUNTY
import com.jervisffb.engine.rules.common.skills.SkillType.SURE_HANDS
import com.jervisffb.engine.rules.common.skills.SkillType.THROW_TEAMMATE
import com.jervisffb.engine.rules.common.skills.SkillType.TITCHY
import com.jervisffb.engine.sprites.RosterLogo
import com.jervisffb.engine.sprites.SingleSprite
import com.jervisffb.engine.sprites.SpriteSheet
import com.jervisffb.resources.iconRootPath
import com.jervisffb.resources.portraitRootPath
import kotlinx.serialization.Serializable

val UNDERWORLD_DENIZENS_GOBLIN_LINEMAN = RosterPosition(
    PositionId("underworld_denizens-goblin-lineman"),
    16,
    "Goblin Linemans",
    "Goblin Lineman",
    "L",
    40000,
    6, 2, 3, 4, 8,
    listOf(DODGE.id(), RIGHT_STUFF.id(), STUNTY.id()),
    listOf(AGILITY, DEVIOUS),
    listOf(GENERAL, STRENGTH, PASSING),
    emptyList(),
    listOf(PlayerKeyword.GOBLIN, PlayerKeyword.LINEMAN),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/underworlddenizens_goblin.png"),
    SingleSprite.ini("${portraitRootPath}/underworlddenizens_goblin.png")
)

val UNDERWORLD_DENIZENS_SNOTLING_LINEMAN = RosterPosition(
    PositionId("underworld_denizens-snotling-lineman"),
    6,
    "Snotling Linemans",
    "Snotling Lineman",
    "S",
    15000,
    5, 1, 3, 4, 6,
    listOf(DODGE.id(), SIDESTEP.id(), RIGHT_STUFF.id(), STUNTY.id(), TITCHY.id(), INSIGNIFICANT.id()),
    listOf(AGILITY, DEVIOUS),
    listOf(GENERAL),
    emptyList(),
    listOf(PlayerKeyword.SNOTLING, PlayerKeyword.LINEMAN),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/underworlddenizens_underworldsnotlings.png"),
    SingleSprite.ini("${portraitRootPath}/underworlddenizens_underworldsnotlings.png")
)

val UNDERWORLD_DENIZENS_SKAVEN_CLANRAT = RosterPosition(
    PositionId("underworld_denizens-skaven-clanrat"),
    3,
    "Skaven Clanrats",
    "Skaven Clanrat",
    "C",
    50000,
    7, 3, 3, 4, 8,
    listOf(ANIMOSITY.id(PlayerKeyword.GOBLIN)),
    listOf(GENERAL, DEVIOUS),
    listOf(AGILITY, STRENGTH),
    emptyList(),
    listOf(PlayerKeyword.SKAVEN),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/underworlddenizens_skavenlineman.png"),
    SingleSprite.ini("${portraitRootPath}/underworlddenizens_skavenlineman.png")
)

val UNDERWORLD_DENIZENS_SKAVEN_THROWER = RosterPosition(
    PositionId("underworld_denizens-skaven-thrower"),
    1,
    "Skaven Throwers",
    "Skaven Thrower",
    "T",
    80000,
    7, 3, 3, 2, 8,
    listOf(SURE_HANDS.id(), PASS.id(), ANIMOSITY.id(PlayerKeyword.GOBLIN)),
    listOf(GENERAL, PASSING),
    listOf(AGILITY, STRENGTH, DEVIOUS),
    emptyList(),
    listOf(PlayerKeyword.SKAVEN, PlayerKeyword.THROWER),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/underworlddenizens_skaventhrower.png"),
    SingleSprite.ini("${portraitRootPath}/underworlddenizens_skaventhrower.png")
)

val UNDERWORLD_DENIZENS_GUTTER_RUNNER = RosterPosition(
    PositionId("underworld_denizens-gutter-runner"),
    1,
    "Gutter Runners",
    "Gutter Runner",
    "G",
    85000,
    9, 2, 2, 4, 8,
    listOf(DODGE.id(), ANIMOSITY.id(PlayerKeyword.GOBLIN), STAB.id()),
    listOf(GENERAL, AGILITY, DEVIOUS),
    listOf(STRENGTH),
    emptyList(),
    listOf(PlayerKeyword.SKAVEN, PlayerKeyword.RUNNER),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/underworlddenizens_gutterrunner.png"),
    SingleSprite.ini("${portraitRootPath}/underworlddenizens_gutterrunner.png")
)

val UNDERWORLD_DENIZENS_SKAVEN_BLITZER = RosterPosition(
    PositionId("underworld_denizens-skaven-blitzer"),
    1,
    "Skaven Blitzers",
    "Skaven Blitzer",
    "B",
    90000,
    8, 3, 3, 4, 9,
    listOf(BLOCK.id(), STRIP_BALL.id(), ANIMOSITY.id(PlayerKeyword.GOBLIN)),
    listOf(GENERAL, STRENGTH),
    listOf(AGILITY, DEVIOUS),
    emptyList(),
    listOf(PlayerKeyword.SKAVEN, PlayerKeyword.BLITZER),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/underworlddenizens_skavenblitzer.png"),
    SingleSprite.ini("${portraitRootPath}/underworlddenizens_skavenblitzer.png")
)

val UNDERWORLD_DENIZENS_TROLL = RosterPosition(
    PositionId("underworld_denizens-troll"),
    1,
    "Trolls",
    "Troll",
    "Tr",
    115000,
    4, 5, 5, 5, 10,
    listOf(MIGHTY_BLOW.idAdjustment(1), ALWAYS_HUNGRY.id(), LONER.idTarget(4), PROJECTILE_VOMIT.id(), REALLY_STUPID.id(), REGENERATION.id(), THROW_TEAMMATE.id()),
    listOf(STRENGTH),
    listOf(GENERAL, AGILITY, PASSING),
    emptyList(),
    listOf(PlayerKeyword.TROLL, PlayerKeyword.BIG_GUY),
    PlayerSize.BIG_GUY,
    SpriteSheet.ini("${iconRootPath}/underworlddenizens_underworldtroll.png"),
    SingleSprite.ini("${portraitRootPath}/underworlddenizens_underworldtroll.png")
)

val UNDERWORLD_DENIZENS_RAT_OGRE = RosterPosition(
    PositionId("underworld_denizens-rat-ogre"),
    1,
    "Rat Ogres",
    "Rat Ogre",
    "RO",
    150000,
    6, 5, 4, 6, 9,
    listOf(FRENZY.id(), PREHENSILE_TAIL.id(), MIGHTY_BLOW.idAdjustment(1), ANIMAL_SAVAGERY.id(), LONER.idTarget(4)),
    listOf(STRENGTH),
    listOf(GENERAL, AGILITY),
    emptyList(),
    listOf(PlayerKeyword.SKAVEN, PlayerKeyword.BIG_GUY),
    PlayerSize.BIG_GUY,
    SpriteSheet.ini("${iconRootPath}/underworlddenizens_mutantratogre.png"),
    SingleSprite.ini("${portraitRootPath}/underworlddenizens_mutantratogre.png")
)

@Serializable
val UNDERWORLD_DENIZENS_TEAM_BB2025 = Roster(
    id = RosterId("jervis-underworld-denizens"),
    name = "Underworld Denizens",
    tier = 2,
    numberOfRerolls = 8,
    rerollCost = 70000,
    allowApothecary = true,
    positions = listOf(UNDERWORLD_DENIZENS_GOBLIN_LINEMAN, UNDERWORLD_DENIZENS_SNOTLING_LINEMAN, UNDERWORLD_DENIZENS_SKAVEN_CLANRAT, UNDERWORLD_DENIZENS_SKAVEN_THROWER, UNDERWORLD_DENIZENS_GUTTER_RUNNER, UNDERWORLD_DENIZENS_SKAVEN_BLITZER, UNDERWORLD_DENIZENS_TROLL, UNDERWORLD_DENIZENS_RAT_OGRE),
    leagues = listOf(RegionalSpecialRule.UNDERWORLD_CHALLENGE),
    specialRules = listOf(TeamSpecialRule.BRIBERY_AND_CORRUPTION),
    logo = RosterLogo(
        large = SingleSprite.fumbbl("603379"),
        small = SingleSprite.fumbbl("603379")
    )
)
