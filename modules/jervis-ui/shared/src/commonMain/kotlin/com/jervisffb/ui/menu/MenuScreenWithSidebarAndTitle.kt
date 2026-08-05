package com.jervisffb.ui.menu

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.skiaCanvas
import androidx.compose.ui.graphics.skiaPaint
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.jervisffb.shared.generated.resources.Res
import com.jervisffb.shared.generated.resources.jervis_frontpage_elf_vs_skeleton
import com.jervisffb.shared.generated.resources.jervis_frontpage_griff
import com.jervisffb.shared.generated.resources.jervis_frontpage_mummy
import com.jervisffb.shared.generated.resources.jervis_frontpage_wall_player
import com.jervisffb.shared.generated.resources.jervis_icon_menu_back
import com.jervisffb.shared.generated.resources.jervis_icon_menu_settings
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.view.SidebarEntryState
import com.jervisffb.ui.game.view.utils.OrangeTitleBorder
import com.jervisffb.ui.game.view.utils.paperBackground
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.components.JervisTooltipArea
import com.jervisffb.ui.menu.components.JervisTooltipPlacement
import com.jervisffb.ui.menu.intro.createGrayscaleNoiseShader
import com.jervisffb.ui.menu.intro.loadJervisFont
import com.jervisffb.ui.utils.applyIf
import com.jervisffb.ui.utils.containsBelowBaselineChars
import com.jervisffb.ui.utils.darken
import com.jervisffb.ui.utils.jdp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.skia.Point
import org.jetbrains.skia.TextLine
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.tan

// Represents a line in a coordinate system
class Line(private val p1: Point, private val p2: Point) {
    private val slope: Float
    private val intercept: Float
    init {
        slope = (p2.y - p1.y) / (p2.x - p1.x)
        intercept = p1.y - slope * p1.x
    }
    fun getY(x: Float): Float {
        return slope * x + intercept
    }
}

@Composable
fun MenuScreenWithSidebarAndTitle(
    menuViewModel: MenuViewModel,
    title: String,
    icon: DrawableResource?,
    // Content at the top, next to the "Back" arrow on the left side
    topMenuLeftContent: (@Composable RowScope.() -> Unit)? = null,
    // Content at the top, next to the "Setting" icon on the right side
    topMenuRightContent: (@Composable RowScope.() -> Unit)? = null,
    sidebarContent: @Composable () -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize().paperBackground(JervisTheme.rulebookPaper),
        contentAlignment = Alignment.TopStart,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box {
                TitleBarWithSidebar(
                    modifier = Modifier.fillMaxHeight(0.20f).fillMaxWidth(),
                    title = title,
                )
                Row(
                    modifier = Modifier.align(Alignment.TopEnd).padding(start = 16.dp, top = 4.dp, end = 8.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    topMenuRightContent?.let {
                        it(this)
                    }
                    TopbarButton(
                        Res.drawable.jervis_icon_menu_settings,
                        "Settings",
                        onClick = { menuViewModel.openSettings(true) }
                    )
                }
            }
            Box(modifier = Modifier
                .padding(start = 282.dp)
                .fillMaxSize()
                .weight(1f)
                , contentAlignment = Alignment.Center
            ) {
                content()
            }
            Row(modifier = Modifier.height(48.dp).fillMaxWidth().paperBackground(JervisTheme.rulebookRed)) {

            }
        }
        MenuSidebar(menuViewModel, sidebarContent, topMenuLeftContent)
        when (icon) {
            Res.drawable.jervis_frontpage_griff -> {
                Image(
                    modifier = Modifier.align(Alignment.BottomStart).width(330.dp).offset(x = -10.dp, y = 0.dp).scale(scaleX = 1f, scaleY = 1f),
                    painter = painterResource(Res.drawable.jervis_frontpage_griff),
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                )
            }
            Res.drawable.jervis_frontpage_elf_vs_skeleton -> {
                Image(
                    modifier = Modifier.align(Alignment.BottomStart).width(330.dp).offset(x = 10.dp, y = 10.dp),
                    painter = painterResource(Res.drawable.jervis_frontpage_elf_vs_skeleton),
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                )
            }
            Res.drawable.jervis_frontpage_wall_player -> {
                Image(
                    modifier = Modifier.align(Alignment.BottomStart).width(420.dp).offset(x = 0.dp, y = 20.dp),
                    painter = painterResource(Res.drawable.jervis_frontpage_wall_player),
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                )
            }
            Res.drawable.jervis_frontpage_mummy -> {
                Image(
                    modifier = Modifier.align(Alignment.BottomStart).width(340.dp).offset(x = -0.dp /*-40.dp*/, y = 0.dp /*15.dp*/),
                    painter = painterResource(Res.drawable.jervis_frontpage_mummy),
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                )
            }
            null -> { /* Show nothing */ }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun TopbarButton(icon: DrawableResource, contentDescription: String, onClick: () -> Unit) {
    JervisTooltipArea(
        tooltip = {
            Surface(
                tonalElevation = 4.dp,
                shadowElevation = 4.dp,
                shape = RoundedCornerShape(8.dp),
                color = JervisTheme.white.copy(alpha = 0.95f),
            ) {
                Text(
                    text = contentDescription,
                    Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = JervisTheme.extendedDefaultFontFamily(),
                        baselineShift = BaselineShift(0.05f)
                    ),
                )
            }
        },
        modifier = Modifier.requiredSize(48.dp),
        delayMillis = 300,
        tooltipPlacement = JervisTooltipPlacement.CursorPoint(offset = DpOffset((-16).dp, 16.dp))
    ) {
        var isHovered by remember { mutableStateOf(false) }
        Image(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .fillMaxSize()
                .alpha(if (isHovered) 1f else 0.8f)
                .clickable { onClick() }
                .onPointerEvent(PointerEventType.Enter) { isHovered = true }
                .onPointerEvent(PointerEventType.Exit) { isHovered = false }
                .padding(8.dp)
            ,
            painter = painterResource(icon),
            contentDescription = contentDescription,
            contentScale = ContentScale.FillHeight,
            colorFilter = ColorFilter.tint(JervisTheme.white),
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TopbarButton(title: String, onClick: () -> Unit) {
    var isHovered by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .clip(shape = RoundedCornerShape(4.dp))
            .height(48.dp)
            .alpha(if (isHovered) 1f else 0.8f)
            .clickable { onClick() }
            .onPointerEvent(PointerEventType.Enter) { isHovered = true }
            .onPointerEvent(PointerEventType.Exit) { isHovered = false }
            .padding(8.dp)
        ,
        contentAlignment = Alignment.Center,
    ) {
        Text(
            lineHeight = 1.0.em,
            text = title.uppercase(),
            fontWeight = FontWeight.Medium,
            color = JervisTheme.white
        )
    }
}

@Composable
fun MenuSidebar(menuViewModel: MenuViewModel, sidebarContent: @Composable () -> Unit, topMenuLeftContent: @Composable (RowScope.() -> Unit)?) {
    Box(modifier = Modifier
        .padding(start = 16.dp)
        .width(250.dp)
        .fillMaxHeight(1f)
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(0.95f)
        ) {
            sidebarContent()
        }
        Row(modifier = Modifier.padding(start = 0.dp, top = 4.dp)) {
            TopbarButton(Res.drawable.jervis_icon_menu_back, "Back", onClick = { menuViewModel.backToLastScreen() })
            topMenuLeftContent?.let { it(this) }
        }
    }
}

@Composable
fun SidebarEntry(
    text: String,
    state: SidebarEntryState,
    alternativeBackground: Boolean = false,
    onClick: (() -> Unit)
) {
    val activeBarAlpha = if (state == SidebarEntryState.ACTIVE) 1f else 0f
    val backgroundColor = when (alternativeBackground) {
        true -> JervisTheme.rulebookBlue.darken(0.05f)
        false -> Color.Transparent
    }
    val fontColor = when (state) {
        SidebarEntryState.NOT_READY -> JervisTheme.white.copy(alpha = 0.7f)
        SidebarEntryState.DONE_NOT_AVAILABLE -> JervisTheme.white
        SidebarEntryState.DONE_AVAILABLE -> JervisTheme.white
        SidebarEntryState.ACTIVE -> JervisTheme.rulebookOrange
    }
    Column {
        OrangeTitleBorder(alpha = activeBarAlpha)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 36.dp)
                .background(backgroundColor)
                .applyIf(state == SidebarEntryState.ACTIVE || state == SidebarEntryState.DONE_AVAILABLE) {
                    clickable { onClick() }
                }
            ,
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                text = text.uppercase(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = fontColor
            )
        }
        OrangeTitleBorder(alpha = activeBarAlpha)
    }
}

