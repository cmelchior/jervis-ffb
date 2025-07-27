package com.jervisffb.ui.game.view

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jervisffb.ui.menu.GameScreenModel
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import org.jetbrains.skia.Point
import kotlin.time.Duration.Companion.seconds

@Composable
fun LoadingScreen(
    viewModel: GameScreenModel,
    content: @Composable () -> Unit,
) {
    // TODO Should this be a non-zero value? If the load screen is really quick it
    //  looks like the icons are not there. Might be nice for real games, but annoying
    //  while developing.
    val minimumLoadingTime = 0.seconds
    val isReadyToStart by viewModel.isReadyToStartGame.collectAsState()
    var showGameScreen by remember { mutableStateOf(false) }

    if (isReadyToStart) {
        val density = LocalDensity.current
        LaunchedEffect(Unit) {
            viewModel.initialize(density)
        }
    }
    LaunchedEffect(Unit) {
        val startTime = Clock.System.now()
        viewModel.isLoaded.collect { isLoaded ->
            if (isLoaded) {
                val elapsed = Clock.System.now() - startTime
                delay(minimumLoadingTime - elapsed)
                showGameScreen = true
            }
        }
    }
    if (!showGameScreen) {
        LoadingScreen(viewModel)
    } else {
        content()
    }
}

private enum class PathMode {
    DIVIDER,
    LEFT_BOX,
    RIGHT_BOX
}

private data class PathPoint(
    val left: Point,
    val middle: Point,
    val right: Point,
)

private class JaggedPathShape(
    val mode: PathMode,
    val data: List<PathPoint>,
) : Shape {
    lateinit var path: Path
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            when (mode) {
                PathMode.DIVIDER -> {
                    moveTo(data[0].left.x, data[0].left.y)
                    data.forEachIndexed { index, point ->
                        if (index > 0) {
                            lineTo(point.left.x, point.left.y)
                        }
                    }
                    lineTo(data.last().right.x, data.last().right.y)
                    data.reversed().forEachIndexed { index, point ->
                        if (index > 0) {
                            lineTo(point.right.x, point.right.y)
                        }
                    }
                    close()
                }

                PathMode.LEFT_BOX -> {
                    moveTo(0f, 0f)
                    lineTo(data.first().middle.x, data.first().middle.y)
                    data.forEachIndexed { index, point ->
                        if (index > 0) {
                            lineTo(point.middle.x, point.middle.y)
                        }
                    }
                    lineTo(0f, size.height)
                    close()
                }

                PathMode.RIGHT_BOX -> {
                    moveTo(size.width, 0f)
                    lineTo(data.first().middle.x, data.first().middle.y)
                    data.forEachIndexed { index, point ->
                        if (index > 0) {
                            lineTo(point.middle.x, point.middle.y)
                        }
                    }
                    lineTo(size.width, size.height)
                    close()
                }
            }
        }
        this.path = path
        return Outline.Generic(path)
    }
}

private fun createPathData(
    boxSize: Size,
    dividerWidth: Float,
): List<PathPoint> = buildList {
    val xTop = boxSize.width * 55 / 100 // Top of screen
    val xBottom = boxSize.width * 45 / 100 // Bottom of screen
    add(PathPoint(Point(xTop - dividerWidth / 2f, 0f), Point(xTop, 0f), Point(xTop + dividerWidth / 2f, 0f)))
    add(PathPoint(Point(xBottom - dividerWidth / 2f, boxSize.height), Point(xBottom, boxSize.height), Point(xBottom + dividerWidth / 2f, boxSize.height)))
}

