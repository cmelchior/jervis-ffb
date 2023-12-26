package dk.ilios.jervis.ui.model

import androidx.compose.runtime.snapshots.SnapshotStateList
import dk.ilios.jervis.model.Field
import dk.ilios.jervis.model.FieldSquare
import dk.ilios.jervis.model.Player
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class Square(val x: Int, val y: Int)

enum class FieldDetails(val resource: String, val description: String) {
    NICE("icons/cached/pitches/default/nice.png", "Nice Weather")
}

class FieldViewModel(private val state: Field) {
    val aspectRatio: Float = 782f/452f
    val width = 26
    val height = 15

    private val field = MutableStateFlow(FieldDetails.NICE)
    private val highlights = SnapshotStateList<Square>()
    private val _highlights = MutableStateFlow<Square?>(null)
    fun field(): StateFlow<FieldDetails> = field
    fun observeSquare(x: Int, y: Int): Flow<FieldSquare> {
        return state[x, y].squareFlow
    }
    fun highlights(): StateFlow<Square?> = _highlights

    fun hoverOver(square: Square) {
        _highlights.value = square
    }
}