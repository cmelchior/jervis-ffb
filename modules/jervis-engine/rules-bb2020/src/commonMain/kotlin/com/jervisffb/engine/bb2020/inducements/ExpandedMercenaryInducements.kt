package com.jervisffb.engine.bb2020.inducements

import com.jervisffb.engine.model.inducements.settings.InducementType
import com.jervisffb.engine.model.inducements.settings.TeamPlayerInducement
import kotlinx.serialization.Serializable

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
    override val type: InducementType = BB2020InducementType.EXPANDED_MERCENARY_PLAYERS
    override val name: String = "Expanded Mercenary Players"

    override fun toBuilder() = Builder(this)

    class Builder(inducement: ExpandedMercenaryInducements): BB2020TeamPlayerInducementBuilder {
        override val type: InducementType = inducement.type
        override val name: String = inducement.name
        override var max: Int = inducement.max
        override var enabled: Boolean = inducement.enabled

        override fun build() = ExpandedMercenaryInducements(max, enabled)
    }
}
