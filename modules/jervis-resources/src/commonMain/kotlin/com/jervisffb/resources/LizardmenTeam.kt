import com.jervisffb.engine.model.PositionId
import com.jervisffb.engine.model.RosterId
import com.jervisffb.engine.rules.bb2020.BB2020SkillCategory.AGILITY
import com.jervisffb.engine.rules.bb2020.BB2020SkillCategory.GENERAL
import com.jervisffb.engine.rules.bb2020.BB2020SkillCategory.PASSING
import com.jervisffb.engine.rules.bb2020.BB2020SkillCategory.STRENGTH
import com.jervisffb.engine.rules.bb2020.roster.BB2020Position
import com.jervisffb.engine.rules.bb2020.roster.BB2020Roster
import com.jervisffb.engine.rules.bb2020.roster.RegionalSpecialRule
import com.jervisffb.engine.rules.bb2020.skills.BoneHead
import com.jervisffb.engine.rules.bb2020.skills.Dodge
import com.jervisffb.engine.rules.bb2020.skills.Loner
import com.jervisffb.engine.rules.bb2020.skills.MightyBlow
import com.jervisffb.engine.rules.bb2020.skills.MultipleBlock
import com.jervisffb.engine.rules.bb2020.skills.PrehensileTail
import com.jervisffb.engine.rules.bb2020.skills.Stunty
import com.jervisffb.engine.rules.bb2020.skills.ThickSkull
import com.jervisffb.engine.serialize.RosterLogo
import com.jervisffb.engine.serialize.SingleSprite
import com.jervisffb.engine.serialize.SpriteSheet
import com.jervisffb.resources.iconRootPath
import com.jervisffb.resources.portraitRootPath
import kotlinx.serialization.Serializable

val SKINK_RUNNER_LINEMEN =
    BB2020Position(
        PositionId("lizardmen-skink-runner-lineman"),
        12,
        "Skink Runner Linemen",
        "Skink Runner Lineman",
        "Sk",
        60_000,
        8, 2, 3, 4, 8,
        listOf(Dodge.Factory, Stunty.Factory),
        listOf(AGILITY),
        listOf(GENERAL, PASSING, STRENGTH),
        SpriteSheet.embedded("$iconRootPath/lizardmen_skinkrunnerlineman.png",6),
        SingleSprite.embedded("$portraitRootPath/lizardmen_skinkrunner.png")
    )
val CHAMELEON_SKINKS =
    BB2020Position(
        PositionId("lizardmen-chameleon-skink"),
        2,
        "Chameleon Skinks",
        "Chameleon Skink",
        "Cs",
        70_000,
        7, 2, 3, 3, 8,
        listOf(Dodge.Factory, /* On the Ball, Shadowing */ Stunty.Factory),
        listOf(AGILITY),
        listOf(GENERAL, PASSING, STRENGTH),
        SpriteSheet.embedded("$iconRootPath/lizardmen_chameleonskink.png",2),
        SingleSprite.embedded("$portraitRootPath/lizardmen_chameleonskink.png")
    )
val SAURUS_BLOCKERS =
    BB2020Position(
        PositionId("lizardmen-saurus-blocker"),
        6,
        "Saurus Blockers",
        "Saurus Blocker",
        "S",
        85_000,
        6, 4, 5, 6, 10,
        emptyList(),
        listOf(GENERAL, STRENGTH),
        listOf(AGILITY),
        SpriteSheet.embedded("$iconRootPath/lizardmen_saurusblocker.png",6),
        SingleSprite.embedded("$portraitRootPath/lizardmen_saurusblocker.png")
    )
val KROXIGOR =
    BB2020Position(
        PositionId("lizardmen-kroxigor"),
        1,
        "Kroxigor",
        "Kroxigor",
        "K",
        140_000,
        6, 5, 5, null, 10,
        listOf(
            BoneHead.Factory,
            Loner.Factory(4),
            MightyBlow.Factory(1),
            ThickSkull.Factory,
            PrehensileTail.Factory,
            MultipleBlock.Factory
        ),
        listOf(STRENGTH),
        listOf(AGILITY, GENERAL),
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
