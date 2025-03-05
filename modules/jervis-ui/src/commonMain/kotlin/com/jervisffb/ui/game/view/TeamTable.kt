package com.jervisffb.ui.game.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.TeamId
import com.jervisffb.engine.serialize.SpriteSource
import com.jervisffb.ui.formatCurrency
import com.jervisffb.ui.game.icons.IconFactory
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sin

private val col1Width = 40.dp // Nr.
private val col2Width = 40.dp // Icon
private val col3Width = 200.dp // Name/Position
private val col4Width = 40.dp // Ma
private val col5Width = 40.dp // St
private val col6Width = 40.dp // Ag
private val col7Width = 40.dp // Pa
private val col8Width = 40.dp // Av
private val col9Width = 234.dp // Skills & Traits
private val col10Width = 100.dp // Injury
private val col11Width = 60.dp // Spp
private val col12Width = 60.dp // Cost
private val totalWidth = col1Width + col2Width + col3Width + col4Width + col5Width + col6Width + col7Width + col8Width + col9Width + col10Width + col11Width + col12Width

@Composable
fun TeamTable(width: Dp, team: Team, isOnHomeTeam: Boolean) {
    Column(modifier = Modifier.width(width).background(Color.Transparent)) {
        TeamTableWrapper(team.name)
        TeamTableHeader()
        team.forEachIndexed { index, player ->
            TeamTableRow(
                isOnHomeTeam = isOnHomeTeam,
                rowNo = index,
                player.number.value,
                player,
                listOf(player.name, player.position.titleSingular),
                player.move,
                player.strength,
                player.agility,
                player.passing ,
                player.armorValue,
                listOf(player.positionSkills.map { it.name }, player.extraSkills.map { it.name }),
                Pair(player.missNextGame, player.nigglingInjuries),
                player.starPlayerPoints,
                player.cost
            )
        }
        TeamTableDivider()
        TeamInfoSection(
            team.id,
            team.coach.name,
            team.roster.name,
            team.rerolls.size,
            team.dedicatedFans,
            team.assistantCoaches,
            team.cheerLeaders,
            team.apothecaries,
            team.treasury,
            team.teamValue,
            team.currentTeamValue,
            team.teamLogo ?: team.roster.rosterLogo
        )
        TeamTableWrapper()
    }
}

@Composable
private fun TeamTableDivider() {
    Box(modifier = Modifier
        .padding(vertical = 2.dp)
        .height(3.dp)
        .fillMaxSize()
        .background(color = JervisTheme.rulebookRed)
    ) {

    }
}

