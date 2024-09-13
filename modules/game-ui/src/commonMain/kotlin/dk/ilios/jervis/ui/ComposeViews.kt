package dk.ilios.jervis.ui

import MultipleSelectUserActionDialog
import UserActionDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonColors
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import dk.ilios.jervis.actions.CalculatedAction
import dk.ilios.jervis.actions.Cancel
import dk.ilios.jervis.actions.CoinSideSelected
import dk.ilios.jervis.actions.CoinTossResult
import dk.ilios.jervis.actions.CompositeGameAction
import dk.ilios.jervis.actions.Confirm
import dk.ilios.jervis.actions.Continue
import dk.ilios.jervis.actions.DiceResults
import dk.ilios.jervis.actions.DieResult
import dk.ilios.jervis.actions.DogoutSelected
import dk.ilios.jervis.actions.EndAction
import dk.ilios.jervis.actions.EndSetup
import dk.ilios.jervis.actions.EndTurn
import dk.ilios.jervis.actions.FieldSquareSelected
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.InducementSelected
import dk.ilios.jervis.actions.MoveTypeSelected
import dk.ilios.jervis.actions.NoRerollSelected
import dk.ilios.jervis.actions.PlayerActionSelected
import dk.ilios.jervis.actions.PlayerDeselected
import dk.ilios.jervis.actions.PlayerSelected
import dk.ilios.jervis.actions.PlayerSubActionSelected
import dk.ilios.jervis.actions.RandomPlayersSelected
import dk.ilios.jervis.actions.RerollOptionSelected
import dk.ilios.jervis.actions.SkillSelected
import dk.ilios.jervis.actions.Undo
import dk.ilios.jervis.ui.images.IconFactory
import dk.ilios.jervis.ui.viewmodel.ActionSelectorViewModel
import dk.ilios.jervis.ui.viewmodel.CompositeUserInput
import dk.ilios.jervis.ui.viewmodel.DialogsViewModel
import dk.ilios.jervis.ui.viewmodel.DiceRollUserInputDialog
import dk.ilios.jervis.ui.viewmodel.FieldViewModel
import dk.ilios.jervis.ui.viewmodel.GameProgress
import dk.ilios.jervis.ui.viewmodel.GameStatusViewModel
import dk.ilios.jervis.ui.viewmodel.LogViewModel
import dk.ilios.jervis.ui.viewmodel.ReplayViewModel
import dk.ilios.jervis.ui.viewmodel.SidebarViewModel
import dk.ilios.jervis.ui.viewmodel.SingleChoiceInputDialog
import dk.ilios.jervis.ui.viewmodel.UnknownInput
import dk.ilios.jervis.ui.viewmodel.UserInput
import dk.ilios.jervis.ui.viewmodel.WaitingForUserInput

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

@Composable
fun SectionDivider(modifier: Modifier) {
    Box(
        modifier =
            modifier
                .height(10.dp)
                .shadow(1.dp)
                .padding(4.dp)
                .background(color = Color.White),
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
            modifier = Modifier.wrapContentSize().shadow(2.dp),
        )
        SectionDivider(modifier = Modifier.weight(1f))
    }
}

@Composable
fun Screen(
    field: FieldViewModel,
    leftDugout: SidebarViewModel,
    rightDugout: SidebarViewModel,
    gameStatusController: GameStatusViewModel,
    replayController: ReplayViewModel,
    actionSelector: ActionSelectorViewModel,
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
            ReplayController(replayController, actionSelector, modifier = Modifier.height(48.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                LogViewer(logs, modifier = Modifier.width(200.dp))
                ActionSelector(actionSelector, modifier = Modifier.width(200.dp))
            }
        }
        Column(modifier = Modifier.weight(145f).align(Alignment.Top)) {
            Sidebar(rightDugout, Modifier)
        }
    }
}

@Composable
fun GameStatus(
    vm: GameStatusViewModel,
    modifier: Modifier,
) {
    val progress by vm.progress().collectAsState(GameProgress(0, 0, "", 0, "", 0))
    Box(modifier = modifier) {
        Image(
            bitmap = IconFactory.getScorebar(),
            contentDescription = null,
            alignment = Alignment.Center,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize(),
        )
        val textModifier = Modifier.padding(4.dp)

        // Turn counter
        Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                modifier = textModifier,
                text = "Turn",
                fontSize = 14.sp,
                color = Color.White,
            )
            Text(
                modifier = textModifier,
                text = "${progress.homeTeamTurn} / ${progress.awayTeamTurn}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
            )

            val half = when (progress.half) {
                1 -> "1st half"
                2 -> "2nd half"
                3 -> "Overtime"
                else -> null
            }
            if (half != null) {
                Text(
                    modifier = textModifier,
                    text = "of $half",
                    fontSize = 14.sp,
                    color = Color.White,
                )
            }
        }

        // Score counter
        // TODO Need to scale the distance between them
        Row(modifier = Modifier.align(Alignment.Center), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${progress.homeTeamScore}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
            )
            Spacer(Modifier.width(78.dp))
            Text(
                text = "${progress.awayTeamScore}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
            )
        }


    }
}

