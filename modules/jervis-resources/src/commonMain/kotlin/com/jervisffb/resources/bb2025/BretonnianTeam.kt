package com.jervisffb.resources.bb2025

import com.jervisffb.engine.model.PlayerKeyword
import com.jervisffb.engine.model.PlayerSize
import com.jervisffb.engine.model.PositionId
import com.jervisffb.engine.model.RosterId
import com.jervisffb.engine.rules.common.roster.RegionalSpecialRule
import com.jervisffb.engine.rules.common.roster.RegionalSpecialRule.OLD_WORLD_CLASSIC
import com.jervisffb.engine.rules.common.roster.Roster
import com.jervisffb.engine.rules.common.roster.RosterPosition
import com.jervisffb.engine.rules.common.skills.SkillCategory.AGILITY
import com.jervisffb.engine.rules.common.skills.SkillCategory.GENERAL
import com.jervisffb.engine.rules.common.skills.SkillCategory.PASSING
import com.jervisffb.engine.rules.common.skills.SkillCategory.STRENGTH
import com.jervisffb.engine.rules.common.skills.SkillType.BLOCK
import com.jervisffb.engine.rules.common.skills.SkillType.CATCH
import com.jervisffb.engine.rules.common.skills.SkillType.DAUNTLESS
import com.jervisffb.engine.rules.common.skills.SkillType.NERVES_OF_STEEL
import com.jervisffb.engine.rules.common.skills.SkillType.PASS
import com.jervisffb.engine.rules.common.skills.SkillType.STEADY_FOOTING
import com.jervisffb.engine.rules.common.skills.SkillType.WRESTLE
import com.jervisffb.engine.sprites.RosterLogo
import com.jervisffb.engine.sprites.SingleSprite
import com.jervisffb.engine.sprites.SpriteSheet
import com.jervisffb.resources.iconRootPath
import com.jervisffb.resources.portraitRootPath
import kotlinx.serialization.Serializable

val BRETONNIAN_BRETONNIAN_SQUIRE = RosterPosition(
    PositionId("bretonnian-bretonnian-squire"),
    16,
    "Bretonnian Squires",
    "Bretonnian Squire",
    "S",
    50000,
    6, 3, 3, 4, 8,
    listOf(WRESTLE.id()),
    listOf(GENERAL),
    listOf(AGILITY, STRENGTH),
    emptyList(),
    listOf(PlayerKeyword.HUMAN, PlayerKeyword.LINEMAN),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/bretonnian_linemen.png"),
    SingleSprite.ini("${portraitRootPath}/bretonnian_linemen.png")
)

val BRETONNIAN_BRETONNIAN_KNIGHT_CATCHER = RosterPosition(
    PositionId("bretonnian-bretonnian-knight-catcher"),
    2,
    "Bretonnian Knight Catchers",
    "Bretonnian Knight Catcher",
    "C",
    85000,
    7, 3, 3, 4, 9,
    listOf(CATCH.id(), DAUNTLESS.id(), NERVES_OF_STEEL.id()),
    listOf(GENERAL, AGILITY),
    listOf(STRENGTH),
    emptyList(),
    listOf(PlayerKeyword.HUMAN, PlayerKeyword.CATCHER),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/bretonnian_yeomen.png"),
    SingleSprite.ini("${portraitRootPath}/bretonnian_yeomen.png")
)

val BRETONNIAN_BRETONNIAN_KNIGHT_THROWER = RosterPosition(
    PositionId("bretonnian-bretonnian-knight-thrower"),
    2,
    "Bretonnian Knight Throwers",
    "Bretonnian Knight Thrower",
    "T",
    80000,
    6, 3, 3, 3, 9,
    listOf(DAUNTLESS.id(), NERVES_OF_STEEL.id(), PASS.id()),
    listOf(GENERAL, PASSING),
    listOf(AGILITY, STRENGTH),
    emptyList(),
    listOf(PlayerKeyword.HUMAN, PlayerKeyword.THROWER),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/bretonnian_yeomen.png"),
    SingleSprite.ini("${portraitRootPath}/bretonnian_yeomen.png")
)

val BRETONNIAN_GRAIL_KNIGHT = RosterPosition(
    PositionId("bretonnian-grail-knight"),
    2,
    "Grail Knights",
    "Grail Knight",
    "G",
    95000,
    7, 3, 3, 4, 10,
    listOf(BLOCK.id(), DAUNTLESS.id(), STEADY_FOOTING.id()),
    listOf(GENERAL, STRENGTH),
    listOf(AGILITY),
    emptyList(),
    listOf(PlayerKeyword.HUMAN),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/bretonnian_blitzers.png"),
    SingleSprite.ini("${portraitRootPath}/bretonnian_blitzers.png")
)

@Serializable
val BRETONNIAN_TEAM_BB2025 = Roster(
    id = RosterId("jervis-bretonnian"),
    name = "Bretonnian",
    tier = 2,
    numberOfRerolls = 8,
    rerollCost = 60000,
    allowApothecary = true,
    positions = listOf(BRETONNIAN_BRETONNIAN_SQUIRE, BRETONNIAN_BRETONNIAN_KNIGHT_CATCHER, BRETONNIAN_BRETONNIAN_KNIGHT_THROWER, BRETONNIAN_GRAIL_KNIGHT),
    leagues = listOf(RegionalSpecialRule.OLD_WORLD_CLASSIC),
    specialRules = listOf(),
    logo = RosterLogo(
        large = SingleSprite.fumbbl("769337"),
        small = SingleSprite.fumbbl("769337")
    )
)
