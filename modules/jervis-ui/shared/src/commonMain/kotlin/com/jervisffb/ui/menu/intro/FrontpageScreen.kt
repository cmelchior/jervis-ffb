package com.jervisffb.ui.menu.intro

import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asComposeShader
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.skiaCanvas
import androidx.compose.ui.graphics.skiaShader
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.FontLoadResult
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.jervisffb.shared.generated.resources.Res
import com.jervisffb.shared.generated.resources.jervis_frontpage_orc
import com.jervisffb.shared.generated.resources.jervis_icon_menu_settings
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.view.utils.BorderText
import com.jervisffb.ui.game.view.utils.OrangeTitleBorder
import com.jervisffb.ui.game.view.utils.PixelatedImage
import com.jervisffb.ui.game.view.utils.drawEmbossedLine
import com.jervisffb.ui.game.view.utils.drawPathDropShadow
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.JervisScreen
import com.jervisffb.ui.menu.MenuScreen
import com.jervisffb.ui.menu.TopbarButton
import com.jervisffb.ui.theme.Pixellari
import com.jervisffb.ui.utils.darken
import com.jervisffb.ui.utils.jdp
import com.jervisffb.ui.utils.jsp
import com.jervisffb.ui.utils.lighten
import com.jervisffb.ui.utils.toPx
import io.ktor.client.request.invoke
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.skia.ColorFilter
import org.jetbrains.skia.ColorMatrix
import org.jetbrains.skia.FilterBlurMode
import org.jetbrains.skia.FilterTileMode
import org.jetbrains.skia.FontEdging
import org.jetbrains.skia.ISize
import org.jetbrains.skia.MaskFilter
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.Rect
import org.jetbrains.skia.RuntimeEffect
import org.jetbrains.skia.RuntimeShaderBuilder
import org.jetbrains.skia.SamplingMode
import org.jetbrains.skia.Surface
import org.jetbrains.skia.TextLine
import org.jetbrains.skia.impl.use
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.math.tan
import org.jetbrains.skia.Canvas as SkiaCanvas
import org.jetbrains.skia.Font as SkiaFont
import org.jetbrains.skia.Paint as SkiaPaint
import org.jetbrains.skia.Shader as SkiaShader

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

// Unclear if this is the correct way or how this behaves during recomposition
@Composable
fun loadPixellari(): SkiaFont {
    val fontResolver = LocalFontFamilyResolver.current
    val resolvedFont: Any by fontResolver.resolve(Pixellari())
    if (resolvedFont !is FontLoadResult) TODO("Failed to load font: $resolvedFont")
    return (resolvedFont as FontLoadResult).typeface?.let {
        SkiaFont(it)
    } ?: error("Failed to load type face: $resolvedFont")
}


