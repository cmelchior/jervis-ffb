package manual

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DrawerDefaults.shape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.adamglin.composeshadow.dropShadow
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.DBlockResult
import com.jervisffb.engine.actions.DieResult
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.dblock
import com.jervisffb.engine.model.DieId
import com.jervisffb.engine.model.Field
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.StandardBB2020Rules
import com.jervisffb.jervis_ui.generated.resources.Res
import com.jervisffb.jervis_ui.generated.resources.jervis_brush_chalk
import com.jervisffb.ui.createDefaultAwayTeam
import com.jervisffb.ui.createDefaultHomeTeam
import com.jervisffb.ui.debugBorder
import com.jervisffb.ui.dropShadow
import com.jervisffb.ui.game.icons.ActionIcon
import com.jervisffb.ui.game.icons.IconFactory
import com.jervisffb.ui.game.view.ActionButton
import com.jervisffb.ui.game.view.ExpandableDiceSelector
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.view.utils.paperBackground
import com.jervisffb.ui.utils.applyIf
import com.jervisffb.ui.utils.scalePixels
import com.jervisffb.utils.runBlocking
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.imageResource
import org.junit.Test
import sun.java2d.loops.ProcessPath.drawPath
import java.awt.SystemColor.text
import java.util.Collections.rotate
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.test.Ignore


class MenuController(
    viewModel: ActionWheelViewModel,
    startingAngle: Float
) {
    val topLevelMenu = TopLevelMenuItem(viewModel, startingAngle, 120f)
    val menuItems = topLevelMenu.subMenu

    // Add Action Button at the first level
    fun addActionButton(
        label: String,
        icon: ActionIcon,
        onClick: (parent: MenuItem?, button: MenuItem) -> Unit,
        enabled: Boolean = true,
        expandMode: MenuExpandMode = MenuExpandMode.FAN_OUT,
        subMenu: SnapshotStateList<MenuItem> = mutableStateListOf()
    ): ActionMenuItem {
        return topLevelMenu.addActionButton(
            label,
            icon,
            onClick,
            enabled,
            expandMode,
            subMenu
        )
    }

    // Add dice button at the first level
    fun <T: DieResult> addDiceButton(
        id: DieId,
        startValue: T,
        options: List<T>,
        preferLtr: Boolean = true,
        enabled: Boolean = true,
        expandable: Boolean = true,
        animatingFrom: T? = null,
    ): DiceMenuItem<T> {
        return topLevelMenu.addDiceButton(
            id,
            startValue,
            options,
            preferLtr,
            enabled,
            expandable,
            animatingFrom,
        )
    }
}


/**
 * ViewModel wrapping all relevant state we need to modify a Circular Menu.
 * All properties used by the UI should be exposed as [androidx.compose.runtime.MutableState]
 * so the UI can react correctly to changes.
 *
 * At a high level the menu comes in two modes:
 *
 * 1. A "simple" menu system with submenus: In this case, all menu items
 *    are the same. Items can either be evenly spread out or grouped
 *    together.
 *
 * 2. A mix of actions and dice: Dice are placed at the top, actions at the bottom.
 *
 * The 2nd mode is used if [dice] contains elements, otherwise the first mode
 * is selected.
 *
 * The submenu items layout can be configured depend on the type [SubMenuMode]
 * used.
 *
 * The [CircularMenu] will attempt to nicely animate changes to
 * the best of its abilities. If you want to "reset" the state, it is quickest
 * to just create a new [ActionWheelViewModel] instance.
 */
class ActionWheelViewModel {
    // Wheel is shown on screen
    // TODO This should probably be a `shownAt: Offset` instead
    var shown by mutableStateOf(true)
    var topMenu = MenuController(this, -90f)
    var bottomMenu = MenuController(this, 90f)
    var startingHoverText: String? by mutableStateOf("Hello")
    var topMessage: String? by mutableStateOf(null)
    var bottomMessage: String? by mutableStateOf(null)

    fun onActionOptionsExpandChange(item: MenuItem, expanded: Boolean) {
        topMenu.menuItems.forEach {
            it.onActionOptionsExpandChange(item, expanded)
        }
        bottomMenu.menuItems.forEach {
            it.onActionOptionsExpandChange(item, expanded)
        }
    }

    fun rolldice() {
        val dice = topMenu.menuItems
        val currentDie = dice.first()
        val max = dice.size
        topMenu.addDiceButton(
            id = DieId((max + 1).toString()),
            startValue = D6Result.random(),
            options = D6Result.allOptions(),
            animatingFrom = (currentDie as DiceMenuItem<*>).value,
            preferLtr = true,
        )
    }
}

