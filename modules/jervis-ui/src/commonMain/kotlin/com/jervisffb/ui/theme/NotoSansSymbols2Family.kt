package com.jervisffb.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.jervisffb.jervis_ui.generated.resources.NotoSansSymbols2_Regular
import com.jervisffb.jervis_ui.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.Font

/**
 * Fallback font family for the Jervis UI. We use Noto Symbols 2 in order to support
 * Apple ⌘ symbol.
 */
@OptIn(ExperimentalResourceApi::class)
@Composable
fun NotoSansSymbols2() = FontFamily(
    Font(Res.font.NotoSansSymbols2_Regular, weight = FontWeight.Normal),
)
