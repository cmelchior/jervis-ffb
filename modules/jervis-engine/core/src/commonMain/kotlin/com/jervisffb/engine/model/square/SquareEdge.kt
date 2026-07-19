package com.jervisffb.engine.model.square

import com.jervisffb.engine.model.PitchSquare
import kotlinx.serialization.Serializable

/**
 * Class wrapping the behavior of edges for a given [PitchSquare].
 * @see [PitchSquare.edges]
 */
@Serializable
data class SquareEdge(
    var left: SquareEdgeType = SquareEdgeType.OPEN,
    var top: SquareEdgeType = SquareEdgeType.OPEN,
    var right: SquareEdgeType = SquareEdgeType.OPEN,
    var bottom: SquareEdgeType = SquareEdgeType.OPEN,
)
