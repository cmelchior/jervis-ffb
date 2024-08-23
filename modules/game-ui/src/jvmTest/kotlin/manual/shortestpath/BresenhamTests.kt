package manual.shortestpath

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import dk.ilios.jervis.model.FieldCoordinate
import dk.ilios.jervis.rules.BB2020Rules
import dk.ilios.jervis.utils.createDefaultGameState
import dk.ilios.jervis.utils.createStartingTestSetup
import org.junit.Test

class BresenhamTests {
    @Test
    fun run() {
        application {
            val windowState = rememberWindowState()
            Window(onCloseRequest = ::exitApplication, state = windowState) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    val rules = BB2020Rules
                    val state = createDefaultGameState(rules)
                    createStartingTestSetup(state)
                    val path = remember { mutableStateOf(listOf<FieldCoordinate>()) }
                    BresenhamGrid(
                        rules.fieldHeight.toInt(),
                        rules.fieldWidth.toInt(),
                        path.value,
                        { start: FieldCoordinate, end: FieldCoordinate ->
                            path.value = rules.pathFinder.getStraightLine(state, start, end)
                        },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun BresenhamGrid(
    rows: Int,
    cols: Int,
    path: List<FieldCoordinate>,
    update: (start: FieldCoordinate, end: FieldCoordinate) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        repeat(rows) { y ->
            Row {
                repeat(cols) { x ->
                    val onPath = path.contains(FieldCoordinate(x, y))
                    val bgColor: Color =
                        when {
                            onPath -> Color.Blue
                            else -> Color.White
                        }
                    Box(
                        modifier =
                            Modifier
                                .onPointerEvent(PointerEventType.Enter) {
                                    update(FieldCoordinate(12, 7), FieldCoordinate(x, y))
                                }
                                .size(30.dp)
                                .padding(1.dp)
                                .background(bgColor),
                        contentAlignment = Alignment.Center,
                    ) {
                    }
                }
            }
        }
    }
}
