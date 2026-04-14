package manual.shortestpath

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.StandardBB2020Rules
import com.jervisffb.test.bb2020.createDefaultGameStateBB2020
import com.jervisffb.test.bb2020.createStartingTestSetup
import org.junit.Ignore
import org.junit.Test

class DjikstraTests {
    @Test
    @Ignore // Run this manually
    fun run() {
        application {
            val windowState = rememberWindowState()
            Window(onCloseRequest = ::exitApplication, state = windowState) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    DjiekstraContent()
                }
            }
        }
    }
}

@Composable
fun DjiekstraContent() {
    val rules = StandardBB2020Rules()
    val state = createDefaultGameStateBB2020(rules)
    createStartingTestSetup(state)

    val result = rules.pathFinder.calculateAllPaths(state, PitchCoordinate(12, 6), 6)
    val path = remember { mutableStateOf(listOf<PitchCoordinate>()) }
    DjiekstraBoxGrid(
        state,
        rules.pitchHeight,
        rules.pitchWidth,
        result.distances,
        path.value,
        update = { end: PitchCoordinate ->
            val newPath = rules.pathFinder.calculateShortestPath(state, PitchCoordinate(12, 6), end, 4, false)
            path.value = result.getClosestPathTo(end) // newPath.path
        },
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DjiekstraBoxGrid(
    state: Game,
    rows: Int,
    cols: Int,
    distances: Map<PitchCoordinate, Int>,
    path: List<PitchCoordinate>,
    update: (end: PitchCoordinate) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        repeat(rows) { y ->
            Row {
                repeat(cols) { x ->
                    val squareValue: Int? = distances[PitchCoordinate(x, y)]
                    val onPath = path.contains(PitchCoordinate(x, y))
                    val isStart = distances[PitchCoordinate(x, y)] == 0
                    val isOccupied = state.pitch[x, y].isOccupied()
                    val (text: String, bgColor: Color) =
                        when {
                            isStart -> "" to Color.Red
                            isOccupied -> "" to Color.Black
                            onPath -> (squareValue?.toString() ?: "") to Color.Blue
                            else -> (squareValue?.toString() ?: "") to Color.White
                        }
                    Box(
                        modifier =
                            Modifier
                                .onPointerEvent(PointerEventType.Enter) {
                                    update(PitchCoordinate(x, y))
                                }
                                .size(30.dp)
                                .padding(1.dp)
                                .background(bgColor),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = text)
                    }
                }
            }
        }
    }
}
