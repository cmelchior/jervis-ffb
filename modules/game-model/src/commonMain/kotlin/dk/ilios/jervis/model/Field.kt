package dk.ilios.jervis.model

import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.utils.INVALID_GAME_STATE
import dk.ilios.jervis.utils.safeTryEmit
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class Field(val width: Int, val height: Int) : Iterable<FieldSquare> {
    private val field: Array<Array<FieldSquare>> =
        Array(width) { x: Int ->
            Array(height) { y: Int ->
                FieldSquare(x, y)
            }
        }

    operator fun get(
        x: Int,
        y: Int,
    ): FieldSquare = field[x][y]

    operator fun get(coordinate: FieldCoordinate): FieldSquare = field[coordinate.x][coordinate.y]

    fun addPlayer(
        player: Player,
        x: Int,
        y: Int,
    ) {
        assertEmptySquare(x, y)
        field[x][y].player = player
    }

    fun removePlayer(
        x: Int,
        y: Int,
    ): Player {
        val player: Player = field[x][y].player ?: INVALID_GAME_STATE("No player could be removed at: ($x, $y)")
        return player
    }

    private fun assertEmptySquare(
        x: Int,
        y: Int,
    ) {
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

    fun notifyFieldChange() {
        // TODO Snapshot the field?
        _fieldState.safeTryEmit(this)
    }

    private val _fieldState = MutableSharedFlow<Field>(replay = 1, onBufferOverflow = BufferOverflow.SUSPEND)
    val fieldFlow: SharedFlow<Field> = _fieldState

    companion object {
        fun createForRuleset(rules: Rules): Field = Field(rules.fieldWidth, rules.fieldHeight)
    }
}
