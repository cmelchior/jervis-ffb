package com.jervisffb.engine.rules.common.tables

/**
 *  Determines in what corner the Random Direction Template is placed.
 *  "Top" is defined as the direction towards 0 on the x-axis for the [com.jervisffb.engine.model.locations.FieldCoordinate]
 *  "Left" is defined as the direction towards 0 on the y-axis for the [com.jervisffb.engine.model.locations.FieldCoordinate]
 */
enum class CornerThrowInPosition(val rotateDegrees: Int) {
    TOP_LEFT(135),
    TOP_RIGHT(-135),
    BOTTOM_RIGHT(-45),
    BOTTOM_LEFT(45),
}
