package com.jervisffb.resources.bb2025

import com.jervisffb.engine.model.PlayerKeyword
import com.jervisffb.engine.model.PlayerSize
import com.jervisffb.engine.model.PositionId
import com.jervisffb.engine.model.RosterId
import com.jervisffb.engine.rules.common.roster.RegionalSpecialRule
import com.jervisffb.engine.rules.common.roster.RegionalSpecialRule.BADLANDS_BRAWL
import com.jervisffb.engine.rules.common.roster.RegionalSpecialRule.WORLDS_EDGE_SUPERLEAGUE
import com.jervisffb.engine.rules.common.roster.Roster
import com.jervisffb.engine.rules.common.roster.RosterPosition
import com.jervisffb.engine.rules.common.roster.TeamSpecialRule
import com.jervisffb.engine.rules.common.roster.TeamSpecialRule.BRAWLIN_BRUTES
import com.jervisffb.engine.rules.common.roster.TeamSpecialRule.LOW_COST_LINEMEN
import com.jervisffb.engine.rules.common.skills.SkillCategory.AGILITY
import com.jervisffb.engine.rules.common.skills.SkillCategory.DEVIOUS
import com.jervisffb.engine.rules.common.skills.SkillCategory.GENERAL
import com.jervisffb.engine.rules.common.skills.SkillCategory.PASSING
import com.jervisffb.engine.rules.common.skills.SkillCategory.STRENGTH
import com.jervisffb.engine.rules.common.skills.SkillType.BONE_HEAD
import com.jervisffb.engine.rules.common.skills.SkillType.DODGE
import com.jervisffb.engine.rules.common.skills.SkillType.KICK_TEAMMATE
import com.jervisffb.engine.rules.common.skills.SkillType.MIGHTY_BLOW
import com.jervisffb.engine.rules.common.skills.SkillType.RIGHT_STUFF
import com.jervisffb.engine.rules.common.skills.SkillType.SIDESTEP
import com.jervisffb.engine.rules.common.skills.SkillType.STUNTY
import com.jervisffb.engine.rules.common.skills.SkillType.THICK_SKULL
import com.jervisffb.engine.rules.common.skills.SkillType.THROW_TEAMMATE
import com.jervisffb.engine.rules.common.skills.SkillType.TITCHY
import com.jervisffb.engine.sprites.RosterLogo
import com.jervisffb.engine.sprites.SingleSprite
import com.jervisffb.engine.sprites.SpriteSheet
import com.jervisffb.resources.iconRootPath
import com.jervisffb.resources.portraitRootPath
import kotlinx.serialization.Serializable

val OGRE_GNOBLAR_LINEMAN = RosterPosition(
    PositionId("ogre-gnoblar-lineman"),
    16,
    "Gnoblar Linemans",
    "Gnoblar Lineman",
    "L",
    15000,
    5, 1, 3, 4, 6,
    listOf(DODGE.id(), SIDESTEP.id(), RIGHT_STUFF.id(), STUNTY.id(), TITCHY.id()),
    listOf(AGILITY, DEVIOUS),
    listOf(GENERAL),
    emptyList(),
    listOf(PlayerKeyword.GNOBLAR, PlayerKeyword.LINEMAN),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/ogre_gnoblarlineman.png"),
    SingleSprite.ini("${portraitRootPath}/ogre_gnoblar.png")
)

val OGRE_OGRE_RUNT_PUNTER = RosterPosition(
    PositionId("ogre-ogre-runt-punter"),
    1,
    "Ogre Runt Punters",
    "Ogre Runt Punter",
    "RP",
    145000,
    5, 5, 4, 4, 10,
    listOf(MIGHTY_BLOW.idAdjustment(1), THICK_SKULL.id(), BONE_HEAD.id(), KICK_TEAMMATE.id()),
    listOf(STRENGTH, PASSING),
    listOf(GENERAL, AGILITY, DEVIOUS),
    emptyList(),
    listOf(PlayerKeyword.OGRE, PlayerKeyword.BIG_GUY),
    PlayerSize.BIG_GUY,
    SpriteSheet.ini("${iconRootPath}/ogre_ogreruntpunter.png"),
    SingleSprite.ini("${portraitRootPath}/ogre_runtpunter.png")
)

val OGRE_OGRE_BLOCKER = RosterPosition(
    PositionId("ogre-ogre-blocker"),
    5,
    "Ogre Blockers",
    "Ogre Blocker",
    "OB",
    140000,
    5, 5, 4, 5, 10,
    listOf(MIGHTY_BLOW.idAdjustment(1), THICK_SKULL.id(), BONE_HEAD.id(), THROW_TEAMMATE.id()),
    listOf(STRENGTH),
    listOf(GENERAL, AGILITY, PASSING, DEVIOUS),
    emptyList(),
    listOf(PlayerKeyword.OGRE, PlayerKeyword.BLOCKER, PlayerKeyword.BIG_GUY),
    PlayerSize.BIG_GUY,
    SpriteSheet.ini("${iconRootPath}/ogre_ogre.png"),
    SingleSprite.ini("${portraitRootPath}/ogre_ogre.png")
)

@Serializable
val OGRE_TEAM_BB2025 = Roster(
    id = RosterId("jervis-ogre"),
    name = "Ogre",
    tier = 3,
    numberOfRerolls = 8,
    rerollCost = 70000,
    allowApothecary = true,
    positions = listOf(OGRE_GNOBLAR_LINEMAN, OGRE_OGRE_RUNT_PUNTER, OGRE_OGRE_BLOCKER),
    leagues = listOf(RegionalSpecialRule.BADLANDS_BRAWL, RegionalSpecialRule.WORLDS_EDGE_SUPERLEAGUE),
    specialRules = listOf(TeamSpecialRule.LOW_COST_LINEMEN, TeamSpecialRule.BRAWLIN_BRUTES),
    logo = RosterLogo(
        large = SingleSprite.fumbbl("486331"),
        small = SingleSprite.fumbbl("486331")
    )
)
