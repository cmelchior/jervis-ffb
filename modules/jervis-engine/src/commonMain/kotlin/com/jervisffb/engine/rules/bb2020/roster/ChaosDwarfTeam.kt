package com.jervisffb.engine.rules.bb2020.roster

import com.jervisffb.engine.model.PositionId
import com.jervisffb.engine.model.RosterId
import com.jervisffb.engine.rules.bb2020.BB2020SkillCategory.AGILITY
import com.jervisffb.engine.rules.bb2020.BB2020SkillCategory.GENERAL
import com.jervisffb.engine.rules.bb2020.BB2020SkillCategory.MUTATIONS
import com.jervisffb.engine.rules.bb2020.BB2020SkillCategory.PASSING
import com.jervisffb.engine.rules.bb2020.BB2020SkillCategory.STRENGTH
import com.jervisffb.engine.rules.bb2020.skills.Block
import com.jervisffb.engine.rules.bb2020.skills.Sprint
import com.jervisffb.engine.rules.bb2020.skills.SureFeet
import com.jervisffb.engine.rules.bb2020.skills.Tackle
import com.jervisffb.engine.rules.bb2020.skills.ThickSkull
import kotlinx.serialization.Serializable

val HOBGOBLIN_LINEMEN =
    BB2020Position(
        PositionId("chaos-dwarf-hobgoblin-lineman"),
        16,
        "Hobgoblin Linemen",
        "Hobgoblin Lineman",
        "Hg",
        40_000,
        6, 3, 3, 4, 8,
        emptyList(),
        listOf(GENERAL),
        listOf(AGILITY, STRENGTH),
    )
val CHAOS_DWARF_BLOCKERS =
    BB2020Position(
        PositionId("chaos-dwarf-chaos-dwarf-blocker"),
        6,
        "Chaos Dwarf Blockers",
        "Chaos Dwarf Blocker",
        "Cd",
        70_000,
        4, 3, 4, 6, 10,
        listOf(Block.Factory, Tackle.Factory, ThickSkull.Factory),
        listOf(GENERAL, STRENGTH),
        listOf(AGILITY, MUTATIONS),
    )
val BULL_CENTAUR_BLITZERS =
    BB2020Position(
        PositionId("chaos-dwarf-bull-centaur-blitzer"),
        2,
        "Bull Centaur Blitzers",
        "Bull Centaur Blitzer",
        "Bc",
        130_000,
        6, 4, 4, 6, 10,
        listOf(
            Sprint.Factory,
            SureFeet.Factory,
            ThickSkull.Factory
        ),
        listOf(GENERAL, STRENGTH),
        listOf(AGILITY),
    )
val ENSLAVED_MINOTAUR =
    BB2020Position(
        PositionId("chaos-dwarf-enslaved-minotaur"),
        1,
        "Enslaved Minotaur",
        "Enslaved Minotaur",
        "M",
        150_000,
        5, 5, 4, 0, 9,
        emptyList(),
        listOf(AGILITY, GENERAL),
        listOf(STRENGTH, PASSING),
    )

// See Teams of Legend: https://www.warhammer-community.com/wp-content/uploads/2020/11/lFZy1SIuNmWvxPj1.pdf
@Serializable
val CHAOS_DWARF_TEAM = BB2020Roster(
    id = RosterId("jervis-chaos-dwarf"),
    name = "Chaos Dwarf",
    tier = 1,
    numberOfRerolls = 8,
    rerollCost =  70_000,
    allowApothecary = true,
    // Only select one of Favoured of
    specialRules = listOf(
        RegionalSpecialRule.BADLANDS_BRAWL,
        RegionalSpecialRule.WORLDS_EDGE_SUPERLEAGUE,
        TeamSpecialRule.FAVOURED_OF_CHAOS_UNDIVIDED,
        TeamSpecialRule.FAVOURED_OF_KHORNE,
        TeamSpecialRule.FAVOURED_OF_NURGLE,
        TeamSpecialRule.FAVOURED_OF_TZEENTCH,
        TeamSpecialRule.FAVOURED_OF_SLAANESH,
    ),
    positions = listOf(
        HOBGOBLIN_LINEMEN,
        CHAOS_DWARF_BLOCKERS,
        BULL_CENTAUR_BLITZERS,
        ENSLAVED_MINOTAUR,
    )
)
