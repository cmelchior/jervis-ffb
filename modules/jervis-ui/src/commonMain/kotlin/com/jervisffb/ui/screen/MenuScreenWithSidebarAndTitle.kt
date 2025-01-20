package com.jervisffb.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
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
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFontLoader
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.jervisffb.jervis_ui.generated.resources.Res
import com.jervisffb.jervis_ui.generated.resources.frontpage_mummy
import com.jervisffb.jervis_ui.generated.resources.frontpage_wall_player
import com.jervisffb.jervis_ui.generated.resources.icon_menu_back
import com.jervisffb.jervis_ui.generated.resources.icon_menu_settings
import com.jervisffb.jervis_ui.generated.resources.trump_town_pro
import com.jervisffb.ui.screen.intro.createGrayscaleNoiseShader
import com.jervisffb.ui.view.JervisTheme
import com.jervisffb.ui.view.utils.OrangeTitleBorder
import com.jervisffb.ui.view.utils.paperBackground
import com.jervisffb.ui.viewmodel.MenuViewModel
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.skia.Font
import org.jetbrains.skia.Point
import org.jetbrains.skia.TextLine
import org.jetbrains.skia.Typeface
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
    icon: DrawableResource,
    topMenuLeftContent: (@Composable RowScope.() -> Unit)? = null,
    topMenuRightContent: (@Composable RowScope.() -> Unit)? = null,
    sidebarContent: @Composable BoxScope.() -> Unit,
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
                    TopbarButton(Res.drawable.icon_menu_settings, "Settings", onClick = { menuViewModel.openSettings(true) })
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
            Res.drawable.frontpage_wall_player -> {
                Image(
                    modifier = Modifier.align(Alignment.BottomStart).width(400.dp).offset(x = -16.dp),
                    painter = painterResource(Res.drawable.frontpage_wall_player),
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                )
            }
            Res.drawable.frontpage_mummy -> {
                Image(
                    modifier = Modifier.align(Alignment.BottomStart).width(400.dp).offset(x = -108.dp, y = 25.dp),
                    painter = painterResource(Res.drawable.frontpage_mummy),
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                )
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TopbarButton(icon: DrawableResource, contentDescription: String, onClick: () -> Unit) {
    var isHovered by remember { mutableStateOf(false) }
    Image(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .height(48.dp)
            .alpha(if (isHovered) 1f else 0.8f)
            .clickable { onClick() }
            .onPointerEvent(PointerEventType.Enter) { isHovered = true }
            .onPointerEvent(PointerEventType.Exit) { isHovered = false }
            .padding(8.dp)
        ,
        painter = painterResource(icon),
        contentDescription = contentDescription,
        contentScale = ContentScale.FillHeight,
    )
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
fun MenuSidebar(menuViewModel: MenuViewModel, sidebarContent: @Composable BoxScope.() -> Unit, topMenuLeftContent: @Composable (RowScope.() -> Unit)?) {
    Box(modifier = Modifier
        .padding(start = 16.dp)
        .width(250.dp)
        .fillMaxHeight(1f)
    ) {
        sidebarContent()
        Row(modifier = Modifier.padding(start = 0.dp, top = 4.dp)) {
            TopbarButton(Res.drawable.icon_menu_back, "Back", onClick = { menuViewModel.backToLastScreen() })
            topMenuLeftContent?.let { it(this) }
        }
    }
}

@Composable
fun SidebarEntry(text: String, onClick: () -> Unit = {}, selected: Boolean = false) {
    val alpha = if (selected) 1f else 0f
    val fontColor = if (selected) JervisTheme.rulebookOrange else JervisTheme.white

    Column() {
        OrangeTitleBorder(alpha = alpha)
        Box(
            modifier = Modifier.fillMaxWidth().height(36.dp).clickable { onClick() },
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                text = text.uppercase(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = fontColor
            )
        }
        OrangeTitleBorder(alpha = alpha)
    }
}


@Composable
fun TitleBarWithSidebar(modifier: Modifier, title: String) {
    val loader = LocalFontLoader.current
    val composeFont = org.jetbrains.compose.resources.Font(Res.font.trump_town_pro)
    val typeface: Any = remember { loader.load(composeFont) }
    val skiaFont = Font(typeface as Typeface)

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
        val nativePaint = paint.asFrameworkPaint()

        // Calculate how to place the text.
        // It should follow the red line, while skewing the
        // text so it is following the left border.
        // TODO Need to figure out exactly how to scale the text, so it
        //  looks "nice" in more situations
        val scale = 1.0f
        skiaFont.size = (70 * scale).sp.toPx()
        val angleRadians = atan((size.height - (size.height * (160f/280f))) / size.width)
        val angleDegrees = (angleRadians * 180 / PI).toFloat()
        val skewX = tan(-angleRadians)
        val skewY = 0.0f
        val padding = 16.dp.toPx()

        val line = Line(Point(0f, size.height), Point(size.width, (size.height * (160f/280f))))

        drawContext.canvas.nativeCanvas.apply {
            save()
            translate(0f + 282.dp.toPx(), line.getY(316.dp.toPx())) // TODO. How to translate across the line?
            rotate(-angleDegrees)
            skew(skewX.toFloat(), skewY.toFloat())
            this.drawTextLine(TextLine.make(title, skiaFont), padding, -padding, nativePaint)
            restore()
        }
    }
}
