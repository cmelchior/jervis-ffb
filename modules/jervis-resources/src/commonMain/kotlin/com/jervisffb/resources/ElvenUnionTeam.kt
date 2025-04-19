package com.jervisffb.resources

import com.jervisffb.engine.model.PositionId
import com.jervisffb.engine.model.RosterId
import com.jervisffb.engine.rules.bb2020.roster.BB2020Roster
import com.jervisffb.engine.rules.bb2020.roster.RosterPosition
import com.jervisffb.engine.rules.bb2020.skills.SkillCategory
import com.jervisffb.engine.rules.bb2020.skills.SkillType
import com.jervisffb.engine.rules.bb2020.skills.SkillType.BLOCK
import com.jervisffb.engine.rules.bb2020.skills.SkillType.CATCH
import com.jervisffb.engine.rules.bb2020.skills.SkillType.PASS
import com.jervisffb.engine.serialize.RosterLogo
import com.jervisffb.engine.serialize.SingleSprite
import com.jervisffb.engine.serialize.SpriteSheet
import kotlinx.serialization.Serializable

val ELVEN_LINEMAN =
    RosterPosition(
        PositionId("elven-union-lineman"),
        12,
        "Lineman",
        "Lineman",
        "L",
        60_000,
        6, 3, 2, 4, 8,
        emptyList(),
        listOf(SkillCategory.AGILITY, SkillCategory.GENERAL),
        listOf(SkillCategory.GENERAL),
        SpriteSheet.ini("$iconRootPath/elvenunion_lineman.png",8),
        SingleSprite.ini("$portraitRootPath/elvenunion_lineman.png")
    )
val ELVEN_THROWER =
    RosterPosition(
        PositionId("elven-union-thrower"),
        2,
        "Throwers",
        "Thrower",
        "T",
        75_000,
        6, 3, 2, 2, 8,
        listOf(PASS.id()),
        listOf(SkillCategory.AGILITY, SkillCategory.GENERAL, SkillCategory.PASSING),
        listOf(SkillCategory.GENERAL),
        SpriteSheet.ini("$iconRootPath/elvenunion_thrower.png",2),
        SingleSprite.ini("$portraitRootPath/elvenunion_thrower.png")
    )
val ELVEN_CATCHER =
    RosterPosition(
        PositionId("elven-union-catcher"),
        4,
        "Catchers",
        "Catcher",
        "C",
        100_000,
        8, 3, 3, 4, 8,
        listOf(CATCH.id(), /* Nerves Of Steel */),
        listOf(SkillCategory.AGILITY, SkillCategory.GENERAL),
        listOf(SkillCategory.GENERAL),
        SpriteSheet.ini("$iconRootPath/elvenunion_catcher.png", 4),
        SingleSprite.ini("$portraitRootPath/elvenunion_catcher.png")
    )
val ELVEN_BLITZER =
    RosterPosition(
        PositionId("elven-union-blitzer"),
        2,
        "Blitzers",
        "Blitzer",
        "B",
        115_000,
        7, 3, 2, 3, 9,
        listOf(BLOCK.id(), SkillType.SIDESTEP.id()),
        listOf(SkillCategory.GENERAL, SkillCategory.GENERAL),
        listOf(SkillCategory.AGILITY, SkillCategory.PASSING),
        SpriteSheet.ini("$iconRootPath/elvenunion_blitzer.png", 2),
        SingleSprite.ini("$portraitRootPath/elvenunion_blitzer.png")
    )

@Serializable
val ELVEN_UNION_TEAM = BB2020Roster(
    id = RosterId("jervis-elvish-union"),
    name = "Elven Union",
    tier = 2,
    numberOfRerolls = 8,
    rerollCost = 50_000,
    allowApothecary = true,
    specialRules = emptyList(),
    positions = listOf(
        ELVEN_LINEMAN,
        ELVEN_THROWER,
        ELVEN_CATCHER,
        ELVEN_BLITZER
    ),
    logo = RosterLogo.NONE
)
