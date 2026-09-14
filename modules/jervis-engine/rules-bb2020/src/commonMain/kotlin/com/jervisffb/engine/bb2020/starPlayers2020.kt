package com.jervisffb.engine.bb2020

import com.jervisffb.engine.common.inducements.StarPlayerInducement
import com.jervisffb.engine.common.inducements.starPlayerInducements
import com.jervisffb.engine.model.PlayerSize
import com.jervisffb.engine.model.PositionId
import com.jervisffb.engine.rules.common.roster.RegionalSpecialRule
import com.jervisffb.engine.rules.common.roster.StarPlayerPosition
import com.jervisffb.engine.rules.common.skills.SkillType.DODGE
import com.jervisffb.engine.rules.common.skills.SkillType.LONER
import com.jervisffb.engine.rules.common.skills.SkillType.SIDESTEP
import com.jervisffb.engine.rules.common.skills.SkillType.STAB
import com.jervisffb.engine.rules.common.skills.SkillType.STUNTY
import com.jervisffb.engine.sprites.SingleSprite
import com.jervisffb.engine.sprites.SpriteSheet
import com.jervisffb.engine.sprites.iconRootPath
import com.jervisffb.engine.sprites.portraitRootPath

val THE_BLACK_GOBBO_BB2020 =  StarPlayerPosition(
    PositionId("the-black-gobbo"),
    "The Black Gobbo",
    "Bg",
    225_000,
    6, 2, 3, 3, 9,
    listOf(
        // Bombardier.Factory,
        // DisturbingPrecense.Factory
        DODGE.id(),
        LONER.idTarget(3),
        SIDESTEP.id(),
        // SneakyGit.Factory,
        STAB.id(),
        STUNTY.id()
    ),
    emptyList(),
    emptyList(),
    listOf(RegionalSpecialRule.BADLANDS_BRAWL, RegionalSpecialRule.UNDERWORLD_CHALLENGE),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/TheBlackGobbo.png",1),
    SingleSprite.ini("${portraitRootPath}/TheBlackGobbo.png")
)

val STAR_PLAYERS = listOf(
    THE_BLACK_GOBBO_BB2020,
)

/** The Star Players a BB2020 team can hire, as inducements. See [DEFAULT_INDUCEMENTS_BB2020]. */
val STAR_PLAYER_INDUCEMENTS_BB2020: List<StarPlayerInducement> = starPlayerInducements(STAR_PLAYERS)
