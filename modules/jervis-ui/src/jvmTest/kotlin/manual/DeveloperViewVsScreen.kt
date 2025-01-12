package manual

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.jervisffb.jervis_ui.generated.resources.Res
import com.jervisffb.jervis_ui.generated.resources.roster_logo_dark_elf
import com.jervisffb.jervis_ui.generated.resources.roster_logo_elven_union
import com.jervisffb.ui.view.JervisTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.skia.Point
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.test.Test

fun mainVs() =
    application {
        val windowState = rememberWindowState()
        Window(onCloseRequest = ::exitApplication, state = windowState) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Content()
            }
        }
    }

@Composable
fun Content() {
    val cornerShape = with(LocalDensity.current) { 16.dp.toPx() }
    val arrowWidth = with(LocalDensity.current) { 8.dp.toPx() }
    val arrowHeight = with(LocalDensity.current) { 12.dp.toPx() }

    var segments by remember { mutableStateOf(1) }
    var amplitudeRandom by remember { mutableStateOf(1f) }
    var stepRandom by remember { mutableStateOf(1f) }
    var widthFunc: (Float) -> Float by remember { mutableStateOf({ x: Float ->
        (-4 * x * x + 4 * x) * 100f
    }) }
    var amplitudeFunc: (Float) -> Float by remember { mutableStateOf({ x: Float ->
        (-4 * x * x + 4 * x) * 100f
    }) }
    Column(modifier = Modifier.fillMaxSize()) {
        Row {
            ParameterBox("Segments", 1 .. 100) {
                segments = it
            }
            AmplitudeBox("AmplitudeRandom", 0f .. 100f) {
                amplitudeRandom = it
            }
            AmplitudeBox("Width", 0f .. 500f) { width ->
                // width = it
                widthFunc = { x: Float ->
                    // Bell curve. See https://en.wikipedia.org/wiki/Gaussian_function
                    val a = 1f // Height
                    val b = 0.5f // Center
                    val c = 0.2f // Width / Std dev
                    a * exp(-(x - b).pow(2) / (2 * c.pow(2))) * width
                }
            }
            AmplitudeBox("StepRandom", 0f..100f) {
                stepRandom = it
            }
            AmplitudeBox("Amplitude") { amplitude ->
                amplitudeFunc = { x: Float ->
                    (-4 * x * x + 4 * x) * amplitude
                }
            }
        }
        BoxWithConstraints(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            var seed by remember(segments, amplitudeRandom, stepRandom, amplitudeFunc, widthFunc) { mutableStateOf(Random.nextLong()) }
            val shapeData = remember(segments, amplitudeRandom, stepRandom, amplitudeFunc, widthFunc, seed) {
                createPathData(
                    segments,
                    (maxWidth.value * 2) / (segments + 1),
                    stepRandom,
                    Random(seed),
                    amplitudeFunc,
                    Size(maxWidth.value * 2, maxHeight.value * 2),
                    amplitudeRandom,
                    widthFunc
                )
            }
            val topShape = remember(shapeData) {
                JaggedPathShape(
                    PathMode.LEFT,
                    shapeData
                )
            }
            val bottomShape = remember(shapeData) {
                JaggedPathShape(
                    PathMode.RIGHT,
                    shapeData
                )
            }
            val pathShape = remember(shapeData) {
                JaggedPathShape(
                    PathMode.PATH,
                    shapeData
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = JervisTheme.rulebookRed,
                        shape = topShape
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = JervisTheme.rulebookBlue,
                        shape = bottomShape
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
//                .shadow(4.dp, shape = pathShape, )
                    .background(
                        color = JervisTheme.rulebookOrange,
                        shape = pathShape,
                    )
            )
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = "VS",
                        style = MaterialTheme.typography.h1.copy(
                            color = Color.Black,
                            fontWeight = FontWeight.ExtraBold,
//                            drawStyle = Stroke(
//                                width = 20f,
//                                join = StrokeJoin.Round
//                            ),
//                            shadow = Shadow(
//                                color = Color.Black,
//                                offset = Offset(4f, 4f),
//                                blurRadius = 8f
//                            )
                        ),
                    )
                    Text(
                        text = "VS",
                        style = MaterialTheme.typography.h1.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                        ),
                    )
                    Row {
                        Box(modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(modifier = Modifier.wrapContentSize()) {
                                    Image(
                                        modifier = Modifier.width(192.dp),
                                        painter = painterResource(Res.drawable.roster_logo_dark_elf),
                                        contentScale = ContentScale.FillWidth,
                                        contentDescription = null,
                                    )
                                }
                                Text(
                                    text = "Dark Elf Starter #1",
                                    maxLines = 1,
                                    style = MaterialTheme.typography.body1.copy(
                                        fontSize = 30.sp,
                                        fontWeight = FontWeight.ExtraBold,
//                                        color = Color.White,
                                        color = JervisTheme.rulebookOrange,
//                                        shadow = Shadow(
//                                            color = Color.Black,
//                                            offset = Offset(2f, 2f),
//                                            blurRadius = 2f
//                                        ),
                                    )
                                )
                                Text(
                                    modifier = Modifier.padding(start = 4.dp, top = 4.dp, end = 4.dp, bottom = 6.dp),
                                    text = "ilios".uppercase(),
                                    maxLines = 1,
                                    style = MaterialTheme.typography.body1.copy(
                                        fontSize = 18.sp,
                                        color = Color.White,
                                        fontStyle = FontStyle.Italic,
                                        shadow = Shadow(
                                            color = Color.Black,
                                            offset = Offset(0f, 0f),
                                            blurRadius = 1f
                                        )
                                    ),
                                )
                                Text(
                                    modifier = Modifier.background(Color.Black).padding(4.dp),
                                    maxLines = 1,
                                    text = "Dark Elf - TV 1.000K",
                                    style = MaterialTheme.typography.body1.copy(
                                        fontSize = 20.sp,
                                        color = Color.White,
                                        shadow = Shadow()
                                    ),
                                )
                            }
                        }
                        Box(modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(modifier = Modifier.wrapContentSize()) {
                                    Image(
                                        modifier = Modifier.width(192.dp),
                                        painter = painterResource(Res.drawable.roster_logo_elven_union),
                                        contentScale = ContentScale.FillWidth,
                                        contentDescription = null,
                                    )
                                }
                                Text(
                                    text = "Elven Union Starter #1",
                                    maxLines = 1,
                                    style = MaterialTheme.typography.body1.copy(
                                        fontSize = 30.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = JervisTheme.rulebookOrange,
//                                        shadow = Shadow(
//                                            color = Color.Black,
//                                            offset = Offset(2f, 2f),
//                                            blurRadius = 2f
//                                        ),
                                    )
                                )
                                Text(
                                    modifier = Modifier.padding(start = 4.dp, top = 4.dp, end = 4.dp, bottom = 6.dp),
                                    text = "another".uppercase(),
                                    maxLines = 1,
                                    style = MaterialTheme.typography.body1.copy(
                                        fontSize = 18.sp,
                                        color = Color.White,
                                        fontStyle = FontStyle.Italic,
                                        shadow = Shadow(
                                            color = Color.Black,
                                            offset = Offset(0f, 0f),
                                            blurRadius = 1f
                                        )
                                    ),
                                )
                                Text(
                                    modifier = Modifier.background(Color.Black).padding(4.dp),
                                    maxLines = 1,
                                    text = "Elven Union - TV 1.200K",
                                    style = MaterialTheme.typography.body1.copy(
                                        fontSize = 20.sp,
                                        color = Color.White,
                                        shadow = Shadow()
                                    ),
                                )
                            }
                        }
                    }
                }
