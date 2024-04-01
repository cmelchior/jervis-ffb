package dk.ilios.jervis.ui

import UserActionDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonColors
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dk.ilios.jervis.actions.Cancel
import dk.ilios.jervis.actions.CoinSideSelected
import dk.ilios.jervis.actions.CoinTossResult
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
import dk.ilios.jervis.actions.NoRerollSelected
import dk.ilios.jervis.actions.PlayerActionSelected
import dk.ilios.jervis.actions.PlayerDeselected
import dk.ilios.jervis.actions.PlayerSelected
import dk.ilios.jervis.actions.RandomPlayersSelected
import dk.ilios.jervis.actions.RerollOptionSelected
import dk.ilios.jervis.model.FieldSquare
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.PlayerState
import dk.ilios.jervis.ui.images.IconFactory
import dk.ilios.jervis.ui.model.ActionSelectorViewModel
import dk.ilios.jervis.ui.model.UserInputDialog
import dk.ilios.jervis.ui.model.FieldDetails
import dk.ilios.jervis.ui.model.FieldViewModel
import dk.ilios.jervis.ui.model.GameProgress
import dk.ilios.jervis.ui.model.GameStatusViewModel
import dk.ilios.jervis.ui.model.LogViewModel
import dk.ilios.jervis.ui.model.ReplayViewModel
import dk.ilios.jervis.ui.model.SidebarView
import dk.ilios.jervis.ui.model.SidebarViewModel
import dk.ilios.jervis.ui.model.Square
import dk.ilios.jervis.ui.model.UIPlayer
import dk.ilios.jervis.ui.model.UnknownInput
import dk.ilios.jervis.ui.model.UserInput
import java.awt.image.BufferedImage
import java.io.InputStream
import kotlin.random.Random
import kotlinx.coroutines.flow.Flow
import org.jetbrains.skia.Image

// Theme
val debugBorder = BorderStroke(2.dp,Color.Red)

data class FumbblButtonColors(
    private val backgroundColor: Color = Color.Gray,
    private val contentColor: Color = Color.White,
    private val disabledBackgroundColor: Color = Color.DarkGray,
    private val disabledContentColor: Color = Color.White
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
        modifier = modifier
            .height(10.dp)
            .shadow(1.dp)
            .padding(4.dp)
            .background(color = Color.White)
    )
}

