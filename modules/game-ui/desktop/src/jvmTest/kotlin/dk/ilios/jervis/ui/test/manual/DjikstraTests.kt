package dk.ilios.jervis.ui.test.manual

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
import dk.ilios.jervis.model.FieldCoordinate
import dk.ilios.jervis.rules.BB2020Rules
import dk.ilios.jervis.utils.createDefaultGameState
import dk.ilios.jervis.utils.createStartingTestSetup
import org.junit.Test


class DjikstraTests {
    @Test
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
    val rules = BB2020Rules
    val state = createDefaultGameState(rules)
    createStartingTestSetup(state)

    val result = rules.pathFinder.calculateAllPaths(state, FieldCoordinate(12, 6))
    DjiekstraBoxGrid(
        rules.fieldHeight.toInt(),
        rules.fieldWidth.toInt(),
        result.distances
    )
}

@Composable
fun DjiekstraBoxGrid(rows: Int, cols: Int, distances: Map<FieldCoordinate, Int>) {
    Column(modifier = Modifier.fillMaxSize()) {
        repeat(rows) { y ->
            Row {
                repeat(cols) { x ->
                    val squareValue: Int? = distances[FieldCoordinate(x, y)]
                    val (text: String, bgColor: Color) = when {
//                        fieldView[x][y] == Int.MAX_VALUE -> "" to Color.Black
//                        fieldView[x][y] > 0 -> fieldView[x][y].toString() to Color.Gray
                        else -> (squareValue?.toString() ?: "") to Color.White
                    }
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .padding(1.dp)
                            .background(bgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = text)
                    }
                }
            }
        }
    }
}