@Composable
private fun FrontpageScreen.PageContent(menuViewModel: MenuViewModel) {
    val navigator = LocalNavigator.currentOrThrow
    val viewModel = rememberScreenModel { FrontpageScreenModel(menuViewModel) }
    val isUpdateAvailable by viewModel.appUpdate.collectAsState()
    val version = viewModel.clientVersion
    val visibleVersionString = remember(isUpdateAvailable) {
        buildAnnotatedString {
            append(version)
            if (isUpdateAvailable) {
                pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                append(" - Update Available")
                pop()
            }
        }
    }

    MenuScreen {
        Row {
            Column(modifier = Modifier.fillMaxWidth(0.67f).fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween) {
                BoxWithConstraints(modifier = Modifier.weight(1f), contentAlignment = Alignment.TopStart) {
                    val max= maxHeight
                    TitleHeader2(
                        mainTitle = "JERVIS",
                        subTitle = "Fantasy Football",
                        modifier = Modifier.fillMaxWidth().fillMaxHeight(0.33f),
                    )
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
                        modifier = Modifier
                            .clickable { menuViewModel.showAboutDialog(true) }
                            .padding(vertical = 8.jdp, horizontal = 16.jdp),
                        text = visibleVersionString,
                        fontFamily = JervisTheme.pixelFontFootnoteFamily(),
                        fontSize = 16.jsp,
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
                            "Dev Options",
                            onClick = { viewModel.gotoDevModeScreen(navigator) })
                        TopbarButton(
                            Res.drawable.jervis_icon_menu_settings,
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
                        BorderText(
                            modifier = Modifier.padding(bottom = 2.jdp, top = 6.jdp),
                            text = "News",
                            fontSize = 31.jsp,
                            fontFamily = JervisTheme.pixelFontHeaderFamily(),
                            color = JervisTheme.rulebookOrange,
                            borderThickness = 3f,
                            shadowOffset = 4f,
                        )
//                        Text(
//                            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
//                            text = "News",
//                            fontFamily = JervisTheme.pixelFontHeaderFamily(),
//                            style = LocalTextStyle.current.copy(
//                                shadow = Shadow(
//                                    color = JervisTheme.black,
//                                    blurRadius = 0f,
//                                    offset = Offset(2.dp.toPx(), 2.dp.toPx())
//                                ),
//                            ),
//                            fontSize = 24.sp,
//                            color = JervisTheme.rulebookOrange
//                        )
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
            PixelatedImage(
                modifier = Modifier
                    .fillMaxHeight(0.63f)
                    .fillMaxWidth(0.3f)
                    .offset(x = -25.dp, y = 25.dp)
                ,
                painter = painterResource(Res.drawable.jervis_frontpage_orc),
                // outlineColor = JervisTheme.black.copy(alpha = 0.7f),
                // outlineThickness = 1f,
            )
//            Image(
//                modifier = Modifier.fillMaxHeight(0.63f).fillMaxWidth(0.3f).offset(x = -25.dp, y = 25.dp),
//                bitmap = imageResource(Res.drawable.jervis_frontpage_orc),
//                contentDescription = null,
//                alignment = Alignment.BottomEnd,
//                contentScale = ContentScale.Fit,
//            )
        }
    }
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
                    FrontpageMenuEntry("Challenges", { viewModel.gotoChallengesScreen(navigator) })
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
private fun Dp.roundedPixelSp(): TextUnit {
    val density = LocalDensity.current
    return with(density) {
        val px = roundToPx()
        (px / (density.density * fontScale)).sp
    }
}

@Composable
private fun ColumnScope.FrontpageMenuEntry(title: String, onClick: () -> Unit, enabled: Boolean = true) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .let { if (enabled) it.clickable { onClick() } else it }
            .dropShadow(shape = RectangleShape) {
                offset = Offset(8f, 8f)
                color = Color.Black.copy(alpha = 0.5f)
                spread = 2.0f
                radius = 0.0f
            }
            .background(color = if (enabled) JervisTheme.rulebookBlue else JervisTheme.rulebookDisabled)
        ,
        contentAlignment = Alignment.BottomEnd,
    ) {
        val fontSize = (maxWidth * 0.10f).roundedPixelSp()
        BorderText(
            modifier = Modifier.padding(16.jdp),
            text = title.uppercase(),
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            fontFamily = JervisTheme.pixelFontButtonFamily(),
            borderThickness = 2.dp.toPx(),
            shadowOffset = 3.dp.toPx(),
        )
//        Text(
//            text = title.uppercase(),
//            textAlign = TextAlign.End,
//            maxLines = 2,
//            color = JervisTheme.buttonTextColor,
//            fontFamily = JervisTheme.pixelFontButtonFamily(),
//            fontWeight = FontWeight.Bold,
//            fontSize = fontSize,
//            style = LocalTextStyle.current.copy(
//                shadow = Shadow(
//                    color = JervisTheme.black,
//                    blurRadius = 0f,
//                    offset = Offset(3.dp.toPx(), 3.dp.toPx())
//                ),
//                lineHeight = 1.0.em,
//                lineHeightStyle = LineHeightStyle(
//                    alignment = LineHeightStyle.Alignment.Bottom,
//                    trim = LineHeightStyle.Trim.LastLineBottom
//                ),
//            ),
//        )
    }
}

@Composable
fun NewsEntry(header: String, body: String) {
    val text = buildAnnotatedString {
        pushStyle(SpanStyle(fontWeight = FontWeight.Bold, color = JervisTheme.rulebookOrange))
        append("$header: ")
        pop()
        append(body)
    }

    BorderText(
        modifier = Modifier.padding(bottom = 8.dp),
        text = text,
        fontFamily = JervisTheme.pixelFontBodyFamily(),
        fontWeight = FontWeight.Normal,
        fontSize = 26.jsp,
        borderThickness = 3f,
        shadowOffset = 4f,
    )
//    Text(
//        modifier = Modifier.padding(bottom = 8.dp),
//        text = ,
//        color = JervisTheme.white,
//        style = LocalTextStyle.current.copy(
//            fontSize = 26.jsp,
//            fontFamily = JervisTheme.pixelFontBodyFamily(),
//            shadow = Shadow(
//                color = JervisTheme.black,
//                blurRadius = 0f,
//                offset = Offset(3f, 3f)
//
//            ),
//        )
//    )
}

/**
 * Draws the front-page title using a low-resolution pixel-art pass.
 *
 * [gradientStops] describes the continuous gradient before it is reduced to [paletteSteps]
 * hard color bands. Duplicate stop positions create immediate color transitions.
 *
 * [outlineRadius] is measured in low-resolution pixel cells. [shadowOffset] and
 * [shadowBlur] are measured in final canvas pixels and scaled for the raster pass.
 */
@Composable
fun TitleHeader(
    mainTitle: String,
    subTitle: String,
    modifier: Modifier = Modifier,
    gradientStops: ImmutableList<TitleGradientStop> = TitleHeaderDefaults.gradientStops,
    paletteSteps: Int = 10,
    antiAlias: Boolean = false,
    outlineColor: Color = JervisTheme.black.copy(0.7f),
    outlineRadius: Float = 4f,
    shadowColor: Color = JervisTheme.black,
    shadowOffset: Offset = Offset(8f, 8f),
    shadowBlur: Float = 0f,
) {
    require(paletteSteps in 1..256) { "paletteSteps must be between 1 and 256" }
    require(outlineRadius >= 0f) { "outlineRadius cannot be negative" }
    require(shadowBlur >= 0f) { "shadowBlur cannot be negative" }

    val skiaFont = loadPixellari() // loadJervisFont()
    val pixelGradientStops = remember(gradientStops, paletteSteps) {
        createPixelGradientStops(gradientStops, paletteSteps)
    }

    Canvas(modifier = modifier) {
        val grayscaleShader = createGrayscaleNoiseShader()
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(0f, size.height)
            lineTo(0f, 0f)
            close()
        }

        // Calculate the scale of the text.
        // We do this by checking the width and height to get
        // a sense of the triangle and then scale according to the
        // smallest scale factor. There is probably a better way,
        // but for now this seems to work okay-ish.
        val lineWidth = sqrt(size.height.pow(2) + size.width.pow(2))
        val scaleFromHeight = size.height / 385f // 385f (seems to be a bad fit for 16:9)
        val scaleFromLength = lineWidth / 1600f // 1600f (seems to be a bad fit for 16:9)
        val scale = min(scaleFromHeight, scaleFromLength)

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

        // Calculate how to place the text.
        // It should follow the red line, while skewing the
        // text so it is following the left border.
        val maxFontSize = 180f / density
        val screenScaling = 140f / density // Don't remember where this value came from...
        val fontSize = min(screenScaling * scale, maxFontSize).sp.toPx()
        val angleRadians = atan(size.height / size.width)
        val angleDegrees = (angleRadians * 180 / PI).toFloat()
        val skewX = tan(-angleRadians)
        val skewY = 0.0f
        val padding = 32.jdp.toPx()

        // Render the lettering at half resolution, then upscale it exactly 2x with
        // nearest-neighbor sampling. This turns every source pixel into a crisp 2x2 block.
        val pixelScale = 2f
        val rasterWidth = (size.width / pixelScale).roundToInt().coerceAtLeast(1)
        val rasterHeight = (size.height / pixelScale).roundToInt().coerceAtLeast(1)
        val rasterFontSize = fontSize / pixelScale
        val rasterPadding = padding / pixelScale
        skiaFont.size = rasterFontSize
        skiaFont.edging = if (antiAlias) FontEdging.ANTI_ALIAS else FontEdging.ALIAS
        skiaFont.isSubpixel = antiAlias
        skiaFont.isBaselineSnapped = !antiAlias
        val rasterShadowOffset = Offset(
            x = shadowOffset.x / pixelScale,
            y = shadowOffset.y / pixelScale,
        )
        val rasterShadowBlur = shadowBlur / pixelScale

        Surface.makeRasterN32Premul(rasterWidth, rasterHeight).use { surface ->
            val rasterCanvas = surface.canvas
            rasterCanvas.clear(org.jetbrains.skia.Color.TRANSPARENT)

            TextLine.make(subTitle, skiaFont).use { subTitleLine ->
                TextLine.make(mainTitle, skiaFont).use { mainTitleLine ->
                    val lineHeight = rasterFontSize * 1.3f
                    rasterCanvas.save()
                    rasterCanvas.translate(0f, rasterHeight.toFloat())
                    rasterCanvas.rotate(-angleDegrees)
                    rasterCanvas.skew(skewX, skewY)
                    rasterCanvas.drawPixelArtTextLine(
                        textLine = subTitleLine,
                        x = rasterPadding,
                        baselineY = -rasterPadding,
                        gradientStops = pixelGradientStops,
                        antiAlias = antiAlias,
                        outlineColor = outlineColor,
                        outlineRadius = outlineRadius,
                        shadowColor = shadowColor,
                        shadowOffset = rasterShadowOffset,
                        shadowBlur = rasterShadowBlur,
                    )
                    rasterCanvas.drawPixelArtTextLine(
                        textLine = mainTitleLine,
                        x = rasterPadding,
                        baselineY = -lineHeight,
                        gradientStops = pixelGradientStops,
                        antiAlias = antiAlias,
                        outlineColor = outlineColor,
                        outlineRadius = outlineRadius,
                        shadowColor = shadowColor,
                        shadowOffset = rasterShadowOffset,
                        shadowBlur = rasterShadowBlur,
                    )
                    rasterCanvas.restore()
                }
            }

            surface.makeImageSnapshot().use { image ->
                drawContext.canvas.skiaCanvas.drawImageRect(
                    image = image,
                    src = Rect.makeWH(rasterWidth.toFloat(), rasterHeight.toFloat()),
                    dst = Rect.makeWH(rasterWidth * pixelScale, rasterHeight * pixelScale),
                    samplingMode = SamplingMode.DEFAULT,
                    paint = null,
                    strict = true,
                )
            }
        }
    }
}

