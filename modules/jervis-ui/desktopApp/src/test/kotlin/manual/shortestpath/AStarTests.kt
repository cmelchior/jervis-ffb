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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.jervisffb.engine.bb2020.StandardBB2020Rules
import com.jervisffb.engine.common.pathfinder.CommonPathFinder
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.common.pathfinder.PathFinder
import com.jervisffb.fumbbl.net.model.PlayerType
import com.jervisffb.test.bb2020.HUMAN_BLITZER
import com.jervisffb.test.bb2020.createDefaultGameStateBB2020
import com.jervisffb.test.bb2020.createStartingTestSetup
import org.junit.Test
import kotlin.test.Ignore

class AStarTests {
    @Test
    @Ignore // Run this manually
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
    val rules = StandardBB2020Rules()
    val state = createDefaultGameStateBB2020(rules)
    createStartingTestSetup(state)
    val player = Player(
        rules = rules,
        id = PlayerId("0"),
        position = HUMAN_BLITZER,
        icon = null,
        type = com.jervisffb.engine.model.PlayerType.STANDARD
    ).also {
        it.location = PitchCoordinate(12, 6)
    }
    val result = state.rulesContext.actionPlanner.pathFinder.calculateShortestPath(state, player, PitchCoordinate(0, 14), 4, true)
    when (result) {
        is PathFinder.Failure -> {
            (result.debugInformation as CommonPathFinder.DebugInformation).let {
                BoxGrid(
                    rules.pitchHeight,
                    rules.pitchWidth,
                    it.pitchView,
                    it.gScore,
                    result.path,
                )
            }
        }

        is PathFinder.Success -> {
            (result.debugInformation as CommonPathFinder.DebugInformation).let {
                BoxGrid(
                    rules.pitchHeight,
                    rules.pitchWidth,
                    it.pitchView,
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
    pitch: Array<Array<Int>>,
    gScore: Map<PitchCoordinate, Double>,
    path: List<PitchCoordinate>,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        repeat(rows) { y ->
            Row {
                repeat(cols) { x ->
                    val onPath = path.contains(PitchCoordinate(x, y))
                    val squareValue = pitch[x][y]
                    val (text: String, bgColor: Color) =
                        when {
                            onPath -> gScore[PitchCoordinate(x, y)].formatToString(1) to Color.Blue
                            squareValue == Int.MAX_VALUE -> "" to Color.Black
                            squareValue > 0 -> "($squareValue)" to Color.LightGray
                            squareValue == 0 -> gScore[PitchCoordinate(x, y)].formatToString(1) to Color.White
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
