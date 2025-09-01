package com.jervisffb.resources

import com.jervisffb.engine.model.PlayerSize
import com.jervisffb.engine.model.PositionId
import com.jervisffb.engine.rules.common.roster.RegionalSpecialRule
import com.jervisffb.engine.rules.common.roster.StarPlayerPosition
import com.jervisffb.engine.rules.common.skills.SkillType.DODGE
import com.jervisffb.engine.rules.common.skills.SkillType.LONER
import com.jervisffb.engine.rules.common.skills.SkillType.SIDESTEP
import com.jervisffb.engine.rules.common.skills.SkillType.STAB
import com.jervisffb.engine.rules.common.skills.SkillType.STUNTY
import com.jervisffb.engine.serialize.SingleSprite
import com.jervisffb.engine.serialize.SpriteSheet

val THE_BLACK_GOBBO =  StarPlayerPosition(
    PositionId("the-black-gobbo"),
    "The Black Gobbo",
    "Bg",
    225_000,
    6, 2, 3, 3, 9,
    listOf(
        // Bombardier.Factory,
        // DisturbingPrecense.Factory
        DODGE.id(),
        LONER.id(3),
        SIDESTEP.id(),
        // SneakyGit.Factory,
        STAB.id(),
        STUNTY.id()
    ),
    listOf(RegionalSpecialRule.BADLANDS_BRAWL, RegionalSpecialRule.UNDERWORLD_CHALLENGE),
    PlayerSize.STANDARD,
    SpriteSheet.ini("$iconRootPath/TheBlackGobbo.png",1),
    SingleSprite.ini("$portraitRootPath/TheBlackGobbo.png")
)

val STAR_PLAYERS = listOf(
    THE_BLACK_GOBBO,
)


