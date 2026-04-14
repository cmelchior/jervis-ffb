package com.jervisffb.ui.game.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.onClick
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.jervisffb.engine.model.modifiers.TeamFeature
import com.jervisffb.jervis_ui.generated.resources.Res
import com.jervisffb.jervis_ui.generated.resources.jervis_brush_chalk
import com.jervisffb.jervis_ui.generated.resources.jervis_icon_brilliant_coaching_reroll
import com.jervisffb.jervis_ui.generated.resources.jervis_icon_leader_reroll
import com.jervisffb.jervis_ui.generated.resources.jervis_icon_mascot_reroll
import com.jervisffb.jervis_ui.generated.resources.jervis_icon_team_reroll
import com.jervisffb.ui.game.UiGameStatusUpdate
import com.jervisffb.ui.game.UiReroll
import com.jervisffb.ui.game.UiRerollType
import com.jervisffb.ui.game.UiTeamFeature
import com.jervisffb.ui.game.UiTeamFeatureType
import com.jervisffb.ui.game.UiTeamInfoUpdate
import com.jervisffb.ui.game.icons.IconFactory
import com.jervisffb.ui.game.icons.LogoSize
import com.jervisffb.ui.game.view.utils.paperBackground
import com.jervisffb.ui.game.viewmodel.GameStatusViewModel
import com.jervisffb.ui.menu.GameScreenModel
import com.jervisffb.ui.menu.components.JervisTooltipArea
import com.jervisffb.ui.menu.components.JervisTooltipPlacement
import com.jervisffb.ui.toRadians
import com.jervisffb.ui.utils.PixelBorderBox
import com.jervisffb.ui.utils.applyIf
import com.jervisffb.ui.utils.darken
import com.jervisffb.ui.utils.jdp
import com.jervisffb.ui.utils.jsp
import com.jervisffb.ui.utils.onClickWithSmallDragControl
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.compose.resources.painterResource
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.tan

// Game Status Layout that is compatible with a Game Screen layout for a Blood Bowl 3 inspired layout
@Composable
fun GameStatusTopBar(
    vm: GameStatusViewModel,
    modifier: Modifier,
) {
    val progressFlow = remember { vm.progress() }
    val progress by progressFlow.collectAsState(UiGameStatusUpdate.INITIAL)
    val homeTeamFlow = remember { vm.homeTeamInfoFlow() }
    val homeTeamInfo by homeTeamFlow.collectAsState(UiTeamInfoUpdate.INITIAL)
    val awayTeamFlow = remember { vm.awayTeamInfoFlow() }
    val awayTeamInfo by awayTeamFlow.collectAsState(UiTeamInfoUpdate.INITIAL)

    val angle = 5f
    val topPadding = 8.jdp
    val statusBoxWidth = 170.jdp
    Box(
        modifier = modifier
    ) {
        Row {
            TeamBadge(homeTeamInfo, progress.currentTeam == homeTeamInfo.id, JervisTheme.rulebookRed, leftSide = true)
            Spacer(modifier = Modifier.weight(1f))
            TeamBadge(awayTeamInfo, progress.currentTeam == awayTeamInfo.id, JervisTheme.rulebookBlue, leftSide = false)
        }
        Column(
            Modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.weight(1f))
                TurnTracker(
                    modifier = Modifier.padding(top = topPadding),
                    angle = -angle, progress.turnMax,
                    homeTeamInfo.turn,
                    JervisTheme.rulebookRed,
                    vm.controller.state.activeTeam?.isHomeTeam() == true
                )
                ScoreCounter(
                    Modifier.padding(top = topPadding),
                    vm.screenModel,
                    progress,
                    homeTeamInfo,
                    awayTeamInfo,
                    angle,
                    statusBoxWidth
                )
                TurnTracker(
                    modifier = Modifier.padding(top = topPadding),
                    angle = angle, progress.turnMax,
                    awayTeamInfo.turn,
                    JervisTheme.rulebookBlue,
                    vm.controller.state.activeTeam?.isAwayTeam() == true
                )
                Spacer(modifier = Modifier.weight(1f))
            }
            if (progress.badgeSubButtons.isNotEmpty()) {
                Row(
                    modifier = Modifier.offset(y = 16.jdp),
                    horizontalArrangement = Arrangement.spacedBy(8.jdp)
                ) {
                    progress.badgeSubButtons.forEach { button ->
                        StatusBarButton(
                            text = button.title,
                            onClick = button.onClick,
                        )
                    }
                }
            }
        }

        // Reroll icons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 44.jdp)
                .height(44.dp)
            ,
            horizontalArrangement = Arrangement.Center
        ) {
            val distance = 6.jdp
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.End
            ) {
                TeamRerolls(homeTeamInfo.rerolls, distance)
            }
            Spacer(modifier = Modifier.width(statusBoxWidth + 94.jdp))
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.Start
            ) {
                TeamRerolls(awayTeamInfo.rerolls.reversed(), distance)
            }
        }
        GameStatusMessage(vm)
    }
}

