package com.jervisffb.engine.model.context

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.locations.PitchCoordinate

data class MovePlayerIntoSquareContext(
    val player: Player,
    val target: PitchCoordinate,
    /**
     * Whether to bounce any loose balls as soon as the player enters a square,
     * before they are allowed to pick up the ball. This is e.g. required by
     * Ball & Chain.
     *
     * The bounce when a ball carrier enters another ball's square still happens
     * independently of this flag.
     */
    val bounceLooseBallImmediately: Boolean = false
) : ProcedureContext

