package dk.ilios.jervis.model

import dk.ilios.jervis.rules.Rules

/**
 * Top-left is (0,0), bottom-left is (25, 14) for a normal Blood Bowl Field.
 */
sealed interface Location {
    fun isOnLineOfScrimmage(rules: Rules): Boolean
    fun isInWideZone(rules: Rules): Boolean
    fun isInEndZone(rules: Rules): Boolean
    fun isInCenterField(rules: Rules): Boolean
    fun isOnHomeSide(rules: Rules): Boolean
    fun isOnAwaySide(rules: Rules): Boolean
    fun isOnField(): Boolean
}
// (0, 0) is (top, left)
data class FieldCoordinate(val x: Int, val y: Int): Location {
    override fun isOnLineOfScrimmage(rules: Rules): Boolean {
        return x == rules.lineOfScrimmageHome || x == rules.lineOfScrimmageAway
    }
    override fun isInWideZone(rules: Rules): Boolean {
        return (0u until rules.wideZone).contains(y.toUInt())
                || (rules.fieldHeight - rules.wideZone until rules.fieldHeight).contains(y.toUInt())
    }
    override fun isInEndZone(rules: Rules): Boolean {
        return x == 0 || x.toUInt() == rules.fieldWidth - 1u
    }
    override fun isInCenterField(rules: Rules): Boolean {
        val xRange = (rules.endZone until rules.fieldWidth - rules.endZone)
        val yRange = (rules.wideZone until rules.fieldHeight - rules.wideZone)
        return xRange.contains(x.toUInt()) && yRange.contains(y.toUInt())
    }
    override fun isOnHomeSide(rules: Rules): Boolean {
        return x.toUInt() < rules.fieldWidth / 2u
    }
    override fun isOnAwaySide(rules: Rules): Boolean {
        return x.toUInt() >= rules.fieldWidth / 2u
    }
    override fun isOnField(): Boolean {
        return true
    }
}
data object DogOut: Location {
    override fun isOnLineOfScrimmage(rules: Rules): Boolean = false
    override fun isInWideZone(rules: Rules): Boolean = false
    override fun isInEndZone(rules: Rules): Boolean = false
    override fun isInCenterField(rules: Rules): Boolean = false
    override fun isOnHomeSide(rules: Rules): Boolean = false
    override fun isOnAwaySide(rules: Rules): Boolean = false
    override fun isOnField(): Boolean = false
}


