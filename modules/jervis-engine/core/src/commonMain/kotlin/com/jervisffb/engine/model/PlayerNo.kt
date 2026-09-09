package com.jervisffb.engine.model

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
@JvmInline
value class PlayerNo(val value: Int) : Comparable<PlayerNo> {
    override fun compareTo(other: PlayerNo): Int {
        return when {
            (value == other.value) -> 0
            (value < other.value) -> -1
            else -> 1
        }
    }

    override fun toString(): String = value.toString()
}
