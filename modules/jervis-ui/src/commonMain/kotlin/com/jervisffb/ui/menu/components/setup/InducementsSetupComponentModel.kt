package com.jervisffb.ui.menu.components.setup

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import cafe.adriel.voyager.core.model.ScreenModel
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

    val builders: MutableMap<InducementType, InducementBuilder> = mutableMapOf()
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
                builders[type]!!.enabled = enabled
                inducements[index] = inducements[index].copy(enabled = enabled)
            }
        }
    }

    fun updateRulesBuilder(rulesBuilder: Rules.Builder) {
        this.rulesBuilder = rulesBuilder
        builders.clear()

        // Define inducements from the Rulebook
        builders.putAll(
            rulesBuilder.inducements.entries.associate {
                it.key to it.value.toBuilder()
            }
        )

        rulebookInducements.add(builders[InducementType.TEMP_AGENCY_CHEERLEADER]!!.toDataObject())
        rulebookInducements.add(builders[InducementType.PART_TIME_ASSISTANT_COACH]!!.toDataObject())
        rulebookInducements.add(builders[InducementType.WEATHER_MAGE]!!.toDataObject())
        rulebookInducements.add(builders[InducementType.BLOODWEISER_KEG]!!.toDataObject())
        rulebookInducements.add(builders[InducementType.SPECIAL_PLAY]!!.toDataObject())
        rulebookInducements.add(builders[InducementType.BRIBE]!!.toDataObject())
        rulebookInducements.add(builders[InducementType.WANDERING_APOTHECARY]!!.toDataObject())
        rulebookInducements.add(builders[InducementType.MORTUARY_ASSISTANT]!!.toDataObject())
        rulebookInducements.add(builders[InducementType.PLAGUE_DOCTOR]!!.toDataObject())
        rulebookInducements.add(builders[InducementType.RIOTOUS_ROOKIE]!!.toDataObject())
        rulebookInducements.add(builders[InducementType.HALFLING_MASTER_CHEF]!!.toDataObject())
        rulebookInducements.add(builders[InducementType.STANDARD_MERCENARY_PLAYERS]!!.toDataObject())
        rulebookInducements.add(builders[InducementType.STAR_PLAYERS]!!.toDataObject())
        rulebookInducements.add(builders[InducementType.INFAMOUS_COACHING_STAFF]!!.toDataObject())
        rulebookInducements.add(builders[InducementType.WIZARD]!!.toDataObject())
        rulebookInducements.add(builders[InducementType.BIASED_REFEREE]!!.toDataObject())

        // Define inducements from DeathZone
        deathZoneInducements.add(builders[InducementType.WAAAGH_DRUMMER]!!.toDataObject())
        deathZoneInducements.add(builders[InducementType.CAVORTING_NURGLINGS]!!.toDataObject())
        deathZoneInducements.add(builders[InducementType.DWARFEN_RUNESMITH]!!.toDataObject())
        deathZoneInducements.add(builders[InducementType.HALFLING_HOTPOT]!!.toDataObject())
        deathZoneInducements.add(builders[InducementType.MASTER_OF_BALLISTICS]!!.toDataObject())
        deathZoneInducements.add(builders[InducementType.EXPANDED_MERCENARY_PLAYERS]!!.toDataObject())
        deathZoneInducements.add(builders[InducementType.GIANT]!!.toDataObject())    }
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
