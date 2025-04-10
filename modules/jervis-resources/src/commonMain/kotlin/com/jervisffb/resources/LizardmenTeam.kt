import com.jervisffb.engine.model.PositionId
import com.jervisffb.engine.model.RosterId
import com.jervisffb.engine.rules.bb2020.roster.BB2020Roster
import com.jervisffb.engine.rules.bb2020.roster.RegionalSpecialRule
import com.jervisffb.engine.rules.bb2020.roster.RosterPosition
import com.jervisffb.engine.rules.bb2020.skills.SkillCategory
import com.jervisffb.engine.rules.bb2020.skills.SkillType
import com.jervisffb.engine.rules.bb2020.skills.SkillType.BONE_HEAD
import com.jervisffb.engine.rules.bb2020.skills.SkillType.DODGE
import com.jervisffb.engine.rules.bb2020.skills.SkillType.LONER
import com.jervisffb.engine.rules.bb2020.skills.SkillType.MIGHTY_BLOW
import com.jervisffb.engine.rules.bb2020.skills.SkillType.PREHENSILE_TAIL
import com.jervisffb.engine.rules.bb2020.skills.SkillType.STUNTY
import com.jervisffb.engine.rules.bb2020.skills.SkillType.THICK_SKULL
import com.jervisffb.engine.serialize.RosterLogo
import com.jervisffb.engine.serialize.SingleSprite
import com.jervisffb.engine.serialize.SpriteSheet
import com.jervisffb.resources.iconRootPath
import com.jervisffb.resources.portraitRootPath
import kotlinx.serialization.Serializable

val SKINK_RUNNER_LINEMEN =
    RosterPosition(
        PositionId("lizardmen-skink-runner-lineman"),
        12,
        "Skink Runner Linemen",
        "Skink Runner Lineman",
        "Sk",
        60_000,
        8, 2, 3, 4, 8,
        listOf(SkillType.DODGE.id(), STUNTY.id()),
        listOf(SkillCategory.AGILITY),
        listOf(SkillCategory.GENERAL, SkillCategory.PASSING, SkillCategory.STRENGTH),
        SpriteSheet.embedded("$iconRootPath/lizardmen_skinkrunnerlineman.png",6),
        SingleSprite.embedded("$portraitRootPath/lizardmen_skinkrunner.png")
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
        listOf(DODGE.id(), /* On the Ball, Shadowing */ SkillType.STUNTY.id()),
        listOf(SkillCategory.AGILITY),
        listOf(SkillCategory.GENERAL, SkillCategory.PASSING, SkillCategory.STRENGTH),
        SpriteSheet.embedded("$iconRootPath/lizardmen_chameleonskink.png",2),
        SingleSprite.embedded("$portraitRootPath/lizardmen_chameleonskink.png")
    )
val SAURUS_BLOCKERS =
    RosterPosition(
        PositionId("lizardmen-saurus-blocker"),
        6,
        "Saurus Blockers",
        "Saurus Blocker",
        "S",
        85_000,
        6, 4, 5, 6, 10,
        emptyList(),
        listOf(SkillCategory.GENERAL, SkillCategory.STRENGTH),
        listOf(SkillCategory.AGILITY),
        SpriteSheet.embedded("$iconRootPath/lizardmen_saurusblocker.png",6),
        SingleSprite.embedded("$portraitRootPath/lizardmen_saurusblocker.png")
    )
val KROXIGOR =
    RosterPosition(
        PositionId("lizardmen-kroxigor"),
        1,
        "Kroxigor",
        "Kroxigor",
        "K",
        140_000,
        6, 5, 5, null, 10,
        listOf(
            BONE_HEAD.id(),
            LONER.id(4),
            MIGHTY_BLOW.id(1),
            THICK_SKULL.id(),
            PREHENSILE_TAIL.id()
        ),
        listOf(SkillCategory.STRENGTH),
        listOf(SkillCategory.AGILITY, SkillCategory.GENERAL),
        SpriteSheet.embedded("$iconRootPath/lizardmen_kroxigor.png",1),
        SingleSprite.embedded("$portraitRootPath/lizardmen_kroxigor.png")
    )

/**
 * Lizardmen Team
 *
 * See page 118 in the rulebook
 */
@Serializable
val LIZARDMEN_TEAM = BB2020Roster(
    id = RosterId("jervis-lizardmen"),
    name = "Lizardmen Team",
    tier = 1,
    numberOfRerolls = 8,
    rerollCost = 70_000,
    allowApothecary = true,
    specialRules = listOf(RegionalSpecialRule.LUSTRIAN_SUPERLEAGUE),
    positions = listOf(
        SKINK_RUNNER_LINEMEN,
        CHAMELEON_SKINKS,
        SAURUS_BLOCKERS,
        KROXIGOR,
    ),
    logo = RosterLogo(
        large = SingleSprite.embedded("roster/logo/roster_logo_jervis_lizardmen_large.png"),
        small = SingleSprite.embedded("roster/logo/roster_logo_jervis_lizardmen_small.png")
    )
)
