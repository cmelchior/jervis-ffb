package dk.ilios.jervis.rules.tables

import dk.ilios.jervis.model.locations.FieldCoordinate
import kotlin.math.abs



/**
 * Range Ruler
 *
 * See page 48 in the rulebook.
 */
object RangeRuler {

    val maxDistance: Int = 13

    // Represent the template as a string
    // Credit for this idea: https://github.com/christerk/ffb/blob/660b0e10357b10634827b4ed787f21cc9757b0c2/ffb-common/src/main/java/com/fumbbl/ffb/mechanics/bb2020/PassMechanic.java#L22
    val TEMPlATE = """
        T Q Q Q S S S L L L L B B B
        Q Q Q Q S S S L L L L B B B
        Q Q Q S S S S L L L L B B
        Q Q S S S S S L L L B B B
        S S S S S S L L L L B B B
        S S S S S L L L L B B B
        S S S S L L L L L B B B
        L L L L L L L L B B B
        L L L L L L L B B B B
        L L L L L B B B B B
        L L L B B B B B B
        B B B B B B B
        B B B B B
        B B
    """.trimIndent()

    private val ranges: Array<Array<Range>>

    init {
        // Assume the template is symmetrical
        val lines = TEMPlATE.lines()
        ranges = Array(lines.size) { y: Int ->
            Array(lines.size) { x: Int ->
                when(val type: Char? = lines[y].getOrNull(x * 2)) {
                    'T' -> Range.PASSING_PLAYER
                    'Q' -> Range.QUICK_PASS
                    'S' -> Range.SHORT_PASS
                    'L' -> Range.LONG_PASS
                    'B' -> Range.LONG_BOMB
                    null -> Range.OUT_OF_RANGE
                    else -> error("Unknown range type: '$type' ($x, $y)")
                }
            }
        }
    }

    /**
     * Measure the range between two points on the field as if using a Range Ruler.
     */
    fun measure(origin: FieldCoordinate, target: FieldCoordinate): Range {
        val deltaX: Int = abs(target.x - origin.x)
        val deltaY: Int = abs(target.y - origin.y)
        if ((deltaX < ranges.size) && (deltaY < ranges[deltaX].size)) {
            return ranges[deltaX][deltaY]
        } else {
            return Range.OUT_OF_RANGE
        }
    }
}