object TitleHeaderDefaults {
    val gradientStops: ImmutableList<TitleGradientStop> = persistentListOf(
        TitleGradientStop(0f, JervisTheme.rulebookOrange),
        TitleGradientStop(1f, JervisTheme.rulebookOrange),
//        TitleGradientStop(0f, JervisTheme.white),
//        TitleGradientStop(0.01f, JervisTheme.rulebookOrange.lighten(0.1f)),
//        TitleGradientStop(0.8f, JervisTheme.rulebookOrange),
//        TitleGradientStop(1f, JervisTheme.rulebookOrange.darken(0.35f)),
    )
}

/**
 * Draws the front-page title with a directional inner bevel over a pixel gradient.
 *
 * The glyph mask is sampled towards and away from [lightDirection] to produce a
 * [bevelSize]-pixel highlight and shade that follow every letter contour. The bevel
 * is composited over the stepped [gradientStops] before the exact 2x upscale.
 */
@Composable
fun TitleHeader2(
    mainTitle: String,
    subTitle: String,
    modifier: Modifier = Modifier,
    gradientStops: ImmutableList<TitleGradientStop> = TitleHeaderDefaults.gradientStops,
    paletteSteps: Int = 10,
    highlightColor: Color = JervisTheme.rulebookOrange.lighten(0.55f),
    bevelShadowColor: Color = JervisTheme.rulebookOrange.darken(0.30f),
    bevelSize: Float = 1f,
    lightDirection: Offset = Offset(-2f, -2f),
    antiAlias: Boolean = false,
    outlineColor: Color = JervisTheme.black,
    outlineRadius: Float = 2f,
    shadowColor: Color = JervisTheme.black,
    shadowOffset: Offset = Offset(8f, 8f),
    shadowBlur: Float = 0f,
    backgroundShadowColor: Color = JervisTheme.black.copy(alpha = 0.45f),
    backgroundShadowOffset: Offset = Offset(6f, 6f),
    backgroundShadowBlur: Float = 0f,
    embossedLineColor: Color = JervisTheme.rulebookOrange,
    embossedLineInset: Float = 10f, // Distance from bottom border
    embossedLineThickness: Dp = 2.jdp,
    textInset: Float = 16f, // Distance from bottom border
) {
    require(paletteSteps in 1..256) { "paletteSteps must be between 1 and 256" }
    require(bevelSize >= 0f) { "bevelSize cannot be negative" }
    require(outlineRadius >= 0f) { "outlineRadius cannot be negative" }
    require(shadowBlur >= 0f) { "shadowBlur cannot be negative" }
    require(backgroundShadowBlur >= 0f) { "backgroundShadowBlur cannot be negative" }
    require(embossedLineInset >= 0f) { "embossedLineInset cannot be negative" }
    require(embossedLineThickness.value >= 0f) { "embossedLineThickness cannot be negative" }

    val skiaFont = loadJervisFont()
    val pixelGradientStops = remember(gradientStops, paletteSteps) {
        createPixelGradientStops(gradientStops, paletteSteps)
    }
    val etchedTextEffect = remember { RuntimeEffect.makeForShader(etchedTextShader) }

    Canvas(modifier = modifier) {
        val grayscaleShader = createGrayscaleNoiseShader(tileSize = 8)
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(0f, size.height)
            lineTo(0f, 0f)
            close()
        }

        val lineWidth = sqrt(size.height.pow(2) + size.width.pow(2))
        val scaleFromHeight = size.height / 385f
        val scaleFromLength = lineWidth / 1600f
        val scale = min(scaleFromHeight, scaleFromLength)

        drawPathDropShadow(
            path = createTitleBackgroundShadowPath(
                size = size,
                offset = backgroundShadowOffset,
                blurRadius = backgroundShadowBlur,
            ),
            color = backgroundShadowColor,
            offset = backgroundShadowOffset,
            blurRadius = backgroundShadowBlur,
        )
        drawPath(path = path, color = JervisTheme.rulebookRed)
        drawPath(
            path = path,
            brush = ShaderBrush(grayscaleShader),
            alpha = 0.7f,
        )
        drawPath(path = path, color = JervisTheme.rulebookRed.copy(alpha = 0.5f))

        val lineInset = embossedLineInset.jdp.toPx()
        val embossedLineDistance = if (lineWidth > 0f && size.width > 0f && size.height > 0f) {
            val diagonalDistance = size.width * size.height / lineWidth
            diagonalDistance - lineInset
        } else {
            0f
        }

        val maxFontSize = 180f / density
        val screenScaling = 140f / density
        val fontSize = min(screenScaling * scale, maxFontSize).sp.toPx()
        val angleRadians = atan(size.height / size.width)
        val angleDegrees = (angleRadians * 180 / PI).toFloat()
        val skewX = tan(-angleRadians)
        val padding = (32f + textInset).jdp.toPx()

        val pixelScale = 2f
        val rasterWidth = (size.width / pixelScale).roundToInt().coerceAtLeast(1)
        val rasterHeight = (size.height / pixelScale).roundToInt().coerceAtLeast(1)
        val rasterFontSize = fontSize / pixelScale
        val rasterPadding = padding / pixelScale
        skiaFont.size = rasterFontSize
        skiaFont.edging = if (antiAlias) FontEdging.ANTI_ALIAS else FontEdging.ALIAS
        skiaFont.isSubpixel = antiAlias
        skiaFont.isBaselineSnapped = !antiAlias

        val rasterShadowOffset = Offset(
            x = shadowOffset.x / pixelScale,
            y = shadowOffset.y / pixelScale,
        )
        val rasterShadowBlur = shadowBlur / pixelScale
        val bevelOffset = Offset(
            x = lightDirection.x * bevelSize,
            y = lightDirection.y * bevelSize,
        )
        val rasterBounds = Rect.makeWH(rasterWidth.toFloat(), rasterHeight.toFloat())

        Surface.makeRasterN32Premul(rasterWidth, rasterHeight).use { surface ->
            Surface.makeRasterN32Premul(rasterWidth, rasterHeight).use { maskSurface ->
                val rasterCanvas = surface.canvas
                val maskCanvas = maskSurface.canvas
                rasterCanvas.clear(org.jetbrains.skia.Color.TRANSPARENT)
                maskCanvas.clear(org.jetbrains.skia.Color.TRANSPARENT)

                if (embossedLineDistance > 0f) {
                    val outwardNormalX = size.height / lineWidth
                    val outwardNormalY = size.width / lineWidth
                    rasterCanvas.drawEmbossedLine(
                        start = Offset(
                            x = 0f,
                            y = embossedLineDistance / outwardNormalY / pixelScale,
                        ),
                        end = Offset(
                            x = embossedLineDistance / outwardNormalX / pixelScale,
                            y = 0f,
                        ),
                        baseColor = embossedLineColor,
                        thickness = embossedLineThickness.toPx() / pixelScale,
                        lightDirection = Offset(-1f, -1f),
                    )
                }

                TextLine.make(subTitle, skiaFont).use { subTitleLine ->
                    TextLine.make(mainTitle, skiaFont).use { mainTitleLine ->
                        val lineHeight = rasterFontSize * 1.3f

                        rasterCanvas.save()
                        rasterCanvas.translate(0f, rasterHeight.toFloat())
                        rasterCanvas.rotate(-angleDegrees)
                        rasterCanvas.skew(skewX, 0f)
                        rasterCanvas.drawPixelArtTextLine(
                            textLine = subTitleLine,
                            x = rasterPadding,
                            baselineY = -rasterPadding,
                            gradientStops = pixelGradientStops,
                            antiAlias = antiAlias,
                            outlineColor = outlineColor,
                            outlineRadius = outlineRadius,
                            shadowColor = shadowColor,
                            shadowOffset = rasterShadowOffset,
                            shadowBlur = rasterShadowBlur,
                        )
                        rasterCanvas.drawPixelArtTextLine(
                            textLine = mainTitleLine,
                            x = rasterPadding,
                            baselineY = -lineHeight,
                            gradientStops = pixelGradientStops,
                            antiAlias = antiAlias,
                            outlineColor = outlineColor,
                            outlineRadius = outlineRadius,
                            shadowColor = shadowColor,
                            shadowOffset = rasterShadowOffset,
                            shadowBlur = rasterShadowBlur,
                        )
                        rasterCanvas.restore()

                        SkiaPaint().use { maskPaint ->
                            maskPaint.color = org.jetbrains.skia.Color.WHITE
                            maskPaint.mode = PaintMode.FILL
                            maskPaint.isAntiAlias = antiAlias

                            maskCanvas.save()
                            maskCanvas.translate(0f, rasterHeight.toFloat())
                            maskCanvas.rotate(-angleDegrees)
                            maskCanvas.skew(skewX, 0f)
                            maskCanvas.drawTextLine(
                                subTitleLine,
                                rasterPadding,
                                -rasterPadding,
                                maskPaint,
                            )
                            maskCanvas.drawTextLine(
                                mainTitleLine,
                                rasterPadding,
                                -lineHeight,
                                maskPaint,
                            )
                            maskCanvas.restore()
                        }
                    }
                }

                maskSurface.makeImageSnapshot().use { maskImage ->
                    maskImage.makeShader(
                        tmx = FilterTileMode.DECAL,
                        tmy = FilterTileMode.DECAL,
                        sampling = SamplingMode.DEFAULT,
                    ).use { maskShader ->
                        RuntimeShaderBuilder(etchedTextEffect).apply {
                            child("mask", maskShader)
                            uniform(
                                "highlightColor",
                                highlightColor.red,
                                highlightColor.green,
                                highlightColor.blue,
                                highlightColor.alpha,
                            )
                            uniform(
                                "bevelShadowColor",
                                bevelShadowColor.red,
                                bevelShadowColor.green,
                                bevelShadowColor.blue,
                                bevelShadowColor.alpha,
                            )
                            uniform("bevelOffset", bevelOffset.x, bevelOffset.y)
                        }.makeShader().use { etchedShader ->
                            SkiaPaint().use { fillPaint ->
                                fillPaint.shader = etchedShader
                                fillPaint.mode = PaintMode.FILL
                                fillPaint.isAntiAlias = false
                                rasterCanvas.drawRect(rasterBounds, fillPaint)
                            }
                        }
                    }
                }

                surface.makeImageSnapshot().use { image ->
                    drawContext.canvas.skiaCanvas.drawImageRect(
                        image = image,
                        src = rasterBounds,
                        dst = Rect.makeWH(rasterWidth * pixelScale, rasterHeight * pixelScale),
                        samplingMode = SamplingMode.DEFAULT,
                        paint = null,
                        strict = true,
                    )
                }
            }
        }
    }
}

