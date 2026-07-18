package com.jervisffb.resources.bb2025

import com.jervisffb.engine.model.PlayerKeyword
import com.jervisffb.engine.model.PlayerSize
import com.jervisffb.engine.model.PositionId
import com.jervisffb.engine.model.RosterId
import com.jervisffb.engine.rules.common.roster.RegionalSpecialRule
import com.jervisffb.engine.rules.common.roster.Roster
import com.jervisffb.engine.rules.common.roster.RosterPosition
import com.jervisffb.engine.rules.common.skills.SkillCategory.AGILITY
import com.jervisffb.engine.rules.common.skills.SkillCategory.GENERAL
import com.jervisffb.engine.rules.common.skills.SkillCategory.PASSING
import com.jervisffb.engine.rules.common.skills.SkillCategory.STRENGTH
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.rules.common.skills.SkillType.BLOCK
import com.jervisffb.engine.rules.common.skills.SkillType.CATCH
import com.jervisffb.engine.rules.common.skills.SkillType.DIVING_CATCH
import com.jervisffb.engine.rules.common.skills.SkillType.FUMBLEROOSKI
import com.jervisffb.engine.rules.common.skills.SkillType.HAIL_MARY_PASS
import com.jervisffb.engine.rules.common.skills.SkillType.NERVES_OF_STEEL
import com.jervisffb.engine.rules.common.skills.SkillType.PASS
import com.jervisffb.engine.rules.common.skills.SkillType.SIDESTEP
import com.jervisffb.engine.serialize.RosterLogo
import com.jervisffb.engine.serialize.SingleSprite
import com.jervisffb.engine.serialize.SpriteSheet
import com.jervisffb.resources.iconRootPath
import com.jervisffb.resources.portraitRootPath
import kotlinx.serialization.Serializable

val ELVEN_LINEMAN =
    RosterPosition(
        PositionId("elven-union-lineman"),
        16,
        "Linemen",
        "Lineman",
        "L",
        65_000,
        6, 3, 2, 3, 8,
        listOf(FUMBLEROOSKI.id()),
        listOf(GENERAL, AGILITY),
        listOf(STRENGTH),
        emptyList(),
        listOf(PlayerKeyword.ELF, PlayerKeyword.LINEMAN),
        PlayerSize.STANDARD,
        SpriteSheet.ini("${iconRootPath}/elvenunion_lineman.png",8),
        SingleSprite.ini("${portraitRootPath}/elvenunion_lineman.png")
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
        listOf(HAIL_MARY_PASS.id(), PASS.id()),
        listOf(GENERAL, AGILITY, PASSING),
        listOf(STRENGTH),
        emptyList(),
        listOf(PlayerKeyword.ELF, PlayerKeyword.THROWER),
        PlayerSize.STANDARD,
        SpriteSheet.ini("${iconRootPath}/elvenunion_thrower.png",2),
        SingleSprite.ini("${portraitRootPath}/elvenunion_thrower.png")
    )
val ELVEN_CATCHER =
    RosterPosition(
        PositionId("elven-union-catcher"),
        2,
        "Catchers",
        "Catcher",
        "C",
        100_000,
        8, 3, 2, 4, 8,
        listOf(CATCH.id(), DIVING_CATCH.id(), NERVES_OF_STEEL.id()),
        listOf(GENERAL, AGILITY),
        listOf(STRENGTH),
        emptyList(),
        listOf(PlayerKeyword.ELF, PlayerKeyword.CATCHER),
        PlayerSize.STANDARD,
        SpriteSheet.ini("${iconRootPath}/elvenunion_catcher.png", 4),
        SingleSprite.ini("${portraitRootPath}/elvenunion_catcher.png")
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
        listOf(BLOCK.id(), SIDESTEP.id()),
        listOf(GENERAL, AGILITY),
        listOf(PASSING, STRENGTH),
        emptyList(),
        listOf(PlayerKeyword.ELF, PlayerKeyword.BLITZER),
        PlayerSize.STANDARD,
        SpriteSheet.ini("${iconRootPath}/elvenunion_blitzer.png", 2),
        SingleSprite.ini("${portraitRootPath}/elvenunion_blitzer.png")
    )

@Serializable
val ELVEN_UNION_TEAM_BB2025 = Roster(
    id = RosterId("jervis-elven-union"),
    name = "Elven Union",
    tier = 2,
    numberOfRerolls = 8,
    rerollCost = 50_000,
    allowApothecary = true,
    leagues = listOf(RegionalSpecialRule.ELVEN_KINGDOMS_LEAGUE),
    specialRules = emptyList(),
    positions = listOf(
        ELVEN_LINEMAN,
        ELVEN_THROWER,
        ELVEN_CATCHER,
        ELVEN_BLITZER
    ),
    logo = RosterLogo.NONE
)