@Composable
fun GameStatusMessage(viewModel: GameStatusViewModel) {
    val alpha = remember { Animatable(0f) }
    val flow = remember(viewModel) { viewModel.messageFlow() }
    val message by flow.collectAsState(null)
    LaunchedEffect(message) {
        val visible = message.isNullOrBlank().not()
        alpha.animateTo(
            targetValue = if (visible) 1f else 0f,
            tween(300)
        )
    }
    // Message box with a "brush" background
    // Attempt to align it so it looks on the line with the team features row
    Box(
        modifier = Modifier.fillMaxWidth().height(145.jdp).alpha(alpha.value),
        contentAlignment = Alignment.BottomCenter
    ) {
        val chalkTexture = imageResource(Res.drawable.jervis_brush_chalk)
        val imageBrush = remember(chalkTexture) {
            ShaderBrush(
                shader = ImageShader(
                    image = chalkTexture,
                    tileModeX = TileMode.Repeated,
                    tileModeY = TileMode.Repeated,
                ),
            )
        }
        Box(
            modifier = Modifier.height(32.dp).drawWithContent {
                // Create fade brush for left and right edges
                val fadeBrush = Brush.horizontalGradient(
                    0f to Color.Transparent,
                    0.2f to Color.Black,
                    0.8f to Color.Black,
                    1f to Color.Transparent,
                    startX = 0f,
                    endX = size.width
                )
                // Draw the background with the fade mask
                with(drawContext.canvas) {
                    saveLayer(
                        bounds = androidx.compose.ui.geometry.Rect(0f, 0f, size.width, size.height),
                        paint = androidx.compose.ui.graphics.Paint()
                    )
                    drawRect(
                        brush = imageBrush,
                        size = size,
                        alpha = 0.6f,
                        colorFilter = ColorFilter.tint(JervisTheme.black)
                    )
                    drawRect(
                        brush = fadeBrush,
                        size = size,
                        blendMode = BlendMode.DstIn
                    )
                    restore()
                }
                drawContent()
            },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message ?: "",
                lineHeight = 1.em,
                maxLines = 1,
                color = Color.White,
                fontSize = 20.jsp,
                modifier = Modifier.padding(horizontal = 100.jdp),
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TeamRerolls(rerolls: List<UiReroll>, distance: Dp) {
    val availableAlpha = 0.9f // Reduce how much the gfx "pop"
    val unavailableAlpha = 0.3f
    rerolls.forEachIndexed { i, reroll ->
        if (i > 0) {
            Spacer(modifier = Modifier.width(distance))
        }
        val image = when (reroll.type) {
            UiRerollType.TEAM -> Res.drawable.jervis_icon_team_reroll
            UiRerollType.LEADER -> Res.drawable.jervis_icon_leader_reroll
            UiRerollType.BRILLIANT_COACHING -> Res.drawable.jervis_icon_brilliant_coaching_reroll
            UiRerollType.MASCOT -> Res.drawable.jervis_icon_mascot_reroll
            UiRerollType.UNKNOWN -> Res.drawable.jervis_icon_team_reroll
        }
        JervisTooltipArea(
            tooltip = {
                Surface(
                    tonalElevation = 4.dp,
                    shadowElevation = 4.dp,
                    shape = RoundedCornerShape(8.dp),
                    color = JervisTheme.white.copy(alpha = 0.95f),
                ) {
                    Text(reroll.name, Modifier.padding(horizontal = 8.dp, vertical = 6.dp))
                }
            },
            delayMillis = 300,
            tooltipPlacement = JervisTooltipPlacement.CursorPoint(
                offset = DpOffset((-16).dp, 16.dp)
            )
        ) {
            Image(
                modifier = Modifier
                    .width(30.jdp)
                    .height(40.jdp)
                    .alpha(if (!reroll.isAvailable()) unavailableAlpha else availableAlpha)
                ,
                painter = painterResource(image),
                contentDescription = reroll.name,
                contentScale = ContentScale.Fit,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TeamFeaturesRow(
    leftSide: Boolean,
    height: Dp,
    features: List<UiTeamFeature>
) {
    Row(
        modifier = Modifier
            .applyIf(leftSide) { padding(start = 4.jdp) }
            .applyIf(!leftSide) { padding(end = 4.jdp)}
        ,
        horizontalArrangement = Arrangement.spacedBy(16.jdp),
    ) {
        features.forEach { feature ->
            JervisTooltipArea(
                tooltip = {
                    Surface(
                        tonalElevation = 4.dp,
                        shadowElevation = 4.dp,
                        shape = RoundedCornerShape(8.dp),
                        color = JervisTheme.white.copy(alpha = 0.95f),
                    ) {
                        Text(feature.name, Modifier.padding(horizontal = 8.dp, vertical = 6.dp))
                    }
                },
                delayMillis = 300,
                tooltipPlacement = JervisTooltipPlacement.CursorPoint(
                    offset = if (!leftSide) DpOffset((-16).dp, 16.dp) else DpOffset((16).dp, 16.dp)
                )
            ) {
                when (feature.type) {
                    UiTeamFeatureType.APOTHECARY -> {
                        TeamFeature(value = feature.value, icon = IconFactory.getApothecaryIcon(height), available = !feature.used)
                    }
                    UiTeamFeatureType.BLOODWEISER_KEG -> TODO()
                    UiTeamFeatureType.UNKNOWN -> TODO()
                    UiTeamFeatureType.TEAM_CAPTAIN -> {
                        TeamFeature(
                            value = feature.value,
                            content = defaultTeamFeatureStatus("TC")
                        )
                    }
                    UiTeamFeatureType.CHEERING_FANS_OFFENSIVE_ASSIST -> {
                        TeamFeature(
                            value = feature.value,
                            content = defaultTeamFeatureStatus("CF")
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun defaultTeamFeatureStatus(title: String): @Composable () -> Unit = {
    Box(
        modifier = Modifier
            .padding(4.jdp)
            .aspectRatio(1f)
            .fillMaxHeight()
            .background(JervisTheme.rulebookPaperDark, shape = RoundedCornerShape(4.jdp))
            .border(2.dp, JervisTheme.white.copy(alpha = 0.5f), RoundedCornerShape(4.jdp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            lineHeight = 1.em,
            color = JervisTheme.white,
            fontSize = 16.jsp,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodySmall.copy(
                shadow = Shadow(
                    color = Color.Black,
                    offset = Offset(2f, 2f),
                )
            )
        )
    }
}

@Composable
private fun TurnTracker(
    modifier: Modifier,
    angle: Float = 10f,
    turnMax: Int = 8,
    currentTurn: Int = 0,
    teamColor: Color,
    activeTeam: Boolean
) {
    Row(modifier = modifier) {
        for (turnNo in 1..turnMax) {
            val (borderColor, contentColor, alpha) = when {
                currentTurn < turnNo -> Triple(Color.White.copy(0.7f),JervisTheme.white.copy(0.15f), 1f)
                currentTurn == turnNo && activeTeam -> Triple(Color.Transparent,teamColor, 1f)
                currentTurn >= turnNo -> Triple(Color.Transparent,JervisTheme.white.copy(alpha = 0.2f), 0.6f)
                else -> error("Unsupported state: ($currentTurn, $turnNo) ")
            }
            ParallelogramButton(
                modifier = Modifier.width(36.jdp).height(32.jdp).alpha(alpha),
                onClick = { },
                angleDegrees = angle,
                containerColor = contentColor,
                borderColor = borderColor,
                borderWidth = 1.5.jdp,
            ) {
                Text(
                    text = turnNo.toString(),
                    lineHeight = 1.em,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.jsp,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        shadow = Shadow(
                            color = Color.Black,
                            offset = Offset(2f, 2f),
                            blurRadius = 0f
                        )
                    )
                )
            }
        }
    }
}

@Composable
private fun ScoreCounter(
    modifier: Modifier,
    gameScreenModel: GameScreenModel,
    progress: UiGameStatusUpdate,
    homeTeamInfo: UiTeamInfoUpdate,
    awayTeamInfo: UiTeamInfoUpdate,
    angle: Float = 5f,
    statusBoxWidth: Dp
) {
    val scoreTextSize = 28.jsp // 36.jsp
    val bigPadding = 5.jdp
    val smallPadding = 2.jdp
    val counterWidth = 40.jdp // 60.jdp
    val counterHeight = 48.jdp // 76.jdp
    val counterStyle = MaterialTheme.typography.headlineMedium.copy(
        shadow = Shadow(
            color = Color.Black,
            offset = Offset(2f, 2f),
            blurRadius = 0f
        )
    )
    Row(modifier = modifier, verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.Center) {
        ParallelogramButton(
            onClick = { },
            angleDegrees = -angle,
            modifier = Modifier.padding(start = smallPadding).width(counterWidth).height(counterHeight),
        ) {
            Text(
                text = "${homeTeamInfo.score}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                lineHeight = 1.sp,
                fontSize = scoreTextSize,
                style = counterStyle
            )
        }
        Spacer(modifier = Modifier.width(smallPadding))
        GameStatusBox(
            gameScreenModel,
            statusBoxWidth,
            smallPadding,
            angle,
            progress.centerBadgeAction,
        )
        Spacer(modifier = Modifier.width(smallPadding))
        ParallelogramButton(
            onClick = { },
            angleDegrees = angle,
            modifier = Modifier.padding(end = smallPadding).width(counterWidth).height(counterHeight),
        ) {
            Text(
                text = "${awayTeamInfo.score}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                lineHeight = 1.sp,
                fontSize = scoreTextSize,
                style = counterStyle
            )
        }
    }
}

/**
 * Composable responsible for showing current phase, timer and act as End Turn/Setup button.
 */
@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
private fun GameStatusBox(
    gameScreenModel: GameScreenModel,
    statusBoxWidth: Dp,
    padding: Dp = 0.dp,
    angle: Float,
    action: ((GameScreenModel) -> Unit)? = null
) {
    val isEnabled by gameScreenModel.isGameStatusBoxEnabled
    val title by gameScreenModel.gameStatusBoxTitle
    val boxHeight = 72.jdp
    val shape = remember(angle) { TrapezoidShape(angle) }
    var color by remember { mutableStateOf(JervisTheme.gameStatusBackground) }
    var borderColor by remember { mutableStateOf(JervisTheme.white) }
    Box(
        modifier = Modifier
            .width(statusBoxWidth)
            .height(boxHeight) // 76
            .shadow(elevation = if (action == null) 0.dp else 8.jdp, shape = shape, clip = false)
            .paperBackground(shape = shape, color = color)
            .onPointerEvent(PointerEventType.Enter) {
                if (action != null && isEnabled) {
                    color = JervisTheme.gameStatusBackground.darken(0.1f)
                    borderColor = JervisTheme.white.darken(0.1f)
                }
            }
            .onPointerEvent(PointerEventType.Exit) {
                if (action != null && isEnabled) {
                    color = JervisTheme.gameStatusBackground
                    borderColor = JervisTheme.white
                }
            }
            .applyIf(isEnabled) {
                onClick(onClick = {
                    if (action != null) {
                        action(gameScreenModel)
                    }
                })
            }
            .border(4.jdp, borderColor, shape)

        ,
    ) {
        Column(
            modifier = Modifier.alpha(0.8f)
                .fillMaxSize()
                .padding(8.jdp)
            ,
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val timer = "--:--"
            Text(
                modifier = Modifier.offset(y = 3.jdp),
                text = title.uppercase(),
                textAlign = TextAlign.Center,
                color = if (isEnabled) JervisTheme.white else JervisTheme.white.copy(alpha = 0.5f),
                lineHeight = 1.em,
                letterSpacing = 2.jsp,
                // fontFamily = JervisTheme.fontFamily(),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.jsp,
                    fontWeight = FontWeight.Medium,
                    shadow = Shadow(
                        color = if (isEnabled) JervisTheme.black else JervisTheme.black.copy(alpha = 0.5f),
                        offset = Offset(2f, 2f),
                        blurRadius = 0f
                    )
                )
            )
            Text(
                text = timer.uppercase(),
                color = JervisTheme.white,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 28.jsp,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 1.em,
                    shadow = Shadow(
                        color = Color.Black,
                        offset = Offset(2f, 2f),
                        blurRadius = 0f
                    )
                )
            )
        }
    }
}

/**
 * Composable responsible for the team logo, name and coach name in upper left/right corner of the screen.
 */
@Composable
private fun TeamBadge(
    teamInfo: UiTeamInfoUpdate,
    isActive: Boolean,
    backgroundColor: Color,
    leftSide: Boolean
) {
    // Changes here should also modify the text
    val coachBarHeight = 28.jdp // 24.jdp
    val coachBarLength = 200.jdp
    val teamNameBarHeight = 32.jdp // 28.jdp
    val teamNameBarLength = 350.jdp
    val teamLogoSize = 98.jdp
    val inducementIconTopPadding = 4.jdp
    val inducementIconSize = 44.jdp // Same size as the player squares on the reference screen
    val logoTopPadding = 4.jdp

    val backgroundShape = ParallelogramShape(if (leftSide) -10f else 10f)
    val textPadding = 60.jdp
    Column(
        modifier = Modifier.height(teamLogoSize + inducementIconTopPadding + inducementIconSize + logoTopPadding),
        horizontalAlignment = if (leftSide) Alignment.Start else Alignment.End,
        verticalArrangement = Arrangement.Center,
    ) {

        // Team Logo / Name / Coach
        PixelBorderBox(
            // Make the team icons be slightly closer to the edge than dugout.
            // It looks nicer with the current gfx.
            modifier = Modifier.offset(x = if (leftSide) -12.jdp else 12.jdp),
            borderEnabled = isActive
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 4.jdp),
                contentAlignment = if (leftSide) Alignment.CenterStart else Alignment.CenterEnd
            ) {
                Column(
                    modifier = Modifier
                        .applyIf(leftSide) { padding(start = 44.jdp) }
                        .applyIf(!leftSide) { padding(end = 44.jdp) },
                    horizontalAlignment = if (leftSide) Alignment.Start else Alignment.End,
                ) {
                    Box(
                        modifier = Modifier
                            .clip(backgroundShape)
                            .width(coachBarLength)
                            .height(coachBarHeight)
                            .background(JervisTheme.black),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .applyIf(leftSide) { padding(start = textPadding) }
                                .applyIf(!leftSide) { padding(end = textPadding) },
                            textAlign = if (leftSide) TextAlign.Start else TextAlign.End,
                            text = teamInfo.coachName,
                            color = Color.White,
                            // fontStyle = FontStyle.Italic,
                            lineHeight = 1.em,
                            fontSize = 12.jsp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(backgroundShape)
                            .width(teamNameBarLength)
                            .height(teamNameBarHeight)
                            .background(backgroundColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .applyIf(leftSide) { padding(start = textPadding) }
                                .applyIf(!leftSide) { padding(end = textPadding) },
                            textAlign = if (leftSide) TextAlign.Start else TextAlign.End,
                            text = teamInfo.teamName,
                            color = Color.White,
                            lineHeight = 1.em,
                            overflow = TextOverflow.Ellipsis,
                            letterSpacing = 1.jsp,
                            maxLines = 1,
                            fontSize = 18.jsp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = JervisTheme.fontFamily(),
                        )
                    }
                }
                // We want to make the team icon appear a bit pixelated to fit into the rest of the UI,
                // but just using this doesn't scale well. We might need something that can switch between
                // using a normal image and this depending on the size (or switch pixelSize more smartly).
                //        PixelatedImageWithShader(
                //            modifier = Modifier.padding(8.jdp).size(90.jdp),
                //            painter = BitmapPainter(IconFactory.getLogo(team.id, LogoSize.SMALL)),
                //            pixelSize = 2f,
                //        )
                // Team Logo
                if (teamInfo.id.value.isNotEmpty()) {
                    Image(
                        modifier = Modifier.padding(top = logoTopPadding).size(teamLogoSize),
                        bitmap = IconFactory.getLogo(teamInfo.id, LogoSize.SMALL),
                        contentDescription = teamInfo.teamName + " logo",
                        contentScale = ContentScale.Fit,
                        filterQuality = FilterQuality.None,
                    )
                }
            }
        }

        // Inducement icons and other team features
        AnimatedVisibility(
            visible = teamInfo.featureList.isNotEmpty()
        ) {
            Spacer(modifier = Modifier.height(inducementIconTopPadding))
            Row(
                modifier = Modifier.height(inducementIconSize),
            ) {
                TeamFeaturesRow(leftSide = leftSide, height = inducementIconSize,
                    teamInfo.featureList.let {
                        if (leftSide) it else it.asReversed()
                    }
                )
            }
        }
    }
}

/**
 * A [Shape] that fits a parallelogram inside the given bounds.
 * The top edge is horizontally shifted by `tan(angleDegrees) * height`
 * relative to the bottom edge, and the shape is clamped to remain fully inside its bounds.
 */
class ParallelogramShape(
    private val angleDegrees: Float
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val h = size.height
        val w = size.width

        // Horizontal shift of the TOP edge relative to the bottom edge
        val s = (tan(toRadians(angleDegrees.toDouble())) * h).toFloat()

        // Clamp so the shape stays within [0, w]
        val clamped = if ((-w + 1f) > (w - 1f)) {
            s // Fallback. Should not happen unless layout is extremely unusual
        } else {
            s.coerceIn(-w + 1f, w - 1f)
        }
        val innerWidth = (w - abs(clamped)).coerceAtLeast(1f)

        val topLeftX = max(0f, clamped)
        val bottomLeftX = max(0f, -clamped)
        val topRightX = topLeftX + innerWidth
        val bottomRightX = bottomLeftX + innerWidth

        val path = Path().apply {
            moveTo(topLeftX, 0f)
            lineTo(topRightX, 0f)
            lineTo(bottomRightX, h)
            lineTo(bottomLeftX, h)
            close()
        }
        return Outline.Generic(path)
    }
}

/**
 * A trapezoid where the bottom edge is shorter than the top.
 * `angleDegrees` controls how much each side slopes inward.
 */
class TrapezoidShape(
    private val angleDegrees: Float
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val h = size.height
        val w = size.width

        // Horizontal shift per side for bottom edge
        val shift = (tan(toRadians(angleDegrees.toDouble())) * h).toFloat()

        // Clamp so bottom doesn't collapse
        val clamped = shift.coerceAtMost((w / 2) - 1f)

        val topLeftX = 0f
        val topRightX = w
        val bottomLeftX = clamped
        val bottomRightX = w - clamped

        val path = Path().apply {
            moveTo(topLeftX, 0f)
            lineTo(topRightX, 0f)
            lineTo(bottomRightX, h)
            lineTo(bottomLeftX, h)
            close()
        }
        return Outline.Generic(path)
    }
}


@Composable
fun ParallelogramButton(
    onClick: () -> Unit,
    angleDegrees: Float = 15f,
    modifier: Modifier = Modifier,
    containerColor: Color = JervisTheme.white.copy(alpha = 0.2f),
    contentColor: Color = JervisTheme.black,
    borderWidth: Dp = 2.jdp,
    borderColor: Color = JervisTheme.white.copy(alpha = 0.7f),
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val shape = remember(angleDegrees) { ParallelogramShape(angleDegrees) }
    Surface(
        modifier = modifier,
        shape = shape,
        color = if (enabled) containerColor else containerColor.copy(alpha = 0.6f),
        contentColor = contentColor,
        border = if (borderWidth > 0.dp) BorderStroke(borderWidth, borderColor) else null,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier,
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@Composable
private fun StatusBarButton(text: String, onClick: () -> Unit) {
    Button(
        modifier = Modifier
            .onClickWithSmallDragControl(onClick = onClick)
        ,
        colors = ButtonDefaults.buttonColors(containerColor = JervisTheme.rulebookDisabled, disabledContainerColor = JervisTheme.rulebookPaperMediumDark),
        onClick = { /* Ignore */ },
        border = BorderStroke(3.dp, JervisTheme.white),
        enabled = true,
        shape = RectangleShape,
    ) {
        Text(
            text = text.uppercase(),
            fontSize = 14.sp,
            lineHeight = 1.em,
            fontWeight = FontWeight.Medium,
            color = JervisTheme.white,
        )
    }
}
