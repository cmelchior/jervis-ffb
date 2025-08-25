package com.jervisffb.ui.game.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.jervisffb.ui.game.icons.IconFactory
import com.jervisffb.ui.game.model.UiPlayer
import com.jervisffb.ui.game.viewmodel.ButtonData
import com.jervisffb.ui.game.viewmodel.SidebarViewModel
import com.jervisffb.ui.utils.jdp
import kotlinx.coroutines.flow.Flow

@Composable
fun Sidebar(
    vm: SidebarViewModel,
    modifier: Modifier,
) {
    Box(modifier = Modifier, contentAlignment = Alignment.TopCenter) {
        // Side bar content
        Column(modifier = Modifier) {
            // Dogout + player stats
            Box(modifier = modifier.fillMaxSize()) {
                //Box(modifier = modifier.aspectRatio(vm.aspectRatio).fillMaxSize()) {
                Image(
                    alignment = Alignment.TopStart,
                    painter = BitmapPainter(IconFactory.getSidebarBackground()),
                    contentDescription = "Box",
                    contentScale = ContentScale.FillWidth,
                    modifier = modifier.fillMaxSize().padding(bottom = 8.dp).alpha(0.8f),
                )
                Column(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                        Reserves(vm.reserves()) {
                            vm.hoverExit()
                        }
                        Injuries(
                            showIfEmpty = false,
                            vm.knockedOut(),
                            vm.badlyHurt(),
                            vm.seriousInjuries(),
                            vm.dead(),
                            vm.banned(),
                            vm.special()
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }

                // Make sure player stats are shown on top of reserves
                PlayerStatsCard(vm.hoverPlayer())
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
private fun SidebarButton(modifier: Modifier, text: String, onClick: () -> Unit) {
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
private fun LargeSidebarButton(modifier: Modifier, text: String, onClick: () -> Unit) {
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
private fun Reserves(reserves: Flow<List<UiPlayer>>, onExit: () -> Unit) {
    val list: List<UiPlayer> by reserves.collectAsState(emptyList())
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader("Reserves")
        PlayerSection(list, compactView = false, onExit = onExit)
    }
}

@Composable
private fun Injuries(
    showIfEmpty: Boolean,
    knockedOut: Flow<List<UiPlayer>>,
    badlyHurt: Flow<List<UiPlayer>>,
    seriousInjuries: Flow<List<UiPlayer>>,
    dead: Flow<List<UiPlayer>>,
    banned: Flow<List<UiPlayer>>,
    special: Flow<List<UiPlayer>>,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        val knockedOutList: List<UiPlayer> by knockedOut.collectAsState(emptyList())
        val badlyHurtList: List<UiPlayer> by badlyHurt.collectAsState(emptyList())
        val seriousInjuryList: List<UiPlayer> by seriousInjuries.collectAsState(emptyList())
        val deadList: List<UiPlayer> by dead.collectAsState(emptyList())
        val bannedList: List<UiPlayer> by banned.collectAsState(emptyList())
        val specialList: List<UiPlayer> by special.collectAsState(emptyList())
        if (knockedOutList.isNotEmpty() || showIfEmpty) {
            SectionHeader("Knocked Out")
            PlayerSection(knockedOutList)
        }
        if (badlyHurtList.isNotEmpty() || showIfEmpty) {
            SectionHeader("Badly Hurt")
            PlayerSection(badlyHurtList)
        }
        if (seriousInjuryList.isNotEmpty() || showIfEmpty) {
            SectionHeader("Seriously Injured")
            PlayerSection(seriousInjuryList)
        }
        if (deadList.isNotEmpty() || showIfEmpty) {
            SectionHeader("Dead")
            PlayerSection(deadList)
        }
        if (bannedList.isNotEmpty() || showIfEmpty) {
            SectionHeader("Banned")
            PlayerSection(bannedList)
        }
        if (specialList.isNotEmpty() || showIfEmpty) {
            SectionHeader("Special")
            PlayerSection(specialList)
        }
    }
}

/**
 * A list of players under
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun PlayerSection(list: List<UiPlayer>, compactView: Boolean = true, onExit: () -> Unit = {}) {
    val playersPrRow = 3
    val playerSize = 45.jdp
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
            for (index in sortedList.indices step playersPrRow) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onPointerEvent(PointerEventType.Exit) { onExit() }
                        .padding(bottom = 16.jdp)
                    ,
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    val modifier = Modifier.size(playerSize).aspectRatio(1f)
                    repeat(playersPrRow) { x ->
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
        for (index in list.indices step playersPrRow) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .onPointerEvent(PointerEventType.Exit) { onExit() }
                    .padding(bottom = 16.jdp)
                ,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                val modifier = Modifier.size(playerSize).aspectRatio(1f)
                repeat(playersPrRow) { x ->
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
