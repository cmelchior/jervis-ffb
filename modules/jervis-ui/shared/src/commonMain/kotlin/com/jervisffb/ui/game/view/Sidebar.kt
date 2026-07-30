package com.jervisffb.ui.game.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.jervis.generated.SettingsKeys
import com.jervisffb.ui.SETTINGS_MANAGER
import com.jervisffb.ui.game.icons.IconFactory
import com.jervisffb.ui.game.viewmodel.ButtonData
import com.jervisffb.ui.game.viewmodel.SidebarViewModel
import com.jervisffb.ui.game.viewmodel.UiSidebarData
import com.jervisffb.ui.menu.GameScreenModel
import com.jervisffb.ui.menu.LocalPitchDataWrapper
import com.jervisffb.ui.utils.applyIf
import com.jervisffb.ui.utils.jdp
import com.jervisffb.ui.utils.pixelSize
import kotlinx.coroutines.flow.Flow
import kotlin.math.ceil

@Composable
fun Sidebar(
    vm: SidebarViewModel,
    modifier: Modifier,
) {
    val sidebarAction by vm.dogoutAction().collectAsState(null)
    val reservesFlow = remember(vm) { vm.reserves(compact = false) }
    Box(
        modifier = Modifier
            .applyIf(sidebarAction != null) {
                clickable { sidebarAction?.invoke() }.background(JervisTheme.availableActionBackground)
            },
        contentAlignment = Alignment.TopCenter
    ) {
        // Side bar content
        Column(modifier = Modifier) {
            // Dogout + player stats
            Box(modifier = modifier.fillMaxSize()) {
                val useSidebarImage by SETTINGS_MANAGER.observeBooleanKey(SettingsKeys.JERVIS_UI_SHOW_DOGOUT_BACKGROUND_VALUE, true).collectAsState(true)
                if (useSidebarImage) {
                    Image(
                        alignment = Alignment.TopStart,
                        painter = BitmapPainter(IconFactory.getSidebarBackground()),
                        contentDescription = "Box",
                        contentScale = ContentScale.FillWidth,
                        modifier = modifier.fillMaxSize().padding(bottom = 8.dp).alpha(0.8f),
                    )
                }
                Column(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                        Reserves(vm.gameViewModel, reservesFlow, vm.sharedPitchData) {
                            vm.hoverExit()
                        }
                        Injuries(
                            vm.gameViewModel,
                            showIfEmpty = false,
                            vm.sharedPitchData,
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
                PlayerStatsCard(
                    flow = vm.playerStatCardFlow,
                    onClick = vm::dismissFixedCard
                )
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
private fun Reserves(
    screenModel: GameScreenModel,
    reserves: Flow<UiSidebarData>,
    sharedData: LocalPitchDataWrapper,
    onExit: () -> Unit
) {
    val list: UiSidebarData by reserves.collectAsState(UiSidebarData.DEFAULT)
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader("Reserves")
        PlayerSection(screenModel, list, sharedData, compactView = false, onExit = onExit)
    }
}

@Composable
private fun Injuries(
    screenModel: GameScreenModel,
    showIfEmpty: Boolean,
    sharedPitchData: LocalPitchDataWrapper,
    knockedOut: Flow<UiSidebarData>,
    badlyHurt: Flow<UiSidebarData>,
    seriousInjuries: Flow<UiSidebarData>,
    dead: Flow<UiSidebarData>,
    banned: Flow<UiSidebarData>,
    special: Flow<UiSidebarData>,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        val knockedOutList: UiSidebarData by knockedOut.collectAsState(UiSidebarData.DEFAULT)
        val badlyHurtList: UiSidebarData by badlyHurt.collectAsState(UiSidebarData.DEFAULT)
        val seriousInjuryList: UiSidebarData by seriousInjuries.collectAsState(UiSidebarData.DEFAULT)
        val deadList: UiSidebarData by dead.collectAsState(UiSidebarData.DEFAULT)
        val bannedList: UiSidebarData by banned.collectAsState(UiSidebarData.DEFAULT)
        val specialList: UiSidebarData by special.collectAsState(UiSidebarData.DEFAULT)
        if (knockedOutList.isNotEmpty() || showIfEmpty) {
            SectionHeader("Knocked Out")
            PlayerSection(screenModel, knockedOutList, sharedPitchData)
        }
        if (badlyHurtList.isNotEmpty() || showIfEmpty) {
            SectionHeader("Badly Hurt")
            PlayerSection(screenModel, badlyHurtList, sharedPitchData)
        }
        if (seriousInjuryList.isNotEmpty() || showIfEmpty) {
            SectionHeader("Seriously Injured")
            PlayerSection(screenModel, seriousInjuryList, sharedPitchData)
        }
        if (deadList.isNotEmpty() || showIfEmpty) {
            SectionHeader("Dead")
            PlayerSection(screenModel, deadList, sharedPitchData)
        }
        if (bannedList.isNotEmpty() || showIfEmpty) {
            SectionHeader("Banned")
            PlayerSection(screenModel, bannedList, sharedPitchData)
        }
        if (specialList.isNotEmpty() || showIfEmpty) {
            SectionHeader("Special")
            PlayerSection(screenModel, specialList, sharedPitchData)
        }
    }
}

/**
 * A list of players in a dogout section
 *
 * @param compactView If `true` players will group together. If `false` players will remember their position when
 * moving between the pitch and the dogout.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun PlayerSection(
    screenModel: GameScreenModel,
    players: UiSidebarData,
    sharedPitchData: LocalPitchDataWrapper,
    compactView: Boolean = true,
    onExit: () -> Unit = {}
) {
    val playersPrRow = 3
    val pitchSize = sharedPitchData.size

    if (!compactView) {
        if (players.isNotEmpty()) {
            val rows = ceil(players.size/playersPrRow.toFloat()).toInt()
            for (y in 0 until rows) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.jdp)
                        .onPointerEvent(PointerEventType.Exit) { onExit() }
                    ,
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    val modifier = Modifier.aspectRatio(1f)
                    repeat(playersPrRow) { x ->
                        Box(
                            modifier = modifier.weight(1f),
                            contentAlignment = Alignment.Center,
                        ) {
                            val index = y * playersPrRow + x
                            val uiPlayer = if (index < players.size) players[index] else null
                            if (uiPlayer != null) {
                                val player = uiPlayer.player
                                val playerSizePx = pitchSize.getPlayerSquareSize(player.size)
                                Player(
                                    Modifier.pixelSize(playerSizePx),
                                    screenModel,
                                    player,
                                    uiPlayer.transientData,
                                    parentHandleClick = false,
                                    contextMenuShowing = false
                                )
                            }
                        }
                    }
                }
            }
        }
    } else {
        for (index in players.indices step playersPrRow) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .onPointerEvent(PointerEventType.Exit) { onExit() }
                    .padding(horizontal = 24.jdp)
                ,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                val modifier = Modifier.aspectRatio(1f)
                repeat(playersPrRow) { x ->
                    Box(
                        modifier = modifier.weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (players.size > (index + x)) {
                            val player = players[index + x]!!.player
                            val playerSizePx = pitchSize.getPlayerSquareSize(player.size)
                            Player(
                                modifier.pixelSize(playerSizePx),
                                screenModel,
                                player,
                                players[index + x]!!.transientData,
                                parentHandleClick = false,
                                contextMenuShowing = false
                            )
                        }
                    }
                }
            }
        }
    }
}
