package com.jervisffb.engine

import com.jervisffb.engine.model.PositionId
import com.jervisffb.engine.model.inducements.settings.Inducement
import com.jervisffb.engine.model.inducements.settings.InducementBuilder
import com.jervisffb.engine.model.inducements.settings.InducementGroup
import com.jervisffb.engine.model.inducements.settings.InducementGroupBuilder
import com.jervisffb.engine.model.inducements.settings.InducementType
import com.jervisffb.engine.model.inducements.settings.SingleInducementBuilder
import com.jervisffb.engine.model.inducements.settings.StarPlayersProvider
import com.jervisffb.engine.rules.common.roster.StarPlayerPosition
import kotlinx.serialization.Serializable
import kotlin.collections.toMutableMap

@Serializable
class InducementSettings(
    val topDogTopUpLimitFromTreasury: Int,
    val underdogTopUpLimitFromTreasury: Int,
    private val inducements: Map<InducementType, Inducement<*>>
) : MutableMap<InducementType, Inducement<*>> by inducements.toMutableMap() {

    /**
     * Every Star Player that can be hired in this ruleset.
     */
    val starPlayers
        get() = values.flatMap { inducement ->
            when (inducement) {
                is StarPlayersProvider -> inducement.players
                is InducementGroup<*, *, *> -> inducement.items.filterIsInstance<StarPlayersProvider>().flatMap { it.players }
                else -> emptyList()
            }
        }

    /**
     * Find the Star Player with the given [id], or `null` if this ruleset doesn't have
     * them.
     */
    fun findStarPlayer(id: PositionId): StarPlayerPosition? = starPlayers.firstOrNull { it.id == id }

    /**
     * Find the Star Player with the given [title], or `null` if this ruleset doesn't
     * have them. Used when importing teams from sites that identify Star Players by
     * name. They do not always spell them the way the rulebook does, e.g. "Akhorne the
     * Squirrel" or the typographic apostrophes in "Morg ‘n’ Thorg", so the match
     * ignores case and apostrophe style.
     */
    fun findStarPlayer(title: String): StarPlayerPosition? {
        val wanted = title.normalizeStarPlayerTitle()
        return starPlayers.firstOrNull { it.title.normalizeStarPlayerTitle() == wanted }
    }

    private fun String.normalizeStarPlayerTitle(): String =
        trim().lowercase().replace('\u2018', '\'').replace('\u2019', '\'')

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