@Ignore // Must run manually
class AnimateTest() {
    @Test
    fun runTest() {
        val rules = StandardBB2020Rules()
        val game = Game(
            rules,
            createDefaultHomeTeam(rules),
            createDefaultAwayTeam(rules),
            Field.createForRuleset(rules),
        )
        val actionWheel = createActionWheel()
        application {
            val density = LocalDensity.current
            runBlocking {
                IconFactory.initializeFumbblMapping()
                IconFactory.initialize(density, game.homeTeam, game.awayTeam)
            }
            val windowState = rememberWindowState()
            Window(onCloseRequest = ::exitApplication, state = windowState) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    // BackgroundImageLayer(FieldDetails.NICE)
                    Box(
                        contentAlignment = Alignment.TopCenter,
                    ) {
                        CircularMenuDemo(actionWheel)
                    }
                }
            }
        }
    }

    private fun createActionWheel(): ActionWheelViewModel {
        val actionWheel = ActionWheelViewModel()
        actionWheel.topMenu.apply {
            addDiceButton(
                id = DieId("1"),
                startValue = 1.d6,
                options = D6Result.allOptions(),
                preferLtr = true,
                expandable = true,
                animatingFrom = 6.d6,
            )
            addDiceButton(
                id = DieId("2"),
                startValue = 1.dblock,
                options = DBlockResult.allOptions(),
                preferLtr = false,
                expandable = true,
                animatingFrom = 4.dblock,
            )
        }
        actionWheel.bottomMenu.apply {
            addActionButton(
                label = "Pro",
                icon = ActionIcon.BLOCK,
                onClick = { parent, item ->
                    /* TODO */
                },
                expandMode = MenuExpandMode.TWO_WAY,
            ).apply {
                addActionButton(
                    label = "Cancel",
                    icon = ActionIcon.CANCEL,
                    onClick = { parent, item ->
                        parent!!.closeSubMenu()
                    }
                )
                addActionButton(
                    label = "Roll",
                    icon = ActionIcon.CONFIRM,
                    onClick = { parent, item ->
                        actionWheel.rolldice()
                        // val dice = actionWheel.rolldice()
                    }
                )
            }
            addActionButton(
                label = "Team Reroll",
                icon = ActionIcon.TEAM_REROLL,
                onClick = { parent, item ->
                    when (val item = actionWheel.topMenu.menuItems.random()) {
                        is DiceMenuItem<*> -> {
                            item.animationDone = false
                        }
                        else -> { /* Do nothing */ }
                    }
                }
            )
        }
        actionWheel.topMessage = "Use Dodge?"
        return actionWheel
    }
}

@Composable
fun CircularMenuDemo(actionWheel: ActionWheelViewModel) {
    CircularMenu(actionWheel)
}

enum class MenuExpandMode {
    NONE,
    TWO_WAY,
    FAN_OUT,
    COMPACT,
}

/**
 * A menu item has two "modes": Expanded and Contracted
 *
 * Expanded: It has been selected and this menu item is at the "center" and its submenues are spread out around it
 * Contracted: It's parent is currently expanded and its layout is determined by that
 *
 */
sealed class MenuItem {
    abstract val label: String
    abstract val parent: MenuItem?
    abstract val enabled: Boolean
    abstract val expandMode: MenuExpandMode
    abstract val subMenu: SnapshotStateList<MenuItem>

    // Note: Angles in this implementation are in degrees, not radians, unless
    // otherwise noted. This is considered the "starting point" for all
    // relative-based calculations. 90° = bottom of the screen.

    open val defaultStartAngle: Float
        get() = parent?.defaultStartAngle ?: 0f

    val startMainAngle = 90f // Angle item should move to when promoted to "main item"
    var startSubAngle = 0f // Which angle to put this menu in when it is displayed as a "sub item"
    open val stepAngle = 45f

    var currentAngleAnim = Animatable(0f)
    var currentAlphaAnim = Animatable(1f)

    var selectedSubMenu: MenuItem? by mutableStateOf(null)

    abstract fun onActionOptionsExpandChange(die: MenuItem, expanded: Boolean)

    private fun recalculateSubMenuAngles() {
        if (subMenu.isEmpty()) return
        when (expandMode) {
            MenuExpandMode.NONE -> error("Not supported")
            MenuExpandMode.TWO_WAY -> {
                subMenu.forEachIndexed { index, item ->
                    when (index) {
                        0 -> {
                            item.startSubAngle = 180f
                            item.currentAngleAnim = Animatable(item.startSubAngle)
                        }
                        1 -> {
                            item.startSubAngle = 0f
                            item.currentAngleAnim = Animatable(item.startSubAngle)
                        }
                        else -> error("Too many item: ${subMenu.size}")
                    }
                }
            }
            MenuExpandMode.FAN_OUT -> {
                // Evenly distribute items in the ring. This means there is always one menu item
                // directly on `centerAngle`
                if (parent == null) {
                    subMenu.forEachIndexed { i, item ->
                        item.startSubAngle = (360f / subMenu.size) * i + defaultStartAngle
                        item.currentAngleAnim = Animatable(item.startSubAngle)
                    }
                } else {
                    val spread = 180f
                    val step = if (subMenu.size > 1) spread / (subMenu.size - 1) else 0f
                    subMenu.forEachIndexed { i, item ->
                        item.startSubAngle = defaultStartAngle + (spread / 2) + (step * i)
                        item.currentAngleAnim = Animatable(item.startSubAngle)
                    }
                }
            }
            MenuExpandMode.COMPACT -> {
                // Clump menu items together at `centerAngle`. For an even number of menu items
                // This means none of them will be directly on `centerAngle`.
                val offset = if (subMenu.size % 2 == 0) stepAngle / 2f else 0f
                val parentModifier = if (parent == null) 0 else 1
                subMenu.forEachIndexed { index, item ->
                    val direction = if (index % 2 == 1) -1 else 1
                    val magnitude = ceil((index + parentModifier) / 2.0).toFloat()
                    item.startSubAngle = (defaultStartAngle + offset + direction * magnitude * stepAngle)
                    item.currentAngleAnim = Animatable(item.startSubAngle)
                }
            }
        }
    }