@Composable
private fun LoadingScreen(viewModel: GameScreenModel) {
    val loadingMessage: String by viewModel.loadingMessages.collectAsState()
    val homeTeam = viewModel.homeTeamData
    val awayTeam = viewModel.awayTeamData
    val homeTeamIcon by viewModel.homeTeamIcon.collectAsState()
    val awayTeamIcon by viewModel.awayTeamIcon.collectAsState()

    BoxWithConstraints(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val maxWidthPx = maxWidth.value * LocalDensity.current.density
        val maxHeightPx = maxHeight.value * LocalDensity.current.density
        val shapeData = remember(maxWidthPx, maxHeightPx) {
            createPathData(
                boxSize = Size(maxWidthPx, maxHeightPx),
                dividerWidth = 75f,
            )
        }

        // Split into 3 shapes, so we are ready for a more advanced divider line
        val leftShape = remember(shapeData) { JaggedPathShape(PathMode.LEFT_BOX, shapeData) }
        val rightShape = remember(shapeData) { JaggedPathShape(PathMode.RIGHT_BOX, shapeData) }
        val pathShape = remember(shapeData) { JaggedPathShape(PathMode.DIVIDER, shapeData) }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = JervisTheme.rulebookRed,
                    shape = leftShape
                )
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = JervisTheme.rulebookBlue,
                    shape = rightShape
                )
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = JervisTheme.rulebookOrange,
                    shape = pathShape,
                )
        )
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Outlines are not supported by Compose yet, so fake it by overlaying two texts.
            Text(
                text = "VS",
                style = MaterialTheme.typography.h1.copy(
                    color = Color.Black,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 120.sp,
                    shadow = Shadow(
                        color = Color.Black,
                        offset = Offset(2f, 2f),
                        blurRadius = 8f
                    ),
                    drawStyle = Stroke(
                        miter = 10f,
                        width = 10f,
                        join = StrokeJoin.Round
                    )
                ),
            )
            Text(
                text = "VS",
                style = MaterialTheme.typography.h1.copy(
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 120.sp,
                ),
            )
            Row {
                TeamPlayingData(
                    icon = homeTeamIcon,
                    teamName = homeTeam.teamName,
                    coachName = homeTeam.coachName,
                    race = homeTeam.race,
                    teamValue = homeTeam.teamValue,
                )
                TeamPlayingData(
                    icon = awayTeamIcon,
                    teamName = awayTeam.teamName,
                    coachName = awayTeam.coachName,
                    race = awayTeam.race,
                    teamValue = awayTeam.teamValue,
                )
            }
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomEnd,
        )
        {
            val density = LocalDensity.current
            var targetLoadingWidth by remember { mutableStateOf(0.dp) }
            var dotCount by remember { mutableStateOf(1) } // Track the number of dots (1 to 3)
            LaunchedEffect(Unit) {
                while (true) {
                    dotCount = ((dotCount + 1) % 4)
                    delay(500L)
                }
            }
            val loadingText = loadingMessage + ".".repeat(dotCount)

            // Invisible text to measure layout
            // Since we are not using monospace fonts, use some extra ...
            // as "padding" (find a better way to do this)
            var measuredPx by remember { mutableStateOf(0) }
            Text(
                text = loadingMessage + "........",
                maxLines = 1,
                style = MaterialTheme.typography.h3.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                ),
                onTextLayout = { layoutResult ->
                    measuredPx = layoutResult.size.width
                },
                modifier = Modifier.alpha(0f).padding(bottom = 8.dp, end = 16.dp) // keep invisible but laid out
            )
            LaunchedEffect(measuredPx) {
                targetLoadingWidth = with(density) { measuredPx.toDp() }
            }

            // Render real loading text (and make sure it is offset enough from the
            // edge to not "jump around")
            Text(
                modifier = Modifier.width(targetLoadingWidth).padding(bottom = 8.dp, end = 16.dp),
                text = loadingText,
                maxLines = 1,
                style = MaterialTheme.typography.h3.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                ),
            )
        }
    }
}

@Composable
private fun RowScope.TeamPlayingData(
    icon: ImageBitmap?,
    teamName: String,
    coachName: String,
    race: String,
    teamValue: String
) {
    val alpha by animateFloatAsState(
        targetValue = if (icon != null) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "fade"
    )

    Column(
        modifier = Modifier.weight(1f).fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier.size(300.dp).padding(bottom = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (icon != null) {
                Image(
                    modifier = Modifier.fillMaxSize().alpha(alpha),
                    bitmap = icon,
                    contentScale = ContentScale.FillHeight,
                    contentDescription = null,
                )
            }
        }
        Text(
            text = teamName.uppercase(),
            maxLines = 1,
            style = MaterialTheme.typography.body1.copy(
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = JervisTheme.rulebookOrange,

            )
        )
        Text(
            modifier = Modifier.padding(start = 4.dp, top = 10.dp, end = 4.dp, bottom = 12.dp),
            text = coachName.uppercase(),
            maxLines = 1,
            style = MaterialTheme.typography.body1.copy(
                fontSize = 22.sp,
                color = Color.White,
                fontStyle = FontStyle.Italic,
            ),
        )
        Text(
            modifier = Modifier.background(Color.Black).padding(4.dp),
            maxLines = 1,
            text = "$race - TV $teamValue",
            style = MaterialTheme.typography.body1.copy(
                fontSize = 24.sp,
                color = Color.White,
            ),
        )
    }
}
