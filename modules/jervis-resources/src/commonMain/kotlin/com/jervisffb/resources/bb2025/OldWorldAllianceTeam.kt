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
import com.jervisffb.engine.rules.common.skills.SkillCategory.DEVIOUS
import com.jervisffb.engine.rules.common.skills.SkillCategory.GENERAL
import com.jervisffb.engine.rules.common.skills.SkillCategory.PASSING
import com.jervisffb.engine.rules.common.skills.SkillCategory.STRENGTH
import com.jervisffb.engine.rules.common.skills.SkillType.BLOCK
import com.jervisffb.engine.rules.common.skills.SkillType.BONE_HEAD
import com.jervisffb.engine.rules.common.skills.SkillType.CATCH
import com.jervisffb.engine.rules.common.skills.SkillType.DAUNTLESS
import com.jervisffb.engine.rules.common.skills.SkillType.DEFENSIVE
import com.jervisffb.engine.rules.common.skills.SkillType.DIVING_TACKLE
import com.jervisffb.engine.rules.common.skills.SkillType.DODGE
import com.jervisffb.engine.rules.common.skills.SkillType.FRENZY
import com.jervisffb.engine.rules.common.skills.SkillType.HATRED
import com.jervisffb.engine.rules.common.skills.SkillType.LONER
import com.jervisffb.engine.rules.common.skills.SkillType.MIGHTY_BLOW
import com.jervisffb.engine.rules.common.skills.SkillType.PASS
import com.jervisffb.engine.rules.common.skills.SkillType.RIGHT_STUFF
import com.jervisffb.engine.rules.common.skills.SkillType.SPRINT
import com.jervisffb.engine.rules.common.skills.SkillType.STAND_FIRM
import com.jervisffb.engine.rules.common.skills.SkillType.STRONG_ARM
import com.jervisffb.engine.rules.common.skills.SkillType.STUNTY
import com.jervisffb.engine.rules.common.skills.SkillType.SURE_HANDS
import com.jervisffb.engine.rules.common.skills.SkillType.TACKLE
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

val OLD_WORLD_ALLIANCE_HUMAN_LINEMAN = RosterPosition(
    PositionId("old_world_alliance-human-lineman"),
    16,
    "Human Linemans",
    "Human Lineman",
    "L",
    50000,
    6, 3, 3, 4, 9,
    listOf(),
    listOf(GENERAL),
    listOf(AGILITY, STRENGTH),
    emptyList(),
    listOf(PlayerKeyword.HUMAN, PlayerKeyword.LINEMAN),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/oldworldalliance_humanlineman.png"),
    SingleSprite.ini("${portraitRootPath}/oldworldalliance_oldworldhumanlineman.png")
)

val OLD_WORLD_ALLIANCE_HUMAN_THROWER = RosterPosition(
    PositionId("old_world_alliance-human-thrower"),
    1,
    "Human Throwers",
    "Human Thrower",
    "T",
    75000,
    6, 3, 3, 3, 9,
    listOf(SURE_HANDS.id(), PASS.id()),
    listOf(GENERAL, PASSING),
    listOf(AGILITY, STRENGTH),
    emptyList(),
    listOf(PlayerKeyword.HUMAN, PlayerKeyword.THROWER),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/oldworldalliance_humanthrower.png"),
    SingleSprite.ini("${portraitRootPath}/oldworldalliance_oldworldhumanthrower.png")
)

val OLD_WORLD_ALLIANCE_HUMAN_CATCHER = RosterPosition(
    PositionId("old_world_alliance-human-catcher"),
    1,
    "Human Catchers",
    "Human Catcher",
    "C",
    75000,
    8, 3, 3, 4, 8,
    listOf(CATCH.id(), DODGE.id()),
    listOf(GENERAL, AGILITY),
    listOf(STRENGTH, PASSING),
    emptyList(),
    listOf(PlayerKeyword.HUMAN, PlayerKeyword.CATCHER),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/oldworldalliance_humancatcher.png"),
    SingleSprite.ini("${portraitRootPath}/oldworldalliance_oldworldhumancatcher.png")
)

val OLD_WORLD_ALLIANCE_HUMAN_BLITZER = RosterPosition(
    PositionId("old_world_alliance-human-blitzer"),
    1,
    "Human Blitzers",
    "Human Blitzer",
    "HB",
    85000,
    7, 3, 3, 4, 9,
    listOf(BLOCK.id(), TACKLE.id()),
    listOf(GENERAL, STRENGTH),
    listOf(AGILITY),
    emptyList(),
    listOf(PlayerKeyword.HUMAN, PlayerKeyword.BLITZER),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/oldworldalliance_humanblitzer.png"),
    SingleSprite.ini("${portraitRootPath}/oldworldalliance_oldworldhumanblitzer.png")
)

val OLD_WORLD_ALLIANCE_DWARF_LINEMAN = RosterPosition(
    PositionId("old_world_alliance-dwarf-lineman"),
    3,
    "Dwarf Linemans",
    "Dwarf Lineman",
    "DL",
    70000,
    4, 3, 4, 5, 10,
    listOf(DEFENSIVE.id(), BLOCK.id(), THICK_SKULL.id()),
    listOf(GENERAL, DEVIOUS),
    listOf(STRENGTH),
    emptyList(),
    listOf(PlayerKeyword.DWARF, PlayerKeyword.LINEMAN),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/oldworldalliance_dwarfblocker.png"),
    SingleSprite.ini("${portraitRootPath}/oldworldalliance_oldworlddwarfblocker.png")
)