/**
 * Extends the diagonal edge past both canvas boundaries so translating a hard
 * shadow cannot expose wedges between the shadow and the foreground triangle.
 */
private fun createTitleBackgroundShadowPath(
    size: Size,
    offset: Offset,
    blurRadius: Float,
): Path {
    val width = size.width
    val height = size.height
    val diagonalLength = sqrt(width * width + height * height)
    if (width <= 0f || height <= 0f || diagonalLength == 0f) return Path()

    val blurOverflow = blurRadius * 3f
    val horizontalOverflow = abs(offset.x) + blurOverflow + 1f
    val verticalOverflow = abs(offset.y) + blurOverflow + 1f
    val tangentX = width / diagonalLength
    val tangentY = height / diagonalLength
    val diagonalExtension = max(
        horizontalOverflow / tangentX,
        verticalOverflow / tangentY,
    )

    return Path().apply {
        moveTo(-horizontalOverflow, -verticalOverflow)
        lineTo(
            width + tangentX * diagonalExtension,
            -tangentY * diagonalExtension,
        )
        lineTo(
            -tangentX * diagonalExtension,
            height + tangentY * diagonalExtension,
        )
        close()
    }
}

private val etchedTextShader =
    """
    uniform shader mask;
    uniform float4 highlightColor;
    uniform float4 bevelShadowColor;
    uniform float2 bevelOffset;

    half4 coloredLayer(float4 color, float coverage) {
        float alpha = color.a * coverage;
        return half4(color.rgb * alpha, alpha);
    }

    half4 over(half4 foreground, half4 background) {
        return foreground + background * (1.0 - foreground.a);
    }

    half4 main(float2 fragCoord) {
        float alpha = mask.eval(fragCoord).a;
        float highlightEdge = max(alpha - mask.eval(fragCoord + bevelOffset).a, 0.0);
        float shadowEdge = max(alpha - mask.eval(fragCoord - bevelOffset).a, 0.0);

        half4 highlight = coloredLayer(highlightColor, highlightEdge);
        half4 shadow = coloredLayer(bevelShadowColor, shadowEdge);
        return over(shadow, highlight);
    }
    """.trimIndent()

