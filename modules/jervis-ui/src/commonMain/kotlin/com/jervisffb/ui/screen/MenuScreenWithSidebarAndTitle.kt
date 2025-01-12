package com.jervisffb.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFontLoader
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jervisffb.jervis_ui.generated.resources.Res
import com.jervisffb.jervis_ui.generated.resources.frontpage_wall_player
import com.jervisffb.jervis_ui.generated.resources.icon_menu_back
import com.jervisffb.jervis_ui.generated.resources.icon_menu_settings
import com.jervisffb.jervis_ui.generated.resources.trump_town_pro
import com.jervisffb.ui.view.JervisTheme
import com.jervisffb.ui.view.utils.OrangeTitleBorder
import com.jervisffb.ui.view.utils.paperBackground
import com.jervisffb.ui.view.utils.paperBackgroundWithLine
import kotlinx.coroutines.flow.StateFlow
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
    title: String,
    icon: DrawableResource,
    currentPageFlow: StateFlow<Int>,
    onClick: (Int) -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    val topPadding = 24.dp
    val fontSize = 30.sp
    Box(
        modifier = Modifier.fillMaxSize().paperBackground(JervisTheme.rulebookPaper),
        contentAlignment = Alignment.TopStart,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box {
                TitleBarWithSidebar(Modifier.fillMaxHeight(0.20f).fillMaxWidth(), title = title)
                Row(
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                ) {
                    Image(
                        modifier = Modifier.size(36.dp).alpha(0.8f),
                        painter = painterResource(Res.drawable.icon_menu_back),
                        contentDescription = "Back"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Image(
                        modifier = Modifier.size(36.dp).alpha(0.8f),
                        painter = painterResource(Res.drawable.icon_menu_settings),
                        contentDescription = "Settings"
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
        MenuSidebar(currentPageFlow, onClick)
        Image(
            modifier = Modifier.align(Alignment.BottomStart).width(400.dp).offset(x = -16.dp),
            painter = painterResource(Res.drawable.frontpage_wall_player),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
        )
    }
}

@Composable
fun MenuSidebar(currentPageFlow: StateFlow<Int>, onClick: (Int) -> Unit) {
    val currentPage by currentPageFlow.collectAsState()
    val entries = listOf("1. Configure Game", "2. Select Team", "3. Wait For Opponent", "4. Start Game")
    Box(modifier = Modifier
        .padding(start = 16.dp)
        .width(250.dp)
        .fillMaxHeight(1f)
    ) {
        Column(
            modifier = Modifier.paperBackgroundWithLine(JervisTheme.rulebookBlue).padding(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 8.dp),
        ) {
            Spacer(modifier = Modifier.fillMaxHeight(0.2f))
            Spacer(modifier = Modifier.height(16.dp))
            entries.forEachIndexed { index, entry ->
                val selected = (index == currentPage)
                val isPrevious = (index < currentPage)
                val clickHandler: () -> Unit = if (isPrevious) ({ onClick(index) }) else ({ })
                SidebarEntry(entry, selected = selected, onClick = clickHandler)
            }
            Spacer(modifier = Modifier.fillMaxHeight(0.20f))
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
    val textMeasure = rememberTextMeasurer()
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
