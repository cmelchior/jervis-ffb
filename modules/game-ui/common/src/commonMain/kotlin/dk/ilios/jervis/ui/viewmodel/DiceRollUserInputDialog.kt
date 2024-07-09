package dk.ilios.jervis.ui.viewmodel

import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.actions.Dice
import dk.ilios.jervis.actions.DiceResults
import dk.ilios.jervis.actions.DieResult
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.rules.Rules
import javax.swing.Icon

/**
 * Class wrapping the intent to show a dialog for a dice roll involving multiple dice.
 * Each die gets its own line (since we assume this is only being used up to D8)
 * And the confirm button will show the final result
 */
data class DiceRollUserInputDialog(
    val icon: Icon? = null,
    val title: String,
    val message: String,
    val dice: List<Pair<Dice, List<DieResult>>>,
    val result: (DiceResults) -> String
): UserInputDialog {
    override val actions: List<GameAction> = emptyList()
    companion object {

        fun createWeatherRollDialog(rules: Rules, actions: List<Pair<GameAction, String>>): UserInput {
            return DiceRollUserInputDialog(
                title = "Weather roll",
                message = "Roll 2D6 for the weather",
                dice = listOf(
                    Pair(Dice.D6, D6Result.allOptions()),
                    Pair(Dice.D6, D6Result.allOptions())
                ),
                result = { rolls: DiceResults ->
                    rules.weatherTable.roll(rolls.rolls.first() as D6Result , rolls.rolls.last() as D6Result).title
                }
            )

        }

//        fun createSelectKickoffCoinTossResultDialog(team: Team, actions: List<GameAction>) = create(
//            title = "Call Coin Toss Outcome",
//            message = "${team.name} must select a side of the coin",
//            actions = actions
//        )
//
//        fun createTossDialog(actions: List<GameAction>): UserInputDialog = create(
//            title = "Coin Toss",
//            message = "Flip coin into the air",
//            actions = actions
//        )
//
//        fun createChooseToKickoffDialog(team: Team, actions: List<Pair<GameAction, String>>): UserInputDialog = createWithDescription(
//            title = "Kickoff?",
//            message = "${team.name} must choose to kick-off or receive",
//            actions = actions
//        )
//
//        fun createInvalidSetupDialog(team: Team): UserInputDialog = create(
//            title = "Invalid Setup",
//            message = "Invalid setup, please try again",
//            actions = listOf(Confirm)
//        )
//
//        fun createKickOffDeviatesDialog(actions: List<Pair<GameAction, String>>): UserInputDialog = createWithDescription(
//            title = "The KickOff",
//            message = "Roll 1D8 + 1D6 to deviate the ball.",
//            actions = actions
//        )
//
//        fun createKickOffEventDialog(actions: List<Pair<GameAction, String>>): UserInputDialog = createWithDescription(
//            title = "Kickoff Event",
//            message = "Roll 2D6 for the KickOff event",
//            actions = actions
//        )
//

    }
}