private fun SkiaCanvas.drawPixelArtTextLine(
    textLine: TextLine,
    x: Float,
    baselineY: Float,
    gradientStops: PixelGradientStops,
    antiAlias: Boolean,
    outlineColor: Color,
    outlineRadius: Float,
    shadowColor: Color,
    shadowOffset: Offset,
    shadowBlur: Float,
) {
    drawPixelArtTextEffects(
        textLine = textLine,
        x = x,
        baselineY = baselineY,
        antiAlias = antiAlias,
        outlineColor = outlineColor,
        outlineRadius = outlineRadius,
        shadowColor = shadowColor,
        shadowOffset = shadowOffset,
        shadowBlur = shadowBlur,
    )

    val gradientShader = SkiaShader.makeLinearGradient(
        x0 = 0f,
        y0 = baselineY + textLine.ascent,
        x1 = 0f,
        y1 = baselineY + textLine.descent,
        colors = gradientStops.colors,
        positions = gradientStops.positions,
    )

    val paperTextShader = """
        uniform shader fill;
        uniform shader paper;
        uniform float strength;
    
        half4 main(float2 position) {
            half4 base = fill.eval(position);
            half noise = paper.eval(position).r;
            half brightness = mix(1.0 - strength, 1.0 + strength, noise);
    
            return half4(base.rgb * brightness, base.a);
        }
    """.trimIndent()

    val paperShader = createGrayscaleNoiseShader().skiaShader
    val paperEffect = RuntimeEffect.makeForShader(paperTextShader)

    RuntimeShaderBuilder(paperEffect).apply {
        child("fill", gradientShader)
        child("paper", paperShader)
        uniform("strength", 0.12f)
    }.makeShader().use { shader ->
        SkiaPaint().use { fillPaint ->
            fillPaint.shader = shader
            fillPaint.mode = PaintMode.FILL
            fillPaint.isAntiAlias = antiAlias
            drawTextLine(textLine, x, baselineY, fillPaint)
        }
    }
}

