package com.jervisffb.ui.game.model

import androidx.compose.runtime.Stable
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.Team

/**
 * Similar to [UiAction] this class is used to wrap a reference to a model
 * class, so Compose do not trigger recompositions needlessly. See [UiAction]
 * for a detailed description.
 *
 * This class is mostly used to wrap [Player] or [Team], and we do it only
 * because it was the quickest way to access a large number of model properties.
 * Composable functions should not rely on recompositions triggering from
 * changes to these classes, so wrapping them in a @Stable wrapper is fine.
 *
 * However, this class is an antipattern, and we should eventually get rid of
 * it.
 */
@Stable
data class ModelRef<T>(
    val key: Any,
    val model: T,
) {
    override fun equals(other: Any?): Boolean = (other is ModelRef<T> && key == other.key)
    override fun hashCode(): Int = key.hashCode()
    override fun toString(): String = "ModelRef($key)"
}
