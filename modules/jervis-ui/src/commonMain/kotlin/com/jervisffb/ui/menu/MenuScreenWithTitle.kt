package com.jervisffb.ui.menu

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFontLoader
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jervisffb.jervis_ui.generated.resources.Res
import com.jervisffb.jervis_ui.generated.resources.icon_menu_back
import com.jervisffb.jervis_ui.generated.resources.icon_menu_settings
import com.jervisffb.jervis_ui.generated.resources.menu_background
import com.jervisffb.jervis_ui.generated.resources.trump_town_pro
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.view.utils.paperBackground
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.intro.createGrayscaleNoiseShader
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.skia.Font
import org.jetbrains.skia.TextLine
import org.jetbrains.skia.Typeface
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.tan

@Composable
fun MenuScreenWithTitle(
    menuViewModel: MenuViewModel,
    title: String,
    icon: DrawableResource,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .paperBackground(JervisTheme.rulebookPaper)
        ,
        contentAlignment = Alignment.TopStart,
    ) {
        Image(
            modifier = Modifier.fillMaxSize(),
            bitmap = imageResource(Res.drawable.menu_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            colorFilter = ColorFilter.tint(JervisTheme.rulebookPaper, BlendMode.Color),
            alpha = 0.075f
        )
        Column(modifier = Modifier.fillMaxSize()) {
            Box {
                TitleBar(Modifier.fillMaxHeight(0.20f).fillMaxWidth(), title = title)
                Row(
                    modifier = Modifier.align(Alignment.TopStart).padding(start = 16.dp, top = 4.dp, end = 8.dp, bottom = 16.dp)
                ) {
                    TopbarButton(Res.drawable.icon_menu_back, "Back", onClick = { menuViewModel.backToLastScreen() })
                    Spacer(modifier = Modifier.weight(1f))
                    TopbarButton(Res.drawable.icon_menu_settings, "Settings", onClick = { menuViewModel.openSettings(true) })
                }
            }
            Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                content()
            }
            Row(modifier = Modifier.height(48.dp).fillMaxWidth().paperBackground(JervisTheme.rulebookRed)) {

            }
        }
        Image(
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp).size(150.dp),
            painter = painterResource(icon),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
        )
    }
}

@Composable
fun TitleBar(modifier: Modifier, title: String) {
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
        val padding = 32.dp.toPx()

        drawContext.canvas.nativeCanvas.apply {
            save()
            translate(0f, size.height)
            rotate(-angleDegrees)
            skew(skewX.toFloat(), skewY.toFloat())
            this.drawTextLine(TextLine.make(title, skiaFont), padding, -padding, nativePaint)
            restore()
        }
    }
}
