package com.jervisffb.ui.game.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.ContentScale
import com.jervisffb.ui.game.icons.IconFactory
import com.jervisffb.ui.game.model.UiPlayer
import org.jetbrains.skia.ImageFilter
import org.jetbrains.skia.RuntimeEffect
import org.jetbrains.skia.RuntimeShaderBuilder
import kotlin.math.min

// Background highlight effect for players that are available for selecting
// **Developer's Commentary:**
// I tried to investigate if it was possible to use shaders for this to make it more customizable.
// It probably is, but the APIs are really annoying to work with. If we get back to this at some
// point.https://www.pushing-pixels.org/2022/04/09/shader-based-render-effects-in-compose-desktop-with-skia.html
// is probably the best starting point.
//
// Keeping this around while experimenting with the Shader approach.
// This can be re-enabled using `modifier.graphicsLayer(renderEffect = ...)
//
// Maybe it is worth creating all the player images with borders up front during the initialization phase.
// This would prevent the excessive amount of Shader creation in this file, but it is unclear if it is
// worth it. Especially if the client is resized. Also, it will probably degrade the "sharpness" of the
// border if it ends up being a part of a scaled image.
private val playerAvailableDropShadowEffect: RenderEffect = ImageFilter.makeDropShadow(
    dx = 0.0f, dy = 0.0f, sigmaX = 5.0f, sigmaY = 5.0f,
    color = JervisTheme.darkYellow.toSkiaColor()
).asComposeRenderEffect()

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Player(
    modifier: Modifier,
    player: UiPlayer,
    parentHandleClick: Boolean,
    contextMenuShowing: Boolean,
) {
    val playerImage = remember(player) { IconFactory.getImage(player) }
    val ballImage = IconFactory.getHeldBallOverlay()

    var playerModifier: Modifier = modifier.aspectRatio(1f)

    if (player.isSelectable && !parentHandleClick) {
        playerModifier = playerModifier.clickable {
            player.selectAction!!()
        }
    }
    if (player.onHover != null) {
        playerModifier =
            playerModifier.onPointerEvent(eventType = PointerEventType.Enter) {
                player.onHover!!.invoke()
            }
    }
    if (player.onHoverExit != null) {
        playerModifier =
            playerModifier.onPointerEvent(eventType = PointerEventType.Exit) {
                player.onHoverExit!!.invoke()
            }
    }

    Box(modifier = playerModifier) {
        //println("Player: ${player.model.location} -> down: ${player.isGoingDown}")
        PlayerImage(
            bitmap = playerImage,
            isSelectable = player.isSelectable,
            isActionWheelFocus = contextMenuShowing,
            isGoingDown = player.isGoingDown,
            alpha = if (player.hasActivated || player.isStunned) 0.5f else 1.0f,
        )
        if (player.carriesBall) {
            Image(
                bitmap = ballImage,
                contentDescription = null,
                alignment = Alignment.Center,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )
        }
        if (player.isProne) {
            Image(
                bitmap = IconFactory.getProneDecoration(),
                contentDescription = null,
                alignment = Alignment.Center,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )
        }
        if (player.isStunned) {
            Image(
                bitmap = IconFactory.getStunnedDecoration(),
                contentDescription = null,
                alignment = Alignment.Center,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )
        }
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
private val playerBorderShaderTemplate = """
        uniform shader image;
        uniform vec2 resolution; // [widthPx, heightPx] 
        uniform vec2 scaleFactor; // [xScale, yScale]
        const float blurRadius = 2.0; // Loop initializers must be constant 
        
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

val playerSelectedBorderShader = playerBorderShaderTemplate.replace(
    oldValue = "%tintColor%",
    newValue = "vec4(56.0/255.0, 162.0/255.0, 59.0/255.0, 1.0); // JervisTheme.rulebookGreenAccent"
)
val playerDownBorderShader = playerBorderShaderTemplate.replace(
    oldValue = "%tintColor%",
    newValue = "vec4(198.0/255.0, 0.0/255.0, 0.0/255.0, 1.0); // JervisTheme.rulebookRed"
)

val playerInFocus = playerBorderShaderTemplate.replace(
    oldValue = "%tintColor%",
    newValue = "vec4(255.0/255.0, 190.0/255.0, 38.0/255.0, 1.0); // JervisTheme.orange"
    //    newValue = "vec4(190.0/255.0, 38.0/255.0, 255.0/255.0, 1.0); // JervisTheme.purple"
    //    newValue = "vec4(0.0/255.0, 119.0/255.0, 198.0/255.0, 1.0); // JervisTheme.rulebookBlue"
    //    newValue = "vec4(0.0/255.0, 0.0/255.0, 0.0/255.0, 1.0); // JervisTheme.rulebookBlue"
    //    newValue = "vec4(56.0/255.0, 162.0/255.0, 59.0/255.0, 1.0); // JervisTheme.rulebookGreenAccent"
    //    newValue = "vec4(255.0/255.0, 255.0/255.0, 255.0/255.0, 1.0); // JervisTheme.rulebookBlue"
)

// Custom rendering of Player images on a field square.
// Players that are available will render with a glowing border around them.
// Some of the icons seem to go all the way to the edge, which means the glow doesn't render correctly
// Maybe we need to draw the image to a slightly larger canvas before applying the blur. This requires
// more experimentation.
@Composable
private fun PlayerImage(
    bitmap: ImageBitmap,
    isSelectable: Boolean,
    isActionWheelFocus: Boolean,
    isGoingDown: Boolean,
    alpha: Float
) {
    // println("PlayerImage ($bitmap): $isGoingDown")
    // Use Decal to avoid artifacts at the edges. It would be nice if we could render the "glow" outside
    // the canvas. It seems possible when using renderEffects on the graphicsLayer. But will need
    // more investigation.
    val imageShader = remember(bitmap) { ImageShader(bitmap, TileMode.Decal, TileMode.Decal) }
    val playerBorderShader = when {
        isActionWheelFocus -> playerInFocus
        isGoingDown -> playerDownBorderShader
        else -> playerSelectedBorderShader
    }
    // val runtimeEffect = remember(playerBorderShader.hashCode()) { RuntimeEffect.makeForShader(playerBorderShader) }
    val runtimeEffect = RuntimeEffect.makeForShader(playerBorderShader)
    BoxWithConstraints(
        modifier = Modifier
            .aspectRatio(1f)
            .fillMaxSize()
            .drawWithCache {
                if (!isSelectable && !isGoingDown && !isActionWheelFocus) {
                    return@drawWithCache onDrawBehind { /* Do nothing */ }
                }
                val canvasWidth = size.width
                val canvasHeight = size.height
                val shaderWidth = bitmap.width.toFloat()
                val shaderHeight = bitmap.height.toFloat()

                // Calculate scale factors
                val scaleX = canvasWidth / shaderWidth
                val scaleY = canvasHeight / shaderHeight
                val scale = min(scaleX, scaleY) // Maintain aspect ratio

                // Feed data to the Shader
                val shader = RuntimeShaderBuilder(runtimeEffect).apply {
                    child("image", imageShader)
                    uniform("resolution", size.width, size.height)
                    uniform("scaleFactor", scaleX, scaleY)
                }.makeShader()
                val shaderBrush = ShaderBrush(shader)

                // Draw the "glow" behind the real player image
                onDrawBehind {
                    withTransform({
                        scale(scale, scale, pivot = Offset.Zero)
                    }) {
                        drawRect(brush = shaderBrush, topLeft = Offset.Zero, size = size, alpha = alpha)
                    }
                }
            }

    ) {
        // Draw the original sharp image on top
        Image(
            bitmap = bitmap,
            contentDescription = null,
            // Try to keep as much of the "pixel" feel, while still
            // allowing dynamic scaling. We probably need to play around with
            // this setting.
            filterQuality = FilterQuality.Low,
            modifier = Modifier.aspectRatio(1f).fillMaxSize().alpha(alpha)
        )
    }
}
