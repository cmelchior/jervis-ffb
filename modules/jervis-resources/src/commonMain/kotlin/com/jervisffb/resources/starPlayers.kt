package com.jervisffb.resources

import com.jervisffb.engine.model.PositionId
import com.jervisffb.engine.rules.bb2020.roster.RegionalSpecialRule
import com.jervisffb.engine.rules.bb2020.roster.StarPlayerPosition
import com.jervisffb.engine.rules.bb2020.skills.SkillType.DODGE
import com.jervisffb.engine.rules.bb2020.skills.SkillType.LONER
import com.jervisffb.engine.rules.bb2020.skills.SkillType.SIDESTEP
import com.jervisffb.engine.rules.bb2020.skills.SkillType.STAB
import com.jervisffb.engine.rules.bb2020.skills.SkillType.STUNTY
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
    SpriteSheet.embedded("$iconRootPath/TheBlackGobbo.png",1),
    SingleSprite.embedded("$portraitRootPath/TheBlackGobbo.png")
)

val STAR_PLAYERS = listOf(
    THE_BLACK_GOBBO,
)


