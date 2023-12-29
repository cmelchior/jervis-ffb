package dk.ilios.jervis.model

import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.utils.INVALID_GAME_STATE
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class FieldSquare(private val coordinate: FieldCoordinate): Observable<FieldSquare>(), Location {
    constructor(x: Int, y: Int): this(FieldCoordinate(x, y))

    val x = coordinate.x
    val y = coordinate.y
    var player: Player? by observable(null)
    var ball: Ball? by observable(null)

    fun isEmpty(): Boolean = (player == null)
    fun isOnTeamHalf(team: Team, rules: Rules): Boolean {
        return if (team.isHomeTeam()) isOnHomeSide(rules) else isOnAwaySide(rules)
    }
    val squareFlow: SharedFlow<FieldSquare> = observeState
    override fun isOnLineOfScrimmage(rules: Rules): Boolean = coordinate.isOnLineOfScrimmage(rules)
    override fun isInWideZone(rules: Rules): Boolean = coordinate.isInWideZone(rules)
    override fun isInEndZone(rules: Rules): Boolean = coordinate.isInEndZone(rules)
    override fun isInCenterField(rules: Rules): Boolean = coordinate.isInCenterField(rules)
    override fun isOnHomeSide(rules: Rules): Boolean = coordinate.isOnHomeSide(rules)
    override fun isOnAwaySide(rules: Rules): Boolean = coordinate.isOnAwaySide(rules)
    override fun isOnField(rules: Rules): Boolean = coordinate.isOnField(rules)
    override fun isOutOfBounds(rules: Rules): Boolean = false
}

class Field(width: UInt, height: UInt): Iterable<FieldSquare> {

    private val field: Array<Array<FieldSquare>> = Array(width.toInt()) { x: Int ->
        Array(height.toInt()) {y: Int ->
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

    private inline fun assertEmptySquare(x: Int, y: Int) {
        if (field[x][y].player != null) {
            INVALID_GAME_STATE("Cannot add player to location: ($x, $y)")
        }
    }

    override fun iterator(): Iterator<FieldSquare> {
        return object: Iterator<FieldSquare> {
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
        _fieldState.tryEmit(this)
    }
    private val _fieldState = MutableSharedFlow<Field>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val fieldFlow: SharedFlow<Field> = _fieldState

    companion object {
        fun createForRuleset(rules: Rules): Field = Field(rules.fieldWidth, rules.fieldHeight)
    }



}