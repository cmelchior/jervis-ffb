@file:OptIn(
    InternalResourceApi::class,
    ExperimentalResourceApi::class,
)
package com.jervisffb.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFontLoader
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.jervisffb.jervis_ui.generated.resources.Res
import com.jervisffb.jervis_ui.generated.resources.frontpage_orc
import com.jervisffb.jervis_ui.generated.resources.icon_menu_settings
import com.jervisffb.jervis_ui.generated.resources.trump_town_pro
import com.jervisffb.ui.BuildConfig
import com.jervisffb.ui.screen.fumbbl.FumbblScreen
import com.jervisffb.ui.screen.fumbbl.FumbblScreenModel
import com.jervisffb.ui.view.JervisTheme
import com.jervisffb.ui.view.MenuBox
import com.jervisffb.ui.view.utils.OrangeTitleBorder
import com.jervisffb.ui.viewmodel.MenuViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.skia.ColorFilter
import org.jetbrains.skia.ColorMatrix
import org.jetbrains.skia.Font
import org.jetbrains.skia.ISize
import org.jetbrains.skia.TextLine
import org.jetbrains.skia.Typeface
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.tan

class IntroScreenModel(private val menuViewModel: MenuViewModel) : JervisScreenModel {

    fun gotoFumbblScreen(navigator: Navigator) {
        menuViewModel.navigatorContext.launch {
            val screenModel = FumbblScreenModel(menuViewModel)
            screenModel.initialize()
            navigator.push(FumbblScreen(menuViewModel, screenModel))
        }
    }

    fun gotoStandAloneScreen(navigator: Navigator) {
        menuViewModel.navigatorContext.launch {
            val screenModel = StandAloneScreenModel(menuViewModel)
            navigator.push(StandAloneScreen(menuViewModel, screenModel))
        }

    }

    fun gotoDevModeScreen(navigator: Navigator) {
        menuViewModel.navigatorContext.launch {
            val screenModel = DevScreenModel(menuViewModel)
            navigator.push(DevScreen(menuViewModel, screenModel))
        }
    }

    val clientVersion: String = BuildConfig.releaseVersion
}

class IntroScreen(private val menuViewModel: MenuViewModel) : Screen {

    override val key: ScreenKey = "IntroScreen"

    @Composable
    override fun Content() {
        JervisScreen(menuViewModel) {
            IntroPage(menuViewModel)
        }
    }
}

@Composable
private fun IntroScreen.IntroPage(menuViewModel: MenuViewModel) {
    val navigator = LocalNavigator.currentOrThrow
    val screenModel = rememberScreenModel { IntroScreenModel(menuViewModel) }
    MenuScreen {
        Row {
            Column(modifier = Modifier.fillMaxWidth(0.67f)) {
                TitleHeader()
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
//                            horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    Spacer(modifier = Modifier.weight(4f / 36f))
                    MenuBox(
                        label = "FUMBBL",
                        onClick = { screenModel.gotoFumbblScreen(navigator) },
                        frontPage = true
                    )
                    Spacer(modifier = Modifier.weight(2f / 36f))
                    MenuBox(
                        label = "Standalone",
                        onClick = { screenModel.gotoStandAloneScreen(navigator) },
                        frontPage = true
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .padding(8.dp)
                ) {
                    Text(
                        text = screenModel.clientVersion,
                        color = JervisTheme.contentTextColor,
                    )
                }
            }
            Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(), contentAlignment = Alignment.BottomEnd) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.67f)
                        .fillMaxHeight()
                        .drawBehind { drawPaperBackground(size) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 4.dp, end = 8.dp, bottom = 16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TopbarButton("Dev Mode", onClick = { screenModel.gotoDevModeScreen(navigator) })
                        TopbarButton(Res.drawable.icon_menu_settings, "Settings", onClick = { menuViewModel.openSettings(true) })
                    }
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp).wrapContentHeight(align = Alignment.CenterVertically),
                    ) {
                        OrangeTitleBorder()
                        Text(
                            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
                            text = "News",
                            fontFamily = JervisTheme.fontFamily(),
                            fontSize = 24.sp,
                            color = JervisTheme.rulebookOrange
                        )
                        OrangeTitleBorder()
                        Spacer(modifier = Modifier.height(8.dp))
                        NewsEntry("11-01-2025", "Lorem ipsum dolor sit amet")
                        NewsEntry("11-01-2025", "Lorem ipsum dolor sit amet")
                    }

                }
                Image(
                    modifier = Modifier.fillMaxWidth(1f).offset(x = 24.dp),
                    bitmap = imageResource(Res.drawable.frontpage_orc),
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                )
            }
        }
    }
}