private fun SkiaCanvas.drawPixelArtTextEffects(
    textLine: TextLine,
    x: Float,
    baselineY: Float,
    antiAlias: Boolean,
    outlineColor: Color,
    outlineRadius: Float,
    shadowColor: Color,
    shadowOffset: Offset,
    shadowBlur: Float,
) {
    if (shadowColor.alpha > 0f) {
        SkiaPaint().use { shadowPaint ->
            shadowPaint.color = shadowColor.toArgb()
            shadowPaint.mode = PaintMode.FILL
            shadowPaint.isAntiAlias = antiAlias

            val drawShadow = {
                drawTextLine(
                    textLine,
                    x + shadowOffset.x,
                    baselineY + shadowOffset.y,
                    shadowPaint,
                )
            }
            if (shadowBlur > 0f) {
                MaskFilter.makeBlur(FilterBlurMode.NORMAL, shadowBlur, respectCTM = false).use { blurFilter ->
                    shadowPaint.maskFilter = blurFilter
                    drawShadow()
                }
            } else {
                drawShadow()
            }
        }
    }

    if (outlineColor.alpha > 0f && outlineRadius > 0f) {
        SkiaPaint().use { outlinePaint ->
            outlinePaint.color = outlineColor.toArgb()
            outlinePaint.mode = PaintMode.STROKE
            outlinePaint.strokeWidth = outlineRadius * 2f
            outlinePaint.isAntiAlias = antiAlias
            drawTextLine(textLine, x, baselineY, outlinePaint)
        }
    }
}

