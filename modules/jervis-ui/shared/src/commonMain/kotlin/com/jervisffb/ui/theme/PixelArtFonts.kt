package com.jervisffb.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.jervisffb.shared.generated.resources.GeistPixel
import com.jervisffb.shared.generated.resources.PixelCode
import com.jervisffb.shared.generated.resources.PixelOperator
import com.jervisffb.shared.generated.resources.PixelOperatorSC_Bold
import com.jervisffb.shared.generated.resources.PixelOperator_Bold
import com.jervisffb.shared.generated.resources.Pixellari
import com.jervisffb.shared.generated.resources.PixeloidSans
import com.jervisffb.shared.generated.resources.PixeloidSans_Bold
import com.jervisffb.shared.generated.resources.Res
import com.jervisffb.shared.generated.resources.Tiny5
import org.jetbrains.compose.resources.Font
import org.jetbrains.skia.Font

@Composable
fun GeistPixel() = FontFamily(
    Font(Res.font.GeistPixel, weight = FontWeight.Normal)
)


@Composable
fun PixelCode() = FontFamily(
    Font(Res.font.PixelCode, weight = FontWeight.Normal)
)

@Composable
fun Pixellari() = FontFamily(
    Font(Res.font.Pixellari, weight = FontWeight.Normal)
)

@Composable
fun PixelOperator() = FontFamily(
    Font(Res.font.PixelOperator, weight = FontWeight.Normal),
    Font(Res.font.PixelOperator_Bold, weight = FontWeight.Bold),
    Font(Res.font.PixelOperatorSC_Bold, weight = FontWeight.ExtraBold)
)

@Composable
fun Pixeloid() = FontFamily(
    Font(Res.font.PixeloidSans, weight = FontWeight.Normal),
    Font(Res.font.PixeloidSans_Bold, weight = FontWeight.Bold)
)

@Composable
fun Tiny5() = FontFamily(
    Font(Res.font.Tiny5, weight = FontWeight.Normal)
)
