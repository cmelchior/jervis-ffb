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
import com.jervisffb.engine.rules.common.skills.SkillType.BONE_HEAD
import com.jervisffb.engine.rules.common.skills.SkillType.CATCH
import com.jervisffb.engine.rules.common.skills.SkillType.FEND
import com.jervisffb.engine.rules.common.skills.SkillType.GIVE_AND_GO
import com.jervisffb.engine.rules.common.skills.SkillType.LONER
import com.jervisffb.engine.rules.common.skills.SkillType.MIGHTY_BLOW
import com.jervisffb.engine.rules.common.skills.SkillType.PASS
import com.jervisffb.engine.rules.common.skills.SkillType.PRO
import com.jervisffb.engine.rules.common.skills.SkillType.STAND_FIRM
import com.jervisffb.engine.rules.common.skills.SkillType.THICK_SKULL
import com.jervisffb.engine.rules.common.skills.SkillType.THROW_TEAMMATE
import com.jervisffb.engine.rules.common.skills.SkillType.WRESTLE
import com.jervisffb.engine.sprites.RosterLogo
import com.jervisffb.engine.sprites.SingleSprite
import com.jervisffb.engine.sprites.SpriteSheet
import com.jervisffb.resources.iconRootPath
import com.jervisffb.resources.portraitRootPath
import kotlinx.serialization.Serializable

val IMPERIAL_NOBILITY_IMPERIAL_RETAINER = RosterPosition(
    PositionId("imperial_nobility-imperial-retainer"),
    16,
    "Imperial Retainers",
    "Imperial Retainer",
    "L",
    45000,
    6, 3, 3, 4, 8,
    listOf(FEND.id()),
    listOf(GENERAL),
    listOf(AGILITY, STRENGTH),
    emptyList(),
    listOf(PlayerKeyword.HUMAN, PlayerKeyword.LINEMAN),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/imperialnobility_imperialretainerlineman.png"),
    SingleSprite.ini("${portraitRootPath}/imperialnobility_imperialretainerlineman.png")
)

val IMPERIAL_NOBILITY_IMPERIAL_THROWER = RosterPosition(
    PositionId("imperial_nobility-imperial-thrower"),
    2,
    "Imperial Throwers",
    "Imperial Thrower",
    "T",
    75000,
    6, 3, 3, 2, 9,
    listOf(PRO.id(), PASS.id(), GIVE_AND_GO.id()),
    listOf(GENERAL, PASSING),
    listOf(AGILITY, STRENGTH),
    emptyList(),
    listOf(PlayerKeyword.HUMAN, PlayerKeyword.THROWER),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/imperialnobility_imperialthrower.png"),
    SingleSprite.ini("${portraitRootPath}/imperialnobility_imperialthrower.png")
)

val IMPERIAL_NOBILITY_NOBLE_BLITZER = RosterPosition(
    PositionId("imperial_nobility-noble-blitzer"),
    2,
    "Noble Blitzers",
    "Noble Blitzer",
    "B",
    90000,
    7, 3, 3, 4, 9,
    listOf(CATCH.id(), BLOCK.id(), PRO.id()),
    listOf(GENERAL, AGILITY),
    listOf(STRENGTH, PASSING),
    emptyList(),
    listOf(PlayerKeyword.HUMAN, PlayerKeyword.BLITZER),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/imperialnobility_nobleblitzer.png"),
    SingleSprite.ini("${portraitRootPath}/imperialnobility_nobleblitzer.png")
)

val IMPERIAL_NOBILITY_BODYGUARD = RosterPosition(
    PositionId("imperial_nobility-bodyguard"),
    4,
    "Bodyguards",
    "Bodyguard",
    "Bo",
    85000,
    5, 3, 3, 4, 9,
    listOf(WRESTLE.id(), STAND_FIRM.id()),
    listOf(GENERAL, STRENGTH),
    listOf(AGILITY),
    emptyList(),
    listOf(PlayerKeyword.HUMAN, PlayerKeyword.BLOCKER),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/imperialnobility_bodyguard.png"),
    SingleSprite.ini("${portraitRootPath}/imperialnobility_bodyguard.png")
)

val IMPERIAL_NOBILITY_OGRE = RosterPosition(
    PositionId("imperial_nobility-ogre"),
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
    SpriteSheet.ini("${iconRootPath}/imperialnobility_ogre.png"),
    SingleSprite.ini("${portraitRootPath}/imperialnobility_ogre.png")
)

@Serializable
val IMPERIAL_NOBILITY_TEAM_BB2025 = Roster(
    id = RosterId("jervis-imperial-nobility"),
    name = "Imperial Nobility",
    tier = 2,
    numberOfRerolls = 8,
    rerollCost = 60000,
    allowApothecary = true,
    positions = listOf(IMPERIAL_NOBILITY_IMPERIAL_RETAINER, IMPERIAL_NOBILITY_IMPERIAL_THROWER, IMPERIAL_NOBILITY_NOBLE_BLITZER, IMPERIAL_NOBILITY_BODYGUARD, IMPERIAL_NOBILITY_OGRE),
    leagues = listOf(RegionalSpecialRule.OLD_WORLD_CLASSIC),
    specialRules = listOf(),
    logo = RosterLogo(
        large = SingleSprite.fumbbl("673311"),
        small = SingleSprite.fumbbl("673311")
    )
)
