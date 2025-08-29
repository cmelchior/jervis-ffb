package com.jervisffb.ui.game.view.field

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.jervisffb.ui.game.UiGameSnapshot
import com.jervisffb.ui.game.icons.IconFactory
import com.jervisffb.ui.game.viewmodel.FieldViewModel

/**
 * Layer 9: Ball Layer.
 *
 * This layer is responsible for placing loose balls or bombs.
 *
 * See [Field] for more details about layer ordering.
 */
@Composable
fun BallLayer(
    vm: FieldViewModel,
) {
    val fieldSizeData = LocalFieldData.current.size
    val uiSnapshotFlow = remember { vm.observeSnapshot() }
    val snapshot: UiGameSnapshot? by uiSnapshotFlow.collectAsState(null)

    snapshot?.freeBalls?.let { balls ->
        balls.forEach { (coordinate, ball) ->
            // Should we change the ball color or outline depending on the state (in air, bouncing, scatter...)?
            Box(
                modifier = Modifier.jervisSquare(fieldSizeData, coordinate)
            ) {
                Image(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(2.dp)
                    ,
                    alignment = Alignment.Center,
                    contentScale = ContentScale.FillBounds,
                    bitmap = IconFactory.getBall(),
                    contentDescription = "Ball free at $coordinate",
                )
            }
        }
    }
}
