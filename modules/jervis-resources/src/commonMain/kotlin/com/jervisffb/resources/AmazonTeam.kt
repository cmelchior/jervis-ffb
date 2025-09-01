package com.jervisffb.resources

import com.jervisffb.engine.model.PlayerSize
import com.jervisffb.engine.model.PositionId
import com.jervisffb.engine.model.RosterId
import com.jervisffb.engine.rules.bb2020.roster.BB2020Roster
import com.jervisffb.engine.rules.common.roster.RegionalSpecialRule
import com.jervisffb.engine.rules.common.roster.RosterPosition
import com.jervisffb.engine.rules.common.skills.SkillCategory.AGILITY
import com.jervisffb.engine.rules.common.skills.SkillCategory.GENERAL
import com.jervisffb.engine.rules.common.skills.SkillCategory.PASSING
import com.jervisffb.engine.rules.common.skills.SkillCategory.STRENGTH
import com.jervisffb.engine.rules.common.skills.SkillType.DEFENSIVE
import com.jervisffb.engine.rules.common.skills.SkillType.DODGE
import com.jervisffb.engine.rules.common.skills.SkillType.HIT_AND_RUN
import com.jervisffb.engine.rules.common.skills.SkillType.JUMP_UP
import com.jervisffb.engine.rules.common.skills.SkillType.ON_THE_BALL
import com.jervisffb.engine.rules.common.skills.SkillType.PASS
import com.jervisffb.engine.rules.common.skills.SkillType.SAFE_PASS
import com.jervisffb.engine.serialize.RosterLogo
import com.jervisffb.engine.serialize.SingleSprite
import com.jervisffb.engine.serialize.SpriteSheet

/**
 * Amazon Teams
 *
 * See page 47 in the 2022 Almanac.
 */
val AMAZON_LINEMAN =
    RosterPosition(
        PositionId("amazon-lineman"),
        16,
        "Eagle Warrior Linewomen",
        "Eagle Warrior Linewoman",
        "L",
        50_000,
        6, 3, 3, 4, 8,
        listOf(
            DODGE.id()
        ),
        listOf(GENERAL),
        listOf(AGILITY, STRENGTH),
        PlayerSize.STANDARD,
        SpriteSheet.ini("$iconRootPath/amazon_triballinewoman.png",9),
        SingleSprite.ini("$portraitRootPath/amazon_triballinewoman.png")

    )
val AMAZON_THROWER =
    RosterPosition(
        PositionId("amazon-thrower"),
        2,
        "Python Warrior Throwers",
        "Python Warrior Thrower",
        "T",
        80_000,
        6, 3, 3, 3, 8,
        listOf(
            DODGE.id(),
            ON_THE_BALL.id(),
            PASS.id(),
            SAFE_PASS.id()
        ),
        listOf(GENERAL, PASSING),
        listOf(AGILITY, STRENGTH),
        PlayerSize.STANDARD,
        SpriteSheet.ini("$iconRootPath/amazon_eaglewarriorthrower.png",2),
        SingleSprite.ini("$portraitRootPath/amazon_eaglewarriorthrower.png")
    )
val AMAZON_BLITZER =
    RosterPosition(
        PositionId("amazon-blitzer"),
        2,
        "Piranha Warrior Blitzers",
        "Piranha Warrior Blitzer",
        "B",
        90_000,
        7, 3, 3, 5, 8,
        listOf(
            DODGE.id(),
            HIT_AND_RUN.id(),
            JUMP_UP.id()
        ),
        listOf(AGILITY, GENERAL),
        listOf(STRENGTH),
        PlayerSize.STANDARD,
        SpriteSheet.ini("$iconRootPath/amazon_piranhawarriorblitzer.png", 2),
        SingleSprite.ini("$portraitRootPath/amazon_piranhawarriorblitzer.png")
    )
val AMAZON_BLOCKER =
    RosterPosition(
        PositionId("amazon-blocker"),
        2,
        "Jaguar Warrior Blockers",
        "Jaguar Warrior Blocker",
        "Bl",
        110_000,
        6, 4, 3, 5, 9,
        listOf(
            DEFENSIVE.id(),
            DODGE.id(),
        ),
        listOf(GENERAL, STRENGTH),
        listOf(AGILITY),
        PlayerSize.STANDARD,
        SpriteSheet.ini("$iconRootPath/amazon_jaguarwarriorblocker.png", 2),
        SingleSprite.ini("$portraitRootPath/amazon_jaguarwarriorblocker.png")
    )

val AMAZON_TEAM = BB2020Roster(
    id = RosterId("jervis-amazon"),
    name = "Amazon Team",
    tier = 1,
    numberOfRerolls = 8,
    rerollCost = 60_000,
    allowApothecary = true,
    specialRules = listOf(RegionalSpecialRule.LUSTRIAN_SUPERLEAGUE),
    positions = listOf(
        AMAZON_LINEMAN,
        AMAZON_THROWER,
        AMAZON_BLITZER,
        AMAZON_BLOCKER,
    ),
    logo = RosterLogo(
        large = SingleSprite.embedded("jervis/roster/logo_amazon_large.png"),
        small = SingleSprite.embedded("jervis/roster/logo_amazon_small.png")
    )
)
