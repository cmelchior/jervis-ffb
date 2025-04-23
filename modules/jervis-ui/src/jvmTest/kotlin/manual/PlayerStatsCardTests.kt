package manual

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.jervisffb.ui.game.view.Field
import com.jervisffb.ui.game.view.Sidebar
import com.jervisffb.ui.game.viewmodel.FieldViewModel
import com.jervisffb.ui.game.viewmodel.SidebarViewModel
import manual.dummies.TestDummy
import org.junit.Test
import kotlin.test.Ignore

class PlayerStatsCardTests {
    @Test
    @Ignore // Run this manually
    fun run() {
        val left = TestDummy.leftSidebar
        val right = TestDummy.rightSidebar
        val field = TestDummy.fieldVieModel

        application {
            val windowState = rememberWindowState()
            Window(onCloseRequest = ::exitApplication, state = windowState) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    PlayerStatsContent(left, right, field)
                }
            }
        }
    }
}

@Composable
private fun PlayerStatsContent(
    leftDugout: SidebarViewModel,
    rightDugout: SidebarViewModel,
    field: FieldViewModel,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .aspectRatio((152.42f + 782f + 152.42f) / 452f),
        verticalAlignment = Alignment.Top,
    ) {
        Sidebar(leftDugout, Modifier.weight(152.42f))
        Field(field)
        Sidebar(rightDugout, Modifier.weight(152.42f))
    }
}
