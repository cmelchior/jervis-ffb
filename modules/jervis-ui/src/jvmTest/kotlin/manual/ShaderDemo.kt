package manual

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.jervisffb.jervis_ui.generated.resources.Res
import com.jervisffb.jervis_ui.generated.resources.icons_actions_jump
import org.intellij.lang.annotations.Language
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.skia.RuntimeEffect
import org.jetbrains.skia.RuntimeShaderBuilder
import kotlin.math.min
import kotlin.test.Ignore
import kotlin.test.Test

/**
 * Manual tests for playing around with shaders
 */
class ShaderTests() {

    @Test
    @Ignore // This is only a manual test
    fun main() {
        app()
    }
}

fun app() {
    application {
        val state = rememberWindowState(
            placement = WindowPlacement.Floating,
            position = WindowPosition.Aligned(Alignment.Center),
            size = DpSize(300.dp, 300.dp)
        )
        Window(
            title = "Compose / Skia shader demo",
            state = state,
            onCloseRequest = ::exitApplication,
        ) {
            val density = LocalDensity.current // OR, for example, Density(1f, 1f)
            val image = painterResource(Res.drawable.icons_actions_jump)
            val size = image.intrinsicSize
            ImageShaderExample(image.toImageBitmap(size, density))
        }
    }

}

fun Painter.toImageBitmap(
    size: Size,
    density: Density,
): ImageBitmap {
    val layoutDirection = LayoutDirection.Ltr
    val bitmap = ImageBitmap(size.width.toInt(), size.height.toInt())
    val canvas = Canvas(bitmap)
    CanvasDrawScope().draw(density, layoutDirection, canvas, size) {
        draw(size)
    }
    return bitmap
}


@Composable
fun ImageShaderExample(bitmap: ImageBitmap) {
    // GLSL Shader Code
    @Language("GLSL")
    val shaderCode = """
uniform shader image;
uniform vec2 resolution; // [widthPx, heightPx] 
uniform vec2 scaleFactor; // [xScale, yScale]
// uniform float blurRadius;

const float blurRadius = 1.0;

vec4 blur(vec2 uv) {
    vec4 sum = vec4(0.0);
    float total = 0.0;

    for (float x = -blurRadius; x <= blurRadius; x++) {
        for (float y = -blurRadius; y <= blurRadius; y++) {
            vec2 offset = vec2(x, y);
            vec4 sample = image.eval(uv + offset);
            
            // Use alpha or brightness as the mask source
            float maskValue = sample.a * 5; // Use sample.r for brightness-based masking
            float weight = exp(-0.5 * (x * x + y * y) / (blurRadius * blurRadius));
            
            sum += vec4(maskValue) * weight;
            total += weight;
        }
    }
    
    return sum / total;
}

half4 main(vec2 fragCoord) {
    vec2 uv = fragCoord;
    vec4 original = image.eval(uv);
    vec4 blurredMask = blur(uv);

    // Green background with intensity from the blurred mask
    vec4 tint = vec4(56.0/255.0, 162.0/255.0, 59.0/255.0, 1.0); // JervisTheme.rulebookGreenAccent
    vec4 greenEffect = tint * blurredMask;

    return greenEffect;
}
    """.trimIndent()

    val imageShader = ImageShader(bitmap, TileMode.Decal, TileMode.Decal)
    val runtimeEffect = RuntimeEffect.makeForShader(shaderCode)

    BoxWithConstraints(
        modifier = Modifier
            .aspectRatio(1f)
            .fillMaxSize()
            .drawWithCache {
                val canvasWidth = size.width
                val canvasHeight = size.height

                val shaderWidth = bitmap.width.toFloat()
                val shaderHeight = bitmap.height.toFloat()

                // Calculate scale factors
                val scaleX = canvasWidth / shaderWidth
                val scaleY = canvasHeight / shaderHeight
                val scale = min(scaleX, scaleY) // Maintain aspect ratio

                val shader = RuntimeShaderBuilder(runtimeEffect).apply {
                    child("image", imageShader)
                    uniform("resolution", size.width / 2.0f, size.height / 2.0f)
                    uniform("scaleFactor", scaleX, scaleY)
                }.makeShader()
                val brush = ShaderBrush(shader)

                // Apply transformation
                onDrawBehind {
                    withTransform({
                        scale(scale, scale, pivot = Offset.Zero)
                    }) {
                        drawRect(brush = brush, topLeft = Offset.Zero, size = size)
                    }
                }
            }
    ) {
        // Draw the original sharp image on top
        Image(
            bitmap = bitmap,
            contentDescription = null,
            filterQuality = FilterQuality.None,
            modifier = Modifier.matchParentSize()
        )
    }
}
