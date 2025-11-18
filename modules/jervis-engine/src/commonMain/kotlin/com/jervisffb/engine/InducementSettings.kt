package com.jervisffb.engine

import com.jervisffb.engine.model.inducements.settings.BiasedRefereesInducement
import com.jervisffb.engine.model.inducements.settings.ExpandedMercenaryInducements
import com.jervisffb.engine.model.inducements.settings.Inducement
import com.jervisffb.engine.model.inducements.settings.InducementBuilder
import com.jervisffb.engine.model.inducements.settings.InducementType
import com.jervisffb.engine.model.inducements.settings.InfamousCoachingStaffsInducement
import com.jervisffb.engine.model.inducements.settings.SimpleInducement
import com.jervisffb.engine.model.inducements.settings.StandardMercenaryInducements
import com.jervisffb.engine.model.inducements.settings.StarPlayersInducement
import com.jervisffb.engine.model.inducements.settings.WizardsInducement
import kotlinx.serialization.Serializable

@Serializable
class InducementSettings(private val inducements: Map<InducementType, Inducement<*>>) : MutableMap<InducementType, Inducement<*>> by inducements.toMutableMap() {
    fun toBuilder(): Builder {
        val builders = this.entries.associate {
            it.key to it.value.toBuilder()
        }
        return Builder(builders)
    }

    class Builder(private val builders: Map<InducementType, InducementBuilder>) : MutableMap<InducementType, InducementBuilder> by builders.toMutableMap() {
        fun build(): InducementSettings {
            val inducements = this.entries.associate {
                it.key to when (val builder = it.value) {
                    is BiasedRefereesInducement.Builder -> builder.build()
                    is ExpandedMercenaryInducements.Builder -> builder.build()
                    is InfamousCoachingStaffsInducement.Builder -> builder.build()
                    is SimpleInducement.Builder -> builder.build()
                    is StandardMercenaryInducements.Builder -> builder.build()
                    is StarPlayersInducement.Builder -> builder.build()
                    is WizardsInducement.Builder -> builder.build()
                }
            }
            return InducementSettings(inducements)
        }
    }
}




