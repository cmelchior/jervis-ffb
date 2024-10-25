package com.jervisffb.ui

import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.bb2020.tables.CasualtyResult
import com.jervisffb.fumbbl.net.model.BloodSpot
import org.jetbrains.compose.resources.DrawableResource

data class MoveUsed(val coordinate: FieldCoordinate, val value: Int)
data class BloodSpot(val coordinate: FieldCoordinate, val injury: CasualtyResult, val icon: DrawableResource)

/**
 * Tracking persistent state that is only relevant for the UI.
 */
class UiGameState {

    // State used to track UI decorators for things that are not tracked
    // in the rules engine layer.
//    val bloodspots: MutableList<BloodSpot> = mutableListOf()
//    val movesUsed: MutableList<MoveUsed> = mutableListOf()

    val blodspots: MutableMap<FieldCoordinate, BloodSpot> = mutableMapOf()
    val movesUsed: MutableMap<FieldCoordinate, MoveUsed> = mutableMapOf()
}

