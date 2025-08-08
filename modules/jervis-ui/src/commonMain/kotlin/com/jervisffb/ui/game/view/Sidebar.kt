package com.jervisffb.ui.game.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.layout.LayoutModifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.jervisffb.ui.game.icons.IconFactory
import com.jervisffb.ui.game.model.UiPlayer
import com.jervisffb.ui.game.viewmodel.ButtonData
import com.jervisffb.ui.game.viewmodel.SidebarView
import com.jervisffb.ui.game.viewmodel.SidebarViewModel
import kotlinx.coroutines.flow.Flow
import org.jetbrains.skia.FilterBlurMode
import org.jetbrains.skia.MaskFilter

@Composable
fun Sidebar(
    vm: SidebarViewModel,
    modifier: Modifier,
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        // Background images
        Column(modifier = Modifier.fillMaxSize().align(Alignment.TopCenter)) {
            Image(
                bitmap = IconFactory.getSidebarBannerTop(vm.team.isHomeTeam()),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.aspectRatio(145f/430f).fillMaxWidth(),
            )
            Image(
                bitmap = IconFactory.getSidebarBannerMiddle(vm.team.isHomeTeam()),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.aspectRatio(145f/92f).fillMaxWidth(),
            )
            Image(
                bitmap = IconFactory.getSidebarBannerBottom(vm.team.isHomeTeam()),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize(), // aspectRatio(145f/168f) Avoid setting aspect ration as rounding gets it slightly wrong
            )
        }

        // Side bar content
        Column(modifier = Modifier.fillMaxSize()) {
            // Dogout + player statss
            Box(modifier = modifier.aspectRatio(vm.aspectRatio).fillMaxSize()) {
                Image(
                    alignment = Alignment.TopStart,
                    painter = BitmapPainter(IconFactory.getSidebarBackground()),
                    contentDescription = "Box",
                    modifier = modifier.fillMaxSize(),
                )
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        val view by vm.view().collectAsState()
                        when (view) {
                            SidebarView.RESERVES -> Reserves(vm.reserves()) {
                                vm.hoverExit()
                            }
                            SidebarView.INJURIES ->
                                Injuries(
                                    vm.knockedOut(),
                                    vm.badlyHurt(),
                                    vm.seriousInjuries(),
                                    vm.dead(),
                                    vm.banned(),
                                    vm.special()
                                )
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }

                // Make sure player stats are shown on top of reserves
                PlayerStatsCard(vm.hoverPlayer())
            }

            // Dogout buttons
            Row {
                val injuriesCount by vm.injuriesCount().collectAsState(0)
                SidebarButton(modifier = Modifier.weight(1f), text = "$injuriesCount Out", onClick = { vm.toggleInjuries() })
                val reserveCount by vm.reserveCount().collectAsState(0)
                SidebarButton(modifier = Modifier.weight(1f), text = "$reserveCount Rsv", onClick = { vm.toggleToReserves() })
            }

            // End Turn
            Column(
                modifier = Modifier,
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                SidebarButtons(vm.actionButtons())
            }

            // Rest of content
            Box {

            }
        }
    }
}

// Area just below the Sidebar where we can show extra buttons like "End Turn", "End Setup"
// or
@Composable
private fun ColumnScope.SidebarButtons(buttons: Flow<List<ButtonData>>) {
    val buttons by buttons.collectAsState(emptyList())
    buttons.forEach { button ->
        LargeSidebarButton(
            modifier = Modifier,
            text = button.title,
            onClick = button.onClick
        )
    }
}