    fun addActionButton(
        label: String,
        icon: ActionIcon,
        onClick: (parent: MenuItem?, button: MenuItem) -> Unit,
        enabled: Boolean = true,
        expandMode: MenuExpandMode = MenuExpandMode.FAN_OUT,
        subMenu: SnapshotStateList<MenuItem> = mutableStateListOf()
    ): ActionMenuItem {
        val button = ActionMenuItem(
            this,
            label,
            icon,
            onClick,
            enabled,
            expandMode,
            subMenu
        )
        this.subMenu.add(button)
        recalculateSubMenuAngles()
        return button
    }

    fun <T: DieResult> addDiceButton(
        id: DieId,
        startValue: T,
        options: List<T>,
        preferLtr: Boolean = true,
        enabled: Boolean = true,
        expandable: Boolean = true,
        animatingFrom: T? = null,
    ): DiceMenuItem<T> {
        val button = DiceMenuItem(
            this,
            id,
            startValue,
            options,
            preferLtr,
            enabled,
            expandable,
            animatingFrom,
        )
        subMenu.add(button)
        recalculateSubMenuAngles()
        return button
    }

    fun closeSubMenu() {
//        expanded = false
    }
}

// Wrapper representing the "top"-level node of a menu system
class TopLevelMenuItem(
    val viewModel: ActionWheelViewModel,
    originAngle: Float,
    override val stepAngle: Float,
    override val subMenu: SnapshotStateList<MenuItem> = mutableStateListOf()
): MenuItem() {
    override val label: String = "TopLevel"
    val size: Int get() = subMenu.size
    override val parent: MenuItem? = null
    override val enabled: Boolean = true
    override val expandMode: MenuExpandMode = MenuExpandMode.COMPACT
    var selectedMenuItem: MenuItem? by mutableStateOf(null)
    override val defaultStartAngle: Float = originAngle

    override fun onActionOptionsExpandChange(item: MenuItem, expanded: Boolean) {
        // Do nothing
    }

    init {
        startSubAngle = originAngle
    }
}

class ActionMenuItem(
    override val parent: MenuItem,
    override val label: String,
    val icon: ActionIcon,
    val onClick: (parent: MenuItem?, button: MenuItem) -> Unit,
    override val enabled: Boolean = true,
    override val expandMode: MenuExpandMode = MenuExpandMode.FAN_OUT,
    override val subMenu: SnapshotStateList<MenuItem> = mutableStateListOf()
): MenuItem() {

    override fun onActionOptionsExpandChange(item: MenuItem, expanded: Boolean) {
        // Do nothing
    }

}

class DiceMenuItem<T: DieResult>(
    override val parent: MenuItem,
    val id: DieId,
    startValue: T,
    options: List<T>,
    val preferLtr: Boolean = true,
    enabled: Boolean = true,
    val expandable: Boolean = true,
    startAnimationFrom: T? = null,
): MenuItem() {
    override val label: String = "Dice[${startValue::class.simpleName}]"
    override val expandMode: MenuExpandMode = MenuExpandMode.NONE
    override val subMenu: SnapshotStateList<MenuItem> = mutableStateListOf()
    override var enabled: Boolean by mutableStateOf(enabled)
    var animatingFrom: T? by mutableStateOf(startAnimationFrom)
    var animationDone: Boolean by mutableStateOf(startAnimationFrom == null)
    var value: T by mutableStateOf(startValue)
    var diceList: List<T> by mutableStateOf(emptyList())

    // We assume this is sorted
    private val originalOptions = options

    init {
        reorderOptions()
    }

    override fun onActionOptionsExpandChange(item: MenuItem, expanded: Boolean) {
        if (item is DiceMenuItem<*>) {
            enabled = !expanded || !(parent == item.parent && id != item.id)
        }
    }

    private fun reorderOptions() {
        val updatedList  = originalOptions.toMutableList().also {
            it.remove(value)
            it.add(0, value)
        }
        diceList = updatedList
    }

    fun valueSelected(value: T) {
        this.value = value
        reorderOptions()
    }
}

// Controls all the animations for a single "level" of the menu.
// In this case a level correspon
@Composable
private fun CircularMenu(
    viewModel: ActionWheelViewModel,
    ringSize: Dp = 250.dp,
    borderSize: Dp = 20.dp,
    animationDuration: Int = 300,
) {
    // Center of the menu in pixels
    val centerPx = with(LocalDensity.current) { Offset((ringSize/2f).toPx(), (ringSize/2f).toPx()) }
    var hoverText: String? by remember { mutableStateOf(viewModel.startingHoverText) }

    var topMessage = viewModel.topMessage
    val maxSize = (hypot(ringSize.value, ringSize.value)).dp
    Box(
        modifier = Modifier
            .size(maxSize)
            .debugBorder(color = Color.Red)
        ,
        contentAlignment = Alignment.Center
    ) {
        val updateHover = remember {
            { desc: String? -> hoverText = desc }
        }
        ActionWheelBackgroundRing(ringSize, borderSize)
//        NestedMenuLayout(
//            viewModel = viewModel,
//            viewModel.bottomMenu,
//            centerPx,
//            (ringSize - borderSize)/2f,
//            animationDuration,
//            onHover = updateHover,
//        )
////        if (topMessage == null) {
//            NestedMenuLayout(
//                viewModel = viewModel,
//                viewModel.topMenu,
//                centerPx,
//                (ringSize - borderSize)/2f,
//                animationDuration,
//                onHover = updateHover,
//            )
////        } else {
//            RingMessage(
//                message = topMessage,
//                angle = viewModel.topMenu.topLevelMenu.startSubAngle,
//                radius = (ringSize - borderSize)/2f
//            )
//        }
//        HoverText(hoverText)
    }
}

