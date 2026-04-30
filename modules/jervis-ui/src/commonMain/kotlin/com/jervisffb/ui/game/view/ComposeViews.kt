package com.jervisffb.ui.game.view

import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextFieldDefaults.contentPadding
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
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
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.jervisffb.engine.model.Player
import com.jervisffb.ui.dropShadow
import com.jervisffb.ui.game.dialogs.AbstractActionWheelViewModel
import com.jervisffb.ui.game.dialogs.DicePoolUserInputDialog
import com.jervisffb.ui.game.dialogs.MultipleChoiceUserInputDialog
import com.jervisffb.ui.game.dialogs.SingleChoiceInputDialog
import com.jervisffb.ui.game.dialogs.UserInputDialog
import com.jervisffb.ui.game.model.UiPlayerCard
import com.jervisffb.ui.game.viewmodel.DialogsViewModel
import com.jervisffb.ui.game.viewmodel.LogViewModel
import com.jervisffb.ui.game.viewmodel.RandomActionsControllerViewModel
import com.jervisffb.ui.game.viewmodel.ReplayControllerViewModel
import com.jervisffb.ui.game.viewmodel.ReplayState
import com.jervisffb.ui.utils.jdp
import com.jervisffb.ui.utils.jsp
import kotlinx.coroutines.flow.map

@Composable
fun SectionDivider(modifier: Modifier) {
    Box(
        modifier =
            modifier
                .padding(4.jdp)
                .height(2.jdp)
                .background(color = Color.White)
                .dropShadow(
                    color = Color.Black,
                    offsetX = 0.dp,
                    offsetY = 0.5.dp,
                    blurRadius = 1.dp
                )
    )
}

@Composable
fun SectionHeader(title: String) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(start = 4.jdp, end = 4.jdp)
                .aspectRatio(152.42f / (452f / 15))
        ,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SectionDivider(modifier = Modifier.weight(1f))
        Text(
            text = title,
            color = Color.White,
            maxLines = 1,
            lineHeight = 1.em,
            textAlign = TextAlign.Center,
            modifier = Modifier.wrapContentSize(),
            style = LocalTextStyle.current.copy(
                fontSize = 20.jsp,
                shadow = Shadow(
                    color = Color.Black,
                    offset = Offset(2f, 2f),
                    blurRadius = 2f
                )
            )
        )
        SectionDivider(modifier = Modifier.weight(1f))
    }
}

@Composable
fun ReplayCommandBar(
    vm: ReplayControllerViewModel,
    modifier: Modifier,
) {
    val state by vm.state.collectAsState()
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(color = Color.Red),
    ) {
        Row(modifier = Modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            when (state) {
                ReplayState.STARTED -> {
                    Button(modifier = Modifier.weight(1f), onClick = { vm.pause() }) {
                        Text("Pause Replay")
                    }
                }
                ReplayState.PAUSED ->  {
                    Button(modifier = Modifier.weight(1f), onClick = { vm.start() }) {
                        Text("Start Replay")
                    }
                }
            }
        }
    }
}

@Composable
fun RandomCommandBar(
    vm: RandomActionsControllerViewModel,
    modifier: Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(color = Color.Red),
    ) {
        Row(modifier = Modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { vm.startActions() }) {
                Text("Start")
            }
            Button(onClick = { vm.pauseActions() }) {
                Text("Pause")
            }
        }
    }
}

@Composable
fun Dialogs(vm: DialogsViewModel) {
    val dialogData: UserInputDialog? by vm.dialogData.collectAsState(null)
    val contextMenuData: Player? by vm.contextMenu.collectAsState(null)
    when (dialogData) {
        is SingleChoiceInputDialog -> {
            val dialog = dialogData as SingleChoiceInputDialog
            SingleSelectUserActionDialog(dialog, vm)
        }
        is MultipleChoiceUserInputDialog -> {
            val dialog = dialogData as MultipleChoiceUserInputDialog
            MultipleSelectUserActionDialog(dialog, vm)
        }
        is DicePoolUserInputDialog -> {
            val dialog = dialogData as DicePoolUserInputDialog
            DicePoolSelectorDialog(dialog, vm)
        }
        is AbstractActionWheelViewModel,
        null -> { /* Do nothing */ }
    }
    contextMenuData?.let { player ->
        PlayerContextMenuDialog(vm.screenViewModel, UiPlayerCard(player))
    }
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LogViewer(
    vm: LogViewModel,
    modifier: Modifier,
    listState: LazyListState
) {
    var tabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Game", "Debug")

    Column(modifier = modifier) {
        if (vm.showDebugLogs) {
            var hovered by remember { mutableStateOf(false) }
            Box(
                Modifier
                    .onPointerEvent(PointerEventType.Enter) { hovered = true }
                    .onPointerEvent(PointerEventType.Exit) { hovered = false }
                ,
                contentAlignment = Alignment.TopStart
            ) {

                // Always show the content for the selected tab
                when (tabIndex) {
                    0 -> GameLog(vm, listState)
                    1 -> DebugLog(vm)
                }

                // Show the tab bar over the content when the mouse hovers over area
                // This way, it leaves more space available on 16:9 screens
                if (hovered) {
                    PrimaryTabRow(
                        selectedTabIndex = tabIndex,
                        containerColor = JervisTheme.rulebookGreen.copy(0.9f),
                        indicator = {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabIndex, matchContentSize = false),
                                color = Color.White,
                            )
                        }
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                text = { Text(title, color = Color.White) },
                                selected = tabIndex == index,
                                onClick = { tabIndex = index }
                            )
                        }
                    }
                }
            }
        } else {
            GameLog(vm, listState)
        }
    }
}

@Composable
fun GameLog(vm: LogViewModel, listState: LazyListState) {
    val listData by remember(vm.logs) { vm.logs.map { it.asReversed() } }.collectAsState(initial = emptyList())
    LaunchedEffect(listData) {
        listState.scrollToItem(0)
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .animateContentSize(
                alignment = Alignment.BottomCenter,
            )
        ,
        reverseLayout = true,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        state = listState
    ) {
        items(items = listData, key = { item -> item.id }) {
            Text(
                color = Color.White,
                text = it.message,
                fontSize = 14.sp,
                lineHeight = if (it.message.lines().size > 1) 1.5.em else 1.0.em,
            )
        }
    }
}

@Composable
fun DebugLog(vm: LogViewModel) {
    val listData by vm.debugLogs.collectAsState(initial = emptyList())
    val listState = rememberLazyListState()
    LaunchedEffect(listData) {
        if (listData.isNotEmpty()) {
            listState.scrollToItem(listData.lastIndex)
        }
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        state = listState
    ) {
        items(items = listData, key = { item -> item.id }) {
            Text(
                color = Color.White,
                text = it.message,
                softWrap = true,
                lineHeight = if (it.message.lines().size > 1) 1.5.em else 1.0.em,
            )
        }
    }
}


