package com.jervisffb.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.jervisffb.utils.jervisLogger

private val LOG = jervisLogger()

private const val MAX_VALUE_LENGTH = 200

/**
 * Debug companion to `Modifier.recomposeHighlighter`: the highlighter shows
 * *that* something recomposed, this shows *why*.
 *
 * Drop it at the top of a composable and hand it the parameters you suspect:
 *
 * ```
 * trackChanges("square ${square.coordinates}", "square" to square, "player" to player)
 * ```
 *
 * For every tracked value it compares the current value against the previous
 * composition's with both `===` and `==`, and reports the three cases
 * separately. That split is the whole point, because Compose picks the
 * comparison for you: stable parameters go through `Composer.changed`
 * (`equals`), unstable ones through `Composer.changedInstance` (`===`).
 *
 * - **`EQUAL VALUE`** - `===` differs but `==` matches. The recomposition was
 *   not earned. Either the type is unstable, so Compose compares by identity
 *   and never consults `equals`, or the value is simply rebuilt from scratch
 *   every snapshot. Fix the stability of the type; the call site is innocent.
 * - **`CHANGED`** - both differ, so the recomposition is normally legitimate.
 *   The exception to watch for is a value you expected to be unchanged, which
 *   usually means a raw lambda somewhere inside it: a Kotlin lambda that
 *   captures anything is a fresh object with identity `equals`, so two handlers
 *   that behave identically never compare equal, and any model holding one can
 *   never compare equal either. Wrap it in a [UiAction][com.jervisffb.ui.game.model.UiAction]
 *   and this line flips to `EQUAL VALUE` - which is the empirical answer to
 *   "is this the same onClick handler?".
 *
 * When *nothing* is reported but the composable still ran, the cause is not in
 * the list you passed: an untracked parameter, or a snapshot state read inside
 * the body.
 */
@Composable
fun trackChanges(tag: String, vararg values: Pair<String, Any?>) {
    // Deliberately not snapshot state: reading and writing these must not invalidate this
    // scope, or the tracker would cause the very recompositions it is meant to explain. Same
    // trick as `recomposeHighlighter`.
    val cache = remember(values.size) { ChangeCache(values.size) }
    val compositions = remember { arrayOf(0L) }
    compositions[0]++

    if (!cache.seeded) {
        cache.seeded = true
        values.forEachIndexed { index, (_, value) -> cache.previous[index] = value }
        LOG.d { "[$tag] first composition, ${values.size} value(s) tracked" }
        return
    }

    val report = mutableListOf<String>()
    values.forEachIndexed { index, (name, value) ->
        val before = cache.previous[index]
        cache.previous[index] = value
        when {
            before === value -> Unit // Identical, nothing to explain.
            before == value -> report += "$name: EQUAL VALUE, new instance -> ${render(value)}"
            else -> report += "$name: CHANGED -> ${render(before)} => ${render(value)}"
        }
    }

    LOG.d {
        val header = "[$tag] recomposition #${compositions[0]}"
        when {
            report.isEmpty() -> "$header - every tracked value is identical, so the cause is not in this list"
            else -> report.joinToString(prefix = "$header\n  ", separator = "\n  ")
        }
    }
}

private class ChangeCache(size: Int) {
    val previous = arrayOfNulls<Any?>(size)
    var seeded = false
}

private fun render(value: Any?): String {
    val text = value?.toString() ?: "null"
    return if (text.length <= MAX_VALUE_LENGTH) text else text.take(MAX_VALUE_LENGTH) + "..."
}
