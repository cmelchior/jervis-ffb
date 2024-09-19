package manual.shortestpath

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import dk.ilios.jervis.model.locations.FieldCoordinate
import dk.ilios.jervis.rules.BB2020Rules
import dk.ilios.jervis.rules.pathfinder.BB2020PathFinder
import dk.ilios.jervis.rules.pathfinder.PathFinder
import dk.ilios.jervis.utils.createDefaultGameState
import dk.ilios.jervis.utils.createStartingTestSetup
import org.junit.Test
import kotlin.let
import kotlin.text.format
import kotlin.to

class AStarTests {
    @Test
    fun run() {
        application {
            val windowState = rememberWindowState()
            Window(onCloseRequest = ::exitApplication, state = windowState) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    AStarContent()
                }
            }
        }
    }
}

@Composable
fun AStarContent() {
    val rules = BB2020Rules
    val state = createDefaultGameState(rules)
    createStartingTestSetup(state)

    val result = rules.pathFinder.calculateShortestPath(state, FieldCoordinate(12, 6), FieldCoordinate(0, 14), 4, true)
    when (result) {
        is PathFinder.Failure -> {
            (result.debugInformation as BB2020PathFinder.DebugInformation).let {
                BoxGrid(
                    rules.fieldHeight.toInt(),
                    rules.fieldWidth.toInt(),
                    it.fieldView,
                    it.gScore,
                    result.path,
                )
            }
        }

        is PathFinder.Success -> {
            (result.debugInformation as BB2020PathFinder.DebugInformation).let {
                BoxGrid(
                    rules.fieldHeight.toInt(),
                    rules.fieldWidth.toInt(),
                    it.fieldView,
                    it.gScore,
                    result.path,
                )
            }
        }
    }
}

@Composable
fun BoxGrid(
    rows: Int,
    cols: Int,
    field: Array<Array<Int>>,
    gScore: Map<FieldCoordinate, Double>,
    path: List<FieldCoordinate>,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        repeat(rows) { y ->
            Row {
                repeat(cols) { x ->
                    val onPath = path.contains(FieldCoordinate(x, y))
                    val squareValue = field[x][y]
                    val (text: String, bgColor: Color) =
                        when {
                            onPath -> gScore[FieldCoordinate(x, y)].formatToString(1) to Color.Blue
                            squareValue == Int.MAX_VALUE -> "" to Color.Black
                            squareValue > 0 -> "($squareValue)" to Color.LightGray
                            squareValue == 0 -> gScore[FieldCoordinate(x, y)].formatToString(1) to Color.White
                            else -> "" to Color.Red
                        }
                    Box(
                        modifier =
                            Modifier
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

private fun Double?.formatToString(decimals: Int): String {
    return if (this != null) {
        "%.${decimals}f".format(this)
    } else {
        ""
    }
}
