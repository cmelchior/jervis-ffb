package com.jervisffb.resources.bb2025

import com.jervisffb.engine.model.PlayerKeyword
import com.jervisffb.engine.model.PlayerSize
import com.jervisffb.engine.model.PositionId
import com.jervisffb.engine.model.RosterId
import com.jervisffb.engine.rules.common.roster.RegionalSpecialRule
import com.jervisffb.engine.rules.common.roster.RegionalSpecialRule.CHAOS_CLASH
import com.jervisffb.engine.rules.common.roster.RegionalSpecialRule.OLD_WORLD_CLASSIC
import com.jervisffb.engine.rules.common.roster.Roster
import com.jervisffb.engine.rules.common.roster.RosterPosition
import com.jervisffb.engine.rules.common.roster.TeamSpecialRule
import com.jervisffb.engine.rules.common.roster.TeamSpecialRule.FAVOURED_OF_CHAOS_UNDIVIDED
import com.jervisffb.engine.rules.common.skills.SkillCategory.AGILITY
import com.jervisffb.engine.rules.common.skills.SkillCategory.GENERAL
import com.jervisffb.engine.rules.common.skills.SkillCategory.PASSING
import com.jervisffb.engine.rules.common.skills.SkillCategory.STRENGTH
import com.jervisffb.engine.rules.common.skills.SkillType.BLOCK
import com.jervisffb.engine.rules.common.skills.SkillType.CATCH
import com.jervisffb.engine.rules.common.skills.SkillType.CLAWS
import com.jervisffb.engine.rules.common.skills.SkillType.DAUNTLESS
import com.jervisffb.engine.rules.common.skills.SkillType.DISTURBING_PRESENCE
import com.jervisffb.engine.rules.common.skills.SkillType.DODGE
import com.jervisffb.engine.rules.common.skills.SkillType.DRUNKARD
import com.jervisffb.engine.rules.common.skills.SkillType.FRENZY
import com.jervisffb.engine.rules.common.skills.SkillType.JUMP_UP
import com.jervisffb.engine.rules.common.skills.SkillType.LONER
import com.jervisffb.engine.rules.common.skills.SkillType.NO_BALL
import com.jervisffb.engine.rules.common.skills.SkillType.PASS
import com.jervisffb.engine.rules.common.skills.SkillType.PICK_ME_UP
import com.jervisffb.engine.rules.common.skills.SkillType.STRIP_BALL
import com.jervisffb.engine.rules.common.skills.SkillType.STUNTY
import com.jervisffb.engine.rules.common.skills.SkillType.THICK_SKULL
import com.jervisffb.engine.rules.common.skills.SkillType.TITCHY
import com.jervisffb.engine.rules.common.skills.SkillType.UNCHANNELLED_FURY
import com.jervisffb.engine.rules.common.skills.SkillType.UNSTEADY
import com.jervisffb.engine.sprites.RosterLogo
import com.jervisffb.engine.sprites.SingleSprite
import com.jervisffb.engine.sprites.SpriteSheet
import com.jervisffb.resources.iconRootPath
import com.jervisffb.resources.portraitRootPath
import kotlinx.serialization.Serializable

val NORSE_NORSE_RAIDER = RosterPosition(
    PositionId("norse-norse-raider"),
    16,
    "Norse Raiders",
    "Norse Raider",
    "L",
    50000,
    6, 3, 3, 4, 8,
    listOf(BLOCK.id(), THICK_SKULL.id(), DRUNKARD.id(), UNSTEADY.id()),
    listOf(GENERAL),
    listOf(AGILITY, STRENGTH, PASSING),
    emptyList(),
    listOf(PlayerKeyword.HUMAN, PlayerKeyword.LINEMAN),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/norse_lineman.png"),
    SingleSprite.ini("${portraitRootPath}/norse_lineman.png")
)

