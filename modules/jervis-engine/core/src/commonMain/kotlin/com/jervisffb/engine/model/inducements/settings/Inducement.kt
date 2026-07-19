package com.jervisffb.engine.model.inducements.settings

import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.common.roster.SpecialRules
import kotlinx.serialization.Serializable

/**
 * Top-level interface for describing all inducements available in a given
 * ruleset.
 *
 * It has 3 primary categories:
 * - [SingleInducement] representing simple inducements.
 * - [InducementGroup] representing a group of similar, but different,
 *   inducements, like star players.
 * - [TeamPlayerInducement] representing inducements that can create
 *   players using the normal positions available to a team.
 */
@Serializable
sealed interface Inducement<B: InducementBuilder> {
    val type: InducementType
    val name: String
    val max: Int
    val enabled: Boolean
    fun toBuilder(): B
}

/**
 * Interface representing "simple" inducements. This means inducements that
 * can be created by just knowing the type and how many of them to add.
 */
@Serializable
sealed interface SingleInducement<T: SingleInducementBuilder>: Inducement<T> {
    // The standard price for this inducement. Not counting any reductions from special rules
    val defaultPrice: Int
    // If non-empty, only teams with one of these special rules can use it
    val requirements: Set<SpecialRules>
    // Modifiers that either depend on a special rule or matching a team name
    // regex (represented as a string). Only the first match is used, starting
    // with searching special rules and then matching on team names
    val specialRulesModifier: Map<SpecialRules, Float>
    val teamNameModifier: List<Pair<String, Float>>
    override fun toBuilder(): T

    /**
     * Returns the price for a given team, taking into account any special rule
     * or team name modifiers.
     */
    fun getPrice(team: Team): Int {
        val specialRuleModifier = specialRulesModifier.firstNotNullOfOrNull {
            when (team.specialRules.contains(it.key)) {
                true -> it.value
                false -> null
            }
        }
        val teamNameModifier = teamNameModifier.firstNotNullOfOrNull {
            when (Regex(it.first).matches(team.roster.name)) {
                true -> it.second
                false -> null
            }
        }
        return specialRuleModifier?.times(defaultPrice)?.toInt()
            ?: teamNameModifier?.times(defaultPrice)?.toInt()
            ?: defaultPrice
    }
}

/**
 * Interface for inducements that are based on players on the Team buying them.
 * This is used to model Mercenaries or Expanded Mercenaries as they are hard to
 * fit into other categories.
 */
@Serializable
sealed interface TeamPlayerInducement<T: TeamPlayerInducementBuilder>: Inducement<T>


/**
 * Interface for describing inducements that are a group of non-uniform items,
 * like Star Players or Wizards.
 */
@Serializable
sealed interface InducementGroup<GB: InducementGroupBuilder, IB: SingleInducementBuilder, I: SingleInducement<IB>>: Inducement<GB> {
    val items: List<I>
    override fun toBuilder(): GB
}
