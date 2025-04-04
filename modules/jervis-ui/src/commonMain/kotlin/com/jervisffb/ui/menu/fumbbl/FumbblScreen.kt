package com.jervisffb.ui.menu.fumbbl

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import com.jervisffb.engine.model.TeamId
import com.jervisffb.engine.serialize.SingleSprite
import com.jervisffb.jervis_ui.generated.resources.Res
import com.jervisffb.jervis_ui.generated.resources.frontpage_mummy
import com.jervisffb.ui.game.icons.IconFactory
import com.jervisffb.ui.game.icons.LogoSize
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.view.utils.JervisButton
import com.jervisffb.ui.game.view.utils.TitleBorder
import com.jervisffb.ui.game.view.utils.paperBackgroundWithLine
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.JervisScreen
import com.jervisffb.ui.menu.MenuScreenWithSidebarAndTitle
import com.jervisffb.ui.menu.TopbarButton
import com.jervisffb.ui.menu.components.SmallHeader
import com.jervisffb.ui.menu.p2p.host.BoxHeader
import com.jervisffb.utils.openUrlInBrowser

class FumbblScreen(private val menuViewModel: MenuViewModel, private val viewModel: FumbblScreenModel) : Screen {
    @Composable
    override fun Content() {
        JervisScreen(menuViewModel) {
            FumbblePage(menuViewModel, viewModel, Modifier)
        }
    }
}

@Composable
fun FumbblePage(menuViewModel: MenuViewModel, viewModel: FumbblScreenModel, modifier: Modifier) {
    FumbblLoginDialog(viewModel)
    MenuScreenWithSidebarAndTitle(
        menuViewModel,
        title = "FUMBBL (TODO)",
        icon = Res.drawable.frontpage_mummy,
        topMenuLeftContent = {

        },
        topMenuRightContent = {
            val isLoggedIn: Boolean by viewModel.loggedInState().collectAsState()
            val label = when (isLoggedIn) {
                true -> viewModel.coachName().value
                false -> "Login".uppercase()
            }
            TopbarButton(label, onClick = { viewModel.authMenubarActionInitiated() })
        },
        sidebarContent = {
            Column(
                modifier = Modifier
                    .paperBackgroundWithLine(JervisTheme.rulebookBlue)
                    .padding(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 8.dp)
                ,
            ) {
                Spacer(modifier = Modifier.fillMaxHeight(0.2f))
                Spacer(modifier = Modifier.height(16.dp))
                SidebarBoxHeader("Links", color = JervisTheme.rulebookOrange)
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    val isLoggedIn: Boolean by viewModel.loggedInState().collectAsState()
                    MenuSidebarButton("Gamefinder", onClick = { openUrlInBrowser(viewModel.URL_GAMEFINDER) })
                    if (isLoggedIn) {
                        MenuSidebarButton("Coach page", onClick = { openUrlInBrowser(viewModel.getCoachUrl()) })
                    }
                    MenuSidebarButton("Help", onClick = { openUrlInBrowser(viewModel.URL_HELP)})
                    MenuSidebarButton("News", onClick = { openUrlInBrowser(viewModel.URL_NEWS)})
                }
                Spacer(modifier = Modifier.fillMaxHeight(0.20f))
            }
        },
    ) {
        FumbblePageContent(viewModel)
    }
}

@Composable
fun SidebarBoxHeader(text: String, color: Color = JervisTheme.rulebookOrange) {
    TitleBorder(color)
    Box(
        modifier = Modifier.height(36.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            modifier = Modifier.padding(bottom = 2.dp),
            text = text.uppercase(),
//            fontFamily = JervisTheme.fontFamily(),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = color
        )
    }
    TitleBorder(color)
}

@Composable
fun MenuSidebarButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(JervisTheme.white.copy(alpha = 0.1f)).clickable { onClick() }
            .padding(8.dp)
    ) {
        Text(
            modifier = Modifier.padding(bottom = 2.dp),
            text = text.uppercase(),
//            fontFamily = JervisTheme.fontFamily(),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = JervisTheme.white
        )
    }
}


@Composable
fun FumbblePageContent(viewModel: FumbblScreenModel) {
    Column(
        modifier = Modifier.fillMaxHeight().padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.width(875.dp).weight(1f)) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                ScheduledGame(viewModel)
                Spacer(modifier = Modifier.height(16.dp))
                CurrentGames(viewModel)
                Spacer(modifier = Modifier.height(16.dp))
                RecentMatches(viewModel)
                Spacer(modifier = Modifier.height(16.dp))
                Replays(viewModel)
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            JervisButton(text = "Load Replay", onClick = {  })
        }
    }
}

@Composable
private fun ScheduledGame(viewModel: FumbblScreenModel) {
    Column(modifier = Modifier
        .height(160.dp)
        .background(JervisTheme.rulebookGreen)
        .padding(8.dp)
    ) {
        ScheduledGameHeader(
            "Scheduled Game",
            "Competitive",
            "12. January 2025 20.30 CET",
            color = JervisTheme.rulebookOrange)
        Spacer(Modifier.height(4.dp))
        GameStatusRow(JervisTheme.white, Color.Transparent)
    }
}

