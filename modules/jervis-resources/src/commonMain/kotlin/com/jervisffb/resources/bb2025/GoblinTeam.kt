package com.jervisffb.resources.bb2025

import com.jervisffb.engine.model.PlayerKeyword
import com.jervisffb.engine.model.PlayerSize
import com.jervisffb.engine.model.PositionId
import com.jervisffb.engine.model.RosterId
import com.jervisffb.engine.rules.common.roster.RegionalSpecialRule
import com.jervisffb.engine.rules.common.roster.RegionalSpecialRule.BADLANDS_BRAWL
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
import com.jervisffb.engine.rules.common.skills.SkillType.BALL_AND_CHAIN
import com.jervisffb.engine.rules.common.skills.SkillType.BOMBARDIER
import com.jervisffb.engine.rules.common.skills.SkillType.CHAINSAW
import com.jervisffb.engine.rules.common.skills.SkillType.DIRTY_PLAYER
import com.jervisffb.engine.rules.common.skills.SkillType.DISTURBING_PRESENCE
import com.jervisffb.engine.rules.common.skills.SkillType.DODGE
import com.jervisffb.engine.rules.common.skills.SkillType.MIGHTY_BLOW
import com.jervisffb.engine.rules.common.skills.SkillType.NO_BALL
import com.jervisffb.engine.rules.common.skills.SkillType.POGO_STICK
import com.jervisffb.engine.rules.common.skills.SkillType.PROJECTILE_VOMIT
import com.jervisffb.engine.rules.common.skills.SkillType.REALLY_STUPID
import com.jervisffb.engine.rules.common.skills.SkillType.REGENERATION
import com.jervisffb.engine.rules.common.skills.SkillType.RIGHT_STUFF
import com.jervisffb.engine.rules.common.skills.SkillType.SECRET_WEAPON
import com.jervisffb.engine.rules.common.skills.SkillType.STUNTY
import com.jervisffb.engine.rules.common.skills.SkillType.SWOOP
import com.jervisffb.engine.rules.common.skills.SkillType.TAUNT
import com.jervisffb.engine.rules.common.skills.SkillType.THROW_TEAMMATE
import com.jervisffb.engine.sprites.RosterLogo
import com.jervisffb.engine.sprites.SingleSprite
import com.jervisffb.engine.sprites.SpriteSheet
import com.jervisffb.resources.iconRootPath
import com.jervisffb.resources.portraitRootPath
import kotlinx.serialization.Serializable

val GOBLIN_GOBLIN_LINEMAN = RosterPosition(
    PositionId("goblin-goblin-lineman"),
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
    SpriteSheet.ini("${iconRootPath}/goblin_goblin.png"),
    SingleSprite.ini("${portraitRootPath}/goblin_goblin.png")
)

val GOBLIN_BOMMA = RosterPosition(
    PositionId("goblin-bomma"),
    1,
    "Bommas",
    "Bomma",
    "B",
    45000,
    6, 2, 3, 4, 8,
    listOf(DODGE.id(), BOMBARDIER.id(), SECRET_WEAPON.id(), STUNTY.id()),
    listOf(PASSING, DEVIOUS),
    listOf(GENERAL, AGILITY, STRENGTH),
    emptyList(),
    listOf(),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/goblin_bomma.png"),
    SingleSprite.ini("${portraitRootPath}/goblin_bomma.png")
)

val GOBLIN_LOONEY = RosterPosition(
    PositionId("goblin-looney"),
    1,
    "Looneies",
    "Looney",
    "Lo",
    40000,
    6, 2, 3, null, 8,
    listOf(CHAINSAW.id(), NO_BALL.id(), SECRET_WEAPON.id(), STUNTY.id()),
    listOf(DEVIOUS),
    listOf(GENERAL, AGILITY, STRENGTH),
    emptyList(),
    listOf(),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/goblin_looney.png"),
    SingleSprite.ini("${portraitRootPath}/goblin_looney.png")
)

val GOBLIN_POGOER = RosterPosition(
    PositionId("goblin-pogoer"),
    1,
    "Pogoers",
    "Pogoer",
    "P",
    75000,
    7, 2, 3, 4, 8,
    listOf(DODGE.id(), POGO_STICK.id(), STUNTY.id()),
    listOf(AGILITY),
    listOf(GENERAL, STRENGTH, DEVIOUS),
    emptyList(),
    listOf(),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/goblin_pogoer.png"),
    SingleSprite.ini("${portraitRootPath}/goblin_pogoer.png")
)

val GOBLIN_OOLIGAN = RosterPosition(
    PositionId("goblin-ooligan"),
    1,
    "'Ooligans",
    "'Ooligan",
    "O",
    60000,
    6, 2, 3, 5, 8,
    listOf(DODGE.id(), DIRTY_PLAYER.id(), DISTURBING_PRESENCE.id(), RIGHT_STUFF.id(), STUNTY.id(), TAUNT.id()),
    listOf(AGILITY, DEVIOUS),
    listOf(GENERAL, STRENGTH),
    emptyList(),
    listOf(),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/goblin_ooligan.png"),
    SingleSprite.ini("${portraitRootPath}/goblin_ooligan.png")
)

val GOBLIN_DOOM_DIVER = RosterPosition(
    PositionId("goblin-doom-diver"),
    1,
    "Doom Divers",
    "Doom Diver",
    "DD",
    65000,
    6, 2, 3, 6, 8,
    listOf(DODGE.id(), RIGHT_STUFF.id(), STUNTY.id(), SWOOP.id()),
    listOf(AGILITY),
    listOf(GENERAL, STRENGTH, DEVIOUS),
    emptyList(),
    listOf(),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/goblin_doomdiver.png"),
    SingleSprite.ini("${portraitRootPath}/goblin_doomdiver.png")
)

val GOBLIN_TRAINED_TROLL = RosterPosition(
    PositionId("goblin-trained-troll"),
    2,
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
    SpriteSheet.ini("${iconRootPath}/goblin_troll.png"),
    SingleSprite.ini("${portraitRootPath}/goblin_troll.png")
)

val GOBLIN_FANATIC = RosterPosition(
    PositionId("goblin-fanatic"),
    1,
    "Fanatics",
    "Fanatic",
    "F",
    70000,
    3, 7, 3, null, 8,
    listOf(BALL_AND_CHAIN.id(), NO_BALL.id(), SECRET_WEAPON.id(), STUNTY.id()),
    listOf(STRENGTH, DEVIOUS),
    listOf(GENERAL, AGILITY),
    emptyList(),
    listOf(),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/goblin_fanatic.png"),
    SingleSprite.ini("${portraitRootPath}/goblin_fanatic.png")
)

@Serializable
val GOBLIN_TEAM_BB2025 = Roster(
    id = RosterId("jervis-goblin"),
    name = "Goblin",
    tier = 3,
    numberOfRerolls = 8,
    rerollCost = 60000,
    allowApothecary = true,
    positions = listOf(GOBLIN_GOBLIN_LINEMAN, GOBLIN_BOMMA, GOBLIN_LOONEY, GOBLIN_POGOER, GOBLIN_OOLIGAN, GOBLIN_DOOM_DIVER, GOBLIN_TRAINED_TROLL, GOBLIN_FANATIC),
    leagues = listOf(RegionalSpecialRule.BADLANDS_BRAWL, RegionalSpecialRule.UNDERWORLD_CHALLENGE),
    specialRules = listOf(TeamSpecialRule.BRIBERY_AND_CORRUPTION),
    logo = RosterLogo(
        large = SingleSprite.fumbbl("486277"),
        small = SingleSprite.fumbbl("486277")
    )
)
