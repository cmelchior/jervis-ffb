package com.jervisffb.engine.common.inducements

import com.jervisffb.engine.model.inducements.settings.InducementType
import com.jervisffb.engine.model.inducements.settings.SingleInducement
import com.jervisffb.engine.rules.common.roster.SpecialRules
import com.jervisffb.engine.rules.common.roster.StarPlayerPosition
import com.jervisffb.teams.THE_BLACK_GOBBO
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable

/**
 * This class represents the list of available Star Players inducements in a
 * given ruleset.
 */
@Serializable
data class StarPlayersInducementGroup(
    override val max: Int = 2,
    override val enabled: Boolean = true,
    override val items: ImmutableList<StarPlayerInducement> = persistentListOf(
        StarPlayerInducement(
            THE_BLACK_GOBBO,
            1,
            THE_BLACK_GOBBO.cost,
            enabled = true,
            requirements = THE_BLACK_GOBBO.playsFor.toSet(),
        ),
    )
): InducementGroupCommon<StarPlayersInducementGroup.Builder, StarPlayerInducement.Builder, StarPlayerInducement> {
    override val type: InducementType = InducementTypeCommon.STAR_PLAYERS
    override val name: String = "Star Players"

    override fun toBuilder() = Builder(this)

    class Builder(inducement: StarPlayersInducementGroup): InducementGroupBuilderCommon {
        override val type: InducementType = inducement.type
        override val name: String = inducement.name
        override var max: Int = inducement.max
        override var enabled: Boolean = inducement.enabled
        var starPlayers: List<StarPlayerInducement.Builder> = inducement.items.map { it.toBuilder() }.toMutableList()

        override fun build() = StarPlayersInducementGroup(max, enabled, starPlayers.map { it.build()}.toImmutableList())
    }
}

/**
 * Class wrapping the details for a single Star Player inducement. To be
 * available, it should be added in [StarPlayersInducementGroup.items].
 */
@Serializable
data class StarPlayerInducement(
    val starPlayer: StarPlayerPosition,
    override val max: Int,
    override val defaultPrice: Int,
    override val enabled: Boolean,
    override val requirements: Set<SpecialRules> = emptySet(),
    override val specialRulesModifier: Map<SpecialRules, Float> = emptyMap(),
    override val teamNameModifier: List<Pair<String, Float>> = emptyList()
): SingleInducement<StarPlayerInducement.Builder> {
    override val type: InducementType = InducementTypeCommon.STAR_PLAYERS
    override val name: String = starPlayer.title
    override fun toBuilder() = Builder(this)

    class Builder(inducement: StarPlayerInducement): SingleInducementBuilderCommon {
        override val type: InducementType = InducementTypeCommon.STAR_PLAYERS
        override val name: String = inducement.name
        val starPlayer: StarPlayerPosition = inducement.starPlayer
        override var max: Int = inducement.max
        override var price: Int = inducement.defaultPrice
        override var enabled: Boolean = inducement.enabled
        var requirements: MutableSet<SpecialRules> = inducement.requirements.toMutableSet()
        var specialRulesModifier: Map<SpecialRules, Float> = inducement.specialRulesModifier.toMap()
        var teamNameModifier: MutableList<Pair<String, Float>> = inducement.teamNameModifier.toMutableList()

        override fun build() = StarPlayerInducement(starPlayer, max, price, enabled, requirements, specialRulesModifier, teamNameModifier)
    }
}
