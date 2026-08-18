package com.jervisffb.ui.menu.components.setup

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import cafe.adriel.voyager.core.model.ScreenModel
import com.jervisffb.engine.InducementSettings
import com.jervisffb.engine.bb2020.inducements.BB2020InducementGroupBuilder
import com.jervisffb.engine.bb2020.inducements.BB2020InducementType
import com.jervisffb.engine.bb2020.inducements.BB2020SingleInducementBuilder
import com.jervisffb.engine.bb2020.inducements.BB2020TeamPlayerInducementBuilder
import com.jervisffb.engine.bb2020.inducements.ExpandedMercenaryInducements
import com.jervisffb.engine.bb2025.inducements.BB2025InducementGroupBuilder
import com.jervisffb.engine.bb2025.inducements.BB2025InducementType
import com.jervisffb.engine.bb2025.inducements.BB2025SingleInducementBuilder
import com.jervisffb.engine.bb2025.inducements.BB2025TeamPlayerInducementBuilder
import com.jervisffb.engine.common.inducements.BiasedRefereeInducement
import com.jervisffb.engine.common.inducements.BiasedRefereesInducementGroup
import com.jervisffb.engine.common.inducements.CommonInducementGroupBuilder
import com.jervisffb.engine.common.inducements.CommonInducementType
import com.jervisffb.engine.common.inducements.CommonSingleInducementBuilder
import com.jervisffb.engine.common.inducements.CommonTeamPlayerInducementBuilder
import com.jervisffb.engine.common.inducements.InfamousCoachingStaffInducement
import com.jervisffb.engine.common.inducements.InfamousCoachingStaffsInducementGroup
import com.jervisffb.engine.common.inducements.MercenaryInducement
import com.jervisffb.engine.common.inducements.SimpleInducement
import com.jervisffb.engine.common.inducements.StandardMercenaryInducement
import com.jervisffb.engine.common.inducements.StarPlayerInducement
import com.jervisffb.engine.common.inducements.StarPlayersInducementGroup
import com.jervisffb.engine.common.inducements.WizardInducement
import com.jervisffb.engine.common.inducements.WizardsInducementGroup
import com.jervisffb.engine.model.inducements.settings.InducementBuilder
import com.jervisffb.engine.model.inducements.settings.InducementType
import com.jervisffb.engine.rules.RulesParameterBuilder
import com.jervisffb.engine.rules.builder.GameVersion
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import kotlinx.coroutines.flow.MutableStateFlow

data class InducementData(
    val type: InducementType,
    val name: String,
    val enabled: Boolean,
    val max: Int,
    val price: Int?,
)

/**
 * This component model is responsible for all the UI control needed to configure the inducements
 * available for a game.
 *
 * For now, we only support enabling/disabling the inducement. The UI for supporting reduced prices,
 * mercenaries, star players, etc. is fairly complex, and changing these will probably rarely happen. So not
 * really worth it right now.
 */
class InducementsSetupComponentModel(initialRulesBuilder: RulesParameterBuilder, private val menuViewModel: MenuViewModel) : ScreenModel {

    private val HEADER_RULEBOOK = "Rulebook"
    private val HEADER_DEATH_ZONE = "Death Zone"
    private val HEADER_SPIKE_19 = "Spike Magazine 19 (Bretonnian)"
    private val HEADER_SPIKE_20 = "Spike Magazine 20 (Khemri)"

    var rulesBuilder = initialRulesBuilder

    // It isn't possible to get inducements in an illegal state (I think), but keep it for now in
    // case it changes.
    val isSetupValid: MutableStateFlow<Boolean> = MutableStateFlow(true)

    var builders: InducementSettings.Builder? = null
    val inducementCategories = SnapshotStateList<String>()
    val inducements = mutableStateMapOf<String, SnapshotStateList<InducementData>>()

    init {
        updateRulesBuilder(rulesBuilder)
    }

    fun updateInducementEnabled(category: String, type: InducementType, enabled: Boolean) {
        val inducementsInCategory = inducements[category] ?: error("Inducements for $category not found")
        updateEnabled(inducementsInCategory, type, enabled)

        // Expanded Mercenaries replace the standard rules and vice versa.
        if (enabled && type == CommonInducementType.STANDARD_MERCENARY_PLAYERS) {
            updateEnabled(
                this.inducements[HEADER_DEATH_ZONE]!!,
                BB2020InducementType.EXPANDED_MERCENARY_PLAYERS,
                false
            )
        }
        if (enabled && type == BB2020InducementType.EXPANDED_MERCENARY_PLAYERS) {
            updateEnabled(
                this.inducements[HEADER_RULEBOOK]!!,
                CommonInducementType.STANDARD_MERCENARY_PLAYERS,
                false
            )
        }
    }

