package com.jervisffb.engine.rules.bb2020.roster

import com.jervisffb.engine.rules.bb2020.BB2020SkillCategory.AGILITY
import com.jervisffb.engine.rules.bb2020.BB2020SkillCategory.GENERAL
import com.jervisffb.engine.rules.bb2020.BB2020SkillCategory.PASSING
import com.jervisffb.engine.rules.bb2020.BB2020SkillCategory.STRENGTH
import com.jervisffb.engine.rules.bb2020.skills.BoneHead
import com.jervisffb.engine.rules.bb2020.skills.Dodge
import com.jervisffb.engine.rules.bb2020.skills.Loner
import com.jervisffb.engine.rules.bb2020.skills.MightyBlow
import com.jervisffb.engine.rules.common.roster.RosterId
import com.jervisffb.engine.rules.bb2020.skills.MultipleBlock
import com.jervisffb.engine.rules.bb2020.skills.PrehensileTail
import com.jervisffb.engine.rules.bb2020.skills.Stunty
import com.jervisffb.engine.rules.bb2020.skills.ThickSkull
import kotlinx.serialization.Serializable

/**
 * Lizardmen Team
 *
 * See page 118 in the rulebook
 */
@Serializable
data object LizardmenTeam : BB2020Roster {
    val SKINK_RUNNER_LINEMEN =
        BB2020Position(
            LizardmenTeam,
            12,
            "Skink Runner Linemen",
            "Skink Runner Lineman",
            60_000,
            8, 2, 3, 4, 8,
            listOf(Dodge.Factory, Stunty.Factory),
            listOf(AGILITY),
            listOf(GENERAL, PASSING, STRENGTH),
        )
    val CHAMELEON_SKINKS =
        BB2020Position(
            LizardmenTeam,
            2,
            "Chameleon Skinks",
            "Chameleon Skink",
            70_000,
            7, 2, 3, 3, 8,
            listOf(Dodge.Factory, /* On the Ball, Shadowing */ Stunty.Factory),
            listOf(AGILITY),
            listOf(GENERAL, PASSING, STRENGTH),
        )
    val SAURUS_BLOCKERS =
        BB2020Position(
            LizardmenTeam,
            6,
            "Saurus Blockers",
            "Saurus Blocker",
            85_000,
            6, 4, 5, 6, 10,
            emptyList(),
            listOf(GENERAL, STRENGTH),
            listOf(AGILITY),
        )
    val KROXIGOR =
        BB2020Position(
            LizardmenTeam,
            1,
            "Kroxigor",
            "Kroxigor",
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
        )

    override val id: RosterId = RosterId("jervis-lizardmen")
    override val tier: Int = 1
    override val specialRules: List<SpecialRules> = listOf(RegionalSpecialRule.LUSTRIAN_SUPERLEAGUE)
    override val name: String = "Lizardmen Team"
    override val numberOfRerolls: Int = 8
    override val rerollCost: Int = 70_000
    override val allowApothecary: Boolean = true
    override val positions =
        listOf(
            SKINK_RUNNER_LINEMEN,
            CHAMELEON_SKINKS,
            SAURUS_BLOCKERS,
            KROXIGOR,
        )
}
