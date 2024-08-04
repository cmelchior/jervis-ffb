package dk.ilios.jervis.ui.viewmodel

import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.actions.D8Result
import dk.ilios.jervis.actions.Dice
import dk.ilios.jervis.actions.DiceResults
import dk.ilios.jervis.actions.DieResult
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.tables.Direction
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
    val result: (DiceResults) -> String,
) : UserInputDialog {
    override val actions: List<GameAction> = emptyList()

    companion object {
        fun createWeatherRollDialog(rules: Rules): UserInput {
            return DiceRollUserInputDialog(
                title = "Weather roll",
                message = "Roll 2D6 for the weather",
                dice =
                    listOf(
                        Pair(Dice.D6, D6Result.allOptions()),
                        Pair(Dice.D6, D6Result.allOptions()),
                    ),
                result = { rolls: DiceResults ->
                    val description =
                        rules.weatherTable.roll(
                            rolls.rolls.first() as D6Result,
                            rolls.rolls.last() as D6Result,
                        ).title
                    "$description (${rolls.sumOf { it.result }})"
                },
            )
        }

        fun createKickOffDeviatesDialog(rules: Rules): UserInputDialog {
            return DiceRollUserInputDialog(
                title = "The KickOff",
                message = "Roll Roll 1D8 + 1D6 to deviate the ball.",
                dice =
                    listOf(
                        Pair(Dice.D8, D8Result.allOptions()),
                        Pair(Dice.D6, D6Result.allOptions()),
                    ),
                result = { rolls: DiceResults ->
                    val d8 = rolls.first() as? D8Result ?: rolls.last() as D8Result
                    val d6 = rolls.last() as? D6Result ?: rolls.first() as D6Result
                    val description =
                        when (val direction = rules.direction(d8)) {
                            Direction(-1, -1) -> "Up-Left"
                            Direction(0, -1) -> "Up"
                            Direction(1, -1) -> "Up-Right"
                            Direction(-1, 0) -> "Left"
                            Direction(1, 0) -> "Right"
                            Direction(-1, 1) -> "Down-Left"
                            Direction(0, 1) -> "Down"
                            Direction(1, 1) -> "Down-Right"
                            else -> TODO("Not supported: $direction")
                        }
                    "$description(${d6.result})"
                },
            )
        }

        fun createKickOffEventDialog(rules: Rules): UserInputDialog {
            return DiceRollUserInputDialog(
                title = "KickOff Event",
                message = "Roll 2D6 for the KickOff event.",
                dice =
                    listOf(
                        Pair(Dice.D6, D6Result.allOptions()),
                        Pair(Dice.D6, D6Result.allOptions()),
                    ),
                result = { rolls: DiceResults ->
                    val description: String =
                        rules.kickOffEventTable.roll(
                            rolls.first() as D6Result,
                            rolls.last() as D6Result,
                        ).name
                    "$description(${rolls.sumOf { it.result }})"
                },
            )
        }
    }
}
