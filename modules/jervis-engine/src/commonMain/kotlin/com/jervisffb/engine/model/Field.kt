package com.jervisffb.engine.model

import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * Internal representation of the Blood Bowl playing area. This representation
 * is used across all game types: Standard, BB7, Dungeon Bowl and Gutter Bowl.
 *
 * Going outside the defined field is considered out-of-bounds. Add walls to
 * all border squares to prevent this.
 *
 * Top-left has the coordinates [0, 0] and Bottom-right has the coordinates
 * [ fieldWidth - 1, fieldHeight - 1].
 *
 * Standard Blood Bowl / BB 7:
 * - Field is laid out horizontally. With the home team on the left side and the
 *   away team on the right side
 *
 * Dungeon Ball
 * - The width/height defines the area where within rooms and corridors are placed.
 *   The UI can easily define this after rooms have been placed by creating a
 *   minimal bounding box.
 * - Walls between two squares need to be added to both squares.
 * - The model doesn't care about how to render the room layouts. So each square
 *   defines its own "Tile Type" and leaves it up to the UI to figure out how to display it.
 *   This is intended as we assume the user will either build the dungeon from the UI or use
 *   a pre-build one, which should ensure that only "valid" dungeons, i.e. dungeons that can
 *   be displayed are built.
 * - To make it easier to send dungeon layouts to all parties, we probably need to add some
 *   metadata to [Field]. Right now that is TBD.
 *
 * Gutter Bowl:
 * - Field is laid out horizontally. With the home team on the left side and the
 *   away team on the right side.
 * - All squares on the border have a wall, preventing balls and players from going
 *   out-of-bounds.
 */
class Field(val width: Int, val height: Int) : Iterable<FieldSquare> {
    private val field: Array<Array<FieldSquare>> =
        Array(width) { x: Int ->
            Array(height) { y: Int ->
                FieldSquare(x, y)
            }
        }

    operator fun get(x: Int, y: Int): FieldSquare = field[x][y]
    operator fun get(coordinate: FieldCoordinate): FieldSquare = field[coordinate.x][coordinate.y]

    fun addPlayer(player: Player, x: Int, y: Int) {
        assertEmptySquare(x, y)
        field[x][y].player = player
    }

    fun removePlayer(x: Int, y: Int): Player {
        val player: Player = field[x][y].player ?: INVALID_GAME_STATE("No player could be removed at: ($x, $y)")
        return player
    }

    private fun assertEmptySquare(x: Int, y: Int) {
        if (field[x][y].player != null) {
            INVALID_GAME_STATE("Cannot add player to location: ($x, $y)")
        }
    }

    override fun iterator(): Iterator<FieldSquare> {
        return object : Iterator<FieldSquare> {
            private var rowIndex = 0
            private var colIndex = 0

            override fun hasNext(): Boolean {
                return rowIndex < field.size && colIndex < field[rowIndex].size
            }

            override fun next(): FieldSquare {
                val nextSquare = field[rowIndex][colIndex]
                colIndex++
                if (colIndex >= field[rowIndex].size) {
                    colIndex = 0
                    rowIndex++
                }
                return nextSquare
            }
        }
    }

    companion object {
        fun createForRuleset(rules: Rules): Field = Field(rules.fieldWidth, rules.fieldHeight)
    }
}
