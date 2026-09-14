package com.jervisffb.resources.bb2025

import com.jervisffb.engine.model.PlayerKeyword
import com.jervisffb.engine.model.PlayerSize
import com.jervisffb.engine.model.PositionId
import com.jervisffb.engine.model.RosterId
import com.jervisffb.engine.rules.common.roster.RegionalSpecialRule
import com.jervisffb.engine.rules.common.roster.RegionalSpecialRule.CHAOS_CLASH
import com.jervisffb.engine.rules.common.roster.Roster
import com.jervisffb.engine.rules.common.roster.RosterPosition
import com.jervisffb.engine.rules.common.roster.TeamSpecialRule
import com.jervisffb.engine.rules.common.roster.TeamSpecialRule.FAVOURED_OF_CHAOS_UNDIVIDED
import com.jervisffb.engine.rules.common.skills.SkillCategory.AGILITY
import com.jervisffb.engine.rules.common.skills.SkillCategory.DEVIOUS
import com.jervisffb.engine.rules.common.skills.SkillCategory.GENERAL
import com.jervisffb.engine.rules.common.skills.SkillCategory.PASSING
import com.jervisffb.engine.rules.common.skills.SkillCategory.STRENGTH
import com.jervisffb.engine.rules.common.skills.SkillType.ALWAYS_HUNGRY
import com.jervisffb.engine.rules.common.skills.SkillType.ANIMAL_SAVAGERY
import com.jervisffb.engine.rules.common.skills.SkillType.ANIMOSITY
import com.jervisffb.engine.rules.common.skills.SkillType.BONE_HEAD
import com.jervisffb.engine.rules.common.skills.SkillType.DODGE
import com.jervisffb.engine.rules.common.skills.SkillType.FRENZY
import com.jervisffb.engine.rules.common.skills.SkillType.HORNS
import com.jervisffb.engine.rules.common.skills.SkillType.LONER
import com.jervisffb.engine.rules.common.skills.SkillType.MIGHTY_BLOW
import com.jervisffb.engine.rules.common.skills.SkillType.PASS
import com.jervisffb.engine.rules.common.skills.SkillType.PREHENSILE_TAIL
import com.jervisffb.engine.rules.common.skills.SkillType.PROJECTILE_VOMIT
import com.jervisffb.engine.rules.common.skills.SkillType.REALLY_STUPID
import com.jervisffb.engine.rules.common.skills.SkillType.REGENERATION
import com.jervisffb.engine.rules.common.skills.SkillType.RIGHT_STUFF
import com.jervisffb.engine.rules.common.skills.SkillType.STUNTY
import com.jervisffb.engine.rules.common.skills.SkillType.SURE_HANDS
import com.jervisffb.engine.rules.common.skills.SkillType.THICK_SKULL
import com.jervisffb.engine.rules.common.skills.SkillType.THROW_TEAMMATE
import com.jervisffb.engine.rules.common.skills.SkillType.UNCHANNELLED_FURY
import com.jervisffb.engine.sprites.RosterLogo
import com.jervisffb.engine.sprites.SingleSprite
import com.jervisffb.engine.sprites.SpriteSheet
import com.jervisffb.resources.iconRootPath
import com.jervisffb.resources.portraitRootPath
import kotlinx.serialization.Serializable

val CHAOS_RENEGADE_RENEGADE_HUMAN = RosterPosition(
    PositionId("chaos_renegade-renegade-human"),
    16,
    "Renegade Humans",
    "Renegade Human",
    "L",
    50000,
    6, 3, 3, 4, 9,
    listOf(ANIMOSITY.id()),
    listOf(GENERAL, DEVIOUS),
    listOf(AGILITY, STRENGTH),
    emptyList(),
    listOf(PlayerKeyword.HUMAN),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/chaosrenegade_renegadehumanlineman.png"),
    SingleSprite.ini("${portraitRootPath}/chaosrenegade_renegadehumanlineman.png")
)

