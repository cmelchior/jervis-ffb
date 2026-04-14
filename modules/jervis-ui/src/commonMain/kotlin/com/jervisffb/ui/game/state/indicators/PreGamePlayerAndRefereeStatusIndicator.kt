package com.jervisffb.ui.game.state.indicators

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.common.procedures.PreGame
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.model.UiPitchPlayer
import com.jervisffb.ui.game.model.UiPitchSquare
import com.jervisffb.ui.game.state.actionwheel.AwayTeamFanFactorRoll
import com.jervisffb.ui.game.state.actionwheel.HomeTeamFanFactorRoll

/**
 * During the Pre-game sequence we want to show a player from each team
 * and the referee in the middle of the pitch, like they are resolving the
 * initial dice rolls together.
 *
 * The position of these must be aligned with:
 * [com.jervisffb.ui.game.state.actionwheel.ChooseKickingTeamWheelController]
 * [com.jervisffb.ui.game.state.actionwheel.HomeTeamFanFactorRoll]
 * [com.jervisffb.ui.game.state.actionwheel.AwayTeamFanFactorRoll]
 */
object PreGamePlayerAndRefereeStatusIndicator: PitchStatusIndicator {
    override fun decorate(
        node: ActionNode,
        state: Game,
        request: ActionRequest,
        acc: UiSnapshotAccumulator
    ) {
        if (!state.stack.containsProcedure(PreGame)) return

        val homePlayer = state.homeTeam.firstOrNull()
        val homePlayerCoords = HomeTeamFanFactorRoll.getActionWheelCenter(state)
        val awayPlayer = state.awayTeam.firstOrNull()
        val awayPlayerCoords = AwayTeamFanFactorRoll.getActionWheelCenter(state)

        if (homePlayer != null) {
            acc.addOrUpdateSquare(
                homePlayerCoords,
                UiPitchSquare(
                    coordinates = homePlayerCoords,
                    player = homePlayer.id
                )
            )
            acc.addOrUpdatePlayer(
                homePlayer.id,
                UiPitchPlayer(
                    model = homePlayer,
                    overrideLocation = homePlayerCoords,
                )
            )
        }

        if (awayPlayer != null) {
            acc.addOrUpdateSquare(
                homePlayerCoords,
                UiPitchSquare(
                    coordinates = awayPlayerCoords,
                    player = awayPlayer.id
                )
            )
            acc.addOrUpdatePlayer(
                awayPlayer.id,
                UiPitchPlayer(
                    model = awayPlayer,
                    overrideLocation = awayPlayerCoords,)
            )
        }

        // Place in center of screen
        acc.showReferee(coordinates = null)
    }
}
