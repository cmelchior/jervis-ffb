package com.jervisffb.ui.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Button
import androidx.compose.material.ButtonColors
import androidx.compose.material.Divider
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.jervisffb.ui.dialogs.DicePoolUserInputDialog
import com.jervisffb.ui.dialogs.DiceRollUserInputDialog
import com.jervisffb.ui.dialogs.SingleChoiceInputDialog
import com.jervisffb.ui.dialogs.UserInputDialog
import com.jervisffb.ui.viewmodel.ActionSelectorViewModel
import com.jervisffb.ui.viewmodel.DialogsViewModel
import com.jervisffb.ui.viewmodel.FieldViewModel
import com.jervisffb.ui.viewmodel.GameStatusViewModel
import com.jervisffb.ui.viewmodel.LogViewModel
import com.jervisffb.ui.viewmodel.RandomActionsControllerViewModel
import com.jervisffb.ui.viewmodel.ReplayControllerViewModel
import kotlin.uuid.ExperimentalUuidApi

// Theme
val debugBorder = BorderStroke(2.dp, Color.Red)

data class FumbblButtonColors(
    private val backgroundColor: Color = Color.Gray,
    private val contentColor: Color = Color.White,
    private val disabledBackgroundColor: Color = Color.DarkGray,
    private val disabledContentColor: Color = Color.White,
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

// TODO Figure out how to do drop shadows
@Composable
fun SectionDivider(modifier: Modifier) {
    Box(
        modifier =
            modifier
                .padding(4.dp)
                .height(2.dp)
                .background(color = Color.White)
//                .dropShadow(color = Color.Red, offsetX = 2.dp, offsetY = 2.dp, blurRadius = 2.dp)

    )
}

@Composable
fun SectionHeader(title: String) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .aspectRatio(152.42f / (452f / 15)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SectionDivider(modifier = Modifier.weight(1f))
        Text(
            text = title,
            color = Color.White,
            maxLines = 1,
            modifier = Modifier.wrapContentSize(),
            style = LocalTextStyle.current.copy(
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
fun Screen(
    field: FieldViewModel,
    leftDugout: com.jervisffb.ui.viewmodel.SidebarViewModel,
    rightDugout: com.jervisffb.ui.viewmodel.SidebarViewModel,
    gameStatusController: GameStatusViewModel,
    replayActionsBar: ReplayControllerViewModel? = null,
    randomActionsBar: RandomActionsControllerViewModel? = null,
    unknownActions: ActionSelectorViewModel,
    logs: LogViewModel,
    dialogsViewModel: DialogsViewModel,
) {
    Dialogs(dialogsViewModel)
    val aspectRation = (145f+145f+782f)/690f
    Row(modifier = Modifier.aspectRatio(aspectRation).fillMaxSize()) {
        Column(modifier = Modifier.weight(145f).align(Alignment.Top)) {
            Sidebar(leftDugout, Modifier)
        }
        Column(modifier = Modifier.weight(782f).align(Alignment.Top)) {
            Field(field, Modifier.aspectRatio(field.aspectRatio))
            GameStatus(gameStatusController, modifier = Modifier.aspectRatio(782f/32f).fillMaxSize())
            // ReplayController(replayController, actionSelector, modifier = Modifier.height(48.dp))
            Row(modifier = Modifier.fillMaxSize()) {
                LogViewer(logs, modifier = Modifier.weight(1f).fillMaxSize())
                Divider(color = Color.LightGray, modifier = Modifier.fillMaxHeight().width(1.dp))
                Column(modifier = Modifier.weight(1f).fillMaxSize()) {
                    if (replayActionsBar != null) {
                        ReplayCommandBar(replayActionsBar, modifier = Modifier)
                    }
                    if (randomActionsBar != null) {
                        RandomCommandBar(randomActionsBar, modifier = Modifier)
                    }
                    ActionSelector(unknownActions, modifier = Modifier.fillMaxSize())
                }

            }
        }
        Column(modifier = Modifier.weight(145f).align(Alignment.Top)) {
            Sidebar(rightDugout, Modifier)
        }
    }
}



@Composable
fun ReplayCommandBar(
    vm: ReplayControllerViewModel,
    modifier: Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(color = Color.Red),
    ) {
        Row(modifier = Modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(modifier = Modifier.weight(1f), onClick = { vm.enableReplay() }) {
                Text("Start replay")
            }
            Button(modifier = Modifier.weight(1f), onClick = { vm.rewind() }) {
                Text("Rewind")
            }
            Button(modifier = Modifier.weight(1f), onClick = { vm.back() }) {
                Text("Back")
            }
            Button(modifier = Modifier.weight(1f), onClick = { vm.forward() }) {
                Text("Forward")
            }
            Button(modifier = Modifier.weight(1f), onClick = { vm.stopReplay() }) {
                Text("Stop replay")
            }
            Button(modifier = Modifier.weight(1f), onClick = { vm.start() }) {
                Text("Start Game")
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
    val dialogData: UserInputDialog? by vm.availableActions.collectAsState(null)
    when (dialogData) {
        is SingleChoiceInputDialog -> {
            val dialog = dialogData as SingleChoiceInputDialog
            UserActionDialog(dialog, vm)
        }
        is DiceRollUserInputDialog -> {
            val dialog = dialogData as DiceRollUserInputDialog
            MultipleSelectUserActionDialog(dialog, vm)
        }
        is DicePoolUserInputDialog -> {
            val dialog = dialogData as DicePoolUserInputDialog
            DicePoolSelectorDialog(dialog, vm)
        }
        null -> { /* Do nothing */ }
    }
}


@OptIn(ExperimentalUuidApi::class)
@Composable
fun LogViewer(
    vm: LogViewModel,
    modifier: Modifier,
) {
    val listData by vm.logs.collectAsState(initial = emptyList())
    val listState = rememberLazyListState()

    LaunchedEffect(listData) {
        if (listData.isNotEmpty()) {
            listState.scrollToItem(listData.size - 1)
        }
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        state = listState
    ) {
        items(items = listData, key = { item -> item.id.toString() }) {
            Text(
                text = it.message,
                lineHeight = if (it.message.lines().size > 1) 1.5.em else 1.0.em,
            )
        }
    }
}
