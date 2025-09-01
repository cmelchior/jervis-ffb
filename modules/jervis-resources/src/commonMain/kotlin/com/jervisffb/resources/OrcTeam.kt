package com.jervisffb.resources

import com.jervisffb.engine.model.PlayerSize
import com.jervisffb.engine.model.PositionId
import com.jervisffb.engine.model.RosterId
import com.jervisffb.engine.rules.bb2020.roster.BB2020Roster
import com.jervisffb.engine.rules.bb2020.roster.RegionalSpecialRule
import com.jervisffb.engine.rules.bb2020.roster.RosterPosition
import com.jervisffb.engine.rules.bb2020.skills.SkillCategory.AGILITY
import com.jervisffb.engine.rules.bb2020.skills.SkillCategory.GENERAL
import com.jervisffb.engine.rules.bb2020.skills.SkillCategory.PASSING
import com.jervisffb.engine.rules.bb2020.skills.SkillCategory.STRENGTH
import com.jervisffb.engine.rules.bb2020.skills.SkillType.ALWAYS_HUNGRY
import com.jervisffb.engine.rules.bb2020.skills.SkillType.BLOCK
import com.jervisffb.engine.rules.bb2020.skills.SkillType.DODGE
import com.jervisffb.engine.rules.bb2020.skills.SkillType.LONER
import com.jervisffb.engine.rules.bb2020.skills.SkillType.MIGHTY_BLOW
import com.jervisffb.engine.rules.bb2020.skills.SkillType.PASS
import com.jervisffb.engine.rules.bb2020.skills.SkillType.PROJECTILE_VOMIT
import com.jervisffb.engine.rules.bb2020.skills.SkillType.REALLY_STUPID
import com.jervisffb.engine.rules.bb2020.skills.SkillType.REGENERATION
import com.jervisffb.engine.rules.bb2020.skills.SkillType.RIGHT_STUFF
import com.jervisffb.engine.rules.bb2020.skills.SkillType.STUNTY
import com.jervisffb.engine.rules.bb2020.skills.SkillType.SURE_HANDS
import com.jervisffb.engine.rules.bb2020.skills.SkillType.THROW_TEAMMATE
import com.jervisffb.engine.serialize.RosterLogo
import com.jervisffb.engine.serialize.SingleSprite
import com.jervisffb.engine.serialize.SpriteSheet
import kotlinx.serialization.Serializable

/**
 * Orc Team
 *
 * See page 123 in the rulebook
 */

val ORC_LINEMEN =
    RosterPosition(
        PositionId("orc-lineman"),
        16,
        "Orc Linemen",
        "Orc Lineman",
        "L",
        50_000,
        5, 3, 3, 4, 10,
        emptyList(/* Animosity */),
        listOf(GENERAL),
        listOf(AGILITY, STRENGTH),
        PlayerSize.STANDARD,
        SpriteSheet.ini("$iconRootPath/orc_lineman.png",6),
        SingleSprite.ini("$portraitRootPath/orc_lineman.png")

    )
val ORC_THROWER =
    RosterPosition(
        PositionId("orc-thrower"),
        2,
        "Throwers",
        "Thrower",
        "Tr",
        65_000,
        5, 3, 3, 3, 9,
        listOf(
            /* Animosity */
            PASS.id(),
            SURE_HANDS.id()
        ),
        listOf(GENERAL, PASSING),
        listOf(AGILITY, STRENGTH),
        PlayerSize.STANDARD,
        SpriteSheet.ini("$iconRootPath/orc_thrower.png",2),
        SingleSprite.ini("$portraitRootPath/orc_thrower.png")
    )
val ORC_BLITZER =
    RosterPosition(
        PositionId("orc-blitzer"),
        4,
        "Blitzers",
        "Blitzer",
        "B",
        80_000,
        6, 3, 3, 4, 10,
        listOf(
            /* Animosity */
            BLOCK.id(),
        ),
        listOf(AGILITY, GENERAL),
        listOf(STRENGTH, PASSING),
        PlayerSize.STANDARD,
        SpriteSheet.ini("$iconRootPath/orc_blitzer.png", 4),
        SingleSprite.ini("$portraitRootPath/orc_blitzer.png")
    )
val BIG_UN_BLOCKERS =
    RosterPosition(
        PositionId("orc-bigunblocker"),
        4,
        "Big Un Blockers",
        "Big Un Blocker",
        "Bu",
        90_000,
        5, 4, 4, null, 10,
        listOf(
            /* Animosity */
        ),
        listOf(GENERAL, STRENGTH),
        listOf(AGILITY),
        PlayerSize.STANDARD,
        SpriteSheet.ini("$iconRootPath/orc_bigunblocker.png", 4),
        SingleSprite.ini("$portraitRootPath/orc_bigunblocker.png")
    )
val GOBLIN =
    RosterPosition(
        PositionId("orc-goblin"),
        4,
        "Goblins",
        "Goblin",
        "G",
        40_000,
        6, 2, 3, 4, 8,
        listOf(
            DODGE.id(),
            RIGHT_STUFF.id(),
            STUNTY.id()
        ),
        listOf(AGILITY),
        listOf(GENERAL, STRENGTH),
        PlayerSize.STANDARD,
        SpriteSheet.ini("$iconRootPath/orc_goblin.png", 4),
        SingleSprite.ini("$portraitRootPath/orc_goblin.png")
    )
val UNTRAINED_TROLL =
    RosterPosition(
        PositionId("orc-troll"),
        1,
        "Untrained Troll",
        "Untrained Troll",
        "T",
        115_000,
        4, 5, 5, 5, 10,
        listOf(
            ALWAYS_HUNGRY.id(),
            LONER.id(4),
            MIGHTY_BLOW.id(1),
            PROJECTILE_VOMIT.id(),
            REALLY_STUPID.id(),
            REGENERATION.id(),
            THROW_TEAMMATE.id()
        ),
        listOf(STRENGTH),
        listOf(AGILITY, GENERAL, PASSING),
        PlayerSize.BIG_GUY,
        SpriteSheet.ini("$iconRootPath/orc_troll.png", 1),
        SingleSprite.ini("$portraitRootPath/orc_troll.png")
    )

@Serializable
val ORC_TEAM = BB2020Roster(
    id = RosterId("jervis-orc"),
    name = "Orc",
    tier = 1,
    numberOfRerolls = 8,
    rerollCost = 60_000,
    allowApothecary = true,
    specialRules = listOf(
        RegionalSpecialRule.BADLANDS_BRAWL,
    ),
    positions = listOf(
        ORC_LINEMEN,
        ORC_THROWER,
        ORC_BLITZER,
        BIG_UN_BLOCKERS,
        GOBLIN,
        UNTRAINED_TROLL,
    ),
    logo = RosterLogo(
        large = SingleSprite.embedded("jervis/roster/logo_orc_large.png"),
        small = SingleSprite.embedded("jervis/roster/logo_orc_small.png")
    )
)