    private fun updateEnabled(
        inducements: SnapshotStateList<InducementData>,
        type: InducementType,
        enabled: Boolean
    ) {
        inducements.indexOfFirst { it.type == type }.let { index ->
            if (index >= 0) {
                val inducementBuilders = builders ?: error("Missing Inducement Builders")
                inducementBuilders[type]?.let {
                    it.enabled = enabled
                } ?: error("Builder for $type was not found")
                inducements[index] = inducements[index].copy(enabled = enabled)
            }
        }
    }

    fun updateRulesBuilder(rulesBuilder: RulesParameterBuilder) {
        this.rulesBuilder = rulesBuilder
        builders = rulesBuilder.inducements
        when (rulesBuilder.gameVersion) {
            GameVersion.BB2020 -> updateBB2020Inducements()
            GameVersion.BB2025 -> updateBB2025Inducements()
        }
    }

    private fun updateBB2025Inducements() {
        with(builders!!) {
            inducementCategories.clear()
            inducementCategories.add(HEADER_RULEBOOK)

            inducements.clear()
            val rulebookInducements = mutableStateListOf<InducementData>()
            val spike19Inducements = mutableStateListOf<InducementData>()
            val spike20Inducements = mutableStateListOf<InducementData>()
            inducements[HEADER_RULEBOOK] = rulebookInducements

            // Define inducements from the rule book
            rulebookInducements.add(this[BB2025InducementType.PRAYERS_TO_NUFFLE]!!.toDataObject())
            rulebookInducements.add(this[CommonInducementType.PART_TIME_ASSISTANT_COACH]!!.toDataObject())
            rulebookInducements.add(this[CommonInducementType.TEMP_AGENCY_CHEERLEADER]!!.toDataObject())
            rulebookInducements.add(this[BB2025InducementType.TEAM_MASCOT]!!.toDataObject())
            rulebookInducements.add(this[CommonInducementType.WEATHER_MAGE]!!.toDataObject())
            rulebookInducements.add(this[BB2025InducementType.BLITZERS_BEST_KEGS]!!.toDataObject())
            rulebookInducements.add(this[CommonInducementType.BRIBE]!!.toDataObject())
            rulebookInducements.add(this[CommonInducementType.EXTRA_TEAM_TRAINING]!!.toDataObject())
            rulebookInducements.add(this[CommonInducementType.MORTUARY_ASSISTANT]!!.toDataObject())
            rulebookInducements.add(this[CommonInducementType.PLAGUE_DOCTOR]!!.toDataObject())
            rulebookInducements.add(this[CommonInducementType.RIOTOUS_ROOKIE]!!.toDataObject())
            rulebookInducements.add(this[CommonInducementType.WANDERING_APOTHECARY]!!.toDataObject())
            rulebookInducements.add(this[CommonInducementType.HALFLING_MASTER_CHEF]!!.toDataObject())
            rulebookInducements.add(this[CommonInducementType.BIASED_REFEREE]!!.toDataObject())
            rulebookInducements.add(this[CommonInducementType.INFAMOUS_COACHING_STAFF]!!.toDataObject())
            rulebookInducements.add(this[CommonInducementType.STANDARD_MERCENARY_PLAYERS]!!.toDataObject())
            rulebookInducements.add(this[CommonInducementType.STAR_PLAYERS]!!.toDataObject())
            rulebookInducements.add(this[CommonInducementType.WIZARD]!!.toDataObject())
        }
    }

