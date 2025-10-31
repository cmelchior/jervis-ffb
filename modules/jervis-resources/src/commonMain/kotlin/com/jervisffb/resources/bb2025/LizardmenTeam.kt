package com.jervisffb.resources.bb2025

import com.jervisffb.engine.model.PlayerKeyword
import com.jervisffb.engine.model.PlayerSize
import com.jervisffb.engine.model.PositionId
import com.jervisffb.engine.model.RosterId
import com.jervisffb.engine.rules.common.roster.RegionalSpecialRule
import com.jervisffb.engine.rules.common.roster.Roster
import com.jervisffb.engine.rules.common.roster.RosterPosition
import com.jervisffb.engine.rules.common.skills.SkillCategory.AGILITY
import com.jervisffb.engine.rules.common.skills.SkillCategory.DEVIOUS
import com.jervisffb.engine.rules.common.skills.SkillCategory.GENERAL
import com.jervisffb.engine.rules.common.skills.SkillCategory.PASSING
import com.jervisffb.engine.rules.common.skills.SkillCategory.STRENGTH
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.rules.common.skills.SkillType.BONE_HEAD
import com.jervisffb.engine.rules.common.skills.SkillType.DODGE
import com.jervisffb.engine.rules.common.skills.SkillType.LONER
import com.jervisffb.engine.rules.common.skills.SkillType.MIGHTY_BLOW
import com.jervisffb.engine.rules.common.skills.SkillType.PREHENSILE_TAIL
import com.jervisffb.engine.rules.common.skills.SkillType.STUNTY
import com.jervisffb.engine.rules.common.skills.SkillType.THICK_SKULL
import com.jervisffb.engine.serialize.RosterLogo
import com.jervisffb.engine.serialize.SingleSprite
import com.jervisffb.engine.serialize.SpriteSheet
import com.jervisffb.resources.iconRootPath
import com.jervisffb.resources.portraitRootPath
import kotlinx.serialization.Serializable

val SKINK_RUNNER_LINEMEN =
    RosterPosition(
        PositionId("lizardmen-skink-runner-lineman"),
        16,
        "Skink Runner Linemen",
        "Skink Runner Lineman",
        "Sk",
        60_000,
        8, 2, 3, 4, 8,
        listOf(DODGE.id(), STUNTY.id()),
        listOf(AGILITY),
        listOf(GENERAL, DEVIOUS, PASSING, STRENGTH),
        listOf(PlayerKeyword.LIZARDMAN, PlayerKeyword.LINEMAN),
        PlayerSize.STANDARD,
        SpriteSheet.ini("$iconRootPath/lizardmen_skinkrunner.png",6),
        SingleSprite.ini("$portraitRootPath/lizardmen_skinkrunner.png")
    )
val CHAMELEON_SKINKS =
    RosterPosition(
        PositionId("lizardmen-chameleon-skink"),
        2,
        "Chameleon Skinks",
        "Chameleon Skink",
        "Cs",
        70_000,
        7, 2, 3, 3, 8,
        listOf(DODGE.id(), SkillType.ON_THE_BALL.id(), SkillType.SHADOWING.id(), STUNTY.id()),
        listOf(AGILITY, PASSING),
        listOf(GENERAL, DEVIOUS, STRENGTH),
        listOf(PlayerKeyword.LIZARDMAN, PlayerKeyword.THROWER),
        PlayerSize.STANDARD,
        SpriteSheet.ini("$iconRootPath/lizardmen_chameleonskink.png",2),
        SingleSprite.ini("$portraitRootPath/lizardmen_chameleonskink.png")
    )
val SAURUS_BLOCKERS =
    RosterPosition(
        PositionId("lizardmen-saurus-blocker"),
        6,
        "Saurus Blockers",
        "Saurus Blocker",
        "S",
        90_000,
        6, 4, 5, 6, 10,
        listOf(SkillType.JUGGERNAUT.id(), SkillType.UNSTEADY.id()),
        listOf(GENERAL, STRENGTH),
        listOf(AGILITY),
        listOf(PlayerKeyword.LIZARDMAN, PlayerKeyword.BLOCKER),
        PlayerSize.STANDARD,
        SpriteSheet.ini("$iconRootPath/lizardmen_saurusblocker.png",6),
        SingleSprite.ini("$portraitRootPath/lizardmen_saurusblocker.png")
    )
val KROXIGOR =
    RosterPosition(
        PositionId("lizardmen-kroxigor"),
        1,
        "Kroxigor",
        "Kroxigor",
        "K",
        140_000,
        6, 5, 5, 6, 10,
        listOf(
            BONE_HEAD.id(),
            LONER.id(4),
            MIGHTY_BLOW.id(),
            PREHENSILE_TAIL.id(),
            THICK_SKULL.id(),
        ),
        listOf(STRENGTH),
        listOf(AGILITY, GENERAL),
        listOf(PlayerKeyword.LIZARDMAN, PlayerKeyword.BIG_GUY),
        PlayerSize.BIG_GUY,
        SpriteSheet.ini("$iconRootPath/lizardmen_kroxigor.png",1),
        SingleSprite.ini("$portraitRootPath/lizardmen_kroxigor.png")
    )

/**
 * Lizardmen Team
 *
 * See page 118 in the rulebook
 */
@Serializable
val LIZARDMEN_TEAM_BB2025 = Roster(
    id = RosterId("jervis-lizardmen"),
    name = "Lizardmen Team",
    tier = 1,
    numberOfRerolls = 8,
    rerollCost = 70_000,
    allowApothecary = true,
    positions = listOf(
        SKINK_RUNNER_LINEMEN,
        CHAMELEON_SKINKS,
        SAURUS_BLOCKERS,
        KROXIGOR,
    ),
    leagues = listOf(RegionalSpecialRule.LUSTRIAN_SUPERLEAGUE),
    specialRules = emptyList(),
    logo = RosterLogo(
        large = SingleSprite.embedded("jervis/roster/logo_lizardmen_large.png"),
        small = SingleSprite.embedded("jervis/roster/logo_lizardmen_small.png")
    )
)
