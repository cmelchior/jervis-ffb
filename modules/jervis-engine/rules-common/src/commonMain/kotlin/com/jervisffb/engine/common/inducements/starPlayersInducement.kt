package com.jervisffb.engine.common.inducements

import com.jervisffb.engine.model.PositionId
import com.jervisffb.engine.model.inducements.settings.InducementType
import com.jervisffb.engine.model.inducements.settings.SingleInducement
import com.jervisffb.engine.rules.common.roster.SpecialRules
import com.jervisffb.engine.rules.common.roster.StarPlayerPosition
import kotlinx.serialization.Serializable

/**
 * This class represents the list of available Star Players inducements in a
 * given ruleset.
 *
 * Note, [max] is the number of Star Player *choices* a team may make, not the
 * number of players it ends up with. A pair, like Grak & Crumbleberry, is a
 * single choice that brings two players, each of which takes up a space on the
 * team's roster.
 *
 * [items] defaults to being empty because the Star Players are ruleset-specific
 * and declared by each ruleset, e.g. `STAR_PLAYER_INDUCEMENTS_BB2025`.
 */
@Serializable
data class StarPlayersInducementGroup(
    override val max: Int = 2,
    override val enabled: Boolean = true,
    override val items: List<StarPlayerInducement> = emptyList()
): InducementGroupCommon<StarPlayersInducementGroup.Builder, StarPlayerInducement.Builder, StarPlayerInducement> {
    override val type: InducementType = InducementTypeCommon.STAR_PLAYERS
    override val name: String = "Star Players"

    init {
        /**
         * A Star Player can only be hired through a single inducement, which is what
         * makes the list of players a usable identity for [InducementSelectionCommon.StarPlayer].
         */
        val allPlayers = items.flatMap { it.playerIds }
        require(allPlayers.size == allPlayers.toSet().size) {
            "The same Star Player appears in more than one inducement: $allPlayers"
        }
    }

    override fun toBuilder() = Builder(this)

    class Builder(inducement: StarPlayersInducementGroup): InducementGroupBuilderCommon {
        override val type: InducementType = inducement.type
        override val name: String = inducement.name
        override var max: Int = inducement.max
        override var enabled: Boolean = inducement.enabled
        var starPlayers: List<StarPlayerInducement.Builder> = inducement.items.map { it.toBuilder() }.toMutableList()

        override fun build() = StarPlayersInducementGroup(max, enabled, starPlayers.map { it.build()})
    }
}

/**
 * Class wrapping the details for a single Star Player inducement. To be
 * available, it should be added in [StarPlayersInducementGroup.items].
 *
 * An inducement usually hires a single Star Player, but a few must be hired as a
 * pair, e.g. Grak & Crumbleberry, in which case all of them are listed in
 * [players] and [defaultPrice] is their combined cost.
 */
@Serializable
data class StarPlayerInducement(
    /** The Star Players hired by this inducement, in the order they should be shown. */
    val players: List<StarPlayerPosition>,
    override val max: Int,
    override val defaultPrice: Int,
    override val enabled: Boolean,
    override val requirements: Set<SpecialRules> = emptySet(),
    override val specialRulesModifier: Map<SpecialRules, Float> = emptyMap(),
    override val teamNameModifier: List<Pair<String, Float>> = emptyList(),
    /**
     * Pairs have their own title in the rulebook, e.g. "The Swift Twins", which
     * cannot be derived from the player names. If `null`, the name is derived
     * from [players].
     */
    val titleOverride: String? = null,
): SingleInducement<StarPlayerInducement.Builder> {
    override val type: InducementType = InducementTypeCommon.STAR_PLAYERS
    override val name: String = titleOverride ?: players.joinToString(" & ") { it.title }
    val playerIds: List<PositionId> = players.map { it.id }
    /**
     * How many players this inducement hires. Regardless of this, the inducement only
     * counts as one of [StarPlayersInducementGroup.max] choices.
     */
    val playerCount: Int = players.size

    init {
        require(players.isNotEmpty()) { "A Star Player inducement must hire at least one player" }
    }

    override fun toBuilder() = Builder(this)

    companion object {
        /** A Star Player hired on their own. */
        fun single(star: StarPlayerPosition, enabled: Boolean = true) = StarPlayerInducement(
            players = listOf(star),
            max = 1,
            defaultPrice = star.cost,
            enabled = enabled,
            requirements = star.playsFor.toSet(),
        )

        /**
         * Star Players that can only be hired together, for a single combined cost.
         *
         * [combinedCost] is the source of truth for the total cost of both
         * players, but to make it easier to reason about elsewhere, the players
         * part of the pair should both carry half the cost.
         */
        fun pair(
            first: StarPlayerPosition,
            second: StarPlayerPosition,
            combinedCost: Int,
            title: String? = null,
            enabled: Boolean = true,
        ): StarPlayerInducement {
            // All BB2025 pairs play for the same teams. If that ever stops being true,
            // the rulebook entry will have to say which half decides.
            require(first.playsFor.toSet() == second.playsFor.toSet()) {
                "${first.title} and ${second.title} must play for the same teams"
            }
            return StarPlayerInducement(
                players = listOf(first, second),
                max = 1,
                defaultPrice = combinedCost,
                enabled = enabled,
                requirements = first.playsFor.toSet(),
                titleOverride = title,
            )
        }
    }

    class Builder(inducement: StarPlayerInducement): SingleInducementBuilderCommon {
        override val type: InducementType = InducementTypeCommon.STAR_PLAYERS
        override val name: String = inducement.name
        val players: List<StarPlayerPosition> = inducement.players
        override var max: Int = inducement.max
        override var price: Int = inducement.defaultPrice
        override var enabled: Boolean = inducement.enabled
        var requirements: MutableSet<SpecialRules> = inducement.requirements.toMutableSet()
        var specialRulesModifier: Map<SpecialRules, Float> = inducement.specialRulesModifier.toMap()
        var teamNameModifier: MutableList<Pair<String, Float>> = inducement.teamNameModifier.toMutableList()
        var titleOverride: String? = inducement.titleOverride

        override fun build() = StarPlayerInducement(
            players,
            max,
            price,
            enabled,
            requirements,
            specialRulesModifier,
            teamNameModifier,
            titleOverride,
        )
    }
}

/**
 * The Star Players a ruleset offers, as inducements: everyone not part of a pair on their
 * own, plus the pairs. Deriving it this way means adding a player to the ruleset's list of
 * Star Players is enough to make them hire-able.
 *
 * Sorted by name, which is the order they are listed in when hiring them.
 */
fun starPlayerInducements(
    allPlayers: List<StarPlayerPosition>,
    pairs: List<StarPlayerInducement> = emptyList(),
): List<StarPlayerInducement> {
    val paired = pairs.flatMap { it.playerIds }.toSet()
    return (allPlayers.filterNot { it.id in paired }.map { StarPlayerInducement.single(it) } + pairs)
        .sortedBy { it.name }
}
