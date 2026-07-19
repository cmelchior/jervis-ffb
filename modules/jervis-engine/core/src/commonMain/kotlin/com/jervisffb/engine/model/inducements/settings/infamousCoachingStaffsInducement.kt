package com.jervisffb.engine.model.inducements.settings

import com.jervisffb.engine.model.inducements.InfamousCoachingStaff
import com.jervisffb.engine.model.inducements.JosefBugman
import com.jervisffb.engine.rules.common.roster.SpecialRules
import kotlinx.serialization.Serializable

/**
 * This class represents the list of available Infamous Coaching Staff in a
 * given ruleset.
 */
@Serializable
data class InfamousCoachingStaffsInducementList(
    override val max: Int,
    override val enabled: Boolean,
    override val items: List<InfamousCoachingStaffInducement> = listOf(
        InfamousCoachingStaffInducement(
            JosefBugman(),
            1,
            100_000,
            named = true,
            enabled = true,
        ),
    )
): InducementGroup<InfamousCoachingStaffsInducementList.Builder, InfamousCoachingStaffInducement.Builder, InfamousCoachingStaffInducement> {
    override val type: InducementType = InducementType.INFAMOUS_COACHING_STAFF
    override val name: String = "Infamous Coaching Staff"
    override fun toBuilder() = Builder(this)

    class Builder(private val inducement: InfamousCoachingStaffsInducementList): InducementGroupBuilder {
        override val type: InducementType = inducement.type
        override val name: String = inducement.name
        override var max: Int = inducement.max
        override var enabled: Boolean = inducement.enabled
        var coachingStaff: MutableList<InfamousCoachingStaffInducement.Builder> = inducement.items.toList().map { it.toBuilder() }.toMutableList()

        override fun build() = InfamousCoachingStaffsInducementList(max, enabled, coachingStaff.map { it.build() })
    }
}

/**
 * This class represents a single Infamous Coaching Staff inducement. To be
 * available, it should be added in
 * [InfamousCoachingStaffsInducementList.items].
 */
@Serializable
data class InfamousCoachingStaffInducement(
    val staff: InfamousCoachingStaff,
    override val max: Int,
    override val defaultPrice: Int,
    val named: Boolean, // Is "named" in the context of the rules, i.e. has special restrictions in League Play
    override val enabled: Boolean,
    override val requirements: Set<SpecialRules> = emptySet(),
    override val specialRulesModifier: Map<SpecialRules, Float> = emptyMap(),
    override val teamNameModifier: List<Pair<String, Float>> = emptyList(),
): SingleInducement<InfamousCoachingStaffInducement.Builder> {
    override val type: InducementType = InducementType.INFAMOUS_COACHING_STAFF
    override val name: String = staff.name

    override fun toBuilder() = Builder(this)

    class Builder(inducement: InfamousCoachingStaffInducement): SingleInducementBuilder {
        val staff: InfamousCoachingStaff = inducement.staff
        override val type: InducementType = InducementType.INFAMOUS_COACHING_STAFF
        override val name: String = inducement.name
        override var max: Int = inducement.max
        override var price: Int = inducement.defaultPrice
        var named: Boolean = inducement.named
        override var enabled: Boolean = inducement.enabled
        var requirements: MutableSet<SpecialRules> = inducement.requirements.toMutableSet()
        var specialRulesModifier: Map<SpecialRules, Float> = inducement.specialRulesModifier.toMap()
        var teamNameModifier: MutableList<Pair<String, Float>> = inducement.teamNameModifier.toMutableList()

        override fun build() = InfamousCoachingStaffInducement(staff, max, price, named, enabled, requirements, specialRulesModifier, teamNameModifier)
    }
}
