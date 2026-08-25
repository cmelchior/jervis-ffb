package com.jervisffb.engine.common.inducements

import com.jervisffb.engine.model.inducements.biasedreferee.BiasedReferee
import com.jervisffb.engine.model.inducements.settings.InducementType
import com.jervisffb.engine.model.inducements.settings.SingleInducement
import com.jervisffb.engine.rules.common.roster.SpecialRules
import kotlinx.serialization.Serializable

/**
 * This class represents the list of available Biased Referees in a given ruleset.
 */
@Serializable
data class BiasedRefereesInducementGroup(
    override val max: Int = 1,
    override val enabled: Boolean,
    override val items: List<BiasedRefereeInducement> = emptyList()
): InducementGroupCommon<BiasedRefereesInducementGroup.Builder, BiasedRefereeInducement.Builder, BiasedRefereeInducement> {
    override val name: String = "Biased Referee"
    override val type: InducementType = InducementTypeCommon.BIASED_REFEREE

    override fun toBuilder() = Builder(this)

    class Builder(inducement: BiasedRefereesInducementGroup): InducementGroupBuilderCommon {
        override val type: InducementType = inducement.type
        override val name: String = inducement.name
        override var max: Int = inducement.max
        override var enabled: Boolean = inducement.enabled
        var referees: List<BiasedRefereeInducement.Builder> = inducement.items.map { it.toBuilder() }.toMutableList()

        override fun build() = BiasedRefereesInducementGroup(max, enabled, referees.map { it.build() })
    }
}

/**
 * This class represents a single biased referee inducement. To be
 * available, it should be added in [BiasedRefereesInducementGroup.items].
 */
@Serializable
data class BiasedRefereeInducement(
    val referee: BiasedReferee,
    override val max: Int,
    override val defaultPrice: Int,
    val named: Boolean, // Is "named" in the context of the rules, i.e. has special restrictions in League Play
    override val enabled: Boolean,
    override val requirements: Set<SpecialRules> = emptySet(),
    override val specialRulesModifier: Map<SpecialRules, Float> = emptyMap(),
    override val teamNameModifier: List<Pair<String, Float>> = emptyList()
): SingleInducement<BiasedRefereeInducement.Builder> {
    override val type: InducementType = InducementTypeCommon.BIASED_REFEREE
    override val name: String = referee.name
    override fun toBuilder() = Builder(this)

    class Builder(inducement: BiasedRefereeInducement): SingleInducementBuilderCommon {
        val referee: BiasedReferee = inducement.referee
        override val type: InducementType = InducementTypeCommon.BIASED_REFEREE
        override val name: String = referee.name
        override var max: Int = inducement.max
        override var price: Int = inducement.defaultPrice
        var named: Boolean = inducement.named
        override var enabled: Boolean = inducement.enabled
        var requirements: MutableSet<SpecialRules> = inducement.requirements.toMutableSet()
        var specialRulesModifier: Map<SpecialRules, Float> = inducement.specialRulesModifier.toMap()
        var teamNameModifier: MutableList<Pair<String, Float>> = inducement.teamNameModifier.toMutableList()

        override fun build() = BiasedRefereeInducement(referee, max, price, named, enabled, requirements, specialRulesModifier, teamNameModifier)
    }
}
