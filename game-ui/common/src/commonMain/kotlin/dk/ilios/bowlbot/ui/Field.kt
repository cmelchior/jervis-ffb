package dk.ilios.bowlbot.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.ButtonColors
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dk.ilios.bloodbowl.ui.model.FieldDetails
import dk.ilios.bloodbowl.ui.model.FieldViewModel
import dk.ilios.bloodbowl.ui.model.SidebarView
import dk.ilios.bloodbowl.ui.model.SidebarViewModel
import dk.ilios.bloodbowl.ui.model.Square
import dk.ilios.bloodbowl.ui.model.UIPlayer
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.State
import dk.ilios.bloodbowl.ui.model.ActionSelectorViewModel
import dk.ilios.bloodbowl.ui.model.LogViewModel
import dk.ilios.bloodbowl.ui.model.ReplayViewModel

// Theme
val debugBorder = BorderStroke(2.dp,Color.Red)

data class FumbblButtonColors(
    private val backgroundColor: Color = Color.Gray,
    private val contentColor: Color = Color.White,
    private val disabledBackgroundColor: Color = Color.DarkGray,
    private val disabledContentColor: Color = Color.White
) : ButtonColors {
    @Composable
    override fun backgroundColor(enabled: Boolean): State<Color> {
        return rememberUpdatedState(if (enabled) backgroundColor else disabledBackgroundColor)
    }
    @Composable
    override fun contentColor(enabled: Boolean): State<Color> {
        return rememberUpdatedState(if (enabled) contentColor else disabledContentColor)
    }
}

@Composable
fun SectionDivider(modifier: Modifier) {
    Box(
        modifier = modifier
            .height(10.dp)
            .shadow(1.dp)
            .padding(4.dp)
            .background(color = Color.White)
    )
}

@Composable
fun SectionHeader(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SectionDivider(modifier = Modifier.weight(1f))
        Text(
            text = title,
            color = Color.White,
            maxLines = 1,
            modifier = Modifier.wrapContentSize().shadow(2.dp),

        )
        SectionDivider(modifier = Modifier.weight(1f))
    }
}

@Composable
fun Reserves(reserves: SnapshotStateList<UIPlayer>) {
    val state: SnapshotStateList<UIPlayer> = remember { reserves }
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader("Reserves")
    }
}

@Composable
fun Injuries() {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader("Knocked Out")
        SectionHeader("Badly Hurt")
        SectionHeader("Seriously Injured")
        SectionHeader("Killed")
        SectionHeader("Banned")
    }
}

@Composable
fun Sidebar(vm: SidebarViewModel, modifier: Modifier) {
    Column(modifier = modifier) {
        Box(modifier = Modifier.aspectRatio(145f/430f)) {
            Image(
                painter = painterResource("icons/sidebar/background_box.png"),
                contentDescription = "Box",
                modifier = modifier.fillMaxSize()
            )
            val view by vm.view().collectAsState()
            when(view) {
                SidebarView.RESERVES -> Reserves(vm.reserves())
                SidebarView.INJURIES -> Injuries()
            }
        }
        Row {
            Button(
                onClick = { vm.toggleReserves() },
                colors = FumbblButtonColors(),
                modifier = Modifier.weight(1f),
            ) {
                val reserveCount by vm.reserveCount().collectAsState()
//                AutoSizeText(text = "$reserveCount Rsv", textStyle = TextStyle.Default)
                Text(text = "$reserveCount Rsv")
            }
            Button(
                onClick = { vm.toggleInjuries() },
                colors = FumbblButtonColors(),
                modifier = Modifier.weight(1f)
            ) {
                val injuriesCount by vm.injuriesCount().collectAsState()
//                AutoSizeText(text = "$injuriesCount Out", textStyle = TextStyle.Default)
                Text(text = "$injuriesCount Out")
            }
        }
    }
}

@Composable
fun Screen(
    field: FieldViewModel,
    leftDugout: SidebarViewModel,
    rightDugout: SidebarViewModel,
    replayController: ReplayViewModel,
    actionSelector: ActionSelectorViewModel,
    logs: LogViewModel
) {
    Box {
        Column() {
            Row(modifier = Modifier
                .fillMaxWidth()
                .aspectRatio((145f+782f+145f)/452f),
                verticalAlignment = Alignment.Top
            ) {
                Sidebar(leftDugout, Modifier.weight(145f))
                Field(field, Modifier.weight(782f))
                Sidebar(rightDugout, Modifier.weight(145f))
            }
            Row(modifier = Modifier
                .fillMaxWidth()
            ) {
                ReplayController(replayController, modifier = Modifier.height(48.dp))
            }
            Row(modifier = Modifier
                .fillMaxWidth()
            ) {
                LogViewer(logs, modifier = Modifier.width(200.dp))
                ActionSelector(actionSelector, modifier = Modifier.width(200.dp))
            }
        }
    }
}

@Composable
fun ReplayController(vm: ReplayViewModel, modifier: Modifier) {
    Box(modifier = modifier
        .fillMaxSize()
        .background(color = Color.Red)
    ) {
        Row {
            Button(onClick = { vm.enableReplay() }) {
                Text("Start replay")
            }
            Button(onClick = { vm.rewind() }) {
                Text("Rewind")
            }
            Button(onClick = { vm.back() }) {
                Text("Back")
            }
            Button(onClick = { vm.forward() }) {
                Text("Forward")
            }
            Button(onClick = { vm.stopReplay() }) {
                Text("Stop replay")
            }
        }
    }
}

@Composable
fun ActionSelector(vm: ActionSelectorViewModel, modifier: Modifier) {
    Box(modifier = modifier
        .fillMaxSize()
        .background(color = Color.Blue)
    ) {
        Button(onClick = { vm.start() }) {
            Text("Start")
        }
    }
}

@Composable
fun LogViewer(vm: LogViewModel, modifier: Modifier) {
    val listData by vm.logs.collectAsState(initial = emptyList())
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(items = listData, key = { item -> item.hashCode() }) {
            Text(text = it.message)
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Field(vm: FieldViewModel, modifier: Modifier) {

    val field: FieldDetails by vm.field().collectAsState()
    val highlightedSquare: Square? by vm.highlights().collectAsState()

    Box(modifier = modifier
        .fillMaxSize()
        .aspectRatio(vm.aspectRatio)
    ) {
        Image(
            painter = painterResource(field.resource),
            contentDescription = field.description,
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.TopStart)
        )
        Column(modifier = Modifier
            .fillMaxSize()
        ) {
            repeat(vm.height) { height: Int ->
                Row(modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                ) {
                    repeat(vm.width) { width ->
                        val hover: Boolean by remember {
                            derivedStateOf {
                                Square(width, height) == highlightedSquare
                            }
                        }
                        Box(modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .background(color = if (hover) {
                                Color.Cyan.copy(alpha = 0.25f)
                            } else {
                                Color.Transparent
                            })
                            .onPointerEvent(PointerEventType.Enter) {
                                vm.hoverOver(Square(width, height))
                            }
                        )
                    }
                }
            }
        }
    }
}