@Composable
fun ReplayController(
    vm: ReplayViewModel,
    actionSelector: ActionSelectorViewModel,
    modifier: Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(color = Color.Red),
    ) {
        Row(modifier = Modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { vm.enableReplay() }) {
                Text("Start replay")
            }
            Button(onClick = { vm.rewind() }) {
                Text("Rewind")
            }
            Button(onClick = { vm.back() }) {
                Text("Back")
            }
            Button(onClick = { vm.forward() }) {
                Text("Forward")
            }
            Button(onClick = { vm.stopReplay() }) {
                Text("Stop replay")
            }
            Button(onClick = { actionSelector.start() }) {
                Text("Start Game")
            }
        }
    }
}

@Composable
fun Dialogs(vm: DialogsViewModel) {
    val dialogData: UserInput? by vm.availableActions.collectAsState(null)
    when (dialogData) {
        is SingleChoiceInputDialog -> {
            val dialog = dialogData as SingleChoiceInputDialog
            UserActionDialog(dialog, vm)
        }
        is DiceRollUserInputDialog -> {
            val dialog = dialogData as DiceRollUserInputDialog
            MultipleSelectUserActionDialog(dialog, vm)
        }
        null -> { /* Do nothing */ }
        else -> TODO("Not supported: $dialogData")
    }
}

@Composable
fun ActionSelector(
    vm: ActionSelectorViewModel,
    modifier: Modifier,
) {
    val inputs: UserInput? by vm.availableActions.collectAsState(null)
    Column(
        modifier =
            modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .background(color = Color.Blue),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        val userInputs: List<UserInput> =
            remember(inputs) {
                when (inputs) {
                    is CompositeUserInput -> (inputs as CompositeUserInput).inputs
                    null -> emptyList()
                    else -> listOf(inputs!!)
                }
            }

        userInputs.forEach { input ->
            val actions = input.actions
            when (input) {
                is WaitingForUserInput -> {
                    // Do nothing
                }
                is UnknownInput -> {
                    actions.forEach { action: GameAction ->
                        Button(
                            modifier = Modifier.padding(0.dp),
                            contentPadding = PaddingValues(2.dp),
                            onClick = { vm.actionSelected(action) },
                        ) {
                            val text =
                                when (action) {
                                    Confirm -> "Confirm"
                                    Continue -> "Continue"
                                    is DieResult -> action.toString()
                                    DogoutSelected -> "DogoutSelected"
                                    EndSetup -> "EndSetup"
                                    EndTurn -> "EndTurn"
                                    is FieldSquareSelected -> action.toString()
                                    is PlayerSelected -> "Player[${action.playerId}]"
                                    is DiceResults -> action.rolls.joinToString(prefix = "DiceRolls[", postfix = "]")
                                    is PlayerActionSelected -> "Action: ${action.action.name}"
                                    PlayerDeselected -> "Deselect active player"
                                    EndAction -> "End Action"
                                    Cancel -> "Cancel"
                                    is CoinSideSelected -> "Selected: ${action.side}"
                                    is CoinTossResult -> "Coin flip: ${action.result}"
                                    is RandomPlayersSelected -> "Random players: $action"
                                    NoRerollSelected -> "No reroll"
                                    is RerollOptionSelected -> action.option.toString()
                                    is MoveTypeSelected -> action.moveType.toString()
                                    Undo -> TODO()
                                    is CompositeGameAction -> action.list.joinToString(prefix = "[", postfix = "]")
                                    is PlayerSubActionSelected -> action.action.toString()
                                    is SkillSelected -> action.skill.toString()
                                    is InducementSelected -> action.name
                                    is CalculatedAction -> TODO("Should only be used in tests")
                                }
                            Text(text, fontSize = 10.sp)
                        }
                    }
                }
                else -> TODO("Unsupported type: $actions")
            }
        }
    }
}

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
        items(items = listData, key = { item -> item.hashCode() }) {
            Text(
                text = it.message,
                lineHeight = if (it.message.lines().size > 1) 1.5.em else 1.0.em,
            )
        }
    }
}
