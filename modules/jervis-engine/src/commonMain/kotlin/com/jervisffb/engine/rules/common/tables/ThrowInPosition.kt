package com.jervisffb.engine.rules.common.tables

/**
 * Describe on which edge a throw-in template is placed.
 *
 * Note, for corner throw-ins, the [RandomDirectionTemplate] is used, so that
 * case is not covered here.
 */
enum class ThrowInPosition private constructor(val rotateDegrees: Int) {
    TOP(180),
    BOTTOM(0),
    LEFT(90),
    RIGHT(-90),
}
