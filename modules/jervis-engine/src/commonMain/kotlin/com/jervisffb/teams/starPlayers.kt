package com.jervisffb.teams

import com.jervisffb.engine.model.PlayerSize
import com.jervisffb.engine.model.PositionId
import com.jervisffb.engine.rules.bb2020.roster.RegionalSpecialRule
import com.jervisffb.engine.rules.bb2020.roster.StarPlayerPosition
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.serialize.SingleSprite
import com.jervisffb.engine.serialize.SpriteSheet

val THE_BLACK_GOBBO = StarPlayerPosition(
    PositionId("the-black-gobbo"),
    "The Black Gobbo",
    "Bg",
    225_000,
    6, 2, 3, 3, 9,
    listOf(
        // Bombardier.Factory,
        // DisturbingPrecense.Factory,
        SkillType.DODGE.id(),
        SkillType.LONER.id(3),
        SkillType.SIDESTEP.id(),
        // SneakyGit.Factory,
        SkillType.STAB.id(),
        SkillType.STUNTY.id()
    ),
    listOf(RegionalSpecialRule.BADLANDS_BRAWL, RegionalSpecialRule.UNDERWORLD_CHALLENGE),
    PlayerSize.STANDARD,
    SpriteSheet.embedded("$iconRootPath/TheBlackGobbo.png",1),
    SingleSprite.embedded("$portraitRootPath/TheBlackGobbo.png")
)

val STAR_PLAYERS = listOf(
    THE_BLACK_GOBBO,
)


