package com.jervisffb.engine.model.inducements.settings

import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.rules.common.roster.Position
import com.jervisffb.engine.rules.common.roster.SpecialRules
import kotlinx.serialization.Serializable


/**
 * This class represents the standard mercenaries inducement in a ruleset.
 * It behaves slightly different between BB2020 and BB2025.
 *
 * See page 92 in the BB2020 rulebook.
 * See page XXX in the BB2025 rulebook.
 *
 * In BB2020 this inducement should not be allowed at the same time as
 * [ExpandedMercenaryInducements]. In BB2025, only this kind of mercenaries
 * are allowed.
 */
@Serializable
data class StandardMercenaryInducement(
    override val max: Int = 3,
    override val enabled: Boolean = true,
    val extraCost: Int = 30_000,
    val skillCost: Int = 50_000,
): TeamPlayerInducement<StandardMercenaryInducement.Builder> {
    override val type: InducementType = InducementType.STANDARD_MERCENARY_PLAYERS
    override val name: String = "Mercenary Players"

    override fun toBuilder() = Builder(this)

    class Builder(inducement: StandardMercenaryInducement): TeamPlayerInducementBuilder {
        override val type: InducementType = inducement.type
        override val name: String = inducement.name
        override var max: Int = inducement.max
        override var enabled: Boolean = inducement.enabled
        var extraCost: Int = inducement.extraCost
        var skillCost: Int = inducement.skillCost

        override fun build() = StandardMercenaryInducement(max, enabled, extraCost, skillCost)
    }
}

/**
 * This inducement should not be allowed at the same type as
 * [StandardMercenaryInducement].
 *
 * Not sure how much customization we want in the UI for these as it turns
 * pretty complex quickly. So for now, just expose the minimum. Support for
 * this has a pretty low priority regardless, so just postpone adding it to the
 * pre-game sequence UI.
 *
 * See page 41 in BB20205 Death Zone.
 */
@Serializable
data class ExpandedMercenaryInducements(
    override val max: Int = 3,
    override val enabled: Boolean = true,
): TeamPlayerInducement<ExpandedMercenaryInducements.Builder> {
    override val type: InducementType = InducementType.EXPANDED_MERCENARY_PLAYERS
    override val name: String = "Expanded Mercenary Players"

    override fun toBuilder() = Builder(this)

    class Builder(inducement: ExpandedMercenaryInducements): TeamPlayerInducementBuilder {
        override val type: InducementType = inducement.type
        override val name: String = inducement.name
        override var max: Int = inducement.max
        override var enabled: Boolean = inducement.enabled

        override fun build() = ExpandedMercenaryInducements(max, enabled)
    }
}

/**
 * This class describes a single mercenary inducement.
 * Its usage of the interfaces is slightly hacky, which indicates that we should probably
 * reconsider how mercenaries are modeled.
 */
@Serializable
data class MercenaryInducement(
    val position: Position,
    val extraSkills: List<SkillId>,
    val extraCost: Int = 30_000,
    val skillCost: Int = 50_000,
): SingleInducement<MercenaryInducement.Builder> {
    override val type: InducementType = InducementType.STANDARD_MERCENARY_PLAYERS
    override val name: String = "Mercenary ${position.title}"
    override val requirements: Set<SpecialRules> = emptySet()
    override val specialRulesModifier: Map<SpecialRules, Float> = emptyMap()
    override val teamNameModifier: List<Pair<String, Float>> = emptyList()
    override val max: Int = 1 // We create this inducement for each mercenary
    override val defaultPrice: Int = position.cost + extraCost + (skillCost * extraSkills.size)
    override val enabled: Boolean = true
    override fun toBuilder() = Builder(this)

    class Builder(mercenaryInducement: MercenaryInducement): SingleInducementBuilder {
        override val type: InducementType = InducementType.STANDARD_MERCENARY_PLAYERS
        override val name: String = "Mercenary "
        override var max: Int = mercenaryInducement.max
        override var price: Int = mercenaryInducement.defaultPrice
        override var enabled: Boolean = mercenaryInducement.enabled

        // Mercenaries are built directly and thus do not have configuration options here.
        override fun build() = error("Mercenary builders are not supported")
    }
}
