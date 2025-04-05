package com.jervisffb.resources

import com.jervisffb.engine.model.PositionId
import com.jervisffb.engine.rules.bb2020.roster.BB2020StarPlayer
import com.jervisffb.engine.rules.bb2020.roster.RegionalSpecialRule
import com.jervisffb.engine.rules.bb2020.skills.Dodge
import com.jervisffb.engine.rules.bb2020.skills.Loner
import com.jervisffb.engine.rules.bb2020.skills.Sidestep
import com.jervisffb.engine.rules.bb2020.skills.Stab
import com.jervisffb.engine.rules.bb2020.skills.Stunty
import com.jervisffb.engine.serialize.SingleSprite
import com.jervisffb.engine.serialize.SpriteSheet

val THE_BLACK_GOBBO = BB2020StarPlayer(
    PositionId("the-black-gobbo"),
    "The Black Gobbo",
    "Bg",
    225_000,
    6, 2, 3, 3, 9,
    listOf(
        // Bombardier.Factory,
        // DisturbingPrecense.Factory,
        Dodge.Factory,
        Loner.Factory(3),
        Sidestep.Factory,
        // SneakyGit.Factory,
        Stab.Factory,
        Stunty.Factory
    ),
    listOf(RegionalSpecialRule.BADLANDS_BRAWL, RegionalSpecialRule.UNDERWORLD_CHALLENGE),
    SpriteSheet.embedded("$iconRootPath/TheBlackGobbo.png",1),
    SingleSprite.embedded("$portraitRootPath/TheBlackGobbo.png")
)

val STAR_PLAYERS = listOf(
    THE_BLACK_GOBBO,
)