private data class PixelGradientStops(
    val colors: IntArray,
    val positions: FloatArray,
)

@Immutable
data class TitleGradientStop(
    val position: Float,
    val color: Color,
)

private fun createPixelGradientStops(
    gradientStops: ImmutableList<TitleGradientStop>,
    paletteSteps: Int,
): PixelGradientStops {
    require(gradientStops.isNotEmpty()) { "gradientStops cannot be empty" }
    gradientStops.forEach { stop ->
        require(stop.position in 0f..1f) {
            "Gradient stop positions must be between 0 and 1: ${stop.position}"
        }
    }
    gradientStops.zipWithNext().forEach { (left, right) ->
        require(left.position <= right.position) {
            "gradientStops must be sorted by position"
        }
    }

    val colors = IntArray(paletteSteps * 2)
    val positions = FloatArray(paletteSteps * 2)
    repeat(paletteSteps) { index ->
        val colorPosition = if (paletteSteps == 1) {
            0.5f
        } else {
            index.toFloat() / (paletteSteps - 1)
        }
        val bandColor = interpolateGradientColor(gradientStops, colorPosition).toArgb()
        val bandStart = index.toFloat() / paletteSteps
        val bandEnd = (index + 1).toFloat() / paletteSteps

        colors[index * 2] = bandColor
        colors[index * 2 + 1] = bandColor
        positions[index * 2] = bandStart
        positions[index * 2 + 1] = bandEnd
    }
    return PixelGradientStops(colors, positions)
}