@Composable
private fun TeamInfoSection(
    team: TeamId,
    coachName: String,
    roster: String,
    rerolls: Int,
    dedicatedFanFactor: Int,
    assistantCoaches: Int,
    cheerleaders: Int,
    apothecary: Int,
    treasury: Int,
    teamValue: Int,
    currentTeamValue: Int,
    icon: SpriteSource?,
) {
    val scope = rememberCoroutineScope()

    Row(modifier = Modifier.background(JervisTheme.rulebookPaperMediumDark)) {
        Column(
            modifier = Modifier.weight(1f).padding(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            TeamDataRow("Coach", coachName, "Re-Rerolls", rerolls.toString())
            TeamDataRow("Roster", roster, "Dedicated Fans", dedicatedFanFactor.toString())
            TeamDataRow("Treasury", formatCurrency(treasury), "Assistant Coaches", assistantCoaches.toString())
            TeamDataRow("Team Value", formatCurrency(teamValue), "Cheerleaders", cheerleaders.toString())
            TeamDataRow("Current Team Value", formatCurrency(currentTeamValue), "Apothecary", if (apothecary > 0) "Yes" else "No")
        }

        var teamIcon: ImageBitmap? by remember { mutableStateOf(null) }
        LaunchedEffect(icon) {
            scope.launch {
                teamIcon = IconFactory.loadRosterIcon(team, icon)
            }
        }
        Box(modifier = Modifier.align(Alignment.CenterVertically).background(JervisTheme.rulebookPaperMediumDark).padding(end = 16.dp)) {
            if (teamIcon != null) {
                Image(
                    bitmap = teamIcon!!,
                    contentDescription = "",
                    contentScale = ContentScale.FillHeight,
                )
            }
        }
    }
}

@Composable
private fun TeamDataRow(col1Header: String, col1Value: String, col2Header: String, col2Value: String) {
    Row {
        Row(modifier = Modifier.width((950-462-48).dp)) {
            Text(
                modifier = Modifier.weight(1f),
                text = if (col1Header.isNotBlank()) "$col1Header:" else "",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                modifier = Modifier.weight(1f),
                fontSize = 14.sp,
                text = if (col1Header.isNotBlank()) col1Value else "",
            )
        }
        Row(modifier = Modifier.width((462-48).dp)) {
            Text(
                modifier = Modifier.weight(1f),
                text = if (col2Header.isNotBlank()) "$col2Header:" else "",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                modifier = Modifier.weight(1f).padding(start = 48.dp),
                fontSize = 14.sp,
                text = if (col2Header.isNotBlank()) col2Value else "",
            )
        }
    }
}

@Composable
private fun TeamTableRow(
    isOnHomeTeam: Boolean,
    rowNo: Int,
    no: Int,
    player: Player,
    nameAndPosition: List<String>,
    ma: Int,
    st: Int,
    ag: Int,
    pa: Int?,
    av: Int,
    skills: List<List<String>>,
    injury: Pair<Boolean, Int>,
    spp: Int,
    cost: Int
) {
    Row(
        modifier = Modifier
            .background(color = if (rowNo % 2 == 0) Color.Transparent else JervisTheme.rulebookPaperMediumDark)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TeamTableCellText(no.toString(), col1Width)
        TeamTableCellIcon(player, isOnHomeTeam, col2Width)
        Spacer(modifier = Modifier.width(8.dp))
        TeamTablePlayerNameText(nameAndPosition.first(), nameAndPosition.last(), col3Width)
        TeamTableCellText(ma.toString(), col4Width)
        TeamTableCellText(st.toString(), col5Width)
        TeamTableCellText("$ag+", col6Width)
        TeamTableCellText(if (pa != null) "$pa+" else "-", col7Width)
        TeamTableCellText("$av+", col8Width)
        Spacer(modifier = Modifier.width(8.dp))
        TeamTableCellSkillText(skills.first(), skills.last(), col9Width)

        val injury = buildString {
            if (injury.first) append("M")
            if (injury.second > 0) {
                val niglings = when (injury.second) {
                    1 -> append("N")
                    else -> append("${injury.second}N")
                }
                if (injury.first) {
                    append(", $niglings")
                } else {
                    append(niglings)
                }
            }
        }
        TeamTableCellText(injury, col10Width)
        TeamTableCellText(spp.toString(), col11Width)
        TeamTableCellText(formatCurrency(cost), col12Width)
    }
}

/**
 * Draw the blue wrapper for a Team Table.
 *
 * If a title is provided, the wrapper is assumed to be the top wrapper,
 * if no title is given, it is assumed it is the bottom wrapper.
 */
@Composable
private fun TeamTableWrapper(title: String = "") {
    val textMeasurer = rememberTextMeasurer()
    val up = title.isNotBlank()
    Box(
        modifier = Modifier
            .rotate(if (up) 0f else 180f)
            .padding(bottom = 2.dp)
            .height(50.dp)
            .fillMaxSize()
            .background(color = Color.Transparent)
            .drawBehind {
                // Height of the border near the edges (this is the minimum border height)
                val borderHeight = 5.dp.toPx()
                val starCutoutWidth = 120.dp.toPx()
                val bigStarRadius = 20.dp.toPx()
                val smallStarRadius = 15.dp.toPx()

                // Create title and measure it, so we can correctly draw the surrounding elements
                // around it. If no title is provided, a default width is used that is guaranteed
                // to hold the 3 stars at the bottom.
                val title = title.uppercase()
                val textStyle = TextStyle(
                    color = JervisTheme.rulebookRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                val textLayoutResult = textMeasurer.measure(
                    text = title,
                    style = textStyle,
                )
                val currentTitleWidth = if (up) textLayoutResult.size.width.toFloat() else starCutoutWidth

                val curveFunc = { x: Float ->
                    // We create a baseline gaussian distribution that is going to be
                    // clipped at a certain point for baseline string (and size).
                    // To get consistent curvature regardless of the length of the text,
                    // we calculate how much we need to shift the curve based on the
                    // difference to the baseline. This means we need to shift the curve
                    // either left or right depending on if x is before or after the midpoint.
                    val baselineWidth = 156.dp.toPx()
                    val modifier = if (x <= size.width.toInt() / 2) -1 else 1
                    val xModified = x + modifier * (baselineWidth - currentTitleWidth)/2

                    // Bell curve. See https://en.wikipedia.org/wiki/Gaussian_function
                    val a = 19.dp.toPx() // Height
                    val b = size.width / 2 // Center
                    val c = 125.dp.toPx() // Width / Std dev
                    a * exp(-(xModified - b).pow(2) / (2 * c.pow(2)))
                }

                drawIntoCanvas { canvas ->
                    // Create an isolated layer so that we can erase previous pixels, without
                    // affecting the box background.
                    canvas.saveLayer(Rect(Offset.Zero, size), Paint())
                    // Calculate and draw the curved path for the blue border
                    val path = Path().apply {
                        moveTo(0f, size.height)
                        lineTo(0f, size.height - borderHeight)
                        for (x in 0 until size.width.toInt()) {
                            lineTo(x.toFloat(), max(0f, size.height - borderHeight - curveFunc(x.toFloat())))
                        }
                        lineTo(size.width, size.height - borderHeight)
                        lineTo(size.width, size.height)
                        close()
                    }
                    canvas.drawPath(path, Paint().apply { color = JervisTheme.rulebookBlue })

                    // Remove center of the curve, so there is room for the title text.
                    val titlePadding = 20.dp.toPx()
                    val starWidth = currentTitleWidth + titlePadding
                    val cutoutPath = Path().apply {
                        moveTo(size.width/2 - starWidth/2, 0f)
                        lineTo(size.width/2 - starWidth/2, size.height - borderHeight)
                        lineTo(size.width/2 + starWidth/2, size.height - borderHeight)
                        lineTo(size.width/2 + starWidth/2, 0f)
                        close()
                    }
                    canvas.drawPath(
                        path = cutoutPath,
                        paint = Paint().apply {
                            blendMode = BlendMode.Clear
                        }
                    )

                    canvas.restore()
                }

                if (up) {
                    // Draw Team Name if top wrapper
                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = Offset.Unspecified.copy(
                            x = size.width / 2f - textLayoutResult.size.width / 2f,
                            y = size.height - borderHeight - textLayoutResult.size.height - 1.dp.toPx() /* Small adjustment to make it look nicer */,
                        ),
                    )
                } else {
                    // Draw 3 stars if bottom wrapper. Spread them out in a pattern that looks
                    // similar to the rulebook.
                    drawStar(
                        center = Offset(size.width * 0.5f, size.height * 0.4f),
                        radius = bigStarRadius,
                        color = JervisTheme.rulebookBlue,
                    )
                    drawStar(
                        center = Offset(size.width * 0.5f - 42.dp.toPx(), size.height * 0.55f),
                        radius = smallStarRadius,
                        color = JervisTheme.rulebookBlue,
                    )
                    drawStar(
                        center = Offset(size.width * 0.5f + 42.dp.toPx(), size.height * 0.55f),
                        radius = smallStarRadius,
                        color = JervisTheme.rulebookBlue,
                    )
                }
            },
        contentAlignment = Alignment.Center,
    ) {
    }
}

@Composable
private fun TeamTableHeader() {
    Row(modifier = Modifier.background(color = JervisTheme.rulebookRed).padding(4.dp)) {
        TeamTableHeaderTitle("Nr.", col1Width)
        TeamTableHeaderTitle("", col2Width)
        Spacer(modifier = Modifier.width(8.dp))
        TeamTableHeaderTitle("Name", col3Width, center = false)
        TeamTableHeaderTitle("Ma", col4Width)
        TeamTableHeaderTitle("St", col5Width)
        TeamTableHeaderTitle("Ag", col6Width)
        TeamTableHeaderTitle("Pa", col7Width)
        TeamTableHeaderTitle("Av", col8Width)
        Spacer(modifier = Modifier.width(8.dp))
        TeamTableHeaderTitle("Skills & Traits", col9Width, center = false)
        TeamTableHeaderTitle("Injury", col10Width)
        TeamTableHeaderTitle("Spp", col11Width)
        TeamTableHeaderTitle("Cost", col12Width)
    }
}

@Composable
private fun TeamTableHeaderTitle(text: String, width: Dp, center: Boolean = true) {
    Text(
        modifier = Modifier.width(width),
        text = text.uppercase(),
        fontSize = 14.sp,
        textAlign = if (center) TextAlign.Center else null,
        fontWeight = FontWeight.Medium,
        color = JervisTheme.white
    )
}

@Composable
private fun TeamTableCellText(text: String, width: Dp, center: Boolean = true) {
    Text(
        modifier = Modifier.width(width),
        text = text,
        fontSize = 14.sp,
        lineHeight = 1.em,
        textAlign = if (center) TextAlign.Center else null,
    )
}

@Composable
private fun TeamTableCellIcon(player: Player, isOnHomeTeam: Boolean, width: Dp, center: Boolean = true) {
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(player) {
        scope.launch {
            player.icon?.sprite?.let {
                imageBitmap = IconFactory.loadPlayerSprite(player, isOnHomeTeam)?.default
            }
        }
    }
    Box(
        modifier = Modifier.width(width).padding(2.dp),
    ) {
        if (imageBitmap != null) {
            Image(
                // TODO Need to check this on a non-retina screen
                modifier = Modifier.aspectRatio(1f).fillMaxSize().graphicsLayer(scaleX = 2f, scaleY = 2f),
                bitmap = imageBitmap!!,
                contentDescription = player.name,
                contentScale = ContentScale.None,
            )
        }
    }
}

@Composable
private fun TeamTablePlayerNameText(name: String, position: String, width: Dp) {
    Column(modifier = Modifier.width(width)) {
        Text(
            modifier = Modifier.width(width),
            text = name,
            fontSize = 14.sp,
            lineHeight = 1.em,
        )
        Text(
            modifier = Modifier.width(width),
            text = position,
            lineHeight = 1.em,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun TeamTableCellSkillText(startingSkills: List<String>, gainedSkills: List<String>, width: Dp) {
    Column(modifier = Modifier.width(width)) {
        Text(
            modifier = Modifier.width(width),
            text = startingSkills.joinToString(", "),
            fontSize = 10.sp,
            lineHeight = 1.em,
        )
        if (gainedSkills.isNotEmpty()) {
            Text(
                modifier = Modifier.width(width),
                text = gainedSkills.joinToString(", "),
                fontSize = 14.sp,
                lineHeight = 1.em,
            )
        }
    }
}


private fun DrawScope.drawStar(center: Offset, radius: Float, color: Color) {
    val path = Path()
    val points = calculateStarPoints(
        center = center,
        outerRadius = radius,
        innerRadius = radius * 0.4f,
        numPoints = 5
    )
    path.moveTo(points[0].x, points[0].y)
    for (i in 1 until points.size) {
        path.lineTo(points[i].x, points[i].y)
    }
    path.close()
    drawPath(path = path, color = color)
}

private fun calculateStarPoints(
    center: Offset,
    outerRadius: Float,
    innerRadius: Float,
    numPoints: Int
): List<Offset> {
    val points = mutableListOf<Offset>()
    val angleStep = 2.0 * PI / numPoints
    val rotationOffset = -PI / 2 // Rotate by -90 degrees to point up

    for (i in 0 until numPoints * 2) {
        val angle = angleStep * i / 2.0 + rotationOffset
        val radius = if (i % 2 == 0) outerRadius else innerRadius
        val x = center.x + radius * cos(angle).toFloat()
        val y = center.y - radius * sin(angle).toFloat()
        points.add(Offset(x, y))
    }
    return points
}
