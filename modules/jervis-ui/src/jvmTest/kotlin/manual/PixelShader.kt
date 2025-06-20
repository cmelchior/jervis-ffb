package manual

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.unit.dp
import org.jetbrains.skia.*
import androidx.compose.ui.window.singleWindowApplication
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.LayoutDirection
import com.jervisffb.engine.actions.BlockDice
import com.jervisffb.engine.model.Field
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.StandardBB2020Rules
import com.jervisffb.ui.createDefaultAwayTeam
import com.jervisffb.ui.createDefaultHomeTeam
import com.jervisffb.ui.game.icons.IconFactory
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.view.utils.PixelatedImage
import com.jervisffb.utils.runBlocking
import kotlin.test.Test

class PixelShaderTests() {
    @Test
    fun testShader() {
        main()
    }
}

private fun main() = singleWindowApplication {
    val rules = StandardBB2020Rules()
    val game = Game(
        rules,
        createDefaultHomeTeam(rules),
        createDefaultAwayTeam(rules),
        Field.createForRuleset(rules),
    )
    runBlocking {
        IconFactory.initializeFumbblMapping()
        IconFactory.initialize(game.homeTeam, game.awayTeam)
    }
    Box(
        modifier = Modifier.size(100.dp).background(JervisTheme.diceBackground)
    ) {
        PixelatedImage(painter = IconFactory.getDiceIcon(BlockDice.PLAYER_DOWN))
    }
}

@Composable
fun PixelatedImage(
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

    BoxWithConstraints {
        val width = maxWidth
        val height = maxHeight
        val density = LocalDensity.current
        val widthPx = with(density) { width.toPx() }
        val heightPx = with(density) { height.toPx() }

        // Render SVG to bitmap at requested size
        val skiaBitmap = remember {
            val imageBitmap = painter.toImageBitmap(Size(widthPx, heightPx), density, LayoutDirection.Ltr)
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

        Canvas(modifier = Modifier.size(width, height)) {
            drawIntoCanvas { canvas ->
                val paint = Paint().apply {
                    this.shader = shader
                }
                canvas.nativeCanvas.drawRect(Rect.makeXYWH(0f, 0f, widthPx, heightPx), paint)
            }
        }
    }
}
