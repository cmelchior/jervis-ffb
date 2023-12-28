package dk.ilios.jervis.procedures

import compositeCommandOf
import dk.ilios.jervis.actions.Action
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.PlayerSelected
import dk.ilios.jervis.actions.SelectPlayer
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.SetKickingPlayer
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.logs.ReportKickingPlayer
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.utils.INVALID_GAME_STATE

/**
 * Do the Kick-Off.
 *
 * - See page 40 in the rulebook
 * - See Designer's Commentary - May 2023, page 2.
 */
object TheKickOff: Procedure() {
    override val initialNode: Node = NominateKickingPlayer
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object NominateKickingPlayer: ActionNode() {
        data class PlayersAvailableForKicking(
            var onLos: Int = 0,
            var available: Int = 0,
            val playersOnLoS: MutableList<Player> = mutableListOf(),
            val playersAvailable: MutableList<Player> = mutableListOf()
        )

        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            // Nominate a player on the center field that should kick the ball
            // If all players are on the line of scrimmage or in the wide zone, a player on the
            // line of scrimmage must be selected.
            val players = state.kickingTeam.fold(PlayersAvailableForKicking()) { acc, player ->
                val onLoS = player.location.isOnLineOfScrimmage(rules)
                val available = !(onLoS || player.location.isInWideZone(rules))
                if (onLoS) {
                    acc.onLos += 1
                    acc.playersOnLoS.add(player)
                }
                if (available) {
                    acc.available += 1
                    acc.playersAvailable.add(player)
                }
                acc.available += if (available) 1 else 0
                if (onLoS) {
                    acc.playersOnLoS
                }
                acc
            }

            val eligiblePlayers: List<SelectPlayer> = if (players.available > 0) {
                players.playersAvailable.map {
                    SelectPlayer(it)
                }
            } else {
                players.playersOnLoS.map {
                    SelectPlayer(it)
                }
            }
            if (eligiblePlayers.isEmpty()) {
                INVALID_GAME_STATE("No player available for kicking")
            }
            return eligiblePlayers
        }

        override fun applyAction(action: Action, state: Game, rules: Rules): Command {
            return checkType<PlayerSelected>(action) {
                compositeCommandOf(
                    SetKickingPlayer(it.player),
                    ReportKickingPlayer(it.player)
                )
            }
        }
    }

    object PlaceTheKick: ActionNode() {
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            TODO("Not yet implemented")
        }

        override fun applyAction(action: Action, state: Game, rules: Rules): Command {
            TODO("Not yet implemented")
        }
    }

    object TheKickDeviates: ActionNode() {
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            TODO("Not yet implemented")
        }

        override fun applyAction(action: Action, state: Game, rules: Rules): Command {
            TODO("Not yet implemented")
        }

    }
}