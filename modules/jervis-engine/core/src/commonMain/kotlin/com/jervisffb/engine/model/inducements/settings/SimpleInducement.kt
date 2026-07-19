package com.jervisffb.engine.model.inducements.settings

import com.jervisffb.engine.rules.common.roster.SpecialRules
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

    class Builder(private val inducement: SimpleInducement): SingleInducementBuilder {
        override val type: InducementType = inducement.type
        override val name: String = inducement.name
        override var max: Int = inducement.max
        override var price: Int = inducement.defaultPrice
        override var enabled: Boolean = inducement.enabled
        var requirements: Set<SpecialRules> = inducement.requirements.toSet()
        var specialRulesModifier: Map<SpecialRules, Float> = inducement.specialRulesModifier.toMap()
        var teamNameModifier: List<Pair<String, Float>> = inducement.teamNameModifier.toList()
        override fun build() = SimpleInducement(
            type, inducement.name, max, price, enabled, requirements, specialRulesModifier, teamNameModifier
        )
    }
}
