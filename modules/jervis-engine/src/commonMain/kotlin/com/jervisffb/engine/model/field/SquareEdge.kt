package com.jervisffb.engine.model.field

import kotlinx.serialization.Serializable

/**
 * Class wrapping the behavior of edges for a given [FieldSquare].
 * @see [FieldSquare.edges]
 */
@Serializable
data class SquareEdge(
    var left: SquareEdgeType = SquareEdgeType.OPEN,
    var top: SquareEdgeType = SquareEdgeType.OPEN,
    var right: SquareEdgeType = SquareEdgeType.OPEN,
    var bottom: SquareEdgeType = SquareEdgeType.OPEN,
)