@Composable
private fun ColumnScope.ScheduledGameHeader(
    mainTitle: String,
    subTitleTop: String,
    subTitleBottom: String,
    color: Color = JervisTheme.rulebookOrange
) {
    TitleBorder(color)
    Row(
        modifier = Modifier.height(36.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.padding(bottom = 2.dp).weight(1f),
            text = mainTitle.uppercase(),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = color
        )
        Column(
            horizontalAlignment = Alignment.End,
        ) {
            Text(
                modifier = Modifier.align(Alignment.End),
                text = subTitleTop,
                lineHeight = 1.0.em,
                fontSize = 12.sp,
                color = color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                modifier = Modifier.align(Alignment.End),
                text = subTitleBottom,
                lineHeight = 1.0.em,
                fontSize = 12.sp,
                color = color
            )
        }
    }
    TitleBorder(color)
}

@Composable
fun ColumnScope.GameStatusRow(textColor: Color = JervisTheme.contentTextColor, backgroundColor: Color = JervisTheme.rulebookPaperMediumDark) {
    val imageSize = 75.dp
    val fontSize = 14.sp
    val spaceBetweenTeamAndCoach = 4.dp

    var gnomeLogo: ImageBitmap? by remember { mutableStateOf(null) }
    var lizardLogo: ImageBitmap? by remember { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        gnomeLogo = IconFactory.loadRosterIcon(TeamId("gnome"), SingleSprite.embedded("roster/logo/roster_logo_jervis_skaven_small.png"), LogoSize.SMALL)
        lizardLogo = IconFactory.loadRosterIcon(TeamId("gnome"), SingleSprite.embedded("roster/logo/roster_logo_jervis_lizardmen_small.png"), LogoSize.SMALL)
    }
    Row(
        modifier = Modifier.background(backgroundColor).padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                Text(
                    text = "Brackwater Brawlers 1234567890!",
                    textAlign = TextAlign.End,
                    color = textColor,
                    fontSize = fontSize,
                    lineHeight = 1.0.em,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
                Text(
                    text = "Lizardmen",
                    textAlign = TextAlign.End,
                    lineHeight = 1.0.em,
                    fontSize = 12.sp,
                    color = textColor,
                )
                Spacer(modifier = Modifier.height(spaceBetweenTeamAndCoach))
                Text(
                    text = "(Rookie) Ilios",
                    fontSize = fontSize,
                    textAlign = TextAlign.End,
                    color = textColor,
                )
                Text(
                    text = "CTV 1.000K",
                    fontSize = fontSize,
                    lineHeight = 1.2.em,
                    textAlign = TextAlign.End,
                    color = textColor,
                )
            }
            Box(modifier = Modifier.padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
                if (gnomeLogo != null) {
                    Image(
                        modifier = Modifier.width(imageSize).height(imageSize).aspectRatio(1f),
                        bitmap = gnomeLogo!!,
                        contentDescription = "",
                        contentScale = ContentScale.FillWidth,
                    )
                }
            }
        }
        Box(modifier = Modifier.width(110.dp), contentAlignment = Alignment.Center) {
            Text(text = "vs", color = textColor, fontWeight = FontWeight.Bold)
        }
        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
                if (lizardLogo != null) {
                    Image(
                        modifier = Modifier.width(imageSize).height(imageSize).aspectRatio(1f),
                        bitmap = lizardLogo!!,
                        contentDescription = "",
                        contentScale = ContentScale.FillWidth,
                    )
                }
            }
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                Text(
                    text = "Saltwater Saints",
                    textAlign = TextAlign.End,
                    lineHeight = 1.0.em,
                    color = textColor,
                    fontSize = fontSize,
                    maxLines = 1
                )
                Text(
                    text = "Human",
                    lineHeight = 1.0.em,
                    fontSize = 12.sp,
                    color = textColor,
                )
                Spacer(modifier = Modifier.height(spaceBetweenTeamAndCoach))
                Text(
                    fontSize = fontSize,
                    text = "PurpleChest (Superstar)",
                    color = textColor,
                )
                Text(
                    fontSize = fontSize,
                    lineHeight = 1.2.em,
                    text = "CTV 1.000K",
                    color = textColor,
                )
            }
        }
    }
}


@Composable
private fun ColumnScope.CurrentGames(viewModel: FumbblScreenModel) {
    BoxHeader("Current Games", color = JervisTheme.rulebookRed)
    Spacer(modifier = Modifier.height(8.dp))

    // Figure out how the view model exposes this

    // Header
    SmallHeader("Competitive")
    GameStatusRow(textColor = JervisTheme.contentTextColor, backgroundColor = Color.Transparent)
    GameStatusRow(JervisTheme.contentTextColor, JervisTheme.rulebookPaperMediumDark)

    Spacer(modifier = Modifier.height(8.dp))
    SmallHeader("Blackbox")
}

@Composable
private fun ColumnScope.RecentMatches(viewModel: FumbblScreenModel) {

}

@Composable
private fun ColumnScope.Replays(viewModel: FumbblScreenModel) {

}
