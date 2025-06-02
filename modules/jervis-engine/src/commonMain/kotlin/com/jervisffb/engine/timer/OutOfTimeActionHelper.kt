package com.jervisffb.engine.timer

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.CompositeGameAction
import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.DeselectPlayer
import com.jervisffb.engine.actions.DogoutSelected
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.EndActionWhenReady
import com.jervisffb.engine.actions.EndSetup
import com.jervisffb.engine.actions.EndTurn
import com.jervisffb.engine.actions.EndTurnWhenReady
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.PlayerDeselected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.DogOut
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.bb2020.procedures.SetupTeamContext

/**
 * Wrapper class for generating actions when out of time.
 *
 * It is mostly here to keep [TimerTracker] leaner. But it also makes testing
 * easier and makes it slightly easier to enable configurable behavior down the
 * line (if we want that).
 */
object OutOfTimeActionHelper {

    /**
     * Calculate the action, that is best suited to exit whatever situation we are in.
     * If no obvious action exists, return `null`.
     */
    fun calculateExitAction(state: Game, actions: ActionRequest): GameAction? {
        val actions = actions.actions
        if (actions.contains(EndTurnWhenReady)) {
            return EndTurn
        }
        if (actions.contains(EndActionWhenReady)) {
            return EndAction
        }
        if (actions.contains(CancelWhenReady)) {
            return Cancel
        }
        if (actions.filterIsInstance<DeselectPlayer>().isNotEmpty()) {
            return actions.filterIsInstance<DeselectPlayer>().first().let {
                PlayerDeselected(it.players.first())
            }
        }
        if (actions.contains(ContinueWhenReady)) {
            return Continue
        }
        return null
    }


    /**
     * Calculate how to end the setup phase in the quickest way possible.
     */
    fun calculateOutOfTimeSetupActions(state: Game, actions: ActionRequest): GameAction {
        val rules = state.rules
        // If setup is valid, just accept whatever the current setup is.
        val context = state.getContext<SetupTeamContext>()
        // This doesn't work if a player is currently selected
        if (rules.isValidSetup(state, context.team)) {
            if (context.currentPlayer != null) {
                val setPlayerDownAction = getSetPlayerDownDuringSetupAction(context.currentPlayer)
                return CompositeGameAction(listOfNotNull(setPlayerDownAction, EndSetup))
            } else {
                return EndSetup
            }
        }

        // If not, we need to find a way to get a legal setup
        // For now; this just means putting all players back in the Reserves. Then add them
        // to the LoS (starting from the center) until we have a legal setup.
        // This might involve them standing in multiple rows.
        val team = context.team
        val setDownActions = if (context.currentPlayer != null) {
            getSetPlayerDownDuringSetupAction(context.currentPlayer)
        } else {
            null
        }
        val moveBackActions = listOfNotNull(setDownActions) + team.flatMap {
            if (it.location.isOnField(rules)) {
                listOf(
                    PlayerSelected(it),
                    DogoutSelected
                )
            } else {
                emptyList()
            }
        }

        val playersNeededOnField = rules.maxPlayersOnField
        var playersPlaced = 0
        val availableLocations = generateLocationsForSetup(state, team.isHomeTeam(), playersNeededOnField).reversed().toMutableList()
        val movePlayersToField = team
            // Players are not actually moved back to the Reserves yet, so we need to check both states
            .filter { it.state == PlayerState.RESERVE || it.state == PlayerState.STANDING }
            .sortedBy { it.number }
            .flatMap {
                if (playersNeededOnField == playersPlaced) {
                    emptyList()
                } else {
                    playersPlaced += 1
                    listOf(
                        PlayerSelected(it),
                        FieldSquareSelected(availableLocations.removeLast())
                    )
                }
            }

        return CompositeGameAction(moveBackActions + movePlayersToField + EndSetup)
    }


    // Generate the list of locations we want to place players on
    private fun generateLocationsForSetup(state: Game, isHomeTeam: Boolean, playersNeededOnField: Int): List<FieldCoordinate> {
        val rules = state.rules
        val los = if (isHomeTeam) rules.lineOfScrimmageHome else rules.lineOfScrimmageAway
        val locations = mutableListOf<FieldCoordinate>()
        var x = los
        val y = rules.fieldHeight / 2 // start from the center
        var dir = 1
        var i = 1
        while (locations.size < playersNeededOnField) {
            val newY = y + (i/2) * dir
            if (newY < rules.wideZone || newY > rules.fieldHeight - rules.wideZone) {
                // Row is full, jump to next row
                x += if (isHomeTeam) -1 else 1
                i = 1
            } else {
                locations.add(FieldCoordinate(x, newY))
                i++
                dir *= -1
            }
        }
        return locations
    }

    private fun getSetPlayerDownDuringSetupAction(currentPlayer: Player?): GameAction {
        val setPlayerDownAction = when (val loc = currentPlayer!!.location) {
            DogOut -> DogoutSelected
            is FieldCoordinate -> FieldSquareSelected(loc)
            else -> TODO("Not supported yet: $loc")
        }
        return setPlayerDownAction
    }
}
