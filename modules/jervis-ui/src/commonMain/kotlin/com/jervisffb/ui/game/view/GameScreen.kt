package com.jervisffb.ui.game.view

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.compose.ui.zIndex
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.jervis_ui.generated.resources.Res
import com.jervisffb.jervis_ui.generated.resources.jervis_icon_lock_closed
import com.jervisffb.jervis_ui.generated.resources.jervis_icon_lock_open
import com.jervisffb.jervis_ui.generated.resources.jervis_icon_menu_settings
import com.jervisffb.jervis_ui.generated.resources.jervis_icon_menu_undo
import com.jervisffb.ui.game.view.pitch.Pitch
import com.jervisffb.ui.game.viewmodel.ActionSelectorViewModel
import com.jervisffb.ui.game.viewmodel.DialogsViewModel
import com.jervisffb.ui.game.viewmodel.GameStatusViewModel
import com.jervisffb.ui.game.viewmodel.LogViewModel
import com.jervisffb.ui.game.viewmodel.PanelBackground
import com.jervisffb.ui.game.viewmodel.PitchViewModel
import com.jervisffb.ui.game.viewmodel.RandomActionsControllerViewModel
import com.jervisffb.ui.game.viewmodel.ReplayControllerViewModel
import com.jervisffb.ui.game.viewmodel.SidebarViewModel
import com.jervisffb.ui.keybinds.ClientShortcut
import com.jervisffb.ui.keybinds.KeyBindings
import com.jervisffb.ui.menu.GameScreenModel
import com.jervisffb.ui.menu.TopbarButton
import com.jervisffb.ui.utils.applyIf
import com.jervisffb.ui.utils.jdp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun GameScreen(
    screenModel: GameScreenModel,
    pitch: PitchViewModel,
    leftDugout: SidebarViewModel,
    rightDugout: SidebarViewModel,
    gameStatusController: GameStatusViewModel,
    replayActionsBar: ReplayControllerViewModel? = null,
    randomActionsBar: RandomActionsControllerViewModel? = null,
    unknownActions: ActionSelectorViewModel,
    logs: LogViewModel,
    dialogsViewModel: DialogsViewModel,
    onSettingsClick: () -> Unit,
) {
    //val aspectRation = (145f+145f+782f)/452f
    val aspectRation = (550f+550f+2354f)/1362f
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        GameStatusTopBar(gameStatusController, modifier = Modifier.padding(horizontal = 24.jdp))
        Spacer(modifier = Modifier.height(8.jdp))
        BoxWithConstraints(modifier = Modifier.weight(1f)) {
            val minBottomHeight = 60.dp
            val spacerHeight = 24.jdp
            val gameScreenWidth = maxWidth
            val maxPitchHeight = maxHeight - minBottomHeight - spacerHeight
            // Actual pitch row height is limited by both aspectRatio and maxPitchHeight.
            val pitchHeight = (maxWidth / aspectRation).coerceAtMost(maxPitchHeight)
            val bottomRowHeight = maxHeight - pitchHeight - spacerHeight
            val panelBackground by screenModel.logsBackgroundColor.collectAsState(PanelBackground.DEFAULT)
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .heightIn(max = maxPitchHeight)
                        .aspectRatio(aspectRation)
                        // Pitch should be above the sidebar and bottom log viewers, so the Action Wheel
                        // is the first thing that intercepts touch events if it overlaps with these sections.
                        .zIndex(1f)
                    ,
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(modifier = Modifier.weight(550f /*145f*/).align(Alignment.Top)) {
                        Sidebar(leftDugout, Modifier)
                    }
                    Column(
                        // Make sure that pitch layers (including the Action Wheel) are placed above the sidebar
                        modifier = Modifier
                            .zIndex(1f)
                            .weight(/*782f*/ 2354f)
                            .align(Alignment.Top)
                        ,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Pitch(
                            Modifier,
                            pitch,
                            borderBrushSize = 3.dp
                        )
                        // ReplayController(replayController, actionSelector, modifier = Modifier.height(48.dp))
                    }
                    Column(modifier = Modifier.weight(550f /*145f*/).align(Alignment.Top)) {
                        Sidebar(rightDugout, Modifier)
                    }
                }
                Spacer(modifier = Modifier.height(24.jdp))
                Row(modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 24.jdp)
                ) {
                    // Placeholder so the other bottom panels stay aligned as if ExpandableLogPanel were here.
                    // The actual ExpandableLogPanel is rendered as a BoxWithConstraints overlay below,
                    // so it can expand upward beyond this Row's bounds.
                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(24.jdp))
                    // Placeholder so the other bottom panels stay aligned as if ExpandableActionPanel were here.
                    // The actual ExpandableActionPanel is rendered as a BoxWithConstraints overlay below,
                    // so it can expand upward beyond this Row's bounds.
                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(24.jdp))
                    BoxWithConstraints(
                        modifier = Modifier
                            .width(48.dp)
                            .fillMaxHeight()
                            .background(panelBackground.color)
                        ,
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        val rowHeight = maxHeight
                        Column(verticalArrangement = Arrangement.Bottom) {
                            TopbarButton(
                                icon = Res.drawable.jervis_icon_menu_undo,
                                contentDescription = KeyBindings.createPlatformButtonLabel(ClientShortcut.UNDO),
                                onClick = {
                                    screenModel.menuViewModel.undoAction()
                                }
                            )
                            if (rowHeight >= 96.dp) {
                                TopbarButton(
                                    icon = Res.drawable.jervis_icon_menu_settings,
                                    contentDescription = KeyBindings.createPlatformButtonLabel(ClientShortcut.GAME_MENU),
                                    onClick = onSettingsClick
                                )
                            }
                        }
                    }
                }
            }

            // LogPanel is placed outside the bottom Row, so it can expand beyond its limits.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.jdp)
                ,
                contentAlignment = Alignment.BottomStart,
            ) {
                // Mirror the weight(1f) split from the bottom Row:
                val logPanelWidth = (gameScreenWidth - 4 * 24.jdp - 48.dp) / 2
                ExpandableLogPanel(
                    logs,
                    panelBackground,
                    bottomRowHeight,
                    Modifier.width(logPanelWidth)
                )
            }

            // Chat/Action Panel is placed outside the bottom Row, so it can expand beyond its limits.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(end = 2*24.jdp + 48.dp)
                ,
                contentAlignment = Alignment.BottomEnd,
            ) {
                // Mirror the weight(1f) split from the bottom Row:
                val logPanelWidth = (gameScreenWidth - 4 * 24.jdp - 48.dp) / 2
                ExpandableActionPanel(
                    unknownActions,
                    panelBackground,
                    bottomRowHeight,
                    Modifier.width(logPanelWidth)
                )
            }
        }
    }
    Dialogs(dialogsViewModel)
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun ExpandableLogPanel(
    logs: LogViewModel,
    background: PanelBackground,
    collapsedHeight: Dp,
    modifier: Modifier,
    expandedHeight: Dp = 300.jdp,
    animateHeightChangeMs: Int = 200
) {
    val expansionEnabled = collapsedHeight < expandedHeight
    var lockExpanded by remember { mutableStateOf(false) }
    var hovered by remember { mutableStateOf(false) }
    val isExpanded = hovered || lockExpanded

    val lockAlpha = remember { Animatable(0f) }
    val logListState = rememberLazyListState()

    // Animate only on expand/collapse toggle; snap immediately on window resize.
    val heightAnimatable = remember { Animatable(collapsedHeight.value) }
    LaunchedEffect(isExpanded) {
        // Animate lock icon visibility
        launch {
            when {
                isExpanded && collapsedHeight >= 96.dp -> lockAlpha.snapTo(1f)
                isExpanded -> {
                    delay(animateHeightChangeMs.toLong().milliseconds/4)
                    lockAlpha.snapTo(1f)
                }
                else -> lockAlpha.snapTo(0f)
            }
        }
        // Animate region size change
        launch {
            if (!isExpanded) {
                // List used reservedLayout, so index[0] is the bottom
                logListState.scrollToItem(0)
            }
            val target = if (isExpanded) expandedHeight.value else collapsedHeight.value
            heightAnimatable.animateTo(target, animationSpec = tween(animateHeightChangeMs))
        }
    }
    LaunchedEffect(collapsedHeight) {
        if (!isExpanded) {
            heightAnimatable.snapTo(collapsedHeight.value)
        }
        lockExpanded = false
        lockAlpha.snapTo(0f)
    }
    val animatedHeight = heightAnimatable.value.dp

    Box(
        modifier = modifier
            .height(animatedHeight)
            .background(if (isExpanded && expansionEnabled) background.hoverColor else background.color)
            .applyIf(expansionEnabled) {
                onPointerEvent(PointerEventType.Enter) { hovered = true }
                    .onPointerEvent(PointerEventType.Exit) { hovered = false }
            }
        ,
        contentAlignment = Alignment.BottomStart,
    ) {
        // LogViewer must stay in composition permanently, so its LazyListState is never reset
        // as this causes crashes. The Box height constrains it to 0 when collapsed.
        Box(modifier = Modifier.height(animatedHeight)) {
            LogViewer(logs, modifier = Modifier.fillMaxSize(), logListState)
        }
        if (expansionEnabled) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .width(48.dp)
                    .fillMaxHeight()
                    .clickable { lockExpanded = !lockExpanded }
                ,
                verticalArrangement = Arrangement.Bottom,

            ) {
                if (lockAlpha.value > 0f) {
                    Image(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .offset(y = 8.dp)
                            .padding(start = 14.dp, top = 14.dp, end = 14.dp, bottom = 0.dp)
                            .size(48.dp)
                            .alpha(lockAlpha.value)
                        ,
                        painter = when (lockExpanded) {
                            true -> painterResource(Res.drawable.jervis_icon_lock_closed)
                            false -> painterResource(Res.drawable.jervis_icon_lock_open)
                        },
                        contentDescription = "Lock Logs Panel",
                        contentScale = ContentScale.Fit,
                        colorFilter = ColorFilter.tint(JervisTheme.white),
                    )
                }
                Text(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                    fontFamily = JervisTheme.extendedDefaultFontFamily(),
                    text = if (isExpanded) "▼" else "▲",
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
        }
    }
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun ExpandableActionPanel(
    actions: ActionSelectorViewModel,
    background: PanelBackground,
    collapsedHeight: Dp,
    modifier: Modifier,
    expandedHeight: Dp = 300.jdp,
    animateHeightChangeMs: Int = 200
) {
//    Column(modifier = Modifier.weight(1f).background(panelBackground.color).fillMaxSize()) {
//        if (replayActionsBar != null) {
//            ReplayCommandBar(replayActionsBar, modifier = Modifier)
//        }
//        if (randomActionsBar != null) {
//            RandomCommandBar(randomActionsBar, modifier = Modifier)
//        }
//        ActionSelector(unknownActions, modifier = Modifier.fillMaxSize())
//    }

    val inputs: List<GameAction> by remember(actions.availableActions) { actions.availableActions }.collectAsState(emptyList())
    var hovered by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    // Animate only on expand/collapse toggle; snap immediately on window resize.
    val heightAnimatable = remember { Animatable(collapsedHeight.value) }
    LaunchedEffect(hovered) {
        launch {
            if (!hovered) {
                // List used reservedLayout, so index[0] is the bottom
                scrollState.scrollTo(0)
            }
            val target = if (hovered) expandedHeight.value else collapsedHeight.value
            heightAnimatable.animateTo(target, animationSpec = tween(animateHeightChangeMs))
        }
    }
    LaunchedEffect(collapsedHeight) {
        if (!hovered) {
            heightAnimatable.snapTo(collapsedHeight.value)
        }
    }
    val animatedHeight = heightAnimatable.value.dp

    Box(
        modifier = modifier
            .height(animatedHeight)
            .background(if (hovered) background.hoverColor else background.color)
            .onPointerEvent(PointerEventType.Enter) {
                if (inputs.isNotEmpty()) {
                    hovered = true
                }
            }
            .onPointerEvent(PointerEventType.Exit) { hovered = false }
        ,
        contentAlignment = Alignment.BottomStart,
    ) {
        // LogViewer must stay in composition permanently, so its LazyListState is never reset
        // as this causes crashes. The Box height constrains it to 0 when collapsed.
        Box(modifier = Modifier.height(animatedHeight)) {
            ActionSelector(inputs, modifier = Modifier.fillMaxSize(), scrollState) { action ->
                actions.actionSelected(action)
            }
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .width(48.dp)
                .fillMaxHeight()
            ,
            verticalArrangement = Arrangement.Bottom,
        ) {
            if (inputs.isNotEmpty()) {
                Text(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                    fontFamily = JervisTheme.extendedDefaultFontFamily(),
                    text = if (hovered) "▼" else "▲",
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
        }
    }
}




