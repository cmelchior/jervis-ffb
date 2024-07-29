package dk.ilios.jervis.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import dk.ilios.jervis.ui.model.UiPlayer
import dk.ilios.jervis.ui.model.UiPlayerCard
import dk.ilios.jervis.ui.viewmodel.SidebarView
import dk.ilios.jervis.ui.viewmodel.SidebarViewModel
import kotlinx.coroutines.flow.Flow

@Composable
fun Sidebar(vm: SidebarViewModel, modifier: Modifier) {
    Box(modifier = modifier.aspectRatio(vm.aspectRatio).fillMaxSize()) {
        Image(
            alignment = Alignment.TopStart,
            painter = painterResource("icons/sidebar/background_box.png"),
            contentDescription = "Box",
            modifier = modifier.fillMaxSize()
        )

        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {

                val view by vm.view().collectAsState()
                when(view) {
                    SidebarView.RESERVES -> Reserves(vm.reserves())
                    SidebarView.INJURIES -> Injuries(
                        vm.knockedOut(),
                        vm.badlyHurt(),
                        vm.seriousInjuries(),
                        vm.dead()
                    )
                }

                // Make sure player stats are shown on top of reserves
                PlayerStatsCard(vm.hoverPlayer())
            }

            Row {
                Button(
                    onClick = { vm.toggleReserves() },
                    colors = FumbblButtonColors(),
                    modifier = Modifier.weight(1f),
                ) {
                    val reserveCount by vm.reserveCount().collectAsState()
                    Text(text = "$reserveCount Rsv", maxLines = 1)
                }
                Button(
                    onClick = { vm.toggleInjuries() },
                    colors = FumbblButtonColors(),
                    modifier = Modifier.weight(1f)
                ) {
                    val injuriesCount by vm.injuriesCount().collectAsState()
                    Text(text = "$injuriesCount Out", maxLines = 1)
                }
            }
        }
    }
}

@Composable
fun PlayerStatsCard(flow: Flow<UiPlayerCard?>) {
    val player by flow.collectAsState(null)
    if (player != null) {
        Column(modifier = Modifier.fillMaxSize().background(color = Color.White)) {
            Text("Hello: ${player?.model?.name}")
        }
    }
}


@Composable
fun Reserves(reserves: Flow<List<UiPlayer>>) {
    val state: List<UiPlayer> by reserves.collectAsState(emptyList())
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader("Reserves")
        for (index in state.indices step 5) {
            Row {
                val modifier = Modifier.weight(1f).aspectRatio(1f)
                repeat(5) { x ->
                    if (index + x < state.size) {
                        Player(modifier, state[index + x], false)
                    } else {
                        // Use empty box. Unsure if we can remove this
                        // if we want a partial row to scale correctly.
                        Box(modifier = modifier)
                    }
                }
            }
        }
    }
}

@Composable
fun Injuries(
    knockedOut: SnapshotStateList<UiPlayer>,
    badlyHurt: SnapshotStateList<UiPlayer>,
    seriousInjuries: SnapshotStateList<UiPlayer>,
    dead: SnapshotStateList<UiPlayer>
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader("Knocked Out")
        SectionHeader("Badly Hurt")
        SectionHeader("Seriously Injured")
        SectionHeader("Killed")
        SectionHeader("Banned")
    }
}