val CHAOS_RENEGADE_RENEGADE_HUMAN_THROWER = RosterPosition(
    PositionId("chaos_renegade-renegade-human-thrower"),
    1,
    "Renegade Human Throwers",
    "Renegade Human Thrower",
    "T",
    75000,
    6, 3, 3, 3, 9,
    listOf(SURE_HANDS.id(), PASS.id(), ANIMOSITY.id()),
    listOf(GENERAL, PASSING, DEVIOUS),
    listOf(AGILITY, STRENGTH),
    emptyList(),
    listOf(PlayerKeyword.HUMAN, PlayerKeyword.THROWER),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/chaosrenegade_renegadehumanthrower.png"),
    SingleSprite.ini("${portraitRootPath}/chaosrenegade_renegadehumanthrower.png")
)

val CHAOS_RENEGADE_RENEGADE_GOBLIN = RosterPosition(
    PositionId("chaos_renegade-renegade-goblin"),
    1,
    "Renegade Goblins",
    "Renegade Goblin",
    "G",
    40000,
    6, 2, 3, 4, 8,
    listOf(DODGE.id(), ANIMOSITY.id(), RIGHT_STUFF.id(), STUNTY.id()),
    listOf(AGILITY, DEVIOUS),
    listOf(GENERAL, PASSING),
    emptyList(),
    listOf(PlayerKeyword.GOBLIN),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/chaosrenegade_renegadegoblin.png"),
    SingleSprite.ini("${portraitRootPath}/chaosrenegade_renegadegoblin.png")
)

val CHAOS_RENEGADE_RENEGADE_ORC = RosterPosition(
    PositionId("chaos_renegade-renegade-orc"),
    1,
    "Renegade Orcs",
    "Renegade Orc",
    "O",
    50000,
    5, 3, 3, 4, 10,
    listOf(ANIMOSITY.id()),
    listOf(GENERAL, DEVIOUS),
    listOf(AGILITY, STRENGTH),
    emptyList(),
    listOf(PlayerKeyword.ORC),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/chaosrenegade_renegadeorclineman.png"),
    SingleSprite.ini("${portraitRootPath}/chaosrenegade_renegadeorclineman.png")
)

val CHAOS_RENEGADE_RENEGADE_SKAVEN = RosterPosition(
    PositionId("chaos_renegade-renegade-skaven"),
    1,
    "Renegade Skavens",
    "Renegade Skaven",
    "S",
    50000,
    7, 3, 3, 4, 8,
    listOf(ANIMOSITY.id()),
    listOf(GENERAL, DEVIOUS),
    listOf(AGILITY, STRENGTH),
    emptyList(),
    listOf(PlayerKeyword.SKAVEN),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/chaosrenegade_renegadeskavenlineman.png"),
    SingleSprite.ini("${portraitRootPath}/chaosrenegade_renegadeskavenlineman.png")
)

val CHAOS_RENEGADE_RENEGADE_DARK_ELF = RosterPosition(
    PositionId("chaos_renegade-renegade-dark-elf"),
    1,
    "Elves",
    "Renegade Dark Elf",
    "DE",
    65000,
    6, 3, 2, 3, 9,
    listOf(ANIMOSITY.id()),
    listOf(GENERAL, AGILITY, DEVIOUS),
    listOf(STRENGTH),
    emptyList(),
    listOf(PlayerKeyword.ELF),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/chaosrenegade_renegadedarkelflineman.png"),
    SingleSprite.ini("${portraitRootPath}/chaosrenegade_renegadedarkelflineman.png")
)

