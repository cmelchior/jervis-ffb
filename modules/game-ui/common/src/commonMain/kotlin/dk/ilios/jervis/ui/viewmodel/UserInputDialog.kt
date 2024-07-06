package dk.ilios.jervis.ui.viewmodel

import dk.ilios.jervis.actions.Cancel
import dk.ilios.jervis.actions.CoinSideSelected
import dk.ilios.jervis.actions.CoinTossResult
import dk.ilios.jervis.actions.Confirm
import dk.ilios.jervis.actions.Continue
import dk.ilios.jervis.actions.DiceResults
import dk.ilios.jervis.actions.DieResult
import dk.ilios.jervis.actions.DogoutSelected
import dk.ilios.jervis.actions.EndAction
import dk.ilios.jervis.actions.EndSetup
import dk.ilios.jervis.actions.EndTurn
import dk.ilios.jervis.actions.FieldSquareSelected
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.NoRerollSelected
import dk.ilios.jervis.actions.PlayerActionSelected
import dk.ilios.jervis.actions.PlayerDeselected
import dk.ilios.jervis.actions.PlayerSelected
import dk.ilios.jervis.actions.RandomPlayersSelected
import dk.ilios.jervis.actions.RerollOptionSelected
import dk.ilios.jervis.model.Team
import javax.swing.Icon

data class UserInputDialog(
    val icon: Icon? = null,
    val title: String,
    val message: String,
    val actionDescriptions: List<Pair<GameAction, String>>
): UserInput {
    override val actions: List<GameAction> = actionDescriptions.map { it.first }
    companion object {
        private fun getDescription(action: GameAction): String {
            return when(action) {
                Confirm -> "Confirm"
                Continue -> "Continue"
                is DieResult -> action.result.toString()
                DogoutSelected -> "DogoutSelected"
                EndSetup -> "EndSetup"
                EndTurn -> "EndTurn"
                is FieldSquareSelected -> action.toString()
                is PlayerSelected -> "Player[${action.player.name}, ${action.player.number.number}]"
                is DiceResults -> action.rolls.joinToString(prefix = "DiceRolls[", postfix = "]")
                is PlayerActionSelected -> "Action: ${action.action.name}"
                PlayerDeselected -> "Deselect active player"
                EndAction -> "End Action"
                Cancel -> "Cancel"
                is CoinSideSelected -> action.side.name
                is CoinTossResult -> action.result.name
                is RandomPlayersSelected -> "Random players: $action"
                NoRerollSelected -> "No reroll"
                is RerollOptionSelected -> action.option.toString()
            }
        }

        private fun create(title: String, message: String, actions: List<GameAction>): UserInputDialog {
            return UserInputDialog(null, title, message, actions.map { Pair(it, getDescription(it))})
        }

        private fun createWithDescription(title: String, message: String, actions: List<Pair<GameAction, String>>): UserInputDialog {
            return UserInputDialog(null, title, message, actions)
        }

        fun createFanFactorDialog(team: Team, actions: List<GameAction>): UserInputDialog = create(
            title = "Fan Factor Roll",
            message = "Roll D3 for ${team.name}",
            actions = actions
        )

        fun createWeatherRollDialog(actions: List<Pair<GameAction, String>>): UserInputDialog = createWithDescription(
            title = "Weather Roll",
            message = "Roll 2D6 for the weather",
            actions = actions
        )

        fun createSelectKickoffCoinTossResultDialog(team: Team, actions: List<GameAction>) = create(
            title = "Call Coin Toss Outcome",
            message = "${team.name} must select a side of the coin",
            actions = actions
        )

        fun createTossDialog(actions: List<GameAction>): UserInputDialog = create(
            title = "Coin Toss",
            message = "Flip coin into the air",
            actions = actions
        )

        fun createChooseToKickoffDialog(team: Team, actions: List<Pair<GameAction, String>>): UserInputDialog = createWithDescription(
            title = "Kickoff?",
            message = "${team.name} must choose to kick-off or receive",
            actions = actions
        )

        fun createInvalidSetupDialog(team: Team): UserInputDialog = create(
            title = "Invalid Setup",
            message = "Invalid setup, please try again",
            actions = listOf(Confirm)
        )

        fun createKickOffDeviatesDialog(actions: List<GameAction>): UserInputDialog = create(
            title = "The KickOff",
            message = "Roll 1D8 + 1D6 to deviate the ball.",
            actions = actions
        )

        fun createKickOffEventDialog(actions: List<Pair<GameAction, String>>): UserInputDialog = createWithDescription(
            title = "Kickoff Event",
            message = "Roll 2D6 for the KickOff event",
            actions = actions
        )


    }
}

class UnknownInput(override val actions: List<GameAction>): UserInput