@Composable
fun TitleBarWithSidebar(
    modifier: Modifier, title: String,
    fontSize: Dp = 52.jdp,
) {
    val skiaFont = loadJervisFont()
    Canvas(modifier = modifier) {
        val grayscaleShader = createGrayscaleNoiseShader()
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width, size.height * (160f/280f))
            lineTo(0f, size.height)
            close()
        }
        // Background color
        drawPath(path = path, color = JervisTheme.rulebookRed)
        // Add Noise
        drawPath(
            path = path,
            brush = ShaderBrush(grayscaleShader),
            alpha = 0.3f,
        )
        // Re-add background color to make the noise blend more into the background
        drawPath(path = path, color = JervisTheme.rulebookRed.copy(alpha = 0.5f))

        // Prepare the text paint
        val paint = Paint().apply {
            color = JervisTheme.rulebookOrange
            isAntiAlias = true
        }
        val nativePaint = paint.skiaPaint

        // Calculate how to place the text.
        // It should follow the red line, while skewing the
        // text so it is following the left border.
        val fontSizePx = fontSize.toPx()
        skiaFont.size = fontSizePx.sp.toPx()
        val angleRadians = atan((size.height - (size.height * (160f/280f))) / size.width)
        val angleDegrees = (angleRadians * 180 / PI).toFloat()
        val skewX = tan(-angleRadians)
        val skewY = 0.0f
        val xPadding = 16.dp.toPx()
        val yPadding = when (title.containsBelowBaselineChars()) {
            true -> (16 * 1.5f).dp.toPx()
            false -> xPadding
        }
        val line = Line(Point(0f, size.height), Point(size.width, (size.height * (160f/280f))))

        drawContext.canvas.skiaCanvas.apply {
            save()
            translate(0f + 282.dp.toPx(), line.getY(316.dp.toPx())) // TODO. How to translate across the line?
            rotate(-angleDegrees)
            skew(skewX, skewY)
            this.drawTextLine(TextLine.make(title, skiaFont), xPadding, -yPadding, nativePaint)
            restore()
        }
    }
}
