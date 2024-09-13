package dk.ilios.jervis.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dk.ilios.jervis.ui.images.IconFactory
import dk.ilios.jervis.ui.viewmodel.GameProgress
import dk.ilios.jervis.ui.viewmodel.GameStatusViewModel

@Composable
fun GameStatus(
    vm: GameStatusViewModel,
    modifier: Modifier,
) {
    val progress by vm.progress().collectAsState(GameProgress(0, 0, "", 0, "", 0))
    Box(modifier = modifier) {
        Image(
            bitmap = IconFactory.getScorebar(),
            contentDescription = null,
            alignment = Alignment.Center,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize(),
        )
        val textModifier = Modifier.padding(4.dp)

        // Turn counter
        Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                modifier = textModifier,
                text = "Turn",
                fontSize = 14.sp,
                color = Color.White,
            )
            Text(
                modifier = textModifier,
                text = "${progress.homeTeamTurn} / ${progress.awayTeamTurn}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
            )

            val half = when (progress.half) {
                1 -> "1st half"
                2 -> "2nd half"
                3 -> "Overtime"
                else -> null
            }
            if (half != null) {
                Text(
                    modifier = textModifier,
                    text = "of $half",
                    fontSize = 14.sp,
                    color = Color.White,
                )
            }
        }

        // Score counter
        // TODO Need to scale the distance between them
        Row(modifier = Modifier.align(Alignment.Center), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${progress.homeTeamScore}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                style = MaterialTheme.typography.h4.copy(
                    shadow = Shadow(
                        color = Color.Black,
                        offset = Offset(4f, 4f),
                        blurRadius = 8f
                    )
                )
            )
            Spacer(Modifier.width(78.dp))
            Text(
                text = "${progress.awayTeamScore}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                style = MaterialTheme.typography.h4.copy(
                    shadow = Shadow(
                        color = Color.Black,
                        offset = Offset(2f, 2f),
                        blurRadius = 2f
                    )
                )
            )
        }


    }
}
