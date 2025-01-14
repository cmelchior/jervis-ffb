package manual

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.jervisffb.ui.view.JervisTheme
import org.intellij.lang.annotations.Language
import org.jetbrains.skia.ColorFilter
import org.jetbrains.skia.ColorMatrix
import org.jetbrains.skia.FilterTileMode
import org.jetbrains.skia.ISize
import org.jetbrains.skia.ImageFilter
import org.jetbrains.skia.RuntimeEffect
import org.jetbrains.skia.RuntimeShaderBuilder
import org.pushingpixels.artemis.drawTextOnPath


fun mainBackground() =
    application {
        val windowState = rememberWindowState()
        Window(onCloseRequest = ::exitApplication, state = windowState) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
                GrayscaleNoise()
            }
        }
    }

@Composable
fun GrayscaleNoise(modifier: Modifier = Modifier) {

    // Create Noise
    val shader = Shader.makeFractalNoise(
        baseFrequencyX = 0.1f, // Adjust for desired texture
        baseFrequencyY = 0.1f,
        numOctaves = 5,
        seed = 0f,
        tileSize = ISize.make(4, 4)
    )

    // Apply a color filter to convert to grayscale
    val grayscaleShader = shader.makeWithColorFilter(
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

    Canvas(modifier = Modifier.fillMaxSize()) {
        // Background color
        drawRect(color = JervisTheme.rulebookPaper, size = size)
        // Add Noise
        drawRect(
            size = size,
            brush = ShaderBrush(grayscaleShader),
            alpha = 0.3f,
        )
        // Re-add background color to make the noise blend more into the background
        drawRect(color = JervisTheme.rulebookPaper.copy(alpha = 0.5f), size = size)
    }
}

// Modified from https://github.com/kirill-grouchnikov/artemis/blob/woodland/src/main/kotlin/org/pushingpixels/artemis/Glassmorphic.kt
// Recreate visuals from https://uxmisfit.com/2021/01/13/how-to-create-glassmorphic-card-ui-design/
@Composable
fun NoiseBackgroundExample() {
    Box(modifier = Modifier.fillMaxSize(1.0f).background(Color(0xFF03080D))) {
        @Language("GLSL")
        val compositeSksl = """
                uniform shader content;
                uniform shader blur;
                uniform shader noise;
                
                uniform vec4 rectangle;
                uniform float radius;
                
                uniform float dropShadowSize;
                
                // Simplified version of SDF (signed distance function) for a rounded box
                // from https://www.iquilezles.org/www/articles/distfunctions2d/distfunctions2d.htm
                float roundedRectangleSDF(vec2 position, vec2 box, float radius) {
                    vec2 q = abs(position) - box + vec2(radius);
                    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - radius;   
                }
                
                vec4 main(vec2 coord) {
//                    vec2 shiftRect = (rectangle.zw - rectangle.xy) / 2.0;
//                    vec2 shiftCoord = coord - rectangle.xy;
//                    float distanceToClosestEdge = roundedRectangleSDF(
//                        shiftCoord - shiftRect, shiftRect, radius);
//        
//                    vec4 c = content.eval(coord);
//                    if (distanceToClosestEdge > 0.0) {
//                        // We're outside of the filtered area
//                        if (distanceToClosestEdge < dropShadowSize) {
//                            // Emulate drop shadow around the filtered area
//                            float darkenFactor = (dropShadowSize - distanceToClosestEdge) / dropShadowSize;
//                            // Use exponential drop shadow decay for more pleasant visuals
//                            darkenFactor = pow(darkenFactor, 1.6);
//                            // Shift towards black, by 10% around the edge, dissipating to 0% further away
//                            return c * (0.9 + (1.0 - darkenFactor) / 10.0);
//                        }
//                        return c;
//                    }
                    
                    vec4 b = blur.eval(coord);
                    vec4 n = noise.eval(coord);
                    // How far are we from the top-left corner?
                    float lightenFactor = min(1.0, length(coord - rectangle.xy) / (0.85 * length(rectangle.zw - rectangle.xy)));
                    // Add some noise for extra texture
                    float noiseLuminance = dot(n.rgb, vec3(0.2126, 0.7152, 0.0722));
                    lightenFactor = min(1.0, lightenFactor + noiseLuminance);
                    // Shift towards white, by 35% in top left corner, down to 10% in bottom right corner
                    return b + (vec4(1.0) - b) * (0.35 - 0.25 * lightenFactor);
                }
            """

        val compositeRuntimeEffect = RuntimeEffect.makeForShader(compositeSksl)
        val compositeShaderBuilder = RuntimeShaderBuilder(compositeRuntimeEffect)

        val density = LocalDensity.current.density
        compositeShaderBuilder.uniform(
            "rectangle",
            85.0f * density, 110.0f * density, 405.0f * density, 290.0f * density
        )
        compositeShaderBuilder.uniform("radius", 20.0f * density)
        compositeShaderBuilder.child(
            "noise", Shader.makeFractalNoise(
                baseFrequencyX = 0.45f,
                baseFrequencyY = 0.45f,
                numOctaves = 4,
                seed = 2.0f
            )
        )
//        compositeShaderBuilder.uniform("dropShadowSize", 15.0f * density)

        Canvas(
            modifier = Modifier.fillMaxSize(1.0f)
                .graphicsLayer(
                    renderEffect = ImageFilter.makeRuntimeShader(
                        runtimeShaderBuilder = compositeShaderBuilder,
                        shaderNames = arrayOf("content", "blur"),
                        inputs = arrayOf(
                            null, ImageFilter.makeBlur(
                                sigmaX = 20.0f,
                                sigmaY = 20.0f,
                                mode = FilterTileMode.DECAL
                            )
                        )
                    ).asComposeRenderEffect(),
                )
        ) {

        }
    }

    Canvas(modifier = Modifier.fillMaxSize(1.0f)) {
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(Color(0x80FFFFFF), Color(0x00FFFFFF), Color(0x00FF48DB), Color(0x80FF48DB)),
                start = Offset(120.dp.toPx(), 110.dp.toPx()),
                end = Offset(405.dp.toPx(), 290.dp.toPx()),
                tileMode = TileMode.Clamp
            ),
            topLeft = Offset(86.dp.toPx(), 111.dp.toPx()),
            size = Size(318.dp.toPx(), 178.dp.toPx()),
            cornerRadius = CornerRadius(20.dp.toPx()),
            style = Stroke(width = 2.dp.toPx()),
        )

        drawTextOnPath(
            text = "MEMBERSHIP",
            textSize = 14.dp,
            isEmboldened = true,
            path = Path().also { path ->
                path.moveTo(100.dp.toPx(), 140.dp.toPx())
                path.lineTo(400.dp.toPx(), 140.dp.toPx())
            },
            offset = Offset(2.dp.toPx(), 0.0f),
            textAlign = TextAlign.Left,
            paint = Paint().also {
                it.color = Color(0x80FFFFFF)
                it.style = PaintingStyle.Fill
            },
        )

    }
}

@Preview
@Composable
fun PreviewNoiseBackgroundExample() {
    NoiseBackgroundExample()
}
