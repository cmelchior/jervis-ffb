package com.jervisffb.ui.menu.components.setup

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import cafe.adriel.voyager.core.model.ScreenModel
import com.jervisffb.engine.InducementSettings
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
        val inducementsInCategory = inducements[category]!!
        updateEnabled(inducementsInCategory, type, enabled)

        // Expanded Mercenaries replace the standard rules and vice versa.
        if (enabled && type == InducementType.STANDARD_MERCENARY_PLAYERS) {
            updateEnabled(
                this.inducements[HEADER_DEATH_ZONE]!!,
                InducementType.EXPANDED_MERCENARY_PLAYERS,
                false
            )
        }
        if (enabled && type == InducementType.EXPANDED_MERCENARY_PLAYERS) {
            updateEnabled(
                this.inducements[HEADER_RULEBOOK]!!,
                InducementType.STANDARD_MERCENARY_PLAYERS,
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
                builders!![type]!!.enabled = enabled
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
            inducementCategories.add(HEADER_SPIKE_19)
            inducementCategories.add(HEADER_SPIKE_20)

            inducements.clear()
            val rulebookInducements = mutableStateListOf<InducementData>()
            val spike19Inducements = mutableStateListOf<InducementData>()
            val spike20Inducements = mutableStateListOf<InducementData>()
            inducements[HEADER_RULEBOOK] = rulebookInducements
            inducements[HEADER_SPIKE_19] = spike19Inducements
            inducements[HEADER_SPIKE_20] = spike20Inducements

            // Define inducements from the rule book
            rulebookInducements.add(this[InducementType.PRAYERS_TO_NUFFLE]!!.toDataObject())
            rulebookInducements.add(this[InducementType.PART_TIME_ASSISTANT_COACH]!!.toDataObject())
            rulebookInducements.add(this[InducementType.TEMP_AGENCY_CHEERLEADER]!!.toDataObject())
            rulebookInducements.add(this[InducementType.TEAM_MASCOT]!!.toDataObject())
            rulebookInducements.add(this[InducementType.WEATHER_MAGE]!!.toDataObject())
            rulebookInducements.add(this[InducementType.BLITZERS_BEST_KEGS]!!.toDataObject())
            rulebookInducements.add(this[InducementType.BRIBE]!!.toDataObject())
            rulebookInducements.add(this[InducementType.EXTRA_TEAM_TRAINING]!!.toDataObject())
            rulebookInducements.add(this[InducementType.MORTUARY_ASSISTANT]!!.toDataObject())
            rulebookInducements.add(this[InducementType.PLAGUE_DOCTOR]!!.toDataObject())
            rulebookInducements.add(this[InducementType.RIOTOUS_ROOKIE]!!.toDataObject())
            rulebookInducements.add(this[InducementType.WANDERING_APOTHECARY]!!.toDataObject())
            rulebookInducements.add(this[InducementType.HALFLING_MASTER_CHEF]!!.toDataObject())
            rulebookInducements.add(this[InducementType.BIASED_REFEREE]!!.toDataObject())
            rulebookInducements.add(this[InducementType.INFAMOUS_COACHING_STAFF]!!.toDataObject())
            rulebookInducements.add(this[InducementType.STANDARD_MERCENARY_PLAYERS]!!.toDataObject())
            rulebookInducements.add(this[InducementType.STAR_PLAYERS]!!.toDataObject())
            rulebookInducements.add(this[InducementType.WIZARD]!!.toDataObject())

            // Define inducements from Spike 19
            spike19Inducements.add(this[InducementType.BRETONNIAN_PASTRIES]!!.toDataObject())
            spike19Inducements.add(this[InducementType.BRETONNIAN_DAMSEL]!!.toDataObject())

            // Define inducements from Spike 20
            spike20Inducements.add(this[InducementType.CANOPIC_JAR]!!.toDataObject())
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
            rulebookInducements.add(this[InducementType.TEMP_AGENCY_CHEERLEADER]!!.toDataObject())
            rulebookInducements.add(this[InducementType.PART_TIME_ASSISTANT_COACH]!!.toDataObject())
            rulebookInducements.add(this[InducementType.WEATHER_MAGE]!!.toDataObject())
            rulebookInducements.add(this[InducementType.BLOODWEISER_KEG]!!.toDataObject())
            rulebookInducements.add(this[InducementType.SPECIAL_PLAY]!!.toDataObject())
            rulebookInducements.add(this[InducementType.BRIBE]!!.toDataObject())
            rulebookInducements.add(this[InducementType.WANDERING_APOTHECARY]!!.toDataObject())
            rulebookInducements.add(this[InducementType.MORTUARY_ASSISTANT]!!.toDataObject())
            rulebookInducements.add(this[InducementType.PLAGUE_DOCTOR]!!.toDataObject())
            rulebookInducements.add(this[InducementType.RIOTOUS_ROOKIE]!!.toDataObject())
            rulebookInducements.add(this[InducementType.HALFLING_MASTER_CHEF]!!.toDataObject())
            rulebookInducements.add(this[InducementType.STANDARD_MERCENARY_PLAYERS]!!.toDataObject())
            rulebookInducements.add(this[InducementType.STAR_PLAYERS]!!.toDataObject())
            rulebookInducements.add(this[InducementType.INFAMOUS_COACHING_STAFF]!!.toDataObject())
            rulebookInducements.add(this[InducementType.WIZARD]!!.toDataObject())
            rulebookInducements.add(this[InducementType.BIASED_REFEREE]!!.toDataObject())

            // Define inducements from DeathZone
            deathZoneInducements.add(this[InducementType.WAAAGH_DRUMMER]!!.toDataObject())
            deathZoneInducements.add(this[InducementType.CAVORTING_NURGLINGS]!!.toDataObject())
            deathZoneInducements.add(this[InducementType.DWARFEN_RUNESMITH]!!.toDataObject())
            deathZoneInducements.add(this[InducementType.HALFLING_HOTPOT]!!.toDataObject())
            deathZoneInducements.add(this[InducementType.MASTER_OF_BALLISTICS]!!.toDataObject())
            deathZoneInducements.add(this[InducementType.EXPANDED_MERCENARY_PLAYERS]!!.toDataObject())
            deathZoneInducements.add(this[InducementType.GIANT]!!.toDataObject())
            deathZoneInducements.add(this[InducementType.DESPERATE_MEASURES]!!.toDataObject())
        }
    }
}

private fun InducementBuilder.toDataObject(): InducementData {
    return InducementData(
        type = this.type,
        name = this.name,
        enabled = this.enabled,
        max = this.max,
        price = this.price,
    )
}
