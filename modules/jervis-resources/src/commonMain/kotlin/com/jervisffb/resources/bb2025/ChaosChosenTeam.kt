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
import com.jervisffb.engine.rules.common.skills.SkillType.ARM_BAR
import com.jervisffb.engine.rules.common.skills.SkillType.BONE_HEAD
import com.jervisffb.engine.rules.common.skills.SkillType.FRENZY
import com.jervisffb.engine.rules.common.skills.SkillType.HORNS
import com.jervisffb.engine.rules.common.skills.SkillType.LONER
import com.jervisffb.engine.rules.common.skills.SkillType.MIGHTY_BLOW
import com.jervisffb.engine.rules.common.skills.SkillType.PROJECTILE_VOMIT
import com.jervisffb.engine.rules.common.skills.SkillType.REALLY_STUPID
import com.jervisffb.engine.rules.common.skills.SkillType.REGENERATION
import com.jervisffb.engine.rules.common.skills.SkillType.THICK_SKULL
import com.jervisffb.engine.rules.common.skills.SkillType.THROW_TEAMMATE
import com.jervisffb.engine.rules.common.skills.SkillType.UNCHANNELLED_FURY
import com.jervisffb.engine.sprites.RosterLogo
import com.jervisffb.engine.sprites.SingleSprite
import com.jervisffb.engine.sprites.SpriteSheet
import com.jervisffb.resources.iconRootPath
import com.jervisffb.resources.portraitRootPath
import kotlinx.serialization.Serializable

val CHAOS_CHOSEN_BEASTMAN_RUNNER_LINEMAN = RosterPosition(
    PositionId("chaos_chosen-beastman-runner-lineman"),
    16,
    "Beastman Runner Linemans",
    "Beastman Runner Lineman",
    "R",
    55000,
    6, 3, 3, 3, 9,
    listOf(HORNS.id(), THICK_SKULL.id()),
    listOf(GENERAL),
    listOf(AGILITY, STRENGTH, PASSING, DEVIOUS),
    emptyList(),
    listOf(PlayerKeyword.BEASTMAN, PlayerKeyword.LINEMAN),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/chaoschosen_beastmanrunner.png"),
    SingleSprite.ini("${portraitRootPath}/chaoschosen_beastmanrunner.png")
)

val CHAOS_CHOSEN_CHAOS_CHOSEN = RosterPosition(
    PositionId("chaos_chosen-chaos-chosen"),
    4,
    "Chaos Chosens",
    "Chaos Chosen",
    "C",
    100000,
    5, 4, 3, 5, 10,
    listOf(ARM_BAR.id()),
    listOf(GENERAL, STRENGTH),
    listOf(AGILITY, DEVIOUS),
    emptyList(),
    listOf(PlayerKeyword.BEASTMAN),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/chaoschosen_chosenblocker.png"),
    SingleSprite.ini("${portraitRootPath}/chaoschosen_chosenblocker.png")
)

val CHAOS_CHOSEN_CHAOS_TROLL = RosterPosition(
    PositionId("chaos_chosen-chaos-troll"),
    1,
    "Chaos Trolls",
    "Chaos Troll",
    "T",
    115000,
    4, 5, 5, 5, 10,
    listOf(MIGHTY_BLOW.idAdjustment(1), ALWAYS_HUNGRY.id(), LONER.idTarget(4), PROJECTILE_VOMIT.id(), REALLY_STUPID.id(), REGENERATION.id(), THROW_TEAMMATE.id()),
    listOf(STRENGTH),
    listOf(GENERAL, AGILITY, PASSING),
    emptyList(),
    listOf(PlayerKeyword.TROLL, PlayerKeyword.BIG_GUY),
    PlayerSize.BIG_GUY,
    SpriteSheet.ini("${iconRootPath}/chaoschosen_chaostroll.png"),
    SingleSprite.ini("${portraitRootPath}/chaoschosen_chaostroll.png")
)

val CHAOS_CHOSEN_OGRE = RosterPosition(
    PositionId("chaos_chosen-ogre"),
    1,
    "Ogres",
    "Ogre",
    "O",
    140000,
    5, 5, 4, 5, 10,
    listOf(MIGHTY_BLOW.idAdjustment(1), THICK_SKULL.id(), BONE_HEAD.id(), LONER.idTarget(4), THROW_TEAMMATE.id()),
    listOf(STRENGTH),
    listOf(GENERAL, AGILITY),
    emptyList(),
    listOf(PlayerKeyword.OGRE, PlayerKeyword.BIG_GUY),
    PlayerSize.BIG_GUY,
    SpriteSheet.ini("${iconRootPath}/chaoschosen_chaosogre.png"),
    SingleSprite.ini("${portraitRootPath}/chaoschosen_chaosogre.png")
)

val CHAOS_CHOSEN_MINOTAUR = RosterPosition(
    PositionId("chaos_chosen-minotaur"),
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
    SpriteSheet.ini("${iconRootPath}/chaoschosen_minotaur.png"),
    SingleSprite.ini("${portraitRootPath}/chaoschosen_minotaur.png")
)

@Serializable
val CHAOS_CHOSEN_TEAM_BB2025 = Roster(
    id = RosterId("jervis-chaos-chosen"),
    name = "Chaos Chosen",
    tier = 2,
    numberOfRerolls = 8,
    rerollCost = 50000,
    allowApothecary = true,
    positions = listOf(CHAOS_CHOSEN_BEASTMAN_RUNNER_LINEMAN, CHAOS_CHOSEN_CHAOS_CHOSEN, CHAOS_CHOSEN_CHAOS_TROLL, CHAOS_CHOSEN_OGRE, CHAOS_CHOSEN_MINOTAUR),
    leagues = listOf(RegionalSpecialRule.CHAOS_CLASH),
    specialRules = listOf(TeamSpecialRule.FAVOURED_OF_CHAOS_UNDIVIDED),
    logo = RosterLogo(
        large = SingleSprite.fumbbl("486247"),
        small = SingleSprite.fumbbl("486247")
    )
)