//                Row {
//                    Column(modifier = Modifier.fillMaxWidth().height(125.dp).padding(bottom = 24.dp).shadow(4.dp)) {
//                        Row(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .weight(1f)
//                                .background(JervisTheme.fumbblBlueDarker),
//                            verticalAlignment = Alignment.CenterVertically,
//                        ) {
//                            TeamStatValue(homeTeam = true, "2")
//                            Spacer(modifier = Modifier.weight(1f))
//                            TeamStatValueTitle("Fan Factor")
//                            Spacer(modifier = Modifier.weight(1f))
//                            TeamStatValue(homeTeam = false, "4")
//                        }
//                        Row(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .weight(1f)
//                                .background(JervisTheme.fumbblBlueDark),
//                            verticalAlignment = Alignment.CenterVertically,
//                        ) {
//                            TeamStatValue(homeTeam = true, "2")
//                            Spacer(modifier = Modifier.weight(1f))
//                            TeamStatValueTitle("Journeymen")
//                            Spacer(modifier = Modifier.weight(1f))
//                            TeamStatValue(homeTeam = false, "2")
//                        }
//                    }
//                }
            }
        }
    }
}

@Composable
fun RowScope.TeamStatValueTitle(text: String) {
    Text(
        text = text,
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontStyle = FontStyle.Italic,
        fontSize = 18.sp
    )
}

@Composable
fun RowScope.TeamStatValue(homeTeam: Boolean, text: String) {
    val modifier = Modifier.padding(start = if (homeTeam) 24.dp else 0.dp, end = if (homeTeam) 0.dp else 24.dp)
    Text(
        modifier = modifier,
        text = text,
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
    )
}