/**
 * Composable responsible for rendering a "message" that is replacing buttons
 * on either the bottom or top.
 */
@Composable
private fun RingMessage(
    message: String?,
    angle: Float,
    radius: Dp,
    borderColor: Color = JervisTheme.rulebookRed,
) {
    val radiusPx = with(LocalDensity.current) { radius.toPx() }
    val offset = remember(angle, message) { getOffset(angle, radiusPx)}
    Box(
        modifier = Modifier
            .offset { offset }
            .dropShadow(
                shape = shape,
                color = Color.Black.copy(1f),
                offsetX = 0.dp,
                offsetY = 0.dp,
                blur = 16.dp
            )
            .border(width = 4.dp, borderColor)
            .paperBackground()
            .padding(start = 32.dp, end = 32.dp, top = 16.dp, bottom = 16.dp)
    ) {
        Text(
            modifier = Modifier,
            text = message ?: "",
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}


private enum class ButtonLayoutMode {
    STABLE,
    EXPAND,
    CONTRACT
}


/**
 * Top-level composable responsible for tracking a nested menu and transitions between the layers.
 * The "starting point" for the menu is defined by [TopLevelMenuItem.startSubAngle].
 *
 * This way you can have multiple nested layouts on the same ring, like buttons at the top
 * and actions at the bottom, but they do not know about each other, so can potentially overlap
 * if configured incorrectly.
 */
@Composable
fun NestedMenuLayout(
    viewModel: ActionWheelViewModel,
    menuController: MenuController,
    // This is required to be the offset in a drawing area so `centerPx.x = radius`
    centerPx: Offset,
    radius: Dp,
    // Where we start all relative calculations. If there is only one button, it will be placed here.
//    startAngle: Float,
//    stepAngle: Float,
    animationDuration: Int,
    onHover: (String?) -> Unit
) {
    val stack = remember { mutableStateListOf<MenuItem>() }
    var currentPrimaryMenu by remember { mutableStateOf<MenuItem?>(null) }
    var mode by remember { mutableStateOf(ButtonLayoutMode.STABLE) }

    CircularMenuLevel(
        mode = mode,
        activeButton = currentPrimaryMenu,
        primaryMenuLevel = currentPrimaryMenu?.parent ?: menuController.topLevelMenu,
        secondaryMenuLevel = stack.lastOrNull(),
        radius = radius,
        animationDuration = animationDuration,
        onItemSelected = { item ->
            if (item != currentPrimaryMenu && item.subMenu.isNotEmpty()) {
                mode = ButtonLayoutMode.EXPAND
                stack.add(item)
                currentPrimaryMenu = item
            } else if (item == currentPrimaryMenu) {
                mode = ButtonLayoutMode.CONTRACT
            } else {
                (item as ActionMenuItem).onClick(item.parent, item)
            }
        },
        onHover = onHover,
        onAnimationOver = {
            when (mode) {
                ButtonLayoutMode.STABLE -> { }
                ButtonLayoutMode.EXPAND -> { }
                ButtonLayoutMode.CONTRACT -> {
                    stack.removeLast()
                    currentPrimaryMenu = stack.lastOrNull()
                }
            }
            mode = ButtonLayoutMode.STABLE
        },
        onExpandChanged = { die, isExpanded ->
            viewModel.onActionOptionsExpandChange(die, isExpanded)
        }
    )
}

// Calculate the shortest distance (in degrees) to a target when moving across a ring
private fun shortestPathToTaget(current: Float, desired: Float): Float {
    val delta = ((((desired - current) % 360f) + 540f) % 360f) - 180f
    return current + delta
}

/**
 * Composable controlling a single "level" of a nested circular menu.
 * It also handles the transitions going back up the chain or diving further into
 * submenus.
 */
@Composable
private fun CircularMenuLevel(
    mode: ButtonLayoutMode,
    activeButton: MenuItem?,
    primaryMenuLevel: MenuItem?,
    // This is only used while animating changes. Once a menu is "stable", it is
    // always sent as the `primaryMenuLevel`.
    secondaryMenuLevel: MenuItem?,
    radius: Dp,
    animationDuration: Int,
    onItemSelected: (MenuItem) -> Unit,
    onHover: (String?) -> Unit,
    onAnimationOver: () -> Unit,
    onExpandChanged: (MenuItem, Boolean) -> Unit,
) {
    val radiusPx = with(LocalDensity.current) { radius.toPx() }

    //    // Animation state for primary menu items
    val mainAngleAnims = remember { mutableMapOf<MenuItem, Animatable<Float, AnimationVector1D>>() }
    val mainAlphaAnims = remember { mutableMapOf<MenuItem, Animatable<Float, AnimationVector1D>>() }

    // Animation state for submenu items for the currently selected main menu)
    val subAngleAnims = remember { mutableMapOf<MenuItem, Animatable<Float, AnimationVector1D>>() }
    val subAlphaAnims = remember { mutableMapOf<MenuItem, Animatable<Float, AnimationVector1D>>() }

    Box(Modifier.size(radius), Alignment.Center) {
        // "sub-level" items should be rendered first so they are behind "top-level" menus
        secondaryMenuLevel?.subMenu?.forEachIndexed { i, item ->
            val base = when (mode) {
                ButtonLayoutMode.STABLE,
                ButtonLayoutMode.EXPAND -> activeButton!!.startSubAngle
                ButtonLayoutMode.CONTRACT -> item.startSubAngle // Should never be used because item is already in the map
            }
            val angleA = subAngleAnims.getOrPut(item) { Animatable(base) }
            val alphaA = subAlphaAnims.getOrPut(item) { Animatable(0f) }

            LaunchedEffect(mode, item, activeButton, primaryMenuLevel?.subMenu, secondaryMenuLevel.subMenu) {
                when (mode) {
                    ButtonLayoutMode.STABLE -> {
                        // println("Sub Stable: ${item.label}")
                        // Animate in new items. Existing items are already in this state.
                        launch {
                            angleA.animateTo(shortestPathToTaget(angleA.value, item.startSubAngle), tween(animationDuration))
                        }
                        launch {
                            alphaA.animateTo(1f, tween(animationDuration))
                        }
                    }
                    ButtonLayoutMode.EXPAND -> {
                        // println("Expand sub level: ${item.label}")
                        launch {
                            angleA.animateTo(shortestPathToTaget(angleA.value, item.startSubAngle), tween(animationDuration))
                        }
                        launch {
                            alphaA.animateTo(1f, tween(animationDuration))
                        }

                    }
                    ButtonLayoutMode.CONTRACT -> {
                        // println("Contract sub level: ${item.label}")
                        launch {
                            angleA.animateTo(shortestPathToTaget(angleA.value, activeButton!!.startSubAngle), tween(animationDuration))
                        }
                        launch {
                            alphaA.animateTo(0f, tween(animationDuration))
                        }
                    }
                }
            }

            // Draw button
            MenuItemButton(
                item,
                angleA.value,
                alphaA.value,
                radiusPx,
                onHover,
                onItemSelected,
                onExpandChanged,
            )
        }

        // Render "top-level" menu
        primaryMenuLevel?.subMenu?.forEachIndexed { i, item ->
            val base = item.startSubAngle
            val angleA = mainAngleAnims.getOrPut(item) { Animatable(base) }
            val alphaA = mainAlphaAnims.getOrPut(item) { Animatable(1f) }
            val isPrimary = (activeButton == item)
            LaunchedEffect(mode, item, item.startSubAngle, activeButton, primaryMenuLevel.subMenu, secondaryMenuLevel?.subMenu) {
                when (mode) {
                    ButtonLayoutMode.STABLE -> {
                        // println("Render main: ${item.label}")
                        // Animate in new items. Existing items are already in this state.
                        launch {
                            angleA.animateTo(
                                shortestPathToTaget(
                                    angleA.value,
                                    if (isPrimary) item.startMainAngle else item.startSubAngle
                                ),
                                tween(animationDuration)
                            )
                        }
                        launch {
                            // If there is no "active button" we are at the top-level and then want to show all buttons
                            alphaA.animateTo(
                                if (isPrimary || activeButton == null) 1f else 0f,
                                tween(animationDuration)
                            )
                        }
                    }
                    ButtonLayoutMode.EXPAND -> {
                        when {
                            isPrimary -> {
                                // println("Move to selected position: ${item.label}")
                                launch {
                                    angleA.animateTo(
                                        shortestPathToTaget(angleA.value, item.startMainAngle),
                                        tween(animationDuration)
                                    )
                                }
                                launch {
                                    alphaA.animateTo(
                                        1f,
                                        tween(animationDuration / 2)
                                    )
                                }
                            }
                            else -> {
                                // println("Fade out: ${item.label}")
                                launch {
                                    alphaA.animateTo(
                                        0f,
                                        tween(animationDuration / 2)
                                    )
                                }
                            }
                        }
                    }
                    ButtonLayoutMode.CONTRACT -> {
                        when {
                            isPrimary -> {
                                // println("Move primary back to starting position: ${item.label}")
                                launch {
                                    angleA.animateTo(
                                        shortestPathToTaget(angleA.value, item.startSubAngle),
                                        tween(animationDuration)
                                    )
                                }
                            }
                            else -> {
                                // println("Fade in: ${item.label}")
                                launch {
                                    alphaA.animateTo(
                                        1f,
                                        tween(animationDuration / 2)
                                    )
                                }
                            }
                        }
                    }
                }
                launch {
                    delay(animationDuration.toLong())
                    onAnimationOver()
                }
            }

            // Draw button on the ring
            MenuItemButton(
                item,
                angleA.value,
                alphaA.value,
                radiusPx,
                onHover,
                onItemSelected,
                onExpandChanged,
            )
        }
    }
}

// Composable responsible for rending the actual action/dice button
@Composable
private fun MenuItemButton(
    item: MenuItem,
    angle: Float,
    alpha: Float,
    radiusPx: Float,
    onHover: (String?) -> Unit,
    onItemSelected: (MenuItem) -> Unit,
    onExpandChanged: (MenuItem, Boolean) -> Unit = {  _, _ -> } ,
) {
    if (alpha <= 0f) return
    val offset = getOffset(angle, radiusPx)
    Box(
        modifier = Modifier
            .offset { offset }
            .alpha(alpha)
    ) {
        when (item) {
            is TopLevelMenuItem -> error("Not supported: $item")
            is ActionMenuItem -> {
                ActionButton(
                    item.label,
                    item.icon,
                    enabled = true,
                    onHover = { onHover(it) },
                    onClick = {
                        onItemSelected(item)
                    }
                )
            }
            is DiceMenuItem<*> -> {
                ExpandableDiceSelector(
                    item,
                    disabled = !item.enabled,
                    onExpandedChanged = onExpandChanged,
                    onAnimationDone = {

                    }
                )
            }
        }
    }
}

//@Composable
//private fun ActionWheelBackgroundRing(
//    ringSize: Dp,
//    borderSize: Dp,
//) {
//    val chalkTexture = imageResource(Res.drawable.jervis_brush_chalk)
//    val imageBrush = remember {
//        ShaderBrush(
//            shader = ImageShader(
//                image = chalkTexture.scalePixels(IconFactory.scaleFactor),
//                tileModeX = TileMode.Repeated,
//                tileModeY = TileMode.Repeated,
//            ),
//        )
//    }
//
//    // Draw the ring
//    Canvas(modifier = Modifier
//        .wrapContentWidth(unbounded = true)
//        .size(ringSize)
//        // now draw into that layer
//        .graphicsLayer {
//            clip = false
//            compositingStrategy = CompositingStrategy.Offscreen
//        }
//    ) {
//        drawCircle(
//            brush = imageBrush,
//            alpha = 1f,
//            colorFilter = ColorFilter.tint(Color.Black.copy(alpha = 0.4f)),
//        )
//        drawCircle(
//            color = Color.Transparent,
//            radius = (size.minDimension - (2*borderSize.toPx())) / 2.0f,
//            blendMode = BlendMode.Clear
//        )
//    }
//}

@Composable
private fun ActionWheelBackgroundRing(
    ringSize: Dp,
    borderSize: Dp,
) {
    val chalkTexture = imageResource(Res.drawable.jervis_brush_chalk)
    val imageBrush = remember {
        ShaderBrush(
            shader = ImageShader(
                image = chalkTexture.scalePixels(IconFactory.scaleFactor),
                tileModeX = TileMode.Repeated,
                tileModeY = TileMode.Repeated,
            ),
        )
    }
    val padding = ((hypot(ringSize.value, ringSize.value)).dp - ringSize) / 2f
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                clip = false
                compositingStrategy = CompositingStrategy.Offscreen
            }
    ) {
        val radius = ringSize.toPx() / 2f
        val center = Offset(size.width / 2f, size.height / 2f)
        val tip = Offset(center.x - ringSize.toPx() / 2f, center.y - ringSize.toPx() / 2f) // tip of the droplet
        val path = Path().apply {
            moveTo(tip.x, tip.y)
            lineTo(x = center.x, y = padding.toPx())
            arcTo(
                rect = Rect(
                    center = center,
                    radius = radius
                ),
                startAngleDegrees = -90f,
                sweepAngleDegrees = 270f,
                forceMoveTo = true
            )
            lineTo(x = tip.x, y = tip.y)
            close()
        }

        rotate(degrees = 0f) {
            drawPath(
                path = path,
                brush = imageBrush,
                alpha = 1f,
                colorFilter = ColorFilter.tint(Color.Black.copy(alpha = 0.4f)),
            )
        }

        val innerRadius = radius - borderSize.toPx()
        val innerPath = Path().apply {
            addOval(Rect(center = center, radius = innerRadius))
        }
        drawPath(
            path = innerPath,
            color = Color.Transparent,
            blendMode = BlendMode.Clear
        )
    }
}