    private fun updateBB2020Inducements() {
        with(builders!!) {
            inducementCategories.clear()
            inducementCategories.add(HEADER_RULEBOOK)
            inducementCategories.add(HEADER_DEATH_ZONE)

            inducements.clear()
            val rulebookInducements = mutableStateListOf<InducementData>()
            val deathZoneInducements = mutableStateListOf<InducementData>()
            inducements[HEADER_RULEBOOK] = rulebookInducements
            inducements[HEADER_DEATH_ZONE] = deathZoneInducements

            // Define inducements from the rule book
            rulebookInducements.add(this[CommonInducementType.TEMP_AGENCY_CHEERLEADER]!!.toDataObject())
            rulebookInducements.add(this[CommonInducementType.PART_TIME_ASSISTANT_COACH]!!.toDataObject())
            rulebookInducements.add(this[CommonInducementType.WEATHER_MAGE]!!.toDataObject())
            rulebookInducements.add(this[BB2020InducementType.BLOODWEISER_KEG]!!.toDataObject())
            rulebookInducements.add(this[BB2020InducementType.SPECIAL_PLAY]!!.toDataObject())
            rulebookInducements.add(this[CommonInducementType.BRIBE]!!.toDataObject())
            rulebookInducements.add(this[CommonInducementType.WANDERING_APOTHECARY]!!.toDataObject())
            rulebookInducements.add(this[CommonInducementType.MORTUARY_ASSISTANT]!!.toDataObject())
            rulebookInducements.add(this[CommonInducementType.PLAGUE_DOCTOR]!!.toDataObject())
            rulebookInducements.add(this[CommonInducementType.RIOTOUS_ROOKIE]!!.toDataObject())
            rulebookInducements.add(this[CommonInducementType.HALFLING_MASTER_CHEF]!!.toDataObject())
            rulebookInducements.add(this[CommonInducementType.STANDARD_MERCENARY_PLAYERS]!!.toDataObject())
            rulebookInducements.add(this[CommonInducementType.STAR_PLAYERS]!!.toDataObject())
            rulebookInducements.add(this[CommonInducementType.INFAMOUS_COACHING_STAFF]!!.toDataObject())
            rulebookInducements.add(this[CommonInducementType.WIZARD]!!.toDataObject())
            rulebookInducements.add(this[CommonInducementType.BIASED_REFEREE]!!.toDataObject())

            // Define inducements from DeathZone
            deathZoneInducements.add(this[BB2020InducementType.WAAAGH_DRUMMER]!!.toDataObject())
            deathZoneInducements.add(this[BB2020InducementType.CAVORTING_NURGLINGS]!!.toDataObject())
            deathZoneInducements.add(this[BB2020InducementType.HALFLING_HOTPOT]!!.toDataObject())
            deathZoneInducements.add(this[BB2020InducementType.MASTER_OF_BALLISTICS]!!.toDataObject())
            deathZoneInducements.add(this[BB2020InducementType.EXPANDED_MERCENARY_PLAYERS]!!.toDataObject())
            deathZoneInducements.add(this[BB2020InducementType.GIANT]!!.toDataObject())
            deathZoneInducements.add(this[BB2020InducementType.DESPERATE_MEASURES]!!.toDataObject())
        }
    }
}

private fun InducementBuilder.toDataObject(): InducementData {
    val price: Int? = when (this) {
        is CommonSingleInducementBuilder -> {
            when (this) {
                is MercenaryInducement.Builder -> null
                is BiasedRefereeInducement.Builder -> null
                is InfamousCoachingStaffInducement.Builder -> null
                is SimpleInducement.Builder -> this.price
                is StarPlayerInducement.Builder -> null
                is WizardInducement.Builder -> null
            }
        }
        is CommonInducementGroupBuilder -> {
            when (this) {
                is BiasedRefereesInducementGroup.Builder -> null
                is InfamousCoachingStaffsInducementGroup.Builder -> null
                is StarPlayersInducementGroup.Builder -> null
                is WizardsInducementGroup.Builder -> null
            }
        }
        is CommonTeamPlayerInducementBuilder -> {
            when (this) {
                is StandardMercenaryInducement.Builder -> null
            }
        }
        is BB2020SingleInducementBuilder -> {
            error("Unsupported inducement builder type: $this")
        }
        is BB2020InducementGroupBuilder -> {
            error("Unsupported inducement builder type: $this")
        }
        is BB2020TeamPlayerInducementBuilder -> {
            when (this) {
                is ExpandedMercenaryInducements.Builder -> null
            }
        }
        is BB2025SingleInducementBuilder -> {
            error("Unsupported inducement builder type: $this")
        }
        is BB2025InducementGroupBuilder -> {
            error("Unsupported inducement builder type: $this")
        }
        is BB2025TeamPlayerInducementBuilder -> {
            error("Unsupported inducement builder type: $this")
        }
        else -> error("Unknown inducement builder type: $this")
    }

    return InducementData(
        type = this.type,
        name = this.name,
        enabled = this.enabled,
        max = this.max,
        price = price,
    )
}
