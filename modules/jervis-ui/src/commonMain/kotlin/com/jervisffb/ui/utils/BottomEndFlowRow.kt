package com.jervisffb.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Bottom-right FlowRow:
 * - Packs items right → left within a row.
 * - Rows stack bottom → top (the *partial* row ends up at the top).
 *
 * Developer's Commentary:
 * ChatGPT created this layout, so advanced usage probably requires more testing.
 */
@Composable
fun BottomEndFlowRow(
    modifier: Modifier = Modifier,
    horizontalSpacing: Dp = 0.dp,
    verticalSpacing: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    Layout(modifier = modifier, content = content) { measurables, constraints ->
        val hSpace = with(density) { horizontalSpacing.roundToPx() }.coerceAtLeast(0)
        val vSpace = with(density) { verticalSpacing.roundToPx() }.coerceAtLeast(0)

        val childConstraints = constraints.copy(minWidth = 0, minHeight = 0)
        val placeables = measurables.map { it.measure(childConstraints) }
        val maxW = constraints.maxWidth.coerceAtLeast(0)

        data class Row(val indices: MutableList<Int>, var height: Int, var width: Int)

        val rows = mutableListOf<Row>() // bottom row first
        var row = Row(mutableListOf(), height = 0, width = 0)

        fun commitRow() {
            if (row.indices.isNotEmpty()) {
                rows += row
                row = Row(mutableListOf(), height = 0, width = 0)
            }
        }

        // Build rows from the *end* so the bottom rows are packed first.
        for (i in placeables.indices.reversed()) {
            val p = placeables[i]
            val addW = if (row.indices.isEmpty()) p.width else hSpace + p.width
            if (row.indices.isEmpty() || row.width + addW <= maxW) {
                if (row.indices.isNotEmpty()) row.width += hSpace
                row.indices += i
                row.width += p.width
                row.height = maxOf(row.height, p.height)
            } else {
                commitRow()
                row.indices += i
                row.width = p.width
                row.height = p.height
            }
        }
        commitRow()

        val usedHeight = if (rows.isEmpty()) 0
        else rows.sumOf { it.height } + vSpace * (rows.size - 1)
        val layoutW = maxW
        val layoutH = when {
            constraints.hasBoundedHeight -> constraints.maxHeight.coerceAtLeast(usedHeight)
            else -> usedHeight.coerceIn(constraints.minHeight, Int.MAX_VALUE)
        }

        layout(layoutW, layoutH) {
            var yBottom = layoutH
            rows.forEachIndexed { rowIdx, r ->
                val y = yBottom - r.height
                var xRight = layoutW
                // r.indices currently hold child indices in reverse input order.
                // Place right→left following that order (append-at-bottom puts latest at far right).
                r.indices.forEachIndexed { idxInRow, childIndex ->
                    val p = placeables[childIndex]
                    xRight -= p.width
                    p.placeRelative(xRight, y + (r.height - p.height))
                    if (idxInRow != r.indices.lastIndex) xRight -= hSpace
                }
                yBottom = y - if (rowIdx != rows.lastIndex) vSpace else 0
            }
        }
    }
}
