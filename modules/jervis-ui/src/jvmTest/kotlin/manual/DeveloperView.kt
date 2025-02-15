package manual

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.jervisffb.jervis_ui.generated.resources.Res
import com.jervisffb.jervis_ui.generated.resources.roster_logo_chaos_chosen
import com.jervisffb.ui.view.JervisTheme
import org.jetbrains.compose.resources.painterResource
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sin
import kotlin.test.Test


class DeveloperViewTests() {

    @Test
//    @Ignore // Only run this manually
    fun main() {
        mainTeamTable()
    }
}


fun mainTeamTable() =
    application {
        val windowState = rememberWindowState()
        Window(onCloseRequest = ::exitApplication, state = windowState) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                TeamTable()
            }
        }
    }


@Composable
fun TeamTable(modifier: Modifier = Modifier) {
    Column(modifier = modifier.wrapContentHeight().width(1000.dp)) {
        TeamTableWrapper("Saltwater Saints")
        TeamTableHeader()
        TeamTableRow(
            rowNo = 0,
            1,
            "",
            listOf("Kelgor Foobar", "Human Blitzer"),
            5,
            3,
            3,
            -1,
            10,
            listOf("Dodge", "Right Stuff", "Side Step"),
            "N",
            0,
            15_000
        )
        TeamTableRow(
            rowNo = 1,
            1,
            "",
            listOf("Kelgor Foobar", "Human Blitzer"),
            5,
            3,
            3,
            -1,
            10,
            listOf("Dodge", "Right Stuff", "Side Step"),
            "N",
            0,
            15_000
        )
        TeamTableRow(
            rowNo = 2,
            1,
            "",
            listOf("Kelgor Foobar", "Human Blitzer"),
            5,
            3,
            3,
            -1,
            10,
            listOf("Dodge", "Right Stuff", "Side Step"),
            "N",
            0,
            15_000
        )
        TeamTableRow(
            rowNo = 3,
            1,
            "",
            listOf("Kelgor Foobar", "Human Blitzer"),
            5,
            3,
            3,
            -1,
            10,
            listOf("Dodge", "Right Stuff", "Side Step"),
            "N",
            0,
            15_000
        )
        TeamTableDivider()
        TeamInfoSection(
            "Ilios",
            "Human",
            4,
            1,
            0,
            0,
            1,
            1_00,
            1_000
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
    coachName: String,
    roster: String,
    rerolls: Int,
    dedicatedFanFactor: Int,
    assistantCoaches: Int,
    cheerleaders: Int,
    apothecary: Int,
    treasury: Int,
    teamValue: Int
) {
    Row(modifier = Modifier.background(JervisTheme.rulebookPaperMediumDark)) {
        Column(
            modifier = Modifier.weight(1f).padding(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            TeamDataRow("Coach", coachName, "Re-Rerolls", rerolls.toString())
            TeamDataRow("Roster", roster, "Dedicated Fans", dedicatedFanFactor.toString())
            TeamDataRow("Treasury", treasury.toString(), "Assistant Coaches", assistantCoaches.toString())
            TeamDataRow("Team Value", teamValue.toString(), "Cheerleaders", cheerleaders.toString())
            TeamDataRow("Current Team Value", "900 K", "Apothecary", if (apothecary > 0) "Yes" else "No")
        }
        Box(modifier = Modifier.align(Alignment.CenterVertically).background(JervisTheme.rulebookPaperMediumDark).padding(end = 16.dp)) {
            Image(
                painter = painterResource(Res.drawable.roster_logo_chaos_chosen),
                contentDescription = "",
                contentScale = ContentScale.FillHeight,
            )
        }
    }
}

@Composable
private fun TeamDataRow(col1Header: String, col1Value: String, col2Header: String, col2Value: String) {
    Row {
        Text(
            modifier = Modifier.weight(1f),
            text = if (col1Header.isNotBlank()) "$col1Header:" else "",
            fontWeight = FontWeight.Bold,
        )
        Text(
            modifier = Modifier.weight(1f),
            text = if (col1Header.isNotBlank()) col1Value else "",
        )
        Text(
            modifier = Modifier.weight(1f),
            text = if (col2Header.isNotBlank()) "$col2Header:" else "",
            fontWeight = FontWeight.Bold,
        )
        Text(
            modifier = Modifier.weight(1f),
            text = if (col2Header.isNotBlank()) col2Value else "",
        )
    }
}

private val col1Width = 40.dp // Nr.
private val col2Width = 40.dp // Icon
private val col3Width = 200.dp // Name/Position
private val col4Width = 40.dp // Ma
private val col5Width = 40.dp // St
private val col6Width = 40.dp // Ag
private val col7Width = 40.dp // Pa
private val col8Width = 40.dp // Av
private val col9Width = 300.dp // Skills & Traits
private val col10Width = 100.dp // Injury
private val col11Width = 60.dp // Spp
private val col12Width = 60.dp // Cost

@Composable
private fun TeamTableRow(
    rowNo: Int,
    no: Int,
    icon: String,
    nameAndPosition: List<String>,
    ma: Int,
    st: Int,
    ag: Int,
    pa: Int,
    av: Int,
    skills: List<String>,
    injury: String,
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
        TeamTableCellText("", col2Width)
        TeamTablePlayerNameText("Foo ba", col3Width)
        TeamTableCellText(ma.toString(), col4Width)
        TeamTableCellText(st.toString(), col5Width)
        TeamTableCellText("$ag+", col6Width)
        TeamTableCellText(if (pa > 0) "$pa+" else "-", col7Width)
        TeamTableCellText("$av+", col8Width)
        TeamTableCellSkillText(skills.subList(0, 2), emptyList(), col9Width)
        TeamTableCellText(injury, col10Width)
        TeamTableCellText(spp.toString(), col11Width)
        TeamTableCellText("${cost/1000}K", col12Width)
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
        .drawBehind {
            // Height of the border near the edges (this is the minimum border border height)
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
                // clipped at a certain point for a given lenght of text.
                // To get consistent curvature regardless of the length of the text,
                // we calculate how much we need to shift the curve based on a the
                // difference to the baseline. This means we need to shift the curve
                // either left or right depending on if x is before or after the midpoint.
                val baselineWidth = 156.dp.toPx()
                val modifier = if (x <= size.width.toInt() / 2 ) -1 else 1
                val xModified = x + modifier * (baselineWidth - currentTitleWidth)/2

                // Bell curve. See https://en.wikipedia.org/wiki/Gaussian_function
                val a = 19.dp.toPx() // Height
                val b = size.width / 2 // Center
                val c = 125.dp.toPx() // Width / Std dev
                a * exp(-(xModified - b).pow(2) / (2 * c.pow(2)))
            }

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

            drawPath(path, color = JervisTheme.rulebookBlue)

            // Remove center of curve, so there is rooom for the title text
            val titlePadding = 20.dp.toPx()
            val starWidth = currentTitleWidth + titlePadding
            val cutoutPath = Path().apply {
                moveTo(size.width/2 - starWidth/2, 0f)
                lineTo(size.width/2 - starWidth/2, size.height - borderHeight)
                lineTo(size.width/2 + starWidth/2, size.height - borderHeight)
                lineTo(size.width/2 + starWidth/2, 0f)
                close()
            }
            drawPath(
                path = cutoutPath,
                color = Color.Black,
                blendMode = androidx.compose.ui.graphics.BlendMode.Clear
            )

            if (up) {
                // Draw Title if top wrapper
                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset.Unspecified.copy(
                        x = size.width / 2f - textLayoutResult.size.width / 2f,
                        y = size.height - borderHeight - textLayoutResult.size.height
                    ),
                )
            } else {
                // Draw 3 stars if bottom wrapper
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
fun TeamTableHeader() {
    Row(modifier = Modifier.background(color = JervisTheme.rulebookRed).padding(4.dp)) {
        TeamTableHeaderTitle("Nr.", col1Width)
        TeamTableHeaderTitle("", col2Width)
        TeamTableHeaderTitle("Name", col3Width)
        TeamTableHeaderTitle("Ma", col4Width)
        TeamTableHeaderTitle("St", col5Width)
        TeamTableHeaderTitle("Ag", col6Width)
        TeamTableHeaderTitle("Pa", col7Width)
        TeamTableHeaderTitle("Av", col8Width)
        TeamTableHeaderTitle("Skills & Traits", col9Width)
        TeamTableHeaderTitle("Injury", col10Width)
        TeamTableHeaderTitle("Spp", col11Width)
        TeamTableHeaderTitle("Cost", col12Width)
    }
}

@Composable
fun TeamTableHeaderTitle(text: String, width: Dp) {
    Text(
        modifier = Modifier.width(width),
        text = text.uppercase(),
        fontWeight = FontWeight.Medium,
        color = JervisTheme.white
    )
}

@Composable
fun TeamTableCellText(text: String, width: Dp) {
    Text(
        modifier = Modifier.width(width),
        text = text,
//        fontWeight = FontWeight.Bold,
    )
}

@Composable
fun TeamTablePlayerNameText(text: String, width: Dp) {
    Column(modifier = Modifier.width(width)) {
        Text(
            modifier = Modifier.width(width),
            text = text,
//        fontWeight = FontWeight.Bold,
        )
        Text(
            modifier = Modifier.width(width),
            text = "Blitzer",
            fontSize = 10.sp
//        fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun TeamTableCellSkillText(startingSkills: List<String>, gainedSkills: List<String>, width: Dp) {
    Column(modifier = Modifier.width(width)) {
        Text(
            modifier = Modifier.width(width),
            text = startingSkills.joinToString(", "),
            fontSize = 10.sp
        )
        if (gainedSkills.isNotEmpty()) {
            Text(
                modifier = Modifier.width(width),
                text = gainedSkills.joinToString(", ")
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
