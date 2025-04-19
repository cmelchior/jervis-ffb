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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jervisffb.jervis_ui.generated.resources.Res
import com.jervisffb.jervis_ui.generated.resources.jervis_frontpage_krox
import com.jervisffb.jervis_ui.generated.resources.jervis_icon_menu_back
import com.jervisffb.jervis_ui.generated.resources.jervis_icon_menu_settings
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.view.utils.paperBackground
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.intro.createGrayscaleNoiseShader
import com.jervisffb.ui.menu.intro.loadJervisFont
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.skia.TextLine
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.tan

@Composable
fun MenuScreenWithTitle(
    menuViewModel: MenuViewModel,
    title: String,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .paperBackground(JervisTheme.rulebookPaper)
        ,
        contentAlignment = Alignment.TopStart,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box {
                TitleBar(Modifier.fillMaxHeight(0.20f).fillMaxWidth(), title = title)
                Row(
                    modifier = Modifier.align(Alignment.TopStart).padding(start = 16.dp, top = 4.dp, end = 8.dp, bottom = 16.dp)
                ) {
                    TopbarButton(Res.drawable.jervis_icon_menu_back, "Back", onClick = { menuViewModel.backToLastScreen() })
                    Spacer(modifier = Modifier.weight(1f))
                    TopbarButton(Res.drawable.jervis_icon_menu_settings, "Settings", onClick = { menuViewModel.openSettings(true) })
                }
            }
            Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                content()
            }
            Row(modifier = Modifier.height(48.dp).fillMaxWidth().paperBackground(JervisTheme.rulebookRed)) {
                // Red bar at the bottom of the screen
            }
        }

        Image(
            modifier = Modifier.align(Alignment.BottomEnd).fillMaxWidth(0.35f).offset(x = -50.dp, y = 0.dp).scale(1f,1f),
            bitmap = imageResource(Res.drawable.jervis_frontpage_krox),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
        )
    }
}

@Composable
private fun TitleBar(modifier: Modifier, title: String) {
    val textMeasure = rememberTextMeasurer()
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
        val nativePaint = paint.asFrameworkPaint()

        // Calculate how to place the text.
        // It should follow the red line, while skewing the
        // text so it is following the left border.
        // TODO Need to figure out exactly how to scale the text, so it
        //  looks "nice" in more situations
        val scale = 1.0f
        val fontSize = 90
        skiaFont.size = (fontSize * scale).sp.toPx()
        val angleRadians = atan((size.height - (size.height * (160f/280f))) / size.width)
        val angleDegrees = (angleRadians * 180 / PI).toFloat()
        val skewX = tan(-angleRadians)
        val skewY = 0.0f
        val paddingX = 32.dp.toPx()
        val paddingY = 16.dp.toPx()

        drawContext.canvas.nativeCanvas.apply {
            save()
            translate(0f, size.height)
            rotate(-angleDegrees)
            skew(skewX.toFloat(), skewY.toFloat())
            this.drawTextLine(TextLine.make(title, skiaFont), paddingX, -paddingY, nativePaint)
            restore()
        }
    }
}
