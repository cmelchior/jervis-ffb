package com.jervisffb.engine

import com.jervisffb.engine.model.inducements.settings.BiasedRefereeInducement
import com.jervisffb.engine.model.inducements.settings.BiasedRefereesInducementList
import com.jervisffb.engine.model.inducements.settings.ExpandedMercenaryInducements
import com.jervisffb.engine.model.inducements.settings.Inducement
import com.jervisffb.engine.model.inducements.settings.InducementBuilder
import com.jervisffb.engine.model.inducements.settings.InducementGroupBuilder
import com.jervisffb.engine.model.inducements.settings.InducementType
import com.jervisffb.engine.model.inducements.settings.InfamousCoachingStaffInducement
import com.jervisffb.engine.model.inducements.settings.InfamousCoachingStaffsInducementList
import com.jervisffb.engine.model.inducements.settings.MercenaryInducement
import com.jervisffb.engine.model.inducements.settings.SimpleInducement
import com.jervisffb.engine.model.inducements.settings.SingleInducementBuilder
import com.jervisffb.engine.model.inducements.settings.StandardMercenaryInducement
import com.jervisffb.engine.model.inducements.settings.StarPlayerInducement
import com.jervisffb.engine.model.inducements.settings.StarPlayersInducementList
import com.jervisffb.engine.model.inducements.settings.WizardInducement
import com.jervisffb.engine.model.inducements.settings.WizardsInducementList
import kotlinx.serialization.Serializable
import kotlin.collections.toMutableMap

@Serializable
class InducementSettings(
    val topDogTopUpLimitFromTreasury: Int,
    val underdogTopUpLimitFromTreasury: Int,
    private val inducements: Map<InducementType, Inducement<*>>
) : MutableMap<InducementType, Inducement<*>> by inducements.toMutableMap() {
    fun toBuilder(): Builder {
        val builders = this.entries.associate {
            it.key to it.value.toBuilder()
        }
        return Builder(topDogTopUpLimitFromTreasury, underdogTopUpLimitFromTreasury, builders)
    }

    class Builder(
        var topDogTopUpLimitFromTreasury: Int,
        var underdogTopUpLimitFromTreasury: Int,
        private val builders: Map<InducementType, InducementBuilder>
    ) : MutableMap<InducementType, InducementBuilder> by builders.toMutableMap() {

        fun getSingle(type: InducementType): SingleInducementBuilder {
            return builders[type] as SingleInducementBuilder
        }

        fun getGroup(type: InducementType): InducementGroupBuilder {
            return builders[type] as InducementGroupBuilder
        }

        fun getInducement(type: InducementType): InducementBuilder {
            return builders[type]!!
        }

        fun build(): InducementSettings {
            val inducements = this.entries.associate {
                it.key to when (val builder = it.value) {
                    is BiasedRefereeInducement.Builder -> builder.build()
                    is BiasedRefereesInducementList.Builder -> builder.build()
                    is ExpandedMercenaryInducements.Builder -> builder.build()
                    is InfamousCoachingStaffInducement.Builder -> builder.build()
                    is InfamousCoachingStaffsInducementList.Builder -> builder.build()
                    is SimpleInducement.Builder -> builder.build()
                    is StandardMercenaryInducement.Builder -> builder.build()
                    is StarPlayerInducement.Builder -> builder.build()
                    is StarPlayersInducementList.Builder -> builder.build()
                    is WizardInducement.Builder -> builder.build()
                    is WizardsInducementList.Builder -> builder.build()
                    is MercenaryInducement.Builder -> error("Mercenary builders are not supported")
                }
            }
            return InducementSettings(topDogTopUpLimitFromTreasury, underdogTopUpLimitFromTreasury, inducements)
        }
    }
}




