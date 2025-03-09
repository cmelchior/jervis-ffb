package com.jervisffb.ui.menu.utils

import com.jervisffb.ui.menu.p2p.host.DropdownEntry

data class DropdownEntryWithValue<T>(
    override val name: String,
    val value: T,
    override val available: Boolean = true,
): DropdownEntry

// Helper function, making it easier to locate a dropdown entry based on its value
fun <T> List<Pair<String, List<DropdownEntryWithValue<T>>>>.findEntry(value: T): DropdownEntryWithValue<T> {
    for (section in this) {
        val en = section.second.find { it.value == value }
        if (en != null) {
            return en
        }
    }
    throw IllegalArgumentException("Unable to find entry for value: $value")
}
