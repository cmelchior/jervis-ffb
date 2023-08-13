package dk.ilios.bloodbowl.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonColors
import androidx.compose.material.Text
import androidx.compose.runtime.*
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
import dk.ilios.bloodbowl.model.*
import dk.ilios.bloodbowl.ui.model.UIPlayer

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
fun Screen(field: FieldViewModel, leftDugout: SidebarViewModel, rightDugout: SidebarViewModel) {
    Box {
//        Image(painterResource(""))
        Row(modifier = Modifier
            .fillMaxWidth()
            .aspectRatio((145f+782f+145f)/452f),
            verticalAlignment = Alignment.Top
        ) {
            Sidebar(leftDugout, Modifier.weight(145f))
            Field(field, Modifier.weight(782f))
            Sidebar(rightDugout, Modifier.weight(145f))
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