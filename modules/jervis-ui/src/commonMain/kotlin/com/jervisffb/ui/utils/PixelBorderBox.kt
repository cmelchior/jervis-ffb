package com.jervisffb.ui.utils

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import org.jetbrains.skia.RuntimeEffect
import org.jetbrains.skia.RuntimeShaderBuilder
import org.jetbrains.skia.Shader

// Box that renders a pixel-border around the actual content of the box
@Composable
fun PixelBorderBox(
    modifier: Modifier = Modifier,
    borderEnabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val layer = rememberGraphicsLayer()
    var layerBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var captureRequested by remember { mutableStateOf(false) }

    LaunchedEffect(captureRequested) {
        if (captureRequested && borderEnabled) {
            layerBitmap = layer.toImageBitmap()
            captureRequested = false
        }
    }

    Box(
        modifier = modifier
            .onSizeChanged { layerBitmap = null }
            .drawWithPixelBorder(
                layer = layer,
                borderEnabled = borderEnabled,
                layerBitmap = layerBitmap,
                onCaptureRequested = { captureRequested = true }
            ),
    ) {
        content()
    }
}

// Shader code generated mostly by ChatGPT, so probably there is a better way to achieve
// this, but for now it seems to work.
// There are some issues with drawing outside the Canvas, which is why TileMode.Decal
// is set. There are trade-offs to this as it forces us to keep the entire player icon
// inside its "square". But going outside will also look messy in some cases. Probably
// something to experiment with.
// NOTE: This template has a %color% that must be replaced using string manipulation
// It is possible to provide arguments to shaders, but this is just a quick prototype.
private val borderShaderTemplate = """
        uniform shader image;
        uniform vec2 resolution; // [widthPx, heightPx] 
        uniform vec2 scaleFactor; // [xScale, yScale]
        const float blurRadius = 4.0; // Loop initializers must be constant 
        
        vec4 blur(vec2 uv) {
            vec4 sum = vec4(0.0);
            float total = 0.0;
        
            for (float x = -blurRadius; x <= blurRadius; x++) {
                for (float y = -blurRadius; y <= blurRadius; y++) {
                    vec2 offset = vec2(x, y);
                    vec4 sample = image.eval(uv + offset);
                    
                    // Use alpha or brightness as the mask source
                    float maskValue = sample.a * 5; // Multiply with 5 to increase the opaqueness of the mask
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
            vec4 tint = %tintColor%
            vec4 greenEffect = tint * blurredMask;
        
            return greenEffect;
        }
""".trimIndent()

val borderShader = borderShaderTemplate.replace(
    oldValue = "%tintColor%",
    newValue = "vec4(255.0/255.0, 190.0/255.0, 38.0/255.0, 1.0); // JervisTheme.orange"
    // newValue = "vec4(255.0/255.0, 255.0/255.0, 255.0/255.0, 1.0); // JervisTheme.white"
    //    newValue = "vec4(190.0/255.0, 38.0/255.0, 255.0/255.0, 1.0); // JervisTheme.purple"
    //    newValue = "vec4(0.0/255.0, 119.0/255.0, 198.0/255.0, 1.0); // JervisTheme.rulebookBlue"
    //    newValue = "vec4(0.0/255.0, 0.0/255.0, 0.0/255.0, 1.0); // JervisTheme.rulebookBlue"
    //    newValue = "vec4(56.0/255.0, 162.0/255.0, 59.0/255.0, 1.0); // JervisTheme.rulebookGreenAccent"
    //    newValue = "vec4(255.0/255.0, 255.0/255.0, 255.0/255.0, 1.0); // JervisTheme.rulebookBlue"
)

private fun Modifier.drawWithPixelBorder(
    layer: GraphicsLayer,
    borderEnabled: Boolean,
    layerBitmap: ImageBitmap?,
    onCaptureRequested: () -> Unit,
): Modifier = drawWithCache {

    fun outlineShader(bitmap: ImageBitmap, size: IntSize): Shader {
        val imageShader = ImageShader(bitmap, TileMode.Clamp, TileMode.Clamp)
        val runtimeEffect = RuntimeEffect.makeForShader(borderShader)
        val shader = RuntimeShaderBuilder(runtimeEffect).apply {
            child("image", imageShader)
            uniform("resolution", size.width, size.height)
            uniform("scaleFactor", 1f, 1f)
        }.makeShader()
        return shader
    }

    val paint = Paint()
    val sizeInt = IntSize(size.width.toInt(), size.height.toInt())

    onDrawWithContent {
        // Render children into an offscreen layer
        layer.record { this@onDrawWithContent.drawContent() }

        // Fast path (no effect) if size is empty
        if (sizeInt.width <= 0 || sizeInt.height <= 0) {
            drawLayer(layer)
            return@onDrawWithContent
        }

        if (borderEnabled) {
            if (layerBitmap == null) {
                // Request bitmap capture on next frame
                onCaptureRequested()
            } else {
                // Build (or update) your existing shader from the captured pixels
                paint.shader = outlineShader(layerBitmap, sizeInt)
                paint.alpha = 0.75f

                // Draw a rect filled by the shader
                drawIntoCanvas { canvas ->
                    canvas.drawRect(
                        0f, 0f, size.width, size.height,
                        paint
                    )
                }
            }
        }
        drawContent()
    }
}
