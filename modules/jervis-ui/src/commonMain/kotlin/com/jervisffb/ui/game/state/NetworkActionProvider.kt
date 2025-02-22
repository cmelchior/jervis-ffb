package com.jervisffb.ui.game.state

import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.model.CoachId
import com.jervisffb.engine.model.Team
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.TeamActionMode
import com.jervisffb.ui.menu.p2p.AbstractClintNetworkMessageHandler
import com.jervisffb.ui.menu.p2p.P2PClientGameController
import com.jervisffb.utils.jervisLogger
import kotlinx.coroutines.launch

/**
 * Class responsible for enhancing the UI, so it is able to create a [GameAction]
 * that can be sent to the [GameEngineController].
 */
class NetworkActionProvider(
    team: Team,
    game: GameEngineController,
    menuViewModel: MenuViewModel,
    private val connection: P2PClientGameController,
    clientMode: TeamActionMode,
): ManualActionProvider(
    team,
    game,
    menuViewModel,
    clientMode
) {

    companion object {
        val LOG = jervisLogger()
    }
    var lastServerActionIndex: Int = -1

    override fun startHandler() {
        connection.addMessageHandler(object: AbstractClintNetworkMessageHandler() {
        override fun onGameAction(producer: CoachId, serverIndex: Int, action: GameAction) {
                lastServerActionIndex = serverIndex
                if (team.coach.id == producer) {
                    LOG.d("Handling game action for ${team.name}: $action")
                    // TODO This doesn't seem safe. We are not guaranteed that events are sent to actionSelectedChannel in order
//                    actionScope.launch {
                        LOG.d("Sending UI action for ${team.name}: $action")
                        if (!actionSelectedChannel.trySend(action).isSuccess) {
                            error("Failed to send action to channel: $action")
                        }
//                    }
                }
            }
        })
    }

    override fun syncAction(team: Team?, action: GameAction) {
        val clientIndex = game.history.last().id
        // Should only send this if the event is truly from this client and not just a sync message
        if (clientIndex > lastServerActionIndex && team?.id == this.team.id ) {
            LOG.d("Sending action to server $clientIndex > $lastServerActionIndex: $action")
            actionScope.launch {
                connection.sendActionToServer(clientIndex, action)
            }
        }
    }
}


