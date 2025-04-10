package com.jervisffb.resources

import com.jervisffb.engine.model.PositionId
import com.jervisffb.engine.model.RosterId
import com.jervisffb.engine.rules.bb2020.roster.BB2020Roster
import com.jervisffb.engine.rules.bb2020.roster.RosterPosition
import com.jervisffb.engine.rules.bb2020.roster.TeamSpecialRule
import com.jervisffb.engine.rules.bb2020.skills.SkillCategory
import com.jervisffb.engine.rules.bb2020.skills.SkillType
import com.jervisffb.engine.rules.bb2020.skills.SkillType.FRENZY
import com.jervisffb.engine.rules.bb2020.skills.SkillType.HORNS
import com.jervisffb.engine.rules.bb2020.skills.SkillType.LONER
import com.jervisffb.engine.rules.bb2020.skills.SkillType.UNCHANNELLED_FURY
import com.jervisffb.engine.serialize.RosterLogo
import com.jervisffb.engine.serialize.SingleSprite
import com.jervisffb.engine.serialize.SpriteSheet

val BLOODBORN_MARAUDER_LINEMEN =
    RosterPosition(
        PositionId("khorne-bloodborn-marauder-lineman"),
        16,
        "Bloodborn Marauder Linemen",
        "Bloodborn Marauder Lineman",
        "L",
        50_000,
        6, 3, 3, 4, 8,
        listOf(FRENZY.id()),
        listOf(SkillCategory.GENERAL, SkillCategory.MUTATIONS),
        listOf(SkillCategory.AGILITY, SkillCategory.STRENGTH),
        SpriteSheet.embedded("$iconRootPath/khorne_bloodbornmarauderlineman.png",7),
        SingleSprite.embedded("$portraitRootPath/khorne_bloodbornmarauderlineman.png")
    )
val KHORNGORS =
    RosterPosition(
        PositionId("khorne-khorngor"),
        4,
        "Khorngors",
        "Khorngor",
        "K",
        70_000,
        6, 3, 4, 4, 9,
        listOf(HORNS.id() /*, Juggernaut */),
        listOf(SkillCategory.GENERAL, SkillCategory.MUTATIONS, SkillCategory.STRENGTH),
        listOf(SkillCategory.AGILITY, SkillCategory.PASSING),
        SpriteSheet.embedded("$iconRootPath/khorne_khorngor.png",4),
        SingleSprite.embedded("$portraitRootPath/khorne_khorngor.png")
    )
val BLOODSEEKERS =
    RosterPosition(
        PositionId("khorne-bloodseeker"),
        4,
        "Bloodseekers",
        "Bloodseeker",
        "Bs",
        110_000,
        5, 4, 4, 6, 10,
        listOf(FRENZY.id()),
        listOf(SkillCategory.GENERAL, SkillCategory.MUTATIONS, SkillCategory.STRENGTH),
        listOf(SkillCategory.AGILITY),
        SpriteSheet.embedded("$iconRootPath/khorne_bloodseeker.png", 4),
        SingleSprite.embedded("$portraitRootPath/khorne_bloodseeker.png")
    )
val BLOODSPAWN =
    RosterPosition(
        PositionId("khorne-bloodspawn"),
        1,
        "Bloodspawn",
        "Bloodspawn",
        "B",
        160_000,
        5, 5, 4, null, 9,
        listOf(
            // Claws
            FRENZY.id(),
            LONER.id(4),
            SkillType.MIGHTY_BLOW.id(1),
            UNCHANNELLED_FURY.id()
        ),
        listOf(SkillCategory.MUTATIONS, SkillCategory.STRENGTH),
        listOf(SkillCategory.AGILITY, SkillCategory.GENERAL),
        SpriteSheet.embedded("$iconRootPath/khorne_bloodspawn.png", 1),
        SingleSprite.embedded("$portraitRootPath/khorne_bloodspawn.png")
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
    ),
    logo = RosterLogo(
        large = SingleSprite.embedded("roster/logo/roster_logo_jervis_khorne_large.png"),
        small = SingleSprite.embedded("roster/logo/roster_logo_jervis_khorne_small.png")
    )
)
