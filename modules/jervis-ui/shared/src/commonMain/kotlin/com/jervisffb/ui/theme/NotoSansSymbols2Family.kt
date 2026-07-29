package com.jervisffb.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.jervisffb.shared.generated.resources.NotoSansSymbols2_Regular
import com.jervisffb.shared.generated.resources.Res
import org.jetbrains.compose.resources.Font
import org.jetbrains.skia.Data
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.Typeface

/**
 * Fallback font family for the Jervis UI. We use Noto Symbols 2 in order to support
 * Apple ⌘ symbol.
 */
@Composable
fun NotoSansSymbols2() = FontFamily(
    Font(Res.font.NotoSansSymbols2_Regular, weight = FontWeight.Normal),
)

// TODO Find a better way to cache this
var notoCachedFont: Typeface? = null
suspend fun loadNotoSansSymbols2SkiaFont(): Typeface {
    notoCachedFont?.let { return it }
    val bytes = Res.readBytes("font/NotoSansSymbols2-Regular.ttf")
    val data = Data.makeFromBytes(bytes)
    return FontMgr.default.makeFromData(data)?.also {
        notoCachedFont = it
    } ?: error("Could not parse font bytes")
}