@Composable
fun NewsEntry(header: String, body: String) {
    Text(
        modifier = Modifier.padding(bottom = 8.dp),
        text = buildAnnotatedString {
            pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
            append("$header: ")
            pop()
            append(body)
        },
        color = JervisTheme.white,
    )
}

@Composable
fun TitleHeader() {
    val textMeasure = rememberTextMeasurer()
    val loader = LocalFontLoader.current
    val composeFont = org.jetbrains.compose.resources.Font(Res.font.trump_town_pro)
    val typeface: Any = remember { loader.load(composeFont) }
    val skiaFont = Font(typeface as Typeface)

    Canvas(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.33f)) {
        val grayscaleShader = createGrayscaleNoiseShader()
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(0f, size.height)
            lineTo(0f, 0f)
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
        val scale = 1.3f
        skiaFont.size = (70 * scale).sp.toPx()
        val line1 = "JERVIS"
        val line2 = "Fantasy Football"
        val angleRadians = atan(size.height / size.width)
        val angleDegrees = (angleRadians * 180 / PI).toFloat()
        val skewX = tan(-angleRadians)
        val skewY = 0.0f
        val padding = 32.dp.toPx()
        val lineHeight = (88 * scale).dp.toPx()

        drawContext.canvas.nativeCanvas.apply {
            save()
            translate(0f, size.height)
            rotate(-angleDegrees)
            skew(skewX.toFloat(), skewY.toFloat())
            this.drawTextLine(TextLine.make(line2, skiaFont), padding, -padding, nativePaint)
            this.drawTextLine(TextLine.make(line1, skiaFont), padding, -lineHeight, nativePaint)
            restore()
        }
    }
}

fun createGrayscaleNoiseShader(): Shader {

    // Create Noise
    val shader = Shader.makeFractalNoise(
        baseFrequencyX = 0.1f, // Adjust for desired texture
        baseFrequencyY = 0.1f,
        numOctaves = 5,
        seed = 0f,
        tileSize = ISize.make(4, 4)
    )

    // Apply a color filter to convert to grayscale
    return shader.makeWithColorFilter(
        ColorFilter.makeMatrix(
            // Use NCTS values to convert to grayscale
            // https://en.wikipedia.org/wiki/Grayscale#Converting_color_to_grayscale
            ColorMatrix(
                0.299f, 0.587f, 0.114f, 0f, 0f,   // Red to luminance
                0.299f, 0.587f, 0.114f, 0f, 0f,         // Green to luminance
                0.299f, 0.587f, 0.114f, 0f, 0f,         // Blue to luminance
                0f, 0f, 0f, 1f, 0f                      // Alpha unchanged
            )
        )
    )
}

fun DrawScope.drawPaperBackground(size: Size) {
    val shader = createGrayscaleNoiseShader()
    drawRect(size = size, color = JervisTheme.rulebookRed)
    // Add Noise
    drawRect(
        size = size,
        brush = ShaderBrush(shader),
        alpha = 0.3f,
    )
    // Re-add background color to make the noise blend more into the background
    drawRect(size = size, color = JervisTheme.rulebookRed.copy(alpha = 0.5f))
}
