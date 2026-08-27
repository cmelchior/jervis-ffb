package com.jervisffb.engine.common.inducements

import com.jervisffb.engine.model.inducements.settings.InducementType
import com.jervisffb.engine.model.inducements.settings.SingleInducement
import com.jervisffb.engine.model.inducements.wizard.WizardType
import com.jervisffb.engine.rules.common.roster.SpecialRules
import kotlinx.serialization.Serializable

/**
 * This class represents the list of available Wizard inducements in a
 * given ruleset.
 */
@Serializable
data class WizardsInducementGroup(
    override val max: Int = 1,
    override val enabled: Boolean,
    override val items: List<WizardInducement> = listOf()
): InducementGroupCommon<WizardsInducementGroup.Builder, WizardInducement.Builder, WizardInducement> {
    override val name: String = "Wizard"
    override val type: InducementType = InducementTypeCommon.WIZARD

    override fun toBuilder() = Builder(this)

    class Builder(inducement: WizardsInducementGroup): InducementGroupBuilderCommon {
        override val type: InducementType = inducement.type
        override val name: String = inducement.name
        override var max: Int = inducement.max
        override var enabled: Boolean = inducement.enabled
        var wizards: List<WizardInducement.Builder> = inducement.items.toList().map { it.toBuilder() }
        override fun build() = WizardsInducementGroup(max, enabled, wizards.map { it.build() })
    }
}

/**
 * This class represents a single Wizard inducement. To be available, it
 * should be added in [WizardsInducementGroup.items].
 */
@Serializable
data class WizardInducement(
    val wizard: WizardType,
    override val max: Int,
    override val defaultPrice: Int,
    val named: Boolean, // Is "named" in the context of the rules, i.e. has special restrictions in League Play
    override val enabled: Boolean,
    override val requirements: Set<SpecialRules> = emptySet(),
    override val specialRulesModifier: Map<SpecialRules, Float> = emptyMap(),
    override val teamNameModifier: List<Pair<String, Float>> = emptyList()
): SingleInducement<WizardInducement.Builder> {
    override val name: String = wizard.label
    override val type: InducementType = InducementTypeCommon.WIZARD
    override fun toBuilder() = Builder(this)

    class Builder(wizardInducement: WizardInducement): SingleInducementBuilderCommon {
        override val type: InducementType = InducementTypeCommon.WIZARD
        override val name: String = wizardInducement.name
        val wizard: WizardType = wizardInducement.wizard
        override var max: Int = wizardInducement.max
        override var price: Int = wizardInducement.defaultPrice
        var named: Boolean = wizardInducement.named
        override var enabled: Boolean = wizardInducement.enabled
        var requirements: MutableSet<SpecialRules> = wizardInducement.requirements.toMutableSet()
        var specialRulesModifier: Map<SpecialRules, Float> = wizardInducement.specialRulesModifier.toMap()
        var teamNameModifier: MutableList<Pair<String, Float>> = wizardInducement.teamNameModifier.toMutableList()

        override fun build() = WizardInducement(wizard, max, price, named, enabled, requirements, specialRulesModifier, teamNameModifier)
    }
}
