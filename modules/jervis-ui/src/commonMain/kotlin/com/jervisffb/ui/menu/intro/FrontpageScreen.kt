package com.jervisffb.ui.menu.intro

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.FontLoadResult
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
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
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.view.utils.OrangeTitleBorder
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.JervisScreen
import com.jervisffb.ui.menu.MenuScreen
import com.jervisffb.ui.menu.TopbarButton
import com.jervisffb.ui.menu.components.CreditDialog
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.skia.ColorFilter
import org.jetbrains.skia.ColorMatrix
import org.jetbrains.skia.ISize
import org.jetbrains.skia.TextLine
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.tan
import org.jetbrains.skia.Font as SkiaFont

/**
 * Layout class for the Main starting screen.
 */
class FrontpageScreen(private val menuViewModel: MenuViewModel) : Screen {

    override val key: ScreenKey = "IntroScreen"

    @Composable
    override fun Content() {
        JervisScreen(menuViewModel) {
            PageContent(menuViewModel)
        }
    }
}

// Unclear if this is the correct way or how this behaves during recomposition
@Composable
fun loadJervisFont(): SkiaFont {
    val fontResolver = LocalFontFamilyResolver.current
    val resolvedFont: Any by fontResolver.resolve(JervisTheme.fontFamily())
    if (resolvedFont !is FontLoadResult) TODO("Failed to load font: $resolvedFont")
    return (resolvedFont as FontLoadResult).typeface?.let {
        SkiaFont(it)
    } ?: error("Failed to load type face: $resolvedFont")
}

@Composable
private fun FrontpageScreen.PageContent(menuViewModel: MenuViewModel) {
    val navigator = LocalNavigator.currentOrThrow
    val viewModel = rememberScreenModel { FrontpageScreenModel(menuViewModel) }
    MenuScreen {
        Row {
            Column(modifier = Modifier.fillMaxWidth(0.67f).fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween) {
                BoxWithConstraints(modifier = Modifier.weight(1f), contentAlignment = Alignment.TopStart) {
                    val max= maxHeight
                    TitleHeader("JERVIS", "Fantasy Football")
                    // It is a bit unclear exactly how the menu boxes should change and scale depending on
                    // screen size, for now they are wrapped in a box because it makes it easier to modify
                    // left/right position. This probably needs to the be redone at some point.

                    // `contentAlignment` here doesn't do much except in extrem cases where it prevents
                    // the menu from going into the top banner.
                    Column(modifier = Modifier.height(max - 48.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            modifier = Modifier.fillMaxSize(0.95f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Spacer(modifier = Modifier.fillMaxWidth(0.1f))
                            Column(
                                modifier = Modifier.aspectRatio(1f),
                            ) {
                                FrontpageMenu(viewModel, navigator)
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.height(48.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text(
                        modifier = Modifier.clickable { viewModel.showCreditDialog(visible = true) }.padding(8.dp),
                        text = viewModel.clientVersion,
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
                        modifier = Modifier.fillMaxWidth()
                            .padding(start = 16.dp, top = 4.dp, end = 8.dp, bottom = 16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TopbarButton(
                            "Dev Mode",
                            onClick = { viewModel.gotoDevModeScreen(navigator) })
                        TopbarButton(
                            Res.drawable.icon_menu_settings,
                            "Settings",
                            onClick = { menuViewModel.openSettings(true) })
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .padding(16.dp)
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
                        Column(
                            modifier = Modifier
                                .fillMaxHeight(0.30f)
                                .verticalScroll(rememberScrollState())
                        ) {
                            viewModel.news.forEach { (timestamp: String, body: String) ->
                                NewsEntry(timestamp, body)
                            }
                        }
                    }

                }
            }
        }
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomEnd
        ) {
            Image(
                modifier = Modifier.fillMaxWidth(0.28f).offset(x = -40.dp, y = 40.dp),
                bitmap = imageResource(Res.drawable.frontpage_orc),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
            )
        }
    }
    CreditDialog(viewModel, viewModel.creditData)
}

@Composable
private fun ColumnScope.FrontpageMenu(viewModel: FrontpageScreenModel, navigator: Navigator) {
    Column(modifier = Modifier.fillMaxSize().aspectRatio(1f)) {
        Row(modifier = Modifier.weight(17/36f).fillMaxSize()) {
            // Top Left Corner
            Column(modifier = Modifier.aspectRatio(1f).weight(17f/36f).fillMaxSize()) {
                // Always empty (for now)
            }
            Spacer(modifier = Modifier.weight(2f/36f))
            // Top Right Corner
            Column(modifier = Modifier.aspectRatio(1f).weight(17f/36f).fillMaxSize()) {
                Column(Modifier.weight(8f/17f)) {
                    // Always empty
                }
                Spacer(modifier = Modifier.weight(1f/17f))
                Column(Modifier.weight(8f/17f)) {
                    FrontpageMenuEntry("Challenges", { /* Not supported yet */ }, enabled = false)
                }
            }
        }
        Spacer(modifier = Modifier.weight(2f/36f))
        Row(modifier = Modifier.weight(17/36f).fillMaxSize()) {
            // Bottom Left Corner
            Column(modifier = Modifier.aspectRatio(1f).weight(17f/36f).fillMaxSize()) {
                Column(Modifier.weight(15f/36f)) {
                    FrontpageMenuEntry("Editor", { /* Not supported yet */  }, enabled = false)
                }
                Spacer(modifier = Modifier.weight(4f/36f))
                Column(Modifier.weight(15f/36f)) {
                    FrontpageMenuEntry("FUMBBL", { viewModel.gotoFumbblScreen(navigator) })
                }
            }
            Spacer(modifier = Modifier.weight(2f/36f))
            // Bottom Right Corner
            Column(modifier = Modifier.aspectRatio(1f).weight(17f/36f).fillMaxSize()) {
                FrontpageMenuEntry("Standalone", { viewModel.gotoStandAloneScreen(navigator) })
            }
        }
    }
}

@Composable
private fun ColumnScope.FrontpageMenuEntry(title: String, onClick: () -> Unit, enabled: Boolean = true) {
    Box(
        modifier = Modifier
            .background(color = if (enabled) JervisTheme.rulebookBlue else JervisTheme.rulebookDisabled)
            .fillMaxSize()
            .let { if (enabled) it.clickable { onClick() } else it }
        ,
        contentAlignment = Alignment.BottomEnd,
    ) {
        Text(
            modifier = Modifier.padding(16.dp),
            text = title.uppercase(),
            textAlign = TextAlign.End,
            maxLines = 2,
            color = JervisTheme.buttonTextColor,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            style = LocalTextStyle.current.copy(
                lineHeight = 1.0.em,
                lineHeightStyle = LineHeightStyle(
                    alignment = LineHeightStyle.Alignment.Bottom,
                    trim = LineHeightStyle.Trim.LastLineBottom
                ),
            ),
        )
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
fun TitleHeader(mainTitle: String, subTitle: String) {
    val textMeasure = rememberTextMeasurer()
    val skiaFont = loadJervisFont()
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
        val line1 = mainTitle
        val line2 = subTitle
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

// Generates a "noise" shader that will introduce a paper-like quality to the background
// Need to investigate something better, but this seems okay for a first draft.
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
