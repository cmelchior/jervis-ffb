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
import androidx.compose.runtime.key
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
import com.jervisffb.ui.game.viewmodel.SidebarSection
import com.jervisffb.ui.game.viewmodel.SidebarViewModel
import com.jervisffb.ui.game.viewmodel.UiSidebarData
import com.jervisffb.ui.menu.GameScreenModel
import com.jervisffb.ui.menu.LocalPitchDataWrapper
import com.jervisffb.ui.utils.applyIf
import com.jervisffb.ui.utils.jdp
import com.jervisffb.ui.utils.pixelSize
import kotlinx.coroutines.flow.Flow

private const val PLAYERS_PR_ROW = 3

@Composable
fun Sidebar(
    vm: SidebarViewModel,
    modifier: Modifier = Modifier,
) {
    val sidebarAction by vm.dogoutAction.collectAsState(null)
    Box(
        modifier = modifier
            .applyIf(sidebarAction != null) {
                clickable { sidebarAction?.invoke() }.background(JervisTheme.availableActionBackground)
            },
        contentAlignment = Alignment.TopCenter
    ) {

        Box(modifier = Modifier.fillMaxSize()) {
            val useSidebarImage by SETTINGS_MANAGER.observeBooleanKey(SettingsKeys.JERVIS_UI_SHOW_DOGOUT_BACKGROUND_VALUE, true).collectAsState(true)

            // Background image for the sidebar (if any)
            if (useSidebarImage) {
                Image(
                    alignment = Alignment.TopStart,
                    painter = BitmapPainter(IconFactory.getSidebarBackground()),
                    contentDescription = "Box",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 8.dp)
                        .alpha(0.8f),
                )
            }

            // Players rendered in sections depending on their state
            Column(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Reserves(
                        vm.gameViewModel,
                        vm.reserves,
                        vm.sharedPitchData,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        vm.hoverExit()
                    }
                    Injuries(
                        screenModel = vm.gameViewModel,
                        sections = vm.injurySections,
                        sharedPitchData = vm.sharedPitchData,
                        showIfEmpty = false,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
            }

            // Player Stat Card, will be rendered above everything else when visible.
            PlayerStatsCard(
                flow = vm.playerStatCardFlow,
                onClick = vm::dismissFixedCard
            )
        }
    }
}

// Area just below the Sidebar where we can show extra buttons like "End Turn", "End Setup"
// or the available setups.
//
// TODO Not wired up yet. This and the two button composables below are staged for WASM/iOS,
//  which have no menu bar. `SidebarViewModel._buttons` is the other half and produces the
//  setup buttons this is meant to render.
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
    modifier: Modifier = Modifier,
    onExit: () -> Unit,
) {
    val list: UiSidebarData by reserves.collectAsState(UiSidebarData.DEFAULT)
    Column(modifier = modifier) {
        SectionHeader("Reserves")
        PlayerSection(screenModel, list, sharedData, onExit = onExit)
    }
}

@Composable
private fun Injuries(
    screenModel: GameScreenModel,
    sections: List<SidebarSection>,
    sharedPitchData: LocalPitchDataWrapper,
    showIfEmpty: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        sections.forEach { section ->
            key(section.title) {
                val players: UiSidebarData by section.players.collectAsState(UiSidebarData.DEFAULT)
                if (players.isNotEmpty() || showIfEmpty) {
                    SectionHeader(section.title)
                    PlayerSection(screenModel, players, sharedPitchData)
                }
            }
        }
    }
}

/**
 * A list of players in a dogout section, laid out [PLAYERS_PR_ROW] per row.
 *
 * Whether players group together or keep a fixed position is decided by the [UiSidebarData]
 * itself (see `UiSidebarData.compact` vs `UiSidebarData.fixed`), so this only has to render
 * the list it is given. Empty slots in a fixed list are `null` and render as gaps.
 *
 * Note: this emits one [Row] per line of players into the caller's [Column] rather than a
 * single root layout, so it deliberately has no `modifier` parameter - there is no one node
 * for the caller's modifier to apply to.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun PlayerSection(
    screenModel: GameScreenModel,
    players: UiSidebarData,
    sharedPitchData: LocalPitchDataWrapper,
    onExit: () -> Unit = {}
) {
    val pitchSize = sharedPitchData.size
    for (rowStart in players.indices step PLAYERS_PR_ROW) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.jdp)
                .onPointerEvent(PointerEventType.Exit) { onExit() }
            ,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            repeat(PLAYERS_PR_ROW) { column ->
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    val uiPlayer = players.getOrNull(rowStart + column)
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
