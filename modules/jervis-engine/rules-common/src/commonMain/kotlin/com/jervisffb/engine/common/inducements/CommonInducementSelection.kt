package com.jervisffb.engine.common.inducements

import com.jervisffb.engine.actions.InducementSelection
import com.jervisffb.engine.model.PositionId
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.model.WizardId
import com.jervisffb.engine.model.inducements.BiasedRefereeType
import com.jervisffb.engine.model.inducements.InfamousCoachingStaffType
import com.jervisffb.engine.model.inducements.settings.InducementType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.roster.Position
import kotlinx.serialization.Serializable

object CommonInducementSelection {

    @Serializable
    data class Simple(override val type: InducementType, override val count: Int) : InducementSelection<SimpleInducement> {
        override fun getSettings(rules: Rules): SimpleInducement = rules.inducements[type] as SimpleInducement
    }

    @Serializable
    data class Wizard(val id: WizardId) : InducementSelection<WizardInducement> {
        override val count: Int = 1
        override val type: InducementType = CommonInducementType.WIZARD
        override fun getSettings(rules: Rules): WizardInducement = (rules.inducements[type] as WizardsInducementGroup).items.first { it.wizard.id == id }

    }


    @Serializable
    data class BiasedReferee(val referee: BiasedRefereeType) : InducementSelection<BiasedRefereeInducement> {
        override val count: Int = 1
        override val type: InducementType = CommonInducementType.BIASED_REFEREE
        override fun getSettings(rules: Rules): BiasedRefereeInducement = (rules.inducements[type] as BiasedRefereesInducementGroup).items.first { it.referee.type == referee }
    }

    @Serializable
    data class InfamousCoach(val coachType: InfamousCoachingStaffType) : InducementSelection<InfamousCoachingStaffInducement> {
        override val count: Int = 1
        override val type: InducementType = CommonInducementType.INFAMOUS_COACHING_STAFF
        override fun getSettings(rules: Rules): InfamousCoachingStaffInducement = (rules.inducements[type] as InfamousCoachingStaffsInducementGroup).items.first { it.staff.type == coachType }

    }


    @Serializable
    data class StarPlayer(val position: PositionId) : com.jervisffb.engine.actions.InducementSelection<StarPlayerInducement> {
        override val count: Int = 1
        override val type: InducementType = CommonInducementType.STAR_PLAYERS
        override fun getSettings(rules: Rules): StarPlayerInducement = (rules.inducements[type] as StarPlayersInducementGroup).items.first { it.starPlayer.id == position }

    }

    @Serializable
    data class Mercenary(
        val position: Position,
        val extraSkills: List<SkillId> = emptyList(),
    ) : InducementSelection<MercenaryInducement> {
        override val count: Int = 1
        override val type: InducementType = CommonInducementType.STANDARD_MERCENARY_PLAYERS
        override fun getSettings(rules: Rules): MercenaryInducement {
            val groupSettings = (rules.inducements[type] as StandardMercenaryInducement)
            return MercenaryInducement(
                position,
                extraSkills,
                groupSettings.extraCost,
                groupSettings.skillCost
            )
        }
    }
}
