package dk.ilios.jervis.ui.viewmodel

import androidx.compose.runtime.snapshots.SnapshotStateList
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.PlayerSelected
import dk.ilios.jervis.model.Ball
import dk.ilios.jervis.model.Field
import dk.ilios.jervis.model.FieldSquare
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.ui.model.UiFieldSquare
import dk.ilios.jervis.ui.model.UiPlayer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

data class Square(val x: Int, val y: Int)

enum class FieldDetails(val resource: String, val description: String) {
    NICE("icons/cached/pitches/default/nice.png", "Nice Weather")
}

data class SquareData(val field: FieldSquare, val player: Player?, val ball: Ball?) {}

/**
 * This class should only concern itself with the high-level things, like players moving in and out, ball moving
 * in and out.
 *
 * PlayerViewModel and BallViewModel should handle state changes...is this true? Wh
 */
class FieldViewModel(private val uiActionFactory: UiActionFactory, private val state: Field) {
    val aspectRatio: Float = 782f/452f
    val width = 26
    val height = 15

    private val field = MutableStateFlow(FieldDetails.NICE)
    private val highlights = SnapshotStateList<Square>()
    private val _highlights = MutableStateFlow<Square?>(null)
    fun field(): StateFlow<FieldDetails> = field
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeSquare(x: Int, y: Int): Flow<UiFieldSquare> {
        val flow: Flow<UiFieldSquare> = state[x, y].squareFlow.flatMapLatest {
            combine(
                flow = flowOf(it),
                flow2 = it.player?.observePlayer ?: flowOf<Player?>(null),
                flow3 = it.ball?.observeBall ?: flowOf<Ball?>(null),
                flow4 = uiActionFactory.fieldActions,
                transform = { field: FieldSquare, player: Player?, ball: Ball?, userInput: UserInput ->
                    val squareAction = if (userInput is SelectFieldLocationInput) {
                        val ac = userInput.fieldActions[Pair(x, y)]
                        ac
                    } else {
                        null
                    }
                    val squareSelectAction = if (squareAction != null) {
                        { uiActionFactory.userSelectedAction(squareAction) }
                    } else {
                        null
                    }

                    val playerSelectedAction: (() -> Unit)? = if (userInput is SelectPlayerInput) {
                        field.player?.let {
                            userInput.actions.firstOrNull { (it as PlayerSelected).player == field.player }
                                ?.let { playerAction: GameAction ->
                                    { uiActionFactory.userSelectedAction(playerAction) }
                                }
                        }
                    } else {
                        null
                    }
                    if (squareSelectAction != null) {
                        //                println("Adding onClick to ($x, $y)")
                    } else if (userInput is SelectFieldLocationInput && squareAction == null && x < 13) {
                        //                println("Not adding to ($x, $y)")
                    }
                    UiFieldSquare(
                        field,
                        ball?.state,
                        player?.let { UiPlayer(it, playerSelectedAction) },
                        squareSelectAction,
                    )
                }
            )
        }
        return flow
    }
    fun highlights(): StateFlow<Square?> = _highlights

    fun hoverOver(square: Square) {
        _highlights.value = square
    }
}