package com.jervisffb.ui.theme

//import com.jervisffb.jervis_ui.generated.resources.Res
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.jervisffb.jervis_ui.generated.resources.Res
import com.jervisffb.jervis_ui.generated.resources.trump_town_pro
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.Font

@OptIn(ExperimentalResourceApi::class)
@Composable
fun TrumpTownPro() = FontFamily(
    Font(Res.font.trump_town_pro, weight = FontWeight.Normal)
//    Font(Res.font.)
//    Font(Res.font.Tr, weight = FontWeight.Normal)
//    Font(Res.font, weight = FontWeight.Light),
//    Font(Res.font.teko_regular, weight = FontWeight.Normal),
//    Font(Res.font.teko_medium, weight = FontWeight.Medium),
//    Font(Res.font.teko_semibold, weight = FontWeight.SemiBold),
//    Font(Res.font.teko_bold, weight = FontWeight.Bold)
)
