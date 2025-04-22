package com.jervisffb.ui.menu.components.setup

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import cafe.adriel.voyager.core.model.ScreenModel
import com.jervisffb.engine.InducementSettings
import com.jervisffb.engine.model.inducements.settings.InducementBuilder
import com.jervisffb.engine.model.inducements.settings.InducementType
import com.jervisffb.engine.rules.Rules
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
 * mercenaries, star players, etc. is fairly complex and changing these will probably rarely happen. So not
 * really worth it right now.
 */
class InducementsSetupComponentModel(initialRulesBuilder: Rules.Builder, private val menuViewModel: MenuViewModel) : ScreenModel {

    var rulesBuilder = initialRulesBuilder

    // It isn't possible to get inducements in an illegal state (I think), but keep it for now in
    // case it changes.
    val isSetupValid: MutableStateFlow<Boolean> = MutableStateFlow(true)

    var builders: InducementSettings.Builder? = null
    val rulebookInducements = mutableStateListOf<InducementData>()
    val deathZoneInducements = mutableStateListOf<InducementData>()

    init {
        updateRulesBuilder(rulesBuilder)
    }

    fun updateStandardInducementEnabled(type: InducementType, enabled: Boolean) {
        updateEnabled(rulebookInducements, type, enabled)
        if (enabled && type == InducementType.STANDARD_MERCENARY_PLAYERS) {
            updateEnabled(rulebookInducements, type, enabled)
        }
    }

    fun updateDeathZoneInducementEnabled(type: InducementType, enabled: Boolean) {
        updateEnabled(deathZoneInducements, type, enabled)
        if (enabled && type == InducementType.EXPANDED_MERCENARY_PLAYERS) {
            updateEnabled(deathZoneInducements, type, enabled)
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

    fun updateRulesBuilder(rulesBuilder: Rules.Builder) {
        this.rulesBuilder = rulesBuilder
        builders = rulesBuilder.inducements
        with(builders!!) {
            rulebookInducements.clear()
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
            deathZoneInducements.clear()
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
