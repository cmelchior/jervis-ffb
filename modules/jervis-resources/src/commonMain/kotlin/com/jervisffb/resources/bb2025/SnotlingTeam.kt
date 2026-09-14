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
import com.jervisffb.engine.rules.common.roster.TeamSpecialRule.LOW_COST_LINEMEN
import com.jervisffb.engine.rules.common.roster.TeamSpecialRule.SWARMING
import com.jervisffb.engine.rules.common.skills.SkillCategory.AGILITY
import com.jervisffb.engine.rules.common.skills.SkillCategory.DEVIOUS
import com.jervisffb.engine.rules.common.skills.SkillCategory.GENERAL
import com.jervisffb.engine.rules.common.skills.SkillCategory.PASSING
import com.jervisffb.engine.rules.common.skills.SkillCategory.STRENGTH
import com.jervisffb.engine.rules.common.skills.SkillType.ALWAYS_HUNGRY
import com.jervisffb.engine.rules.common.skills.SkillType.BOMBARDIER
import com.jervisffb.engine.rules.common.skills.SkillType.DIRTY_PLAYER
import com.jervisffb.engine.rules.common.skills.SkillType.DODGE
import com.jervisffb.engine.rules.common.skills.SkillType.INSIGNIFICANT
import com.jervisffb.engine.rules.common.skills.SkillType.JUGGERNAUT
import com.jervisffb.engine.rules.common.skills.SkillType.MIGHTY_BLOW
import com.jervisffb.engine.rules.common.skills.SkillType.POGO_STICK
import com.jervisffb.engine.rules.common.skills.SkillType.PROJECTILE_VOMIT
import com.jervisffb.engine.rules.common.skills.SkillType.REALLY_STUPID
import com.jervisffb.engine.rules.common.skills.SkillType.REGENERATION
import com.jervisffb.engine.rules.common.skills.SkillType.RIGHT_STUFF
import com.jervisffb.engine.rules.common.skills.SkillType.SECRET_WEAPON
import com.jervisffb.engine.rules.common.skills.SkillType.SIDESTEP
import com.jervisffb.engine.rules.common.skills.SkillType.SPRINT
import com.jervisffb.engine.rules.common.skills.SkillType.STAND_FIRM
import com.jervisffb.engine.rules.common.skills.SkillType.STUNTY
import com.jervisffb.engine.rules.common.skills.SkillType.THROW_TEAMMATE
import com.jervisffb.engine.rules.common.skills.SkillType.TITCHY
import com.jervisffb.engine.sprites.RosterLogo
import com.jervisffb.engine.sprites.SingleSprite
import com.jervisffb.engine.sprites.SpriteSheet
import com.jervisffb.resources.iconRootPath
import com.jervisffb.resources.portraitRootPath
import kotlinx.serialization.Serializable

val SNOTLING_SNOTLING_LINEMAN = RosterPosition(
    PositionId("snotling-snotling-lineman"),
    16,
    "Snotling Linemans",
    "Snotling Lineman",
    "L",
    15000,
    5, 1, 3, 4, 6,
    listOf(DODGE.id(), SIDESTEP.id(), RIGHT_STUFF.id(), STUNTY.id(), TITCHY.id(), INSIGNIFICANT.id()),
    listOf(AGILITY, DEVIOUS),
    listOf(GENERAL),
    emptyList(),
    listOf(PlayerKeyword.SNOTLING, PlayerKeyword.LINEMAN),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/snotling_snotling.png"),
    SingleSprite.ini("${portraitRootPath}/snotling_snotling.png")
)

val SNOTLING_FUNGUS_FLINGA = RosterPosition(
    PositionId("snotling-fungus-flinga"),
    2,
    "Fungus Flingas",
    "Fungus Flinga",
    "F",
    30000,
    5, 1, 3, 4, 6,
    listOf(DODGE.id(), SIDESTEP.id(), BOMBARDIER.id(), RIGHT_STUFF.id(), SECRET_WEAPON.id(), STUNTY.id(), TITCHY.id()),
    listOf(AGILITY, PASSING, DEVIOUS),
    listOf(GENERAL),
    emptyList(),
    listOf(),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/snotling_fungusflinga.png"),
    SingleSprite.ini("${portraitRootPath}/snotling_fungusflinga.png")
)

val SNOTLING_FUN_HOPPA = RosterPosition(
    PositionId("snotling-fun-hoppa"),
    2,
    "Fun-hoppas",
    "Fun-hoppa",
    "FH",
    20000,
    6, 1, 3, 4, 6,
    listOf(DODGE.id(), SIDESTEP.id(), POGO_STICK.id(), RIGHT_STUFF.id(), STUNTY.id()),
    listOf(AGILITY, DEVIOUS),
    listOf(GENERAL),
    emptyList(),
    listOf(),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/snotling_funhoppa.png"),
    SingleSprite.ini("${portraitRootPath}/snotling_funhoppa.png")
)

val SNOTLING_STILTY_RUNNA = RosterPosition(
    PositionId("snotling-stilty-runna"),
    2,
    "Stilty Runnas",
    "Stilty Runna",
    "SR",
    20000,
    6, 1, 3, 4, 6,
    listOf(DODGE.id(), SIDESTEP.id(), SPRINT.id(), RIGHT_STUFF.id(), STUNTY.id()),
    listOf(AGILITY, DEVIOUS),
    listOf(GENERAL),
    emptyList(),
    listOf(),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/snotling_stiltyrunna.png"),
    SingleSprite.ini("${portraitRootPath}/snotling_stiltyrunna.png")
)

val SNOTLING_PUMP_WAGON = RosterPosition(
    PositionId("snotling-pump-wagon"),
    2,
    "Pump Wagons",
    "Pump Wagon",
    "P",
    100000,
    5, 5, 5, 6, 9,
    listOf(DIRTY_PLAYER.id(), JUGGERNAUT.id(), MIGHTY_BLOW.idAdjustment(1), STAND_FIRM.id(), REALLY_STUPID.id()),
    listOf(STRENGTH, DEVIOUS),
    listOf(GENERAL, AGILITY),
    emptyList(),
    listOf(PlayerKeyword.BIG_GUY),
    PlayerSize.BIG_GUY,
    SpriteSheet.ini("${iconRootPath}/snotling_pumpwagon.png"),
    SingleSprite.ini("${portraitRootPath}/snotling_pumpwagon.png")
)

val SNOTLING_TRAINED_TROLL = RosterPosition(
    PositionId("snotling-trained-troll"),
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
    SpriteSheet.ini("${iconRootPath}/snotling_troll.png"),
    SingleSprite.ini("${portraitRootPath}/snotling_troll.png")
)

@Serializable
val SNOTLING_TEAM_BB2025 = Roster(
    id = RosterId("jervis-snotling"),
    name = "Snotling",
    tier = 3,
    numberOfRerolls = 8,
    rerollCost = 70000,
    allowApothecary = true,
    positions = listOf(SNOTLING_SNOTLING_LINEMAN, SNOTLING_FUNGUS_FLINGA, SNOTLING_FUN_HOPPA, SNOTLING_STILTY_RUNNA, SNOTLING_PUMP_WAGON, SNOTLING_TRAINED_TROLL),
    leagues = listOf(RegionalSpecialRule.UNDERWORLD_CHALLENGE),
    specialRules = listOf(TeamSpecialRule.BRIBERY_AND_CORRUPTION, TeamSpecialRule.LOW_COST_LINEMEN, TeamSpecialRule.SWARMING),
    logo = RosterLogo(
        large = SingleSprite.fumbbl("643386"),
        small = SingleSprite.fumbbl("643386")
    )
)