@Composable
fun SectionHeader(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(152.42f/(452f/15)),
        verticalAlignment = Alignment.CenterVertically
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

fun loadImageBitmapFromResources(path: String): ImageBitmap {
    // Use the current classloader to get the resource as a stream
    val inputStream: InputStream = Thread.currentThread().contextClassLoader.getResourceAsStream(path)
        ?: throw IllegalArgumentException("Resource not found: $path")

    // Decode the image data
    val skiaImage = Image.makeFromEncoded(inputStream.readBytes())
    return skiaImage.toComposeImageBitmap()
}


@Composable
fun SpriteFromSheet() {
    val spriteSheet: ImageBitmap = loadImageBitmapFromResources("icons/cached/players/iconsets/human_lineman.png")
    val skiaImage: BufferedImage = spriteSheet.toAwtImage()
    val x = (skiaImage.width/28)
    val y = (skiaImage.height/28)
    val imageNo = Random.nextInt(x*y)
    val spriteY: Int = (imageNo/x)*28
    val spriteX: Int = (imageNo % x)*28
    println("$x, $y, $imageNo, $spriteX, $spriteY")
    val spriteWidth: Int = 28
    val spriteHeight: Int = 28
    val playerImage = skiaImage.getSubimage(spriteX, spriteY, 28, 28).toComposeImageBitmap()
    Image(
        bitmap = playerImage,
        contentDescription = null,
        alignment = Alignment.Center,
        contentScale = ContentScale.Fit,
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun Player(modifier: Modifier, player: Player) {
    val backgroundColor = if (player.state == PlayerState.STUNNED) Color.White else Color.Transparent
    Box(modifier = modifier.aspectRatio(1f).background(color = backgroundColor)) {
        val playerImage = remember { IconFactory.getImage(player).toComposeImageBitmap() }
        val ballImage = remember { IconFactory.getHeldBallOverlay().toComposeImageBitmap() }
        Image(
            bitmap = playerImage,
            contentDescription = null,
            alignment = Alignment.Center,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
        if (player.hasBall()) {
            Image(
                bitmap = ballImage,
                contentDescription = null,
                alignment = Alignment.Center,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun Reserves(reserves: Flow<List<Player>>) {
    val state: List<Player> by reserves.collectAsState(emptyList())
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader("Reserves")
        for (index in state.indices step 5) {
            Row {
                val modifier = Modifier.weight(1f).aspectRatio(1f)
                repeat(5) { x ->
                    if (index + x < state.size) {
                        Player(modifier, state[index + x])
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
    knockedOut: SnapshotStateList<UIPlayer>,
    badlyHurt: SnapshotStateList<UIPlayer>,
    seriousInjuries: SnapshotStateList<UIPlayer>,
    dead: SnapshotStateList<UIPlayer>
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader("Knocked Out")
        SectionHeader("Badly Hurt")
        SectionHeader("Seriously Injured")
        SectionHeader("Killed")
        SectionHeader("Banned")
    }
}

@Composable
fun Sidebar(vm: SidebarViewModel, modifier: Modifier) {
    Column(modifier = modifier) {
        Box(modifier = Modifier.aspectRatio(vm.aspectRatio)) {
            Image(
                alignment = Alignment.TopStart,
                painter = painterResource("icons/sidebar/background_box.png"),
                contentDescription = "Box",
                modifier = modifier.fillMaxSize()
            )
            val view by vm.view().collectAsState()
            when(view) {
                SidebarView.RESERVES -> Reserves(vm.reserves())
                SidebarView.INJURIES -> Injuries(
                    vm.knockedOut(),
                    vm.badlyHurt(),
                    vm.seriousInjuries(),
                    vm.dead()
                )
            }

            Row(modifier = Modifier.align(Alignment.BottomCenter)) {
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
                    modifier = Modifier.weight(1f)
                ) {
                    val injuriesCount by vm.injuriesCount().collectAsState()
                    Text(text = "$injuriesCount Out", maxLines = 1)
                }
            }
        }
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
    logs: LogViewModel
) {
    Box {
        Column {
            Row(modifier = Modifier
                .fillMaxWidth()
                .aspectRatio((152.42f+782f+152.42f)/452f),
                verticalAlignment = Alignment.Top
            ) {
                Sidebar(leftDugout, Modifier.weight(152.42f))
                Field(field, Modifier.weight(782f))
                Sidebar(rightDugout, Modifier.weight(152.42f))
            }
            Row(modifier = Modifier
                .fillMaxWidth()
            ) {
                GameStatus(gameStatusController, modifier = Modifier.height(48.dp))
            }
            Row(modifier = Modifier
                .fillMaxWidth()
            ) {
                ReplayController(replayController, modifier = Modifier.height(48.dp))
            }
            Row(modifier = Modifier
                .fillMaxWidth()
            ) {
                LogViewer(logs, modifier = Modifier.width(200.dp))
                ActionSelector(actionSelector, modifier = Modifier.width(200.dp))
            }
        }
    }
}

@Composable
fun GameStatus(vm: GameStatusViewModel, modifier: Modifier) {
    val progress by vm.progress().collectAsState(GameProgress(0, 0, 0, ""))
    val half = if (progress.half == 0) "-" else progress.half.toString()
    val drive = if (progress.half == 0) "-" else progress.half.toString()
    val turn = if (progress.half == 0) "-" else progress.half.toString()
    Box(modifier = modifier
        .fillMaxSize()
        .background(color = Color.White)
    ) {
        Row {
            Text("Half: ${ if (progress.half == 0) "-" else progress.half }")
            Text("Drive: ${ if (progress.drive == 0) "-" else progress.drive }")
            Text("Turn: ${ if (progress.turn == 0) "-" else progress.turn }")
            Text("Active team :${progress.name}")
        }
    }
}

@Composable
fun ReplayController(vm: ReplayViewModel, modifier: Modifier) {
    Box(modifier = modifier
        .fillMaxSize()
        .background(color = Color.Red)
    ) {
        Row {
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
        }
    }
}

@Composable
fun ActionSelector(vm: ActionSelectorViewModel, modifier: Modifier) {
    val actions: UserInput by vm.availableActions.collectAsState(UnknownInput(emptyList()))
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
            .background(color = Color.Blue),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Button(
            modifier = Modifier.padding(0.dp),
            contentPadding = PaddingValues(2.dp),
            onClick = { vm.start() }
        ) {
            Text("Start Game", fontSize = 10.sp)
        }
        when (actions) {
            is UserInputDialog -> {
                UserActionDialog(actions as UserInputDialog, vm)
            }
            else -> {
                if (actions is UnknownInput) {
                    actions.actions.forEach { action: GameAction ->
                        Button(
                            modifier = Modifier.padding(0.dp),
                            contentPadding = PaddingValues(2.dp),
                            onClick = { vm.actionSelected(action) }
                        ) {
                            val text = when(action) {
                                Confirm -> "Confirm"
                                Continue -> "Continue"
                                is DieResult -> action.toString()
                                DogoutSelected -> "DogoutSelected"
                                EndSetup -> "EndSetup"
                                EndTurn -> "EndTurn"
                                is FieldSquareSelected -> action.toString()
                                is PlayerSelected -> "Player[${action.player.name}, ${action.player.number.number}]"
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
                            }
                            Text(text, fontSize = 10.sp)
                        }
                    }
                } else {
                    TODO("Unsupported type: $actions")
                }
            }
        }
    }
}

@Composable
fun LogViewer(vm: LogViewModel, modifier: Modifier) {
    val listData by vm.logs.collectAsState(initial = emptyList())
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(items = listData, key = { item -> item.hashCode() }) {
            Text(text = it.message)
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Field(vm: FieldViewModel, modifier: Modifier) {
    val field: FieldDetails by vm.field().collectAsState()
    val highlightedSquare: Square? by vm.highlights().collectAsState()

    Box(modifier = modifier
        .fillMaxSize()
        .aspectRatio(vm.aspectRatio)
    ) {
        Image(
            painter = painterResource(field.resource),
            contentDescription = field.description,
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.TopStart)
        )
        Column(modifier = Modifier
            .fillMaxSize()
        ) {
            repeat(vm.height) { height: Int ->
                Row(modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                ) {
                    repeat(vm.width) { width ->
                        val hover: Boolean by remember {
                            derivedStateOf {
                                Square(width, height) == highlightedSquare
                            }
                        }
                        val square: FieldSquare by vm.observeSquare(width, height).collectAsState(FieldSquare(-1, -1))
                        val boxModifier = Modifier.fillMaxSize().weight(1f)
                        Box(modifier = boxModifier
                            .background(color = if (hover) {
                                Color.Cyan.copy(alpha = 0.25f)
                            } else {
                                Color.Transparent
                            })
                            .onPointerEvent(PointerEventType.Enter) {
                                vm.hoverOver(Square(width, height))
                            }
                        ) {
                            square.player?.let {
                                Player(boxModifier, it)
                            }
                            square.ball?.let {
                              Image(bitmap = IconFactory.getBall().toComposeImageBitmap(), contentDescription = "")
                            }
                        }
                    }
                }
            }
        }
    }
}