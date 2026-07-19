package com.jervisffb.ui.game.mappings

/**
 * Interface used to map from engine types to UI types.
 *
 * Developer's Commentary:
 * Still not sure if this is the best approach. It seems to work for rerolls, but
 * unclear if it works for other things.
 */
interface UiMapping<T: Any, U: Enum<*>> {
    fun mapFrom(el: T): U
} 
