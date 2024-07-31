package dk.ilios.jervis.ui.test.manual

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
import dk.ilios.jervis.ui.Field
import dk.ilios.jervis.ui.Sidebar
import dk.ilios.jervis.ui.test.manual.dummies.TestDummy
import dk.ilios.jervis.ui.viewmodel.FieldViewModel
import dk.ilios.jervis.ui.viewmodel.SidebarViewModel
import org.junit.Test


class PlayerStatsCard {

    @Test
    fun run() {
        val left = TestDummy.leftSidebar
        val right = TestDummy.rightSidebar
        val field = TestDummy.fieldVieModel
        TestDummy.state.notifyUpdate()
        TestDummy.state.awayTeam.notifyUpdate()
        TestDummy.state.homeTeam.notifyUpdate()
        TestDummy.state.awayTeam.notifyDogoutChange()
        TestDummy.state.homeTeam.notifyDogoutChange()

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
private fun PlayerStatsContent(leftDugout: SidebarViewModel, rightDugout: SidebarViewModel, field: FieldViewModel) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .aspectRatio((152.42f+782f+152.42f)/452f),
        verticalAlignment = Alignment.Top
    ) {
        Sidebar(leftDugout, Modifier.weight(152.42f))
        Field(field, Modifier.weight(782f))
        Sidebar(rightDugout, Modifier.weight(152.42f))
    }
}
