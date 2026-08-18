package com.jervisffb.engine.common.inducements

import com.jervisffb.engine.model.inducements.settings.InducementType
import com.jervisffb.engine.model.inducements.settings.SingleInducement
import com.jervisffb.engine.rules.common.roster.SpecialRules
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.serialization.Serializable

/**
 * This class describes simple inducements, i.e., inducements that only have
 * a price and count and can otherwise be directly added to the team.
 */
@Serializable
data class SimpleInducement(
    override val type: InducementType,
    override val name: String,
    override val max: Int,
    override val defaultPrice: Int,
    override val enabled: Boolean,
    override val requirements: Set<SpecialRules> = emptySet(),
    override val specialRulesModifier: Map<SpecialRules, Float> = emptyMap(),
    override val teamNameModifier: List<Pair<String, Float>> = emptyList()
): SingleInducement<SimpleInducement.Builder> {

    override fun toBuilder() = Builder(this)

    class Builder(private val inducement: SimpleInducement): CommonSingleInducementBuilder {
        override val type: InducementType = inducement.type
        override val name: String = inducement.name
        override var max: Int = inducement.max
        override var price: Int = inducement.defaultPrice
        override var enabled: Boolean = inducement.enabled
        var requirements: ImmutableSet<SpecialRules> = inducement.requirements.toImmutableSet()
        var specialRulesModifier: ImmutableMap<SpecialRules, Float> = inducement.specialRulesModifier.toImmutableMap()
        var teamNameModifier: ImmutableList<Pair<String, Float>> = inducement.teamNameModifier.toImmutableList()
        override fun build() = SimpleInducement(
            type, inducement.name, max, price, enabled, requirements, specialRulesModifier, teamNameModifier
        )
    }
}
