package com.jervisffb.engine.actions

import kotlin.jvm.JvmInline
import kotlin.random.Random

/**
 * Compact representation of a subset of dice faces.
 * Supports between 1 and 20 faces.
 */
@JvmInline
value class DiceFaces private constructor(
    val mask: Int
) {
    fun contains(face: Int): Boolean {
        require(face in 1..MAX_FACES) {
            "Dice face must be between 1 and $MAX_FACES"
        }
        return mask and (1 shl (face - 1)) != 0
    }

    fun isEmpty(): Boolean = (mask == 0)

    fun random(random: Random): Int {
        require(mask != 0) { "Cannot roll a die with no allowed faces" }
        val selected = random.nextInt(mask.countOneBits())
        var remaining = selected
        for (face in 1..MAX_FACES) {
            if (contains(face)) {
                if (remaining == 0) return face
                remaining--
            }
        }
        error("Invalid dice-face mask: $mask")
    }

    companion object {
        private const val MAX_FACES = 20

        fun fromMask(mask: Int) = DiceFaces(mask)

        // Create a DiceFaces containing only the given faces
        fun of(vararg faces: Int): DiceFaces {
            require(faces.isNotEmpty()) { "At least one face must be allowed" }
            var mask = 0
            for (face in faces) {
                require(face in 1..MAX_FACES) {
                    "Dice face must be between 1 and $MAX_FACES"
                }
                mask = mask or (1 shl (face - 1))
            }
            return DiceFaces(mask)
        }

        // Create a DiceFaces containing all faces of a given dice
        fun all(dice: Dice): DiceFaces {
            val sides = dice.sides
            return DiceFaces((1 shl sides) - 1)
        }
    }
}
