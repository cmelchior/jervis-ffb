package com.jervisffb.ui.game.view.utils

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.skiaCanvas
import androidx.compose.ui.graphics.skiaShader
import androidx.compose.ui.platform.LocalDensity
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.utils.toImageBitmap
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Rect
import org.jetbrains.skia.RuntimeEffect
import org.jetbrains.skia.RuntimeShaderBuilder

/**
 * Convert an image into a pixelated image, so it looks more like an 8-bit graphic
 *
 * TODO This doesn't scale automatically when using jdp values. Probably some
 *  caching going wrong.
 */
@Composable
fun PixelatedImage(
    modifier: Modifier = Modifier,
    painter: Painter,
    pixelSize: Float = 3f,
    outlineColor: Color = JervisTheme.black,
    outlineRadius: Float = 1f,
    shadowColor: Color = Color.Transparent,
    shadowOffset: Offset = Offset.Zero,
    shadowBlur: Float = 0f,
) {
    val shaderCode = """
        uniform shader img;
        
        // Pixel-art cell size in local coordinates
        uniform float pixelSize;
        
        // Outline thickness in cells
        uniform float outlineRadius;
        
        layout(color) uniform float4 outlineColor;
        layout(color) uniform float4 shadowColor;
        
        // Should ideally already be aligned to pixelSize on the host side
        uniform float2 shadowOffset;
        
        // Threshold for turning soft alpha into hard pixel edges
        uniform float alphaCutoff;
        
        // Vibrant posterization controls
        uniform float hueSteps;         // e.g. 12
        uniform float satSteps;         // e.g. 3
        uniform float valueSteps;       // e.g. 4
        uniform float saturationBoost;  // e.g. 1.25
        uniform float valueBoost;       // e.g. 1.05
        
        const int MAX_OUTLINE_RADIUS = 2;
        
        float2 pixelCenter(float2 coord) {
            return (floor(coord / pixelSize) + 0.5) * pixelSize;
        }
        
        float alphaMaskAt(float2 coord) {
            return step(alphaCutoff, img.eval(coord).a);
        }
        
        float4 premul(float3 rgb, float a) {
            return float4(rgb * a, a);
        }
        
        float4 coloredLayer(float4 color, float mask) {
            float a = color.a * mask;
            return float4(color.rgb * a, a);
        }
        
        float4 over(float4 fg, float4 bg) {
            return fg + bg * (1.0 - fg.a);
        }
        
        float quantizeHue(float hue, float steps) {
            return floor(hue * steps + 0.5) / steps;
        }

//        float quantize01(float v, float steps) {
//            if (steps <= 1.0) return 0.0;
//            return floor(v * (steps - 1.0) + 0.5) / (steps - 1.0);
//        }
        
        float3 rgb2hsv(float3 c) {
            float4 K = float4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
            float4 p = mix(float4(c.bg, K.wz), float4(c.gb, K.xy), step(c.b, c.g));
            float4 q = mix(float4(p.xyw, c.r), float4(c.r, p.yzx), step(p.x, c.r));
        
            float d = q.x - min(q.w, q.y);
            float e = 1e-10;
        
            return float3(
                abs(q.z + (q.w - q.y) / (6.0 * d + e)),
                d / (q.x + e),
                q.x
            );
        }
        
        float3 hsv2rgb(float3 c) {
            float4 K = float4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
            float3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
            return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
        }
        
        float3 posterizeVibrant(float4 src) {
            float3 rgb = unpremul(src).rgb;
            float3 hsv = rgb2hsv(rgb);
        
            hsv.y = clamp(hsv.y * saturationBoost, 0.0, 1.0);
            hsv.z = clamp(hsv.z * valueBoost, 0.0, 1.0);
        
            hsv.x = quantizeHue(hsv.x, hueSteps);
            hsv.y = quantizeHue(hsv.y, satSteps);
            hsv.z = quantizeHue(hsv.z, valueSteps);
        
            return hsv2rgb(hsv);
        }
        
        float outlineMask(float2 center, float baseMask) {
            float mask = 0.0;
        
            for (int x = -MAX_OUTLINE_RADIUS; x <= MAX_OUTLINE_RADIUS; ++x) {
                for (int y = -MAX_OUTLINE_RADIUS; y <= MAX_OUTLINE_RADIUS; ++y) {
                    if (x == 0 && y == 0) continue;
        
                    float2 offsetCells = float2(float(x), float(y));
        
                    // Square/chunky outline
                    if (max(abs(offsetCells.x), abs(offsetCells.y)) <= outlineRadius) {
                        mask = max(mask, alphaMaskAt(center + offsetCells * pixelSize));
                    }
                }
            }
        
            return mask * (1.0 - baseMask);
        }
        
        half4 main(float2 fragCoord) {
            float2 center = pixelCenter(fragCoord);
        
            float4 sampled = img.eval(center);
            float baseMask = step(alphaCutoff, sampled.a);
        
            float4 image;
            if (baseMask < 0.5) {
                image = float4(0.0);
            } else {
                float3 rgb = posterizeVibrant(sampled);
                image = premul(rgb, 1.0);
            }
        
            float shadowMask = alphaMaskAt(center - shadowOffset);
            float outline = outlineMask(center, baseMask);
        
            float4 result = coloredLayer(shadowColor, shadowMask);
            result = over(coloredLayer(outlineColor, outline), result);
        
            return half4(over(image, result));
        }
    """.trimIndent()
    val shaderCodeWithoutVibration = """
        uniform shader img;

        // Size of one pixel-art cell in local coordinates.
        uniform float pixelSize;

        // Thickness measured in pixel-art cells.
        uniform float outlineRadius;

        layout(color) uniform float4 outlineColor;
        layout(color) uniform float4 shadowColor;

        // Local-coordinate offset. It is snapped to the pixel grid below.
        uniform float2 shadowOffset;

        // Typically around 0.1–0.5 depending on the source image.
        uniform float alphaCutoff;

        // Keep this as small as possible.
        // A one-cell outline only needs MAX_OUTLINE_RADIUS = 1.
        const int MAX_OUTLINE_RADIUS = 2;

        float2 pixelCenter(float2 coord) {
            return (floor(coord / pixelSize) + 0.5) * pixelSize;
        }

        float alphaMaskAt(float2 coord) {
            return step(alphaCutoff, img.eval(coord).a);
        }

        float outlineMask(float2 center, float baseMask) {
            float mask = 0.0;

            for (int x = -MAX_OUTLINE_RADIUS;
                 x <= MAX_OUTLINE_RADIUS;
                 ++x) {
                for (int y = -MAX_OUTLINE_RADIUS;
                     y <= MAX_OUTLINE_RADIUS;
                     ++y) {

                    if (x == 0 && y == 0) {
                        continue;
                    }

                    float2 offsetInCells =
                        float2(float(x), float(y));

                    // Chebyshev distance gives a chunky pixel-art outline.
                    if (max(abs(offsetInCells.x), abs(offsetInCells.y))
                            <= outlineRadius) {
                        mask = max(
                            mask,
                            alphaMaskAt(
                                center + offsetInCells * pixelSize
                            )
                        );
                    }
                }
            }

            return mask * (1.0 - baseMask);
        }

        float4 coloredLayer(float4 color, float mask) {
            float alpha = color.a * mask;
            return float4(color.rgb * alpha, alpha);
        }

        float4 over(float4 foreground, float4 background) {
            return foreground
                + background * (1.0 - foreground.a);
        }

        float4 main(float2 fragCoord) {
            float2 center = pixelCenter(fragCoord);

            float4 image = img.eval(center);
            float baseMask = step(alphaCutoff, image.a);

            /*
             * Optional: force source pixels to either fully opaque or transparent.
             * Remove this block if the input contains intentional translucency.
             *
             * img.eval() returns premultiplied RGB, so divide by the old alpha
             * before changing alpha to 1.
             */
            if (baseMask == 0.0) {
                image = float4(0.0);
            } else {
                image = float4(
                    image.rgb / max(image.a, 0.0001),
                    1.0
                );
            }

            // `round()` not available on Multiplatform
            // float2 snappedShadowOffset = round(shadowOffset / pixelSize) * pixelSize;
            float2 snappedShadowOffset = floor(shadowOffset / pixelSize + float2(0.5)) * pixelSize;
            float shadowMask = alphaMaskAt(center - snappedShadowOffset);
            float outline = outlineMask(center, baseMask);
            float4 result = coloredLayer(shadowColor, shadowMask);

            result = over(
                coloredLayer(outlineColor, outline),
                result
            );

            return over(image, result);
        }
    """.trimIndent()
//    val shaderCode = """
//            uniform shader img;
//            uniform float pixelSize;
//            uniform float4 outlineColor;
//            uniform float outlineThickness;
//            uniform float4 shadowColor;
//            uniform float2 shadowOffset;
//            uniform float shadowBlur;
//
//            const float maxOutlineRadius = 8.0;
//            const float maxShadowBlur = 8.0;
//
//            vec4 pixelatedSample(float2 coord) {
//                // float2 pixelCoord = floor(coord * pixelSize) / pixelSize;
//                float2 pixelCoord = floor(coord / pixelSize) * pixelSize;
//                return img.eval(pixelCoord);
//            }
//
//            float alphaAt(float2 coord) {
//                return pixelatedSample(coord).a;
//            }
//
//            vec4 coloredLayer(float4 color, float mask) {
//                float alpha = color.a * mask;
//                return vec4(color.rgb * alpha, alpha);
//            }
//
//            vec4 over(vec4 foreground, vec4 background) {
//                return foreground + background * (1.0 - foreground.a);
//            }
//
//            float outlineMask(float2 fragCoord, float baseAlpha) {
//                float mask = 0.0;
//                for (float x = -maxOutlineRadius; x <= maxOutlineRadius; x += 1.0) {
//                    for (float y = -maxOutlineRadius; y <= maxOutlineRadius; y += 1.0) {
//                        float2 offset = vec2(x, y) * pixelSize;
//                        if (length(offset) <= outlineThickness) {
//                            mask = max(mask, alphaAt(fragCoord + offset));
//                        }
//                    }
//                }
//                return mask * (1.0 - baseAlpha);
//            }
//
//            float shadowMask(float2 fragCoord) {
//                float mask = 0.0;
//                float weightTotal = 0.0;
//                for (float x = -maxShadowBlur; x <= maxShadowBlur; x += 1.0) {
//                    for (float y = -maxShadowBlur; y <= maxShadowBlur; y += 1.0) {
//                        float2 offset = vec2(x, y);
//                        float distance = length(offset);
//                        if (distance <= shadowBlur) {
//                            float weight = exp(-0.5 * distance * distance /
//                                max(shadowBlur * shadowBlur, 0.001));
//                            mask += alphaAt(fragCoord - shadowOffset + offset) * weight;
//                            weightTotal += weight;
//                        }
//                    }
//                }
//                return mask / max(weightTotal, 1.0);
//            }
//
//            half4 main(float2 fragCoord) {
//                vec4 image = pixelatedSample(fragCoord);
//                vec4 result = coloredLayer(shadowColor, shadowMask(fragCoord));
//                result = over(coloredLayer(outlineColor, outlineMask(fragCoord, image.a)), result);
//                return over(image, result);
//            }
//    """.trimIndent()

    val effect = remember { RuntimeEffect.makeForShader(shaderCode) }
    BoxWithConstraints(
        modifier = modifier
    ) {
        val width = maxWidth
        val height = maxHeight
        val density = LocalDensity.current
        val widthPx = with(density) { width.toPx() }
        val heightPx = with(density) { height.toPx() }

        // Render SVG to bitmap at requested size
        val imageBitmap = remember(painter, widthPx, heightPx) {
            painter.toImageBitmap(Size(widthPx, heightPx), density)
        }

        // Build the shader to pixelate the image
        val shader = remember(
            effect,
            imageBitmap,
            widthPx,
            heightPx,
            pixelSize,
            outlineColor,
            shadowColor,
            shadowOffset,
            shadowBlur,
        ) {
            RuntimeShaderBuilder(effect).apply {
                // Control color palette (reduce number of colors and make them more vibrant)
                val steps = 25f
                uniform("hueSteps", steps)
                uniform("satSteps", steps)
                uniform("valueSteps", steps)
                uniform("saturationBoost", 1.2f)
                uniform("valueBoost", 1.15f)

//                uniform("hueSteps", 12f)
//                uniform("satSteps", 3f)
//                uniform("valueSteps", 4f)
//                uniform("saturationBoost", 1.25f)
//                uniform("valueBoost", 1.05f)

                // One pixel-art cell thick
                uniform("outlineRadius", outlineRadius)
                // Removes most antialiased edge pixels
                uniform("alphaCutoff", 0.25f)

                uniform("pixelSize", pixelSize)
                uniform(
                    "outlineColor",
                    outlineColor.red,
                    outlineColor.green,
                    outlineColor.blue,
                    outlineColor.alpha,
                )
                uniform(
                    "shadowColor",
                    shadowColor.red,
                    shadowColor.green,
                    shadowColor.blue,
                    shadowColor.alpha,
                )
                uniform("shadowOffset", shadowOffset.x, shadowOffset.y)
                // uniform("shadowBlur", shadowBlur)
                child("img", ImageShader(imageBitmap, TileMode.Decal, TileMode.Decal).skiaShader)
            }.makeShader()
        }

        Canvas(modifier = Modifier.size(width, height)) {
            drawIntoCanvas { canvas ->
                val paint = Paint().apply {
                    this.shader = shader
                }
                canvas.skiaCanvas.drawRect(Rect.makeXYWH(0f, 0f, widthPx, heightPx), paint)
            }
        }
    }
}
