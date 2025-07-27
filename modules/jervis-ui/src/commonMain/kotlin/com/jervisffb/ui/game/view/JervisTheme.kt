package com.jervisffb.ui.game.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.jervisffb.ui.theme.TrumpTownPro


object JervisTheme {
    @Composable
    fun fontFamily() = TrumpTownPro()

    val white = Color(0xFFFFFFFF)
    val awayTeamColor = Color(0xFF4588c4)
    val homeTeamColor = Color(0xFFcc0102) // Color(0xFFca0000)
    val accentTeamColorDark =  Color(0xFF236A29) // Color(0xFF006600)
    val accentTeamColor = Color(0xFF38a23b) // Color(0xFF006600)
    val contentBackgroundColor = Color(0xFFF4F4F4)
    val accentContentBackgroundColor = Color(0xFFFFFFFF)
    val buttonColor: Color = homeTeamColor
    val buttonTextColor: Color = Color.White
    val contentTextColor: Color = Color.Black
    val darkGray = Color(0xFF1f1f1f)
    val lightGray = Color(0xFF616161)
    val fieldSquareTextStyle = TextStyle(
        color = Color.Cyan.copy(alpha = 0.75f),
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        textAlign = TextAlign.Center,
        shadow = Shadow(
            color = Color.Black.copy(alpha = 0.75f),
            offset = Offset(2f, 2f),
            blurRadius = 2f
        )
    )


    // Blue shades
    val darkBlue = Color(0xFF0B5598)
    val lightBlue = Color(0xFF2770B2)

    val darkYellow = Color(0xFFDCB465)
    val lightYellow = Color(0xFFDAC59A)

    val darkGreen =  Color(0xFF236A29) // Color(0xFF006600)
    val lightGreen = Color(0xFF38a23b) // Color(0xFF006600)

    // Colors lifted from BB 2024 website images. Keep them here for reference
    //    val rulebookBlue = Color(0xFF163f7f)
    //    val rulebookRed = Color(0xFF9c0704)
    //    val rulebookOrange = Color(0xFFfbca1a)

    val black = Color(0xFF000000)
    val rulebookBlue = Color(0xFF0077C6) // Color(0xFF2a4479)
    val rulebookRed = Color(0xFFC60000) // Color(0xFF991612)
    val rulebookRedLight = Color(0xFFC60000) // Color(0xFF991612)
    val rulebookOrange = Color(0xFFFFBE26) //Color(0xFFeca316)
    val rulebookOrangeContrast = Color(0xFF765912)
    val rulebookGreen = Color(0xFF388235)
    val rulebookPaperDark = Color(0xFF867048)
    val rulebookPaperMediumDark = Color(0xFFe2d2be)
    val rulebookPaper = Color(0xFFf5e3ce)
    val rulebookDisabled = Color.Gray

    val redDiceBottom = Color(0xFFA10000)
    val redDiceTop = Color(0xFFFF4C43)

    val blackDiceBottom = Color(0xFF222222)
    val blackDiceTop = Color(0xFF555555)

    val whiteDiceBottom = Color(0xFFCCCCCC)
    val whiteDiceTop = Color(0xFFF9F9F9)

    val diceBackground = blackDiceTop
    val diceBackgroundTop = blackDiceBottom

//    val diceBackground = Color(0xFF222222)
//    val diceBackgroundTop = Color(0xFF555555)


//    val fumbblBlue = Color(0xFF0093d8)
//    val fumbblRed = Color(0xFFe10525)
//    val fumbblViolet = Color(0xFF593F62)
//    val fumbblVioletLight = Color(0xFF7B6D8D)
//    val fumbblOrange = Color(0xFFFB8B24) // Color(0xFFF58A07)
//    val fumbblBackground = Color(0xFFE8DDB5) // Color(0xFFE6E8E6)
}

fun Color.toSkiaColor(): Int {
    val red = (red * 255).toInt()
    val green = (green * 255).toInt()
    val blue = (blue * 255).toInt()
    return org.jetbrains.skia.Color.makeRGB(r = red, g = green, b = blue)
}