data class PathPoint(
    val bottom: Point,
    val middle: Point,
    val top: Point,
)

class JaggedPathShape(
    val mode: PathMode,
    val data: List<PathPoint>,
): Shape {
    lateinit var path: Path
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            when (mode) {
                PathMode.PATH -> {
                    moveTo(data[0].middle.x, data[0].middle.y)
                    data.forEachIndexed { index, point ->
                        if (index > 0) {
                            lineTo(point.top.x, point.top.y)
                        }
                    }
                    data.reversed().forEachIndexed { index, point ->
                        if (index > 0) {
                            lineTo(point.bottom.x, point.bottom.y)
                        }
                    }
                    close()
                }
                PathMode.LEFT -> {
                    moveTo(0f, size.height)
                    lineTo(data.first().middle.x, data.first().middle.y)
                    data.forEachIndexed { index, point ->
                        if (index > 0) {
                            lineTo(point.bottom.x, point.bottom.y)
                        }
                    }
                    lineTo(0f, 0f)
                    close()
                }
                PathMode.RIGHT -> {
                    moveTo(size.width, size.height)
                    lineTo(data.first().middle.x, data.first().middle.y)
                    data.forEachIndexed { index, point ->
                        if (index > 0) {
                            lineTo(point.top.x, point.top.y)
                        }
                    }
                    lineTo(size.width, 0f)
                    close()
                }
            }
        }
        this.path = path
        return Outline.Generic(path)
    }
}

enum class PathMode {
    PATH,
    LEFT,
    RIGHT
}

private fun createPathData(
    segments: Int,
    stepSize: Float,
    stepRandom: Float,
    random: Random,
    amplitudeFunc: (Float) -> Float,
    boxSize: Size,
    amplitudeRandom: Float,
    widthFunc: (Float) -> Float
): List<PathPoint> = buildList {
    // Define line we are drawing along
    val xTop = boxSize.width * 55 / 100 // Top of screen
    val xBottom = boxSize.width * 45 / 100 // Bottom of screen
    val m = (boxSize.height - 0) / (xBottom - xTop)
    val c = boxSize.height - (m * xBottom)
    val centerLine: (Float) -> Float = { x ->
        m * x + c
    }
    val length = sqrt(((xTop - xBottom).pow(2) + (boxSize.height).pow(2)))
    val segmentSize = if (segments == 1) (abs(xTop - xBottom)) else (abs(xTop - xBottom)) / segments
    // Add starting point
    add(PathPoint(Point(xBottom, boxSize.height), Point(xBottom, boxSize.height),Point(xBottom, boxSize.height)))
    // Add intermediate steps
    repeat(segments - 1) { step ->
        val middleX = xBottom + (step + 1) * segmentSize + (if (stepRandom > 0f) random.nextDouble(-stepRandom.toDouble(), stepRandom.toDouble()).toFloat() else 0f)
        val amplitude = amplitudeFunc((middleX - xBottom) / abs((xTop - xBottom))) * 10f // + (if (amplitudeRandom != 0f) Random.nextDouble(-abs(amplitudeRandom).toDouble(), abs(amplitudeRandom).toDouble()).toFloat() else 0f)
        val adjustUp = if (amplitudeRandom > 0f) random.nextDouble(amplitudeRandom.toDouble()).toFloat() else 0f
        val adjustDown = if (amplitudeRandom > 0f) random.nextDouble(amplitudeRandom.toDouble()).toFloat() else 0f
        val y = centerLine(middleX)

        val width = widthFunc(middleX / abs(xTop - xBottom))
        val (rightPoint, leftPoint) = perpendicularPoint(m.toDouble(), Point(middleX, y), -amplitude /*+ width + adjustDown)*/, amplitude /*+ width + adjustUp*/)
        add(
            PathPoint(
                bottom = Point(leftPoint.x, max(0f, leftPoint.y + adjustDown)),
                middle = (Point(middleX, y)),
                top = Point(rightPoint.x, min(boxSize.height, rightPoint.y + adjustUp)),
            )
        )
    }
    // Add end point
    add(PathPoint(Point(xTop, 0f), Point(xTop, 0f),Point(xTop, 0f)))
}

