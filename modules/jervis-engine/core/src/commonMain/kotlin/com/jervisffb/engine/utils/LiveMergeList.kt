package com.jervisffb.engine.utils

/**
 * A list exposing the underlying lists as a single list without copying
 * any elements.
 *
 * WARNING: This is a live view, so changes to the underlying lists will
 * be reflected immediately in this list. This class is NOT thread-safe.
 */
class LiveMergeList<T>(
    private val first: List<T>,
    private val second: List<T>,
) : AbstractList<T>(), RandomAccess {

    override val size: Int
        get() = first.size + second.size

    override fun get(index: Int): T = when {
        index < 0 || index >= size -> throw IndexOutOfBoundsException("Index: $index, size: $size")
        index < first.size -> first[index]
        else -> second[index - first.size]
    }
}