val NORSE_NORSE_BERSERKER = RosterPosition(
    PositionId("norse-norse-berserker"),
    2,
    "Norse Berserkers",
    "Norse Berserker",
    "B",
    90000,
    6, 3, 3, 5, 8,
    listOf(JUMP_UP.id(), BLOCK.id(), FRENZY.id()),
    listOf(GENERAL, STRENGTH),
    listOf(AGILITY, PASSING),
    emptyList(),
    listOf(PlayerKeyword.HUMAN),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/norse_berserker.png"),
    SingleSprite.ini("${portraitRootPath}/norse_berserker.png")
)

val NORSE_ULFWERENER = RosterPosition(
    PositionId("norse-ulfwerener"),
    2,
    "Ulfwereners",
    "Ulfwerener",
    "U",
    105000,
    6, 4, 4, 6, 9,
    listOf(FRENZY.id(), UNSTEADY.id()),
    listOf(GENERAL, STRENGTH),
    listOf(AGILITY),
    emptyList(),
    listOf(PlayerKeyword.HUMAN),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/norse_ulfwerenar.png"),
    SingleSprite.ini("${portraitRootPath}/norse_ulfwerenar.png")
)

val NORSE_YHETEE = RosterPosition(
    PositionId("norse-yhetee"),
    1,
    "Yhetees",
    "Yhetee",
    "Y",
    140000,
    5, 5, 4, 6, 9,
    listOf(FRENZY.id(), CLAWS.id(), DISTURBING_PRESENCE.id(), LONER.idTarget(4), UNCHANNELLED_FURY.id()),
    listOf(STRENGTH),
    listOf(GENERAL, AGILITY),
    emptyList(),
    listOf(PlayerKeyword.YHETEE, PlayerKeyword.BIG_GUY),
    PlayerSize.BIG_GUY,
    SpriteSheet.ini("${iconRootPath}/norse_snowtroll.png"),
    SingleSprite.ini("${portraitRootPath}/norse_snowtroll.png")
)

val NORSE_BEER_BOAR = RosterPosition(
    PositionId("norse-beer-boar"),
    2,
    "Beer Boars",
    "Beer Boar",
    "b",
    20000,
    5, 1, 3, null, 6,
    listOf(DODGE.id(), NO_BALL.id(), STUNTY.id(), TITCHY.id(), PICK_ME_UP.id()),
    listOf(),
    listOf(AGILITY),
    emptyList(),
    listOf(PlayerKeyword.HUMAN),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/norse_beerboar.png"),
    SingleSprite.ini("${portraitRootPath}/norse_beerboar.png")
)

val NORSE_VALKYRIE = RosterPosition(
    PositionId("norse-valkyrie"),
    2,
    "Valkyries",
    "Valkyrie",
    "V",
    95000,
    7, 3, 3, 3, 8,
    listOf(CATCH.id(), DAUNTLESS.id(), STRIP_BALL.id(), PASS.id()),
    listOf(GENERAL, AGILITY, PASSING),
    listOf(STRENGTH),
    emptyList(),
    listOf(PlayerKeyword.HUMAN),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/norse_valkyrie.png"),
    SingleSprite.ini("${portraitRootPath}/norse_valkyrie.png")
)

@Serializable
val NORSE_TEAM_BB2025 = Roster(
    id = RosterId("jervis-norse"),
    name = "Norse",
    tier = 2,
    numberOfRerolls = 8,
    rerollCost = 60000,
    allowApothecary = true,
    positions = listOf(NORSE_NORSE_RAIDER, NORSE_NORSE_BERSERKER, NORSE_ULFWERENER, NORSE_YHETEE, NORSE_BEER_BOAR, NORSE_VALKYRIE),
    leagues = listOf(RegionalSpecialRule.OLD_WORLD_CLASSIC, RegionalSpecialRule.CHAOS_CLASH),
    specialRules = listOf(TeamSpecialRule.FAVOURED_OF_CHAOS_UNDIVIDED),
    logo = RosterLogo(
        large = SingleSprite.fumbbl("486319"),
        small = SingleSprite.fumbbl("486319")
    )
)
