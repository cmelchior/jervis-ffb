package com.jervisffb.ui.view

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


    // Palette
    val p1aColor = Color(0xFF2274A5)
    val p1bColor = Color(0xFFE6AF2E)
    val p1cColor = Color(0xFF632B30)

    val newRed = Color(0XFFFF5964)
    val newBlue = Color(0XFF35A7FF)

    // https://paletton.com/#uid=33s0I0k++NGtrZOLq+V+WuL+5oc
    val fumbblBlue = Color(0xFF0092D8)
    val fumbblBlueDarker = Color(0xFF00527A)
    val fumbblBlueDark = Color(0xFF00699B)
    val fumbblAccent = Color(0xFFFFCD00)
    val fumbblRed = Color(0xFFFB001D)

    val rulebookBlue = Color(0xFF0077C6) // Color(0xFF2a4479)
    val rulebookRed = Color(0xFFC60000) // Color(0xFF991612)
    val rulebookOrange = Color(0xFFFFBE26) //Color(0xFFeca316)
    val rulebookGreen = Color(0xFF388235)
    val rulebookPaperDark = Color(0xFF867048)
    val rulebookPaperMediumDark = Color(0xFFe2d2be)
    val rulebookPaper = Color(0xFFf5e3ce)
    val rulebookDisabled = Color.Gray

//    val fumbblBlue = Color(0xFF0093d8)
//    val fumbblRed = Color(0xFFe10525)
//    val fumbblViolet = Color(0xFF593F62)
//    val fumbblVioletLight = Color(0xFF7B6D8D)
//    val fumbblOrange = Color(0xFFFB8B24) // Color(0xFFF58A07)
//    val fumbblBackground = Color(0xFFE8DDB5) // Color(0xFFE6E8E6)
}


