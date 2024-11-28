package com.jervisffb.engine.rules.bb2020.roster

import com.jervisffb.engine.rules.bb2020.BB2020SkillCategory.AGILITY
import com.jervisffb.engine.rules.bb2020.BB2020SkillCategory.GENERAL
import com.jervisffb.engine.rules.bb2020.BB2020SkillCategory.PASSING
import com.jervisffb.engine.rules.bb2020.skills.Block
import com.jervisffb.engine.rules.bb2020.skills.CatchSkill
import com.jervisffb.engine.rules.bb2020.skills.Pass
import com.jervisffb.engine.rules.bb2020.skills.SideStep
import com.jervisffb.engine.rules.common.roster.PositionId
import com.jervisffb.engine.rules.common.roster.RosterId
import kotlinx.serialization.Serializable

val ELVEN_LINEMAN =
    BB2020Position(
        PositionId("elven-union-lineman"),
        12,
        "Lineman",
        "Lineman",
        "L",
        60_000,
        6, 3, 2, 4, 8,
        emptyList(),
        listOf(AGILITY, GENERAL),
        listOf(GENERAL),
    )
val ELVEN_THROWER =
    BB2020Position(
        PositionId("elven-union-thrower"),
        2,
        "Throwers",
        "Thrower",
        "T",
        75_000,
        6, 3, 2, 2, 8,
        listOf(Pass.Factory),
        listOf(AGILITY, GENERAL, PASSING),
        listOf(GENERAL),
    )
val ELVEN_CATCHER =
    BB2020Position(
        PositionId("elven-union-catcher"),
        4,
        "Catchers",
        "Catcher",
        "C",
        100_000,
        8, 3, 3, 4, 8,
        listOf(CatchSkill.Factory, /* Nerves Of Steel */),
        listOf(AGILITY, GENERAL),
        listOf(GENERAL),
    )
val ELVEN_BLITZER =
    BB2020Position(
        PositionId("elven-union-blitzer"),
        2,
        "Blitzers",
        "Blitzer",
        "B",
        115_000,
        7, 3, 2, 3, 9,
        listOf(Block.Factory, SideStep.Factory),
        listOf(GENERAL, GENERAL),
        listOf(AGILITY, PASSING),
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
    )
)
