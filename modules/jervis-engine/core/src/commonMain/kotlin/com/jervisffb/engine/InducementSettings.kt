package com.jervisffb.engine

import com.jervisffb.engine.model.inducements.settings.Inducement
import com.jervisffb.engine.model.inducements.settings.InducementBuilder
import com.jervisffb.engine.model.inducements.settings.InducementGroupBuilder
import com.jervisffb.engine.model.inducements.settings.InducementType
import com.jervisffb.engine.model.inducements.settings.SingleInducementBuilder
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
            return builders[type] as? SingleInducementBuilder ?: error("Inducement type $type is not a single inducement")
        }

        fun getGroup(type: InducementType): InducementGroupBuilder {
            return builders[type] as? InducementGroupBuilder ?: error("Inducement type $type is not a group inducement")
        }

        fun getInducement(type: InducementType): InducementBuilder {
            return builders[type] ?: error("Inducement type $type is not a valid inducement")
        }

        fun build(): InducementSettings {
            val inducements = this.entries.associate {
                it.key to it.value.build()
            }
            return InducementSettings(topDogTopUpLimitFromTreasury, underdogTopUpLimitFromTreasury, inducements)
        }
    }
}




