package com.jervisffb.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.jervisffb.jervis_ui.generated.resources.NotoSansSymbols_Bold
import com.jervisffb.jervis_ui.generated.resources.NotoSansSymbols_Medium
import com.jervisffb.jervis_ui.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.Font

/**
 * Fallback font family for the Jervis UI. We use Noto Symbols in order to support
 * direction arrows as they are used by the Action Wheel
 */
@OptIn(ExperimentalResourceApi::class)
@Composable
fun NotoSansSymbols() = FontFamily(
    Font(Res.font.NotoSansSymbols_Medium, weight = FontWeight.Normal),
    Font(Res.font.NotoSansSymbols_Bold, weight = FontWeight.Bold)
)
