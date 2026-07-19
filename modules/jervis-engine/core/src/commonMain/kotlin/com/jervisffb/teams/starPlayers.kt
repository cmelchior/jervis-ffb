package com.jervisffb.teams

import com.jervisffb.engine.model.PlayerSize
import com.jervisffb.engine.model.PositionId
import com.jervisffb.engine.rules.common.roster.RegionalSpecialRule
import com.jervisffb.engine.rules.common.roster.StarPlayerPosition
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
        SkillType.BOMBARDIER.id(),
        SkillType.DISTURBING_PRESENCE.id(),
        SkillType.DODGE.id(),
        SkillType.LONER.idTarget(3),
        SkillType.SIDESTEP.id(),
        SkillType.SNEAKY_GIT.id(),
        SkillType.STAB.id(),
        SkillType.STUNTY.id()
    ),
    emptyList(),
    emptyList(),
    listOf(RegionalSpecialRule.BADLANDS_BRAWL, RegionalSpecialRule.UNDERWORLD_CHALLENGE),
    PlayerSize.STANDARD,
    SpriteSheet.ini("$iconRootPath/TheBlackGobbo.png",1),
    SingleSprite.ini("$portraitRootPath/TheBlackGobbo.png")
)

val STAR_PLAYERS = listOf(
    THE_BLACK_GOBBO,
)