private fun interpolateGradientColor(
    gradientStops: ImmutableList<TitleGradientStop>,
    position: Float,
): Color {
    if (gradientStops.size == 1 || position < gradientStops.first().position) {
        return gradientStops.first().color
    }
    if (position >= gradientStops.last().position) {
        return gradientStops.last().color
    }

    // Taking the last matching stop makes duplicate positions behave as a hard transition.
    val leftIndex = gradientStops.indexOfLast { stop -> stop.position <= position }
    val left = gradientStops[leftIndex]
    val right = gradientStops[leftIndex + 1]
    val segmentSize = right.position - left.position
    if (segmentSize == 0f) {
        return right.color
    }
    val segmentPosition = (position - left.position) / segmentSize
    return lerp(left.color, right.color, segmentPosition)
}

// Generates a "noise" shader that will introduce a paper-like quality to the background
// Need to investigate something better, but this seems okay for a first draft.
fun createGrayscaleNoiseShader(tileSize: Int = 4): Shader {

    // Create Noise
    val shader = org.jetbrains.skia.Shader.makeFractalNoise(
        baseFrequencyX = 0.1f, // Adjust for desired texture
        baseFrequencyY = 0.1f,
        numOctaves = 5,
        seed = 0f,
        tileSize = ISize.make(tileSize, tileSize)
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
    ).asComposeShader()
}

fun DrawScope.drawPaperBackground(size: Size) {
    val shader = createGrayscaleNoiseShader(tileSize = 8)
    drawRect(size = size, color = JervisTheme.rulebookRed)
    // Add Noise
    drawRect(
        size = size,
        brush = ShaderBrush(shader),
        alpha = 0.7f,
    )
    // Re-add background color to make the noise blend more into the background
    drawRect(size = size, color = JervisTheme.rulebookRed.copy(alpha = 0.5f))
}
