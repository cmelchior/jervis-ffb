package manual

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.singleWindowApplication
import com.jervisffb.engine.model.Field
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.StandardBB2020Rules
import com.jervisffb.ui.createDefaultAwayTeam
import com.jervisffb.ui.createDefaultHomeTeam
import com.jervisffb.ui.game.icons.IconFactory
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.utils.scalePixels
import com.jervisffb.utils.runBlocking
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Rect
import org.jetbrains.skia.RuntimeEffect
import org.jetbrains.skia.RuntimeShaderBuilder
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.test.Ignore
import kotlin.test.Test

@Ignore // Must run manually
class PixelShaderTests() {
    @Test
    fun testShader() {
        main()
    }
}

private fun main() = singleWindowApplication {
    val density = LocalDensity.current
    val rules = StandardBB2020Rules()
    val game = Game(
        rules,
        createDefaultHomeTeam(rules),
        createDefaultAwayTeam(rules),
        Field.createForRuleset(rules),
    )
    runBlocking {
        IconFactory.initializeFumbblMapping()
        IconFactory.initialize(density, game.homeTeam, game.awayTeam)
    }
    Box(
        modifier = Modifier.size(48.dp).background(JervisTheme.diceBackground)
    ) {

//        PixelatedImage(
//            painter = IconFactory.getDiceIcon(BlockDice.BOTH_DOWN),
//        )
//        PixelatedImage(
//            image = IconFactory.getDiceIcon(BlockDice.BOTH_DOWN),
//            size = 48.dp,
//            scaleFactor = 2f
//        )

//        PixelatedImage(
////            painter = IconFactory.getDiceIcon(BlockDice.BOTH_DOWN),
//            painter = painterResource(Res.drawable.jervis_icon_reroll_red), // IconFactory.getDiceIcon(BlockDice.PLAYER_DOWN),
//            pixelSize = 2f
//        )
    }
}

@Composable
fun PixelImageUsingResize(
    width: Dp,
    painter: Painter
) {
    Box(modifier = Modifier.graphicsLayer {
        scaleX = 2f
        scaleY = 2f
    }) {
        Box(modifier = Modifier.size(width/2)) {
            Image(
                painter = painter,
                contentDescription = "",
                contentScale = ContentScale.Fit ,
            )
        }
    }
}


@Composable
fun PixelatedImageWithShader(
    modifier: Modifier = Modifier,
    painter: Painter,
    pixelSize: Float = 4f
) {
    val shaderCode = """
            uniform shader img;
            uniform float2 resolution;
            uniform float pixelSize;

            half4 main(float2 fragCoord) {
                float2 pixelCoord = floor(fragCoord / pixelSize) * pixelSize;
                float2 uv = pixelCoord / resolution;
                return img.eval(uv * resolution);
            }
    """.trimIndent()

    val effect = remember { RuntimeEffect.makeForShader(shaderCode) }
    val shaderBuilder = remember(effect) { RuntimeShaderBuilder(effect) }

    BoxWithConstraints(
        modifier = modifier
    ) {
        val width = maxWidth
        val height = maxHeight
        val density = LocalDensity.current
        val widthPx = with(density) { width.toPx() }
        val heightPx = with(density) { height.toPx() }

        // Render SVG to bitmap at requested size
        val skiaBitmap = remember {
            painter.intrinsicSize
            val imageBitmap = painter.toImageBitmap(fitInside(painter.intrinsicSize, Size(widthPx, heightPx)), density)
            imageBitmap.asSkiaBitmap()
        }

        // Build the shader to pixelate the image
        val shader = remember {
            shaderBuilder.uniform(
                "resolution",
                widthPx,
                heightPx
            )
            shaderBuilder.uniform(
                "pixelSize",
                pixelSize
            )
            shaderBuilder.child(
                "img",
                skiaBitmap.makeShader()
            )
            shaderBuilder.makeShader()
        }

        Canvas(modifier = Modifier
        ) {
            drawIntoCanvas { canvas ->
                val paint = Paint().apply {
                    this.shader = shader
                }
                canvas.nativeCanvas.drawRect(Rect.makeXYWH(0f, 0f, skiaBitmap.width.toFloat(), skiaBitmap.height.toFloat()), paint)
            }
        }
    }
}


fun fitInside(
    original: Size,
    maxSize: Size
): Size {
    val scale = min(
        maxSize.width / original.width,
        maxSize.height
            / original.height
    )
    return Size(
        (original.width  * scale),
        (original.height * scale)
    )
}

@Composable
fun PixelatedImage(
    image: Painter,
    size: Dp,
    scaleFactor: Float = 4f,
) {
    val density = LocalDensity.current
    val sizePx = with(density) { size.toPx() }
    val painter = image
    val img = painter
        .toImageBitmap(
            fitInside(painter.intrinsicSize, Size(sizePx/scaleFactor, sizePx/scaleFactor)),
            density
        ).scalePixels(2)

    Canvas(modifier = Modifier.size(size, size).background(Color.Transparent)) {
        val width = (img.width * scaleFactor).roundToInt()
        val height = (img.height * scaleFactor).roundToInt()
        drawImage(
            image = img,
            dstOffset = IntOffset(
                ((this.size.width - width)/2f).roundToInt(),
                (this.size.height - scaleFactor*height/2f).roundToInt()
            ),
            dstSize = IntSize(width, height),
            filterQuality = FilterQuality.None
        )
    }
}

