package com.jervisffb.engine.common.inducements

import com.jervisffb.engine.actions.InducementSelection
import com.jervisffb.engine.model.PositionId
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.model.inducements.biasedreferee.BiasedRefereeType
import com.jervisffb.engine.model.inducements.infamouscoach.InfamousCoachingStaffType
import com.jervisffb.engine.model.inducements.settings.InducementType
import com.jervisffb.engine.model.inducements.wizard.WizardType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.roster.Position
import kotlinx.serialization.Serializable

object InducementSelectionCommon {

    @Serializable
    data class Simple(override val type: InducementType, override val count: Int) : InducementSelection<SimpleInducement> {
        override fun getSettings(rules: Rules): SimpleInducement = rules.inducements[type] as SimpleInducement
    }

    @Serializable
    data class Wizard(val wizard: WizardType) : InducementSelection<WizardInducement> {
        override val count: Int = 1
        override val type: InducementType = InducementTypeCommon.WIZARD
        override fun getSettings(rules: Rules): WizardInducement = (rules.inducements[type] as WizardsInducementGroup).items
            .first { it.wizard == wizard }

    }


    @Serializable
    data class BiasedReferee(val referee: BiasedRefereeType) : InducementSelection<BiasedRefereeInducement> {
        override val count: Int = 1
        override val type: InducementType = InducementTypeCommon.BIASED_REFEREE
        override fun getSettings(rules: Rules): BiasedRefereeInducement = (rules.inducements[type] as BiasedRefereesInducementGroup).items.first { it.referee == referee }
    }

    @Serializable
    data class InfamousCoach(val coachType: InfamousCoachingStaffType) : InducementSelection<InfamousCoachingStaffInducement> {
        override val count: Int = 1
        override val type: InducementType = InducementTypeCommon.INFAMOUS_COACHING_STAFF
        override fun getSettings(rules: Rules): InfamousCoachingStaffInducement = (rules.inducements[type] as InfamousCoachingStaffsInducementGroup).items.first { it.staff == coachType }

    }


    @Serializable
    data class StarPlayer(val position: PositionId) : com.jervisffb.engine.actions.InducementSelection<StarPlayerInducement> {
        override val count: Int = 1
        override val type: InducementType = InducementTypeCommon.STAR_PLAYERS
        override fun getSettings(rules: Rules): StarPlayerInducement = (rules.inducements[type] as StarPlayersInducementGroup).items.first { it.starPlayer.id == position }

    }

    @Serializable
    data class Mercenary(
        val position: Position,
        val extraSkills: List<SkillId> = emptyList(),
    ) : InducementSelection<MercenaryInducement> {
        override val count: Int = 1
        override val type: InducementType = InducementTypeCommon.STANDARD_MERCENARY_PLAYERS
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
