package com.jervisffb.engine.rules.bb2020.roster

import com.jervisffb.engine.rules.bb2020.BB2020SkillCategory.AGILITY
import com.jervisffb.engine.rules.bb2020.BB2020SkillCategory.GENERAL
import com.jervisffb.engine.rules.bb2020.BB2020SkillCategory.MUTATIONS
import com.jervisffb.engine.rules.bb2020.BB2020SkillCategory.PASSING
import com.jervisffb.engine.rules.bb2020.BB2020SkillCategory.STRENGTH
import com.jervisffb.engine.rules.bb2020.skills.Frenzy
import com.jervisffb.engine.rules.bb2020.skills.Horns
import com.jervisffb.engine.rules.bb2020.skills.Loner
import com.jervisffb.engine.rules.bb2020.skills.MightyBlow
import com.jervisffb.engine.rules.bb2020.skills.UnchannelledFury
import com.jervisffb.engine.rules.common.roster.PositionId
import com.jervisffb.engine.rules.common.roster.RosterId

val BLOODBORN_MARAUDER_LINEMEN =
    BB2020Position(
        PositionId("khorne-bloodborn-marauder-lineman"),
        16,
        "Bloodborn Marauder Linemen",
        "Bloodborn Marauder Lineman",
        "L",
        50_000,
        6, 3, 3, 4, 8,
        listOf(Frenzy.Factory),
        listOf(GENERAL, MUTATIONS),
        listOf(AGILITY, STRENGTH),
    )
val KHORNGORS =
    BB2020Position(
        PositionId("khorne-khorngor"),
        4,
        "Khorngors",
        "Khorngor",
        "K",
        70_000,
        6, 3, 4, 4, 9,
        listOf(Horns.Factory /*, Juggernaut */),
        listOf(GENERAL, MUTATIONS, STRENGTH),
        listOf(AGILITY, PASSING),
    )
val BLOODSEEKERS =
    BB2020Position(
        PositionId("khorne-bloodseeker"),
        4,
        "Bloodseekers",
        "Bloodseeker",
        "Bs",
        110_000,
        5, 4, 4, 6, 10,
        listOf(Frenzy.Factory),
        listOf(GENERAL, MUTATIONS, STRENGTH),
        listOf(AGILITY),
    )
val BLOODSPAWN =
    BB2020Position(
        PositionId("khorne-bloodspawn"),
        1,
        "Bloodspawn",
        "Bloodspawn",
        "B",
        160_000,
        5, 5, 4, null, 9,
        listOf(
            // Claws
            Frenzy.Factory,
            Loner.Factory(4),
            MightyBlow.Factory(1),
            UnchannelledFury.Factory
        ),
        listOf(MUTATIONS, STRENGTH),
        listOf(AGILITY, GENERAL),
    )

// See Spike! Journal Issue 13
val KHORNE_TEAM = BB2020Roster(
    id = RosterId("jervis-khorne"),
    tier = 2,
    specialRules = listOf(TeamSpecialRule.FAVOURED_OF_KHORNE),
    name = "Khorne Team",
    numberOfRerolls = 8,
    rerollCost = 60_000,
    allowApothecary = true,
    positions = listOf(
        BLOODBORN_MARAUDER_LINEMEN,
        KHORNGORS,
        BLOODSEEKERS,
        BLOODSPAWN,
    )
)
