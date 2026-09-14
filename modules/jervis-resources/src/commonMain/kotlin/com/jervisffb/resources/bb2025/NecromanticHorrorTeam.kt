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
import com.jervisffb.engine.rules.common.skills.SkillType.CLAWS
import com.jervisffb.engine.rules.common.skills.SkillType.DODGE
import com.jervisffb.engine.rules.common.skills.SkillType.EYE_GOUGE
import com.jervisffb.engine.rules.common.skills.SkillType.FOUL_APPEARANCE
import com.jervisffb.engine.rules.common.skills.SkillType.FRENZY
import com.jervisffb.engine.rules.common.skills.SkillType.NO_BALL
import com.jervisffb.engine.rules.common.skills.SkillType.REGENERATION
import com.jervisffb.engine.rules.common.skills.SkillType.SIDESTEP
import com.jervisffb.engine.rules.common.skills.SkillType.STAND_FIRM
import com.jervisffb.engine.rules.common.skills.SkillType.THICK_SKULL
import com.jervisffb.engine.rules.common.skills.SkillType.UNSTEADY
import com.jervisffb.engine.sprites.RosterLogo
import com.jervisffb.engine.sprites.SingleSprite
import com.jervisffb.engine.sprites.SpriteSheet
import com.jervisffb.resources.iconRootPath
import com.jervisffb.resources.portraitRootPath
import kotlinx.serialization.Serializable

val NECROMANTIC_HORROR_ZOMBIE_LINEMAN = RosterPosition(
    PositionId("necromantic_horror-zombie-lineman"),
    16,
    "Zombie Linemans",
    "Zombie Lineman",
    "L",
    40000,
    4, 3, 4, 6, 9,
    listOf(REGENERATION.id(), EYE_GOUGE.id(), UNSTEADY.id()),
    listOf(GENERAL, DEVIOUS),
    listOf(AGILITY, STRENGTH),
    emptyList(),
    listOf(PlayerKeyword.ZOMBIE, PlayerKeyword.LINEMAN),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/necromantichorror_zombie.png"),
    SingleSprite.ini("${portraitRootPath}/necromantichorror_zombie.png")
)

val NECROMANTIC_HORROR_GHOUL_RUNNER = RosterPosition(
    PositionId("necromantic_horror-ghoul-runner"),
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
    SpriteSheet.ini("${iconRootPath}/necromantichorror_ghoulrunner.png"),
    SingleSprite.ini("${portraitRootPath}/necromantichorror_ghoulrunner.png")
)

val NECROMANTIC_HORROR_WRAITH = RosterPosition(
    PositionId("necromantic_horror-wraith"),
    2,
    "Wraiths",
    "Wraith",
    "Wr",
    85000,
    6, 3, 3, null, 9,
    listOf(SIDESTEP.id(), BLOCK.id(), FOUL_APPEARANCE.id(), NO_BALL.id(), REGENERATION.id()),
    listOf(GENERAL, STRENGTH),
    listOf(AGILITY),
    emptyList(),
    listOf(PlayerKeyword.WRAITH),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/necromantichorror_wraith.png"),
    SingleSprite.ini("${portraitRootPath}/necromantichorror_wraith.png")
)

val NECROMANTIC_HORROR_WEREWOLF = RosterPosition(
    PositionId("necromantic_horror-werewolf"),
    2,
    "Werewolfs",
    "Werewolf",
    "W",
    120000,
    8, 3, 3, 3, 9,
    listOf(FRENZY.id(), CLAWS.id(), REGENERATION.id()),
    listOf(GENERAL, AGILITY),
    listOf(STRENGTH, PASSING, DEVIOUS),
    emptyList(),
    listOf(PlayerKeyword.WEREWOLF),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/necromantichorror_werewolf.png"),
    SingleSprite.ini("${portraitRootPath}/necromantichorror_werewolf.png")
)

val NECROMANTIC_HORROR_FLESH_GOLEM = RosterPosition(
    PositionId("necromantic_horror-flesh-golem"),
    2,
    "Flesh Golems",
    "Flesh Golem",
    "FG",
    110000,
    4, 4, 4, 6, 10,
    listOf(STAND_FIRM.id(), THICK_SKULL.id(), REGENERATION.id(), UNSTEADY.id()),
    listOf(GENERAL, STRENGTH),
    listOf(AGILITY, DEVIOUS),
    emptyList(),
    listOf(PlayerKeyword.UNDEAD),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/necromantichorror_fleshgolem.png"),
    SingleSprite.ini("${portraitRootPath}/necromantichorror_fleshgolem.png")
)

@Serializable
val NECROMANTIC_HORROR_TEAM_BB2025 = Roster(
    id = RosterId("jervis-necromantic-horror"),
    name = "Necromantic Horror",
    tier = 2,
    numberOfRerolls = 8,
    rerollCost = 70000,
    allowApothecary = false,
    positions = listOf(NECROMANTIC_HORROR_ZOMBIE_LINEMAN, NECROMANTIC_HORROR_GHOUL_RUNNER, NECROMANTIC_HORROR_WRAITH, NECROMANTIC_HORROR_WEREWOLF, NECROMANTIC_HORROR_FLESH_GOLEM),
    leagues = listOf(RegionalSpecialRule.SYLVANIAN_SPOTLIGHT),
    specialRules = listOf(TeamSpecialRule.MASTERS_OF_UNDEATH),
    logo = RosterLogo(
        large = SingleSprite.fumbbl("486313"),
        small = SingleSprite.fumbbl("486313")
    )
)