val OLD_WORLD_ALLIANCE_DWARF_RUNNER = RosterPosition(
    PositionId("old_world_alliance-dwarf-runner"),
    1,
    "Dwarf Runners",
    "Dwarf Runner",
    "DR",
    80000,
    6, 3, 3, 4, 9,
    listOf(SPRINT.id(), SURE_HANDS.id(), THICK_SKULL.id()),
    listOf(GENERAL, PASSING),
    listOf(AGILITY, STRENGTH),
    emptyList(),
    listOf(PlayerKeyword.DWARF, PlayerKeyword.RUNNER),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/oldworldalliance_dwarfrunner.png"),
    SingleSprite.ini("${portraitRootPath}/oldworldalliance_oldworlddwarfrunner.png")
)

val OLD_WORLD_ALLIANCE_DWARF_BLITZER = RosterPosition(
    PositionId("old_world_alliance-dwarf-blitzer"),
    1,
    "Dwarf Blitzers",
    "Dwarf Blitzer",
    "DB",
    100000,
    5, 3, 4, 4, 10,
    listOf(DIVING_TACKLE.id(), BLOCK.id(), TACKLE.id(), THICK_SKULL.id()),
    listOf(GENERAL, STRENGTH),
    listOf(PASSING),
    emptyList(),
    listOf(PlayerKeyword.DWARF, PlayerKeyword.BLITZER),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/oldworldalliance_dwarfblitzer.png"),
    SingleSprite.ini("${portraitRootPath}/oldworldalliance_oldworlddwarfblitzer.png")
)

val OLD_WORLD_ALLIANCE_TROLL_SLAYER = RosterPosition(
    PositionId("old_world_alliance-troll-slayer"),
    1,
    "Troll Slayers",
    "Troll Slayer",
    "DT",
    95000,
    5, 3, 4, 5, 9,
    listOf(BLOCK.id(), DAUNTLESS.id(), FRENZY.id(), THICK_SKULL.id(), HATRED.id(PlayerKeyword.TROLL)),
    listOf(GENERAL, STRENGTH),
    listOf(AGILITY),
    emptyList(),
    listOf(PlayerKeyword.DWARF, PlayerKeyword.BIG_GUY),
    PlayerSize.BIG_GUY,
    SpriteSheet.ini("${iconRootPath}/oldworldalliance_dwarftrollslayer.png"),
    SingleSprite.ini("${portraitRootPath}/oldworldalliance_oldworlddwarftrollslayer.png")
)

val OLD_WORLD_ALLIANCE_HALFLING_HOPEFUL = RosterPosition(
    PositionId("old_world_alliance-halfling-hopeful"),
    3,
    "Halfling Hopefuls",
    "Halfling Hopeful",
    "H",
    30000,
    5, 2, 3, 4, 7,
    listOf(DODGE.id(), RIGHT_STUFF.id(), STUNTY.id()),
    listOf(AGILITY),
    listOf(GENERAL, STRENGTH),
    emptyList(),
    listOf(PlayerKeyword.HALFLING),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/oldworldalliance_halflinghopeful.png"),
    SingleSprite.ini("${portraitRootPath}/oldworldalliance_oldworldhalflinghopeful.png")
)

val OLD_WORLD_ALLIANCE_OGRE = RosterPosition(
    PositionId("old_world_alliance-ogre"),
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
    SpriteSheet.ini("${iconRootPath}/oldworldalliance_ogre.png"),
    SingleSprite.ini("${portraitRootPath}/oldworldalliance_ogre.png")
)

val OLD_WORLD_ALLIANCE_ALTERN_FOREST_TREEMAN = RosterPosition(
    PositionId("old_world_alliance-altern-forest-treeman"),
    1,
    "Altern Forest Treemans",
    "Altern Forest Treeman",
    "Tr",
    120000,
    2, 6, 5, 5, 11,
    listOf(MIGHTY_BLOW.idAdjustment(1), STAND_FIRM.id(), STRONG_ARM.id(), THICK_SKULL.id(), LONER.idTarget(4), TAKE_ROOT.id(), TIMMMBER.id(), THROW_TEAMMATE.id()),
    listOf(STRENGTH),
    listOf(GENERAL, AGILITY, PASSING),
    emptyList(),
    listOf(PlayerKeyword.ELF, PlayerKeyword.BIG_GUY),
    PlayerSize.BIG_GUY,
    SpriteSheet.ini("${iconRootPath}/oldworldalliance_alternforesttreeman.png"),
    SingleSprite.ini("${portraitRootPath}/oldworldalliance_alternforesttreeman.png")
)

@Serializable
val OLD_WORLD_ALLIANCE_TEAM_BB2025 = Roster(
    id = RosterId("jervis-old-world-alliance"),
    name = "Old World Alliance",
    tier = 2,
    numberOfRerolls = 8,
    rerollCost = 70000,
    allowApothecary = true,
    positions = listOf(OLD_WORLD_ALLIANCE_HUMAN_LINEMAN, OLD_WORLD_ALLIANCE_HUMAN_THROWER, OLD_WORLD_ALLIANCE_HUMAN_CATCHER, OLD_WORLD_ALLIANCE_HUMAN_BLITZER, OLD_WORLD_ALLIANCE_DWARF_LINEMAN, OLD_WORLD_ALLIANCE_DWARF_RUNNER, OLD_WORLD_ALLIANCE_DWARF_BLITZER, OLD_WORLD_ALLIANCE_TROLL_SLAYER, OLD_WORLD_ALLIANCE_HALFLING_HOPEFUL, OLD_WORLD_ALLIANCE_OGRE, OLD_WORLD_ALLIANCE_ALTERN_FOREST_TREEMAN),
    leagues = listOf(RegionalSpecialRule.OLD_WORLD_CLASSIC),
    specialRules = listOf(),
    logo = RosterLogo(
        large = SingleSprite.fumbbl("637106"),
        small = SingleSprite.fumbbl("637106")
    )
)