// Helper text that hovers just below the center player.
// Generally, this should be a "hover" effect when mousing over buttons
@Composable
private fun HoverText(message: String?) {
    val fontSize = 14.sp
    val borderWidth = 30f
    val fontWeight = FontWeight.Bold
    val textColor = Color.White
    val borderColor = JervisTheme.rulebookRed

    Box(modifier = Modifier.offset(y = 60.dp).padding(8.dp)) {
//        AnimatedVisibility(
//            visible = (message != null),
//            enter = fadeIn(),
//            exit = fadeOut(),
//        ) {
        Box(
            modifier = Modifier.padding(8.dp)
        ) {
            // Background border
            Text(
                modifier = Modifier
//                        .shadow(8.dp)
                ,
                text = message ?: "",
                style = MaterialTheme.typography.body1.copy(
                    color = borderColor,
                    fontWeight = fontWeight,
                    fontSize = fontSize,
                    drawStyle = Stroke(
                        miter = borderWidth,
                        width = borderWidth,
                        join = StrokeJoin.Round
                    ),
                )
            )
            Text(
                text = message ?: "",
                style = MaterialTheme.typography.body1.copy(
                    color = textColor,
                    fontWeight = fontWeight,
                    fontSize = fontSize,
                ),
            )
        }
//        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ActionButton(
    description: String,
    icon: ActionIcon,
    enabled: Boolean = true,
    onHover: (String?) -> Unit = {},
    onClick: () -> Unit = { },
) {
    val icon = remember(icon) { IconFactory.getActionIcon(icon) }
    Box {
        Image(
            modifier = Modifier
                .dropShadow(
                    shape = CircleShape,
                    color = Color.Black.copy(1f),
                    offsetX = 0.dp,
                    offsetY = 0.dp,
                    blur = 8.dp
                ),
            bitmap = icon,
            contentDescription = "Drop shadow",
            filterQuality = FilterQuality.None,
        )
        Image(
            modifier = Modifier
                .border(2.dp, Color.Black.copy(0.5f), CircleShape)
                .clip(CircleShape)
                .applyIf(enabled) {
                    clickable { onClick() }
                        .onPointerEvent(PointerEventType.Enter) { onHover(description) }
                        .onPointerEvent(PointerEventType.Exit) { onHover(null) }
                }
            ,
            filterQuality = FilterQuality.None,
            bitmap = icon,
            contentDescription = "",
        )
    }
}

/**
 * Composable responsible for handling a single die that can be expanded into a
 * value selector. The options will always expand to the right side.
 *
 * It also supports being animated to a new value. This is done by rotating up
 * in the air before landing, similar to a dice roll.
 *
 * TODO Check location of die and how much space is on the screen before
 *  selecting whether to expand to the right or left side. We could also consider
 *  using different layouts, i.e. more "boxed" when closer to the edge rather
 *  that swapping direction.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun <T : DieResult> ExpandableDiceSelector(
    die: DiceMenuItem<T>,
    // When using multiple selectors, the other selectors are disabled (grayed out)
    // while one is open.
    disabled: Boolean = false,
    onExpandedChanged: (MenuItem, Boolean) -> Unit = { _, _ -> },
    onAnimationDone: () -> Unit = {}
) {

    // Current state tracking
    val density = LocalDensity.current
    val diceValue = die.value
    val diceList = die.diceList
    val shadowColor = Color.Black
    var expanded by remember { mutableStateOf(false) }
    val alpha = if (!die.animationDone) 0f else 1f

    // Properties we animate when expanded and deflating the selector
    val expandDurationMs = 200
    val animation = tween<Float>(expandDurationMs, easing = FastOutLinearInEasing)
    val bgWidthDp = remember { Animatable(0f) }
    val bgAlpha = remember { Animatable(0f) }

    // Padding between background bar and dice buttons
    val backgroundPadding = 8.dp
    val spacingBetweenItems = backgroundPadding / 2f

    val buttonSize = IconFactory.getDiceSizeDp(diceValue)
    val (buttonWidth, buttonHeight) = buttonSize
    val maxWidthDp = (backgroundPadding * 2) + (spacingBetweenItems * (diceList.size - 1)) + (buttonWidth * diceList.size)
    val backgroundHeight = buttonHeight + backgroundPadding

    // Determine the placement of popup
    val (popupDirection, popupOffset) = remember {
        when (die.preferLtr) {
            true -> {
                val padding = with(density) { (backgroundPadding/2).toPx().roundToInt() }
                val adjustment = with(density) {
                    backgroundPadding.toPx().roundToInt() * -1
                }
                LayoutDirection.Ltr to IntOffset(adjustment, -padding)
            }
            false -> {
                val padding = with(density) { (backgroundPadding/2).toPx().roundToInt() }
                val adjustment = with(density) {
                    (maxWidthDp - buttonWidth - backgroundPadding - backgroundPadding / 2).toPx().roundToInt()
                }
                LayoutDirection.Rtl to IntOffset(-adjustment, -padding)
            }
        }
    }

    // Handle opening and closing animation of the dice selector
    if (expanded) {
        LaunchedEffect(Unit) {
            launch { bgWidthDp.animateTo(maxWidthDp.value, animation) }
            launch { bgAlpha.animateTo(0.5f) }
        }
    } else {
        LaunchedEffect(Unit) {
            launch { bgWidthDp.animateTo(0f, animation) }
            launch { bgAlpha.animateTo(0f) }
        }
    }

    // The visible button, but the normal and jumping one.
    Box(
        modifier = Modifier
            .alpha(if (disabled) 0.3f else 1f)
        ,
        contentAlignment = Alignment.CenterStart,
    ) {
        if (!die.animationDone) {
            DiceAnimation(
                die.animatingFrom!!,
                options = diceList,
                diceValue,
                content = { value ->
                    DiceButton(
                        DpSize(buttonWidth, buttonHeight),
                        1f,
                        value = value,
                        enabled = false,
                        dropShadow = true,
                        dropShadowColor = shadowColor,
                    )
                },
                onAnimationEnd = {
                    die.animationDone = true
                    onAnimationDone()
                }
            )
        } else {
            DiceButton(
                DpSize(buttonWidth,  buttonHeight),
                1f,
                value = diceValue,
                enabled = !disabled,
                onClick = {
                    expanded = !expanded
                    onExpandedChanged(die, expanded)
                },
                dropShadow = true,
                dropShadowColor = shadowColor
            )
        }
    }

    if (expanded) {
        Popup(
            alignment = Alignment.TopStart,
            offset = popupOffset,
            onDismissRequest = {
                expanded = false
                onExpandedChanged(die, expanded)
            }
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides popupDirection) {
                Box(
                    modifier = Modifier
                        .padding(start = backgroundPadding / 2f)
                        .alpha(alpha)
                        .height(backgroundHeight)
                        .width(maxWidthDp - backgroundPadding)
                ) {
                    // Expanding background
                    Box(
                        modifier = Modifier
                            .width(bgWidthDp.value.dp)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = bgAlpha.value))
                    )
                    // All buttons in the button group
                    Row(
                        modifier =
                            Modifier
                                .alpha(alpha)
                                .fillMaxHeight()
                                .align(Alignment.CenterStart)
                                .padding(start = backgroundPadding/2)
                        ,
                        horizontalArrangement = Arrangement.spacedBy(spacingBetweenItems),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Generally options are sorted, but we always have the currently
                        // selected value in the front
                        diceList.forEachIndexed { index, label ->
                            if (!expanded && index > 0) return@forEachIndexed
                            val currentBgWidth = bgWidthDp.value.dp
                            val alpha = when (index == 0 || currentBgWidth > 52.dp * (index + 1)) {
                                true -> 1f
                                false -> 0f
                            }
                            DiceButton(
                                buttonSize,
                                alpha,
                                value = label,
                                enabled = !disabled,
                                onClick = {
                                    if (!expanded) {
                                        expanded = true
                                    } else {
                                        // selectedIndex = index
                                        expanded = false
                                        die.valueSelected(die.diceList[index])
                                    }
                                    onExpandedChanged(die, expanded)
                                },
                                dropShadow = false,
                                dropShadowColor = Color.Black,
                            )
                        }
                    }
                }
            }
        }
    }
}

// Composable responsible for doing the dice animation i.e., jump up and spin.
// But generic enough to run the animation on any content.
// A `blur` effect is passed to the `content` which
// provides a `blur` hint to the content being rotated.
@Composable
private fun <T: DieResult> DiceAnimation(
    startingValue: T,
    options: List<T>,
    endValue: T,
    content: @Composable (DieResult) -> Unit = {  _ -> },
    onAnimationEnd: () -> Unit = {},
) {
    val yOffset = remember(startingValue, endValue) { Animatable(0f) }
    val rotation = remember(startingValue, endValue) { Animatable(0f) }
    var displayFace by remember(startingValue, endValue) { mutableStateOf(startingValue) }

    // Total animation is 300 ms
    LaunchedEffect(startingValue, endValue) {
        val randJob = launch {
            // For "complex" dice like Block dice, changing face too quick
            // makes it look really messy. With this approach we change once
            // near the top of the arc before switching to correct result.
            // From manual testing, this seems to be a good trade-off between
            // "hiding" the true result a bit and making it look more "random".
            displayFace = startingValue
            delay(100)
            displayFace = options.random()
            delay(100)
            displayFace = endValue
        }

        // Parallel rotation while airborne
        val rotJob = launch {
            rotation.animateTo(
                targetValue = 360f,
                animationSpec = tween(300, easing = LinearEasing)
            )
        }

        // Jump up
        yOffset.animateTo(
            targetValue = -75f,
            animationSpec = tween(150, easing = LinearOutSlowInEasing)
        )

        // Start falling and stop rotation
        yOffset.animateTo(
            targetValue = 0f,
            animationSpec = tween(150, easing = FastOutLinearInEasing)
        )
        rotJob.cancel()
        randJob.cancel()
        rotation.snapTo(0f)
        displayFace = endValue
        onAnimationEnd()
    }

    Box(
        modifier = Modifier
            .graphicsLayer {
                translationY = yOffset.value
                rotationZ = rotation.value
            }
        ,
        contentAlignment = Alignment.Center
    ) {
        content(displayFace)
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun DiceButton(
    buttonSize: DpSize,
    alpha: Float,
    value: DieResult,
    enabled: Boolean = true,
    onHover: (String?) -> Unit = {},
    onClick: () -> Unit = {},
    dropShadowColor: Color = Color.Black,
    dropShadow: Boolean = true,
) {
    Box(
        modifier = Modifier
            .size(buttonSize)
            .alpha(alpha)
        ,
        contentAlignment = Alignment.Center
    ) {

        val bitmap = IconFactory.getDiceIcon(value)
        Image(
            bitmap = bitmap,
            contentDescription = value.value.toString(),
            modifier = Modifier.fillMaxSize()
                .applyIf(dropShadow) {
                    dropShadow(
                        shape = RoundedCornerShape(4.dp),
                        color = dropShadowColor.copy(1f),
                        offsetX = 0.dp,
                        offsetY = 0.dp,
                        blur = 16.dp,
                    )
                }
                .applyIf(enabled) {
                    onPointerEvent(PointerEventType.Press) {
                        onClick()
                    }
                }
            ,
            contentScale = ContentScale.Fit,
            filterQuality = FilterQuality.None,
        )
    }
}

/**
 * Calculates the offset for displacing an item from the center of a circle
 * to the radius for a given angle in degrees
 */
private fun getOffset(angle: Float, radius: Float): IntOffset {
    val rad = Math.toRadians(angle.toDouble())
    return IntOffset(
        (cos(rad).toFloat() * radius).roundToInt(),
        (sin(rad).toFloat() * radius).roundToInt()
    )
}

private fun getFloatOffset(angle: Float, radius: Float): Offset {
    val rad = Math.toRadians(angle.toDouble())
    return Offset(
        cos(rad).toFloat() * radius,
        sin(rad).toFloat() * radius
    )
}