fun perpendicularPoint(lineSlope: Double, point: Point, distanceLeft: Float, distanceRight: Float): Pair<Point, Point> {
    // Slope of the perpendicular line
    val perpSlope = if (lineSlope == 0.0) Double.POSITIVE_INFINITY else -1 / lineSlope

    // Direction vector components (normalized)
    val dx = if (perpSlope.isInfinite()) 0.0 else 1.0 / sqrt(1 + perpSlope * perpSlope)
    val dy = if (perpSlope.isInfinite()) 1.0 else perpSlope / sqrt(1 + perpSlope * perpSlope)

    // Calculate two points at distance d along the perpendicular
    val x1 = (point.x + distanceRight * dx).toFloat()
    val y1 = (point.y + distanceRight * dy).toFloat()
    val x2 = (point.x + distanceLeft * dx).toFloat()
    val y2 = (point.y + distanceLeft * dy).toFloat()

    // Return the two possible points
    return Pair(Point(x1, y1), Point(x2, y2))
}

@Composable
fun RowScope.ParameterBox(header: String, valueRange: IntRange = 0 .. 100, onValueChange: (Int) -> Unit) {
    var value by remember { mutableStateOf(0f) }
    Card(modifier = Modifier.weight(1f)) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(header)
            Slider(
                value = value,
                valueRange = valueRange.first.toFloat()..valueRange.last.toFloat(),
                steps = valueRange.last - valueRange.first - 1,
                onValueChange = {
                    value = it
                    onValueChange(value.roundToInt())
                },
            )
            Text(text = value.roundToInt().toString())
        }
    }
}

@Composable
fun RowScope.AmplitudeBox(header: String, valueRange: ClosedFloatingPointRange<Float> = 0f .. 100f, onValueChange: (Float) -> Unit) {
    var value by remember { mutableStateOf(0f) }
    Card(modifier = Modifier.weight(1f)) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(header)
            Slider(
                value = value,
                valueRange = valueRange,
                onValueChange = {
                    value = it
                    onValueChange(value)
                },
            )
            Text(text = value.roundToInt().toString())
        }
    }
}


class DeveloperViewVsScreen {
    @Test
    fun run() {
        mainVs()
    }
}

class RightBubbleShape(
    private val cornerShape: Float,
    private val arrowWidth: Float,
    private val arrowHeight: Float
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        Path
        return Outline.Generic(Path().apply {
            reset()
            val amplitude = 100f

            val steps = 10 // Make this dynamic
            val stepSize = size.width / (steps - 1)

            moveTo(0f, size.height / 2)
            repeat(steps) { step ->
//                if (step > 0) {
                    val x = step * stepSize
                    val y = size.height / 2 + (Random.nextDouble(amplitude.toDouble()) * (if (step % 2 == 0) 1 else -1))
                    lineTo(x, y.toFloat())
//                } else if (step == steps - 1) {
//                    val x = size.width
//                    val y = size.height / 2
//                }
            }
            close()
        })
    }
}


fun Modifier.drawRightBubble(
    bubbleColor: Color,
    cornerShape: Float,
    arrowWidth: Float,
    arrowHeight: Float
) = then(
    background(
        color = bubbleColor,
        shape = RightBubbleShape(
            cornerShape = cornerShape,
            arrowWidth = arrowWidth,
            arrowHeight = arrowHeight
        )
    )
)

fun extractOutline(bitmap: ImageBitmap): Path {
    val path = Path()
    val width = bitmap.width
    val height = bitmap.height
    val pixels = IntArray(width * height)

    // Extract pixels from bitmap
    bitmap.readPixels(pixels, 0, width, 0, 0, width, height)

    // Traverse each pixel to find non-transparent ones
    for (y in 0 until height) {
        for (x in 0 until width) {
            val pixel = pixels[x + y * width]
            if (pixel != 0) { // Non-transparent
                if (isEdgePixel(x, y, pixels, width, height)) {
                    path.addOval(Rect(center = Offset.Unspecified.copy(x.toFloat(), y.toFloat()), radius = 1f), Path.Direction.Clockwise)
                }
            }
        }
    }
    return path
}

fun isEdgePixel(x: Int, y: Int, pixels: IntArray, width: Int, height: Int): Boolean {
    val currentIndex = x + y * width
    return (x > 0 && pixels[currentIndex - 1] == 0) || // Left
        (x < width - 1 && pixels[currentIndex + 1] == 0) || // Right
        (y > 0 && pixels[currentIndex - width] == 0) || // Top
        (y < height - 1 && pixels[currentIndex + width] == 0) // Bottom
}

fun drawOutline(bitmap: ImageBitmap): ImageBitmap {
    val outlinePath = extractOutline(bitmap)
    val outputBitmap = ImageBitmap(bitmap.width, bitmap.height, ImageBitmapConfig.Argb8888)
    val canvas = Canvas(outputBitmap)
    val paint = Paint().apply {
        color = Color.Black
        style = PaintingStyle.Stroke
        strokeWidth = 2f
    }
    canvas.drawPath(outlinePath, paint)
    return outputBitmap
}

