package dk.ilios.jervis.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.layout.LayoutModifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dk.ilios.jervis.ui.images.IconFactory
import dk.ilios.jervis.ui.model.UiPlayer
import dk.ilios.jervis.ui.model.UiPlayerCard
import dk.ilios.jervis.ui.viewmodel.SidebarView
import dk.ilios.jervis.ui.viewmodel.SidebarViewModel
import kotlinx.coroutines.flow.Flow
import org.pushingpixels.artemis.drawTextOnPath

@Composable
fun Sidebar(
    vm: SidebarViewModel,
    modifier: Modifier,
) {
    Box(modifier = modifier.aspectRatio(vm.aspectRatio).fillMaxSize()) {
        Image(
            alignment = Alignment.TopStart,
            painter = BitmapPainter(IconFactory.getSidebarBackground()),
            contentDescription = "Box",
            modifier = modifier.fillMaxSize(),
        )

        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                val view by vm.view().collectAsState()
                when (view) {
                    SidebarView.RESERVES -> Reserves(vm.reserves())
                    SidebarView.INJURIES ->
                        Injuries(
                            vm.knockedOut(),
                            vm.badlyHurt(),
                            vm.seriousInjuries(),
                            vm.dead(),
                        )
                }
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
                    modifier = Modifier.weight(1f),
                ) {
                    val injuriesCount by vm.injuriesCount().collectAsState()
                    Text(text = "$injuriesCount Out", maxLines = 1)
                }
            }
        }

        // Make sure player stats are shown on top of reserves
        PlayerStatsCard(vm.hoverPlayer())
    }
}

@Composable
fun PlayerStatsCard(flow: Flow<UiPlayerCard?>) {
    val playerData by flow.collectAsState(null)
    playerData?.let { player ->
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopStart) {
            Image(
                painter = BitmapPainter(IconFactory.getPlayerDetailOverlay()),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )

            // Side bar content
            Column(modifier = Modifier.fillMaxWidth()) {
                // Blue Square with player information
                Column(
                    modifier =
                        Modifier
                            .aspectRatio(145f / 213f) // Size of blue square
                            .fillMaxSize(),
                ) {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .weight(1f),
                    ) {
                        // Player name
                        Text(
                            modifier = Modifier.padding(4.dp).fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            text = player.model.name ?: "",
                            color = Color.White,
                            maxLines = 1,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            overflow = TextOverflow.Ellipsis,
                        )

                        // Image, type / number
                        Row(
                            modifier =
                                Modifier
                                    .padding(start = 8.dp, end = 8.dp)
                                    .fillMaxSize(),
                        ) {
                            Image(
                                modifier = Modifier.aspectRatio(95f / 147f).fillMaxSize(),
                                painter = BitmapPainter(IconFactory.getPlayerImage(player.model.id)),
                                contentDescription = "",
                                contentScale = ContentScale.Fit,
                            )

                            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                                // A path with three quad Bezier segments
                                val path = androidx.compose.ui.graphics.Path()
                                path.moveTo(this.size.width, this.size.height - 5)
                                path.lineTo(size.width, 5f)

                                val name = player.model.position.positionSingular.takeDot(10)
                                drawTextOnPath(
                                    text = "$name #${player.model.number.number}",
                                    textSize = 14.sp.toDp(),
                                    isEmboldened = true,
                                    path = path,
                                    offset = Offset(0.dp.toPx(), 0.0f),
                                    textAlign = TextAlign.Start,
                                    paint =
                                        Paint().also {
                                            it.color = Color.White
                                            it.style = PaintingStyle.Fill
                                        },
                                )
                            }
                        }
                    }

                    // Stat boxes
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        val model = player.model
                        val modifier = Modifier.weight(1f).aspectRatio(52f / 58f)
                        StatBox(modifier, "MV", model.move.toString())
                        StatBox(modifier, "ST", model.strength.toString())
                        StatBox(modifier, "AG", "${model.agility}+")
                        StatBox(modifier, "PA", "${model.passing}+")
                        StatBox(modifier, "AV", "${model.armorValue}+")
                    }
                }
                Column(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        modifier = Modifier.padding(4.dp).fillMaxWidth(),
                        text = "${player.model.starPlayerPoints} ${player.model.level.name}",
                        textAlign = TextAlign.Center,
                    )
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        player.model.skills.forEach {
                            Text(
                                modifier = Modifier.padding(4.dp).fillMaxWidth(),
                                text = it.name,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatBox(
    modifier: Modifier,
    title: String,
    value: String,
) {
    Box(
        modifier = modifier.background(color = Color.White).border(1.dp, Color.Black),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .background(color = Color.Black),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = title,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )
            }
            Box(
                modifier = Modifier.weight(1f).fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = value,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                )
            }
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
    dead: SnapshotStateList<UiPlayer>,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader("Knocked Out")
        SectionHeader("Badly Hurt")
        SectionHeader("Seriously Injured")
        SectionHeader("Killed")
        SectionHeader("Banned")
    }
}
//
// fun Modifier.rotateVertically(clockwise: Boolean = true): Modifier {
//    val rotate = rotate(if (clockwise) 90f else -90f)
//
//    val adjustBounds = layout { measurable, constraints ->
//        val placeable = measurable.measure(constraints)
//        layout(placeable.height, placeable.width) {
//            placeable.place(
//                x = -(placeable.width / 2 - placeable.height / 2),
//                y = -(placeable.height / 2 - placeable.width / 2)
//            )
//        }
//    }
//    return rotate then adjustBounds
// }
//
// fun Modifier.vertical() = layout { measurable, constraints ->
//    val placeable = measurable.measure(constraints)
//    layout(placeable.height, placeable.width) {
//        placeable.place(
//            x = -(placeable.width / 2 - placeable.height / 2),
//            y = -(placeable.height / 2 - placeable.width / 2)
//        )
//    }
// }

fun Modifier.rotateVertically(rotation: VerticalRotation) =
    then(
        object : LayoutModifier {
            override fun MeasureScope.measure(
                measurable: Measurable,
                constraints: Constraints,
            ): MeasureResult {
                val placeable = measurable.measure(constraints)
                return layout(constraints.maxHeight, placeable.width) {
                    placeable.place(
                        x = -(placeable.width / 2 - placeable.height / 2),
                        y = -(placeable.height / 2 - placeable.width / 2),
                    )
                }
            }

            override fun IntrinsicMeasureScope.minIntrinsicHeight(
                measurable: IntrinsicMeasurable,
                width: Int,
            ): Int {
                return measurable.maxIntrinsicWidth(width)
            }

            override fun IntrinsicMeasureScope.maxIntrinsicHeight(
                measurable: IntrinsicMeasurable,
                width: Int,
            ): Int {
                return measurable.maxIntrinsicWidth(width)
            }

            override fun IntrinsicMeasureScope.minIntrinsicWidth(
                measurable: IntrinsicMeasurable,
                height: Int,
            ): Int {
                return measurable.minIntrinsicHeight(height)
            }

            override fun IntrinsicMeasureScope.maxIntrinsicWidth(
                measurable: IntrinsicMeasurable,
                height: Int,
            ): Int {
                return measurable.maxIntrinsicHeight(height)
            }
        },
    )
        .then(rotate(rotation.value))

enum class VerticalRotation(val value: Float) {
    CLOCKWISE(90f),
    COUNTER_CLOCKWISE(270f),
}

fun String.takeDot(limit: Int): String {
    return if (this.length <= limit) {
        this
    } else {
        take(limit - 1) + "…"
    }
}