val CHAOS_RENEGADE_TROLL = RosterPosition(
    PositionId("chaos_renegade-troll"),
    1,
    "Trolls",
    "Troll",
    "T",
    115000,
    4, 5, 5, 5, 10,
    listOf(MIGHTY_BLOW.idAdjustment(1), ALWAYS_HUNGRY.id(), LONER.idTarget(4), PROJECTILE_VOMIT.id(), REALLY_STUPID.id(), REGENERATION.id(), THROW_TEAMMATE.id()),
    listOf(STRENGTH),
    listOf(GENERAL, AGILITY, PASSING),
    emptyList(),
    listOf(PlayerKeyword.TROLL, PlayerKeyword.BIG_GUY),
    PlayerSize.BIG_GUY,
    SpriteSheet.ini("${iconRootPath}/chaosrenegade_troll.png"),
    SingleSprite.ini("${portraitRootPath}/chaosrenegade_troll.png")
)

val CHAOS_RENEGADE_OGRE = RosterPosition(
    PositionId("chaos_renegade-ogre"),
    1,
    "Ogres",
    "Ogre",
    "O",
    140000,
    5, 5, 4, 5, 10,
    listOf(MIGHTY_BLOW.idAdjustment(1), THICK_SKULL.id(), BONE_HEAD.id(), LONER.idTarget(3), THROW_TEAMMATE.id()),
    listOf(STRENGTH),
    listOf(GENERAL, AGILITY),
    emptyList(),
    listOf(PlayerKeyword.OGRE, PlayerKeyword.BIG_GUY),
    PlayerSize.BIG_GUY,
    SpriteSheet.ini("${iconRootPath}/chaosrenegade_ogre.png"),
    SingleSprite.ini("${portraitRootPath}/chaosrenegade_ogre.png")
)

val CHAOS_RENEGADE_MINOTAUR = RosterPosition(
    PositionId("chaos_renegade-minotaur"),
    1,
    "Minotaurs",
    "Minotaur",
    "M",
    150000,
    5, 5, 4, 6, 9,
    listOf(FRENZY.id(), HORNS.id(), MIGHTY_BLOW.idAdjustment(1), THICK_SKULL.id(), LONER.idTarget(4), UNCHANNELLED_FURY.id()),
    listOf(STRENGTH),
    listOf(GENERAL, AGILITY),
    emptyList(),
    listOf(PlayerKeyword.MINOTAUR, PlayerKeyword.BIG_GUY),
    PlayerSize.BIG_GUY,
    SpriteSheet.ini("${iconRootPath}/chaosrenegade_minotaur.png"),
    SingleSprite.ini("${portraitRootPath}/chaosrenegade_minotaur.png")
)

val CHAOS_RENEGADE_RAT_OGRE = RosterPosition(
    PositionId("chaos_renegade-rat-ogre"),
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
    SpriteSheet.ini("${iconRootPath}/chaosrenegade_renegaderatogre.png"),
    SingleSprite.ini("${portraitRootPath}/chaosrenegade_renegaderatogre.png")
)

@Serializable
val CHAOS_RENEGADE_TEAM_BB2025 = Roster(
    id = RosterId("jervis-chaos-renegade"),
    name = "Chaos Renegade",
    tier = 3,
    numberOfRerolls = 8,
    rerollCost = 70000,
    allowApothecary = true,
    positions = listOf(CHAOS_RENEGADE_RENEGADE_HUMAN, CHAOS_RENEGADE_RENEGADE_HUMAN_THROWER, CHAOS_RENEGADE_RENEGADE_GOBLIN, CHAOS_RENEGADE_RENEGADE_ORC, CHAOS_RENEGADE_RENEGADE_SKAVEN, CHAOS_RENEGADE_RENEGADE_DARK_ELF, CHAOS_RENEGADE_TROLL, CHAOS_RENEGADE_OGRE, CHAOS_RENEGADE_MINOTAUR, CHAOS_RENEGADE_RAT_OGRE),
    leagues = listOf(RegionalSpecialRule.CHAOS_CLASH),
    specialRules = listOf(TeamSpecialRule.FAVOURED_OF_CHAOS_UNDIVIDED),
    logo = RosterLogo(
        large = SingleSprite.fumbbl("603755"),
        small = SingleSprite.fumbbl("603755")
    )
)
