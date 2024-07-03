package dk.ilios.jervis.ui.viewmodel

import androidx.compose.runtime.snapshots.SnapshotStateList
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.PlayerSelected
import dk.ilios.jervis.model.Field
import dk.ilios.jervis.model.FieldSquare
import dk.ilios.jervis.ui.model.UiBall
import dk.ilios.jervis.ui.model.UiFieldSquare
import dk.ilios.jervis.ui.model.UiPlayer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine

data class Square(val x: Int, val y: Int)

enum class FieldDetails(val resource: String, val description: String) {
    NICE("icons/cached/pitches/default/nice.png", "Nice Weather")
}

class FieldViewModel(private val uiActionFactory: UiActionFactory, private val state: Field) {
    val aspectRatio: Float = 782f/452f
    val width = 26
    val height = 15

    private val field = MutableStateFlow(FieldDetails.NICE)
    private val highlights = SnapshotStateList<Square>()
    private val _highlights = MutableStateFlow<Square?>(null)
    fun field(): StateFlow<FieldDetails> = field
    fun observeSquare(x: Int, y: Int): Flow<UiFieldSquare> {
        return state[x, y].squareFlow.combine(uiActionFactory.fieldActions) { e1: FieldSquare, e2: UserInput ->
            val squareAction = if (e2 is SelectFieldLocationInput) {
                val ac = e2.fieldActions[Pair(x, y)]
                ac
            } else {
                null
            }
            val squareSelectAction = if (squareAction != null) {
                { uiActionFactory.userSelectedAction(squareAction) }
            } else {
                null
            }

            val playerSelectedAction: (() -> Unit)? = if (e2 is SelectPlayerInput) {
                e1.player?.let {
                    e2.actions.firstOrNull { (it as PlayerSelected).player == e1.player }?.let { playerAction: GameAction ->
                        { uiActionFactory.userSelectedAction(playerAction) }
                    }
                }
            } else {
                null
            }
            if (squareSelectAction != null) {
//                println("Adding onClick to ($x, $y)")
            } else if (e2 is SelectFieldLocationInput && squareAction == null && x < 13) {
//                println("Not adding to ($x, $y)")
            }
            UiFieldSquare(
                e1,
                e1.ball?.let { UiBall(it) },
                e1.player?.let { UiPlayer(it, playerSelectedAction) },
                squareSelectAction,
            )
        }
    }
    fun highlights(): StateFlow<Square?> = _highlights

    fun hoverOver(square: Square) {
        _highlights.value = square
    }
}