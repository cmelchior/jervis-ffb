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
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.jervisffb.engine.actions.GameActionId
import com.jervisffb.jervis_ui.generated.resources.Res
import com.jervisffb.jervis_ui.generated.resources.jervis_icon_playing
import com.jervisffb.ui.game.icons.IconFactory
import com.jervisffb.ui.game.model.UiPlayer
import com.jervisffb.ui.game.viewmodel.ButtonData
import com.jervisffb.ui.game.viewmodel.CreateActionIndicator
import com.jervisffb.ui.game.viewmodel.NoIndicator
import com.jervisffb.ui.game.viewmodel.ShowActive
import com.jervisffb.ui.game.viewmodel.ShowTimeOutButton
import com.jervisffb.ui.game.viewmodel.ShowTimer
import com.jervisffb.ui.game.viewmodel.SidebarView
import com.jervisffb.ui.game.viewmodel.SidebarViewModel
import com.jervisffb.ui.game.viewmodel.TimeExpiredBehavior
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.skia.FilterBlurMode
import org.jetbrains.skia.MaskFilter
import kotlin.math.abs
import kotlin.math.round
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Composable
fun Sidebar(
    vm: SidebarViewModel,
    modifier: Modifier,
) {
    val actionIndicator: CreateActionIndicator by vm.createActionIndicatorFlow.collectAsState(NoIndicator(GameActionId(-1)))

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
            // Dogout + player stats
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

            // Timer
            Box {
                ActionTimer(vm, actionIndicator)
            }

            // Other buttons
            Column(
                modifier = Modifier,
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                SidebarButtons(vm.actionButtons())
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
 * This renders a list of players under a header in the sidebar
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
                            Player(modifier, sortedList[index + x]!!, false)
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
                        Player(modifier, list[index + x], false)
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
fun ActionTimer(vm: SidebarViewModel, indictor: CreateActionIndicator) {
    when (indictor) {
        is NoIndicator -> { /* Do nothing */ }
        is ShowActive -> ActiveCoachTimerDescription("Playing")
        is ShowTimeOutButton -> {
            var showTimeOutButton by remember { mutableStateOf(false) }
            LaunchedEffect(indictor) {
                delay(indictor.timeLeft.coerceAtLeast(Duration.ZERO))
                showTimeOutButton = true
            }
            if (showTimeOutButton) {
                LargeSidebarButton(
                    modifier = Modifier,
                    "Call Out of Time!",
                ) {
                    vm.callTimeout(indictor.actionIndex)
                }
            }
        }
        is ShowTimer -> {
            var showTimer by remember { mutableStateOf(true) }
            var stateDescription by remember { mutableStateOf(formatDuration(indictor.timeLeft)) }
            var remainingTimeDuration by remember { mutableStateOf(indictor.timeLeft) }
            LaunchedEffect(indictor) {
                while (indictor.timeLeft > Duration.ZERO || indictor.timeExpiredBehavior == TimeExpiredBehavior.CONTINUE_COUNTING) {
                    stateDescription = formatDuration(remainingTimeDuration)
                    remainingTimeDuration = (remainingTimeDuration - 1.seconds)
                    if (indictor.timeExpiredBehavior != TimeExpiredBehavior.CONTINUE_COUNTING) {
                        remainingTimeDuration = remainingTimeDuration.coerceAtLeast(Duration.ZERO)
                    }
                    delay(1.seconds)
                }
                stateDescription = when (indictor.timeExpiredBehavior) {
                    TimeExpiredBehavior.CONTINUE_COUNTING -> {
                        // Should never get here as this should be covered by the above loop
                        ""
                    }
                    TimeExpiredBehavior.STOP_AT_ZERO -> formatDuration(Duration.ZERO)
                    TimeExpiredBehavior.SHOW_TIMEOUT_MESSAGE -> "Out of Time"
                    TimeExpiredBehavior.HIDE_COUNTER -> {
                        showTimer = false
                        ""
                    }
                }
            }
            if (showTimer) {
                ActiveCoachTimerDescription(stateDescription)
            }
        }
    }
}

@Composable
private fun ActiveCoachTimerDescription(text: String) {
    Box(
        modifier = Modifier.aspectRatio(143f/30f),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Image(
            modifier = Modifier.fillMaxSize(),
            bitmap = imageResource(Res.drawable.jervis_icon_playing),
            contentDescription = "",
            contentScale = ContentScale.Fit,
        )
        Text(
            modifier = Modifier.fillMaxWidth(0.9f),
            text = text,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.body1.copy(
                color = JervisTheme.contentTextColor,
                fontWeight = FontWeight.Bold,
                lineHeight = 1.2.em,
                fontSize = 12.sp,
            ),
        )
    }
}

@Composable
private fun TimeoutButton() {
    Box(
        modifier = Modifier.aspectRatio(143f/30f),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Image(
            modifier = Modifier.fillMaxSize(),
            bitmap = imageResource(Res.drawable.jervis_icon_playing),
            contentDescription = "",
            contentScale = ContentScale.Fit,
        )
        Text(
            modifier = Modifier.fillMaxWidth(0.9f),
            text = "Timeout!",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.body1.copy(
                color = JervisTheme.contentTextColor,
                fontWeight = FontWeight.Bold,
                lineHeight = 1.2.em,
                fontSize = 12.sp,
            ),
        )
    }
}

fun formatDuration(duration: Duration): String {
    // Makes sure we handle the case around 0 correctly
    val totalSeconds = round(duration.inWholeMilliseconds / 1_000f).toInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    if (totalSeconds >= 0) {
        return buildString {
            if (minutes < 10) append('0')
            append(minutes)
            append(':')
            if (seconds < 10) append('0')
            append(seconds)
        }
    } else {
        return buildString {
            append("-")
            if (minutes > -10) append('0')
            append(abs(minutes))
            append(':')
            if (seconds > -10) append('0')
            append(abs(seconds))
        }
    }


}