@Composable
fun SidebarButton(modifier: Modifier, text: String, onClick: () -> Unit) {
    // TODO Add drop shadow to the top
    Box(
        modifier = modifier.aspectRatio(71f/22f),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            modifier = Modifier.fillMaxSize().clickable { onClick() },
            painter = BitmapPainter(IconFactory.getButton()),
            contentDescription = "",
            contentScale = ContentScale.Fit,
        )
        Text(
            modifier = Modifier.padding(top = 2.dp), // Adjust to make it more center
            text = text,
            maxLines = 1,
            lineHeight = 1.em,
            textAlign = TextAlign.Center,
            fontSize = 10.sp,
            color = Color.Black,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun LargeSidebarButton(modifier: Modifier, text: String, onClick: () -> Unit) {
    // TODO Add drop shadow to the top
    Box(
        modifier = modifier.aspectRatio(143f/30f),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            modifier = Modifier.fillMaxSize().clickable { onClick() },
            painter = BitmapPainter(IconFactory.getLargeButton()),
            contentDescription = "",
            contentScale = ContentScale.Fit,
        )
        Text(
            modifier = Modifier.padding(top = 2.dp), // Adjust to make it more center
            text = text,
            maxLines = 1,
            lineHeight = 1.em,
            textAlign = TextAlign.Center,
            fontSize = 10.sp,
            color = Color.Black,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun StatBox(
    modifier: Modifier,
    title: String,
    value: String,
    borderRadius: Dp = 4.dp,
    glowColor: Color = Color.White.copy(alpha = 0.9f),
    glowSize: Dp = 2.dp,
) {
    Box(
        modifier = modifier
            .drawBehind {
                val shape = RoundedCornerShape(borderRadius)
                val outline = shape.createOutline(size, layoutDirection, this)
                drawIntoCanvas { canvas ->
                    canvas.drawOutline(
                        outline,
                        androidx.compose.ui.graphics.Paint().apply {
                            color = glowColor
                            this.asFrameworkPaint().maskFilter = MaskFilter.makeBlur(
                                FilterBlurMode.NORMAL,
                                sigma = glowSize.toPx(),
                            )
                        }
                    )
                }
            }
    ) {
        Column(
            modifier = modifier
                .background(Color.White, RoundedCornerShape(borderRadius))
                .border(2.dp, Color.Black, RoundedCornerShape(borderRadius))
            ,
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.45f)
                        .background(color = Color.Black, shape = RoundedCornerShape(topStart = borderRadius, topEnd = borderRadius))
                ,
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = title,
                    fontSize = 9.sp,
                    lineHeight = 1.em,
                    maxLines = 1,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )
            }
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                ,
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    modifier = Modifier.padding(bottom = 2.dp),
                    text = value,
                    fontSize = 11.sp,
                    lineHeight = 1.em,
                    maxLines = 1,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
fun Reserves(reserves: Flow<List<UiPlayer>>, onExit: () -> Unit) {
    val list: List<UiPlayer> by reserves.collectAsState(emptyList())
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader("Reserves")
        PlayerSection(list, compactView = false, onExit = onExit)
    }
}

@Composable
fun Injuries(
    knockedOut: Flow<List<UiPlayer>>,
    badlyHurt: Flow<List<UiPlayer>>,
    seriousInjuries: Flow<List<UiPlayer>>,
    dead: Flow<List<UiPlayer>>,
    banned: Flow<List<UiPlayer>>,
    special: Flow<List<UiPlayer>>,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader("Knocked Out")
        val knockedOutList: List<UiPlayer> by knockedOut.collectAsState(emptyList())
        PlayerSection(knockedOutList)
        SectionHeader("Badly Hurt")
        val badlyHurtList: List<UiPlayer> by badlyHurt.collectAsState(emptyList())
        PlayerSection(badlyHurtList)
        SectionHeader("Seriously Injured")
        val seriousInjuryList: List<UiPlayer> by seriousInjuries.collectAsState(emptyList())
        PlayerSection(seriousInjuryList)
        SectionHeader("Dead")
        val deadList: List<UiPlayer> by dead.collectAsState(emptyList())
        PlayerSection(deadList)
        SectionHeader("Banned")
        val bannedList: List<UiPlayer> by banned.collectAsState(emptyList())
        PlayerSection(bannedList)
        SectionHeader("Special")
        val specialList: List<UiPlayer> by special.collectAsState(emptyList())
        PlayerSection(specialList)
    }
}

/**
 * A list of players under
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PlayerSection(list: List<UiPlayer>, compactView: Boolean = true, onExit: () -> Unit = {}) {
    if (!compactView) {
        val max = if (list.isNotEmpty()) list.maxBy { it.model.number.value }.model.number.value else 0
        if (max > 0) {
            val sortedList: ArrayList<UiPlayer?> = ArrayList<UiPlayer?>(max)
                .also { list ->
                    repeat(max) {
                        list.add(null)
                    }
                }
            list.forEach { sortedList[it.model.number.value - 1] = it }
            for (index in sortedList.indices step 5) {
                Row(modifier = Modifier.onPointerEvent(PointerEventType.Exit) { onExit() }) {
                    val modifier = Modifier.weight(1f).aspectRatio(1f)
                    repeat(5) { x ->
                        if (sortedList.size > (index + x) && sortedList[index + x] != null) {
                            Player(
                                modifier,
                                sortedList[index + x]!!,
                                parentHandleClick = false,
                                contextMenuShowing = false
                            )
                        } else {
                            // Use empty box. Unsure if we can remove this
                            // if we want a partial row to scale correctly.
                            Box(modifier = modifier)
                        }
                    }
                }
            }
        }
    } else {
        for (index in list.indices step 5) {
            Row {
                val modifier = Modifier.weight(1f).aspectRatio(1f)
                repeat(5) { x ->
                    if (list.size > (index + x)) {
                        Player(
                            modifier,
                            list[index + x],
                            parentHandleClick = false,
                            contextMenuShowing = false
                        )
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
