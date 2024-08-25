package dk.ilios.jervis.ui.viewmodel

import dk.ilios.jervis.actions.Cancel
import dk.ilios.jervis.actions.CoinSideSelected
import dk.ilios.jervis.actions.CoinTossResult
import dk.ilios.jervis.actions.Confirm
import dk.ilios.jervis.actions.Continue
import dk.ilios.jervis.actions.D8Result
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
import dk.ilios.jervis.actions.Undo
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.procedures.PickupRollResultContext
import dk.ilios.jervis.procedures.injury.RiskingInjuryRollContext
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.skills.Skill
import dk.ilios.jervis.rules.tables.Direction

sealed interface UserInputDialog : UserInput

/**
 * Class wrapping the intent of choosing a single option between many
 */
data class SingleChoiceInputDialog(
    val icon: Any? = null, // Replacement for javax.swing.Icon
    val title: String,
    val message: String,
    val actionDescriptions: List<Pair<GameAction, String>>,
) : UserInputDialog {
    override val actions: List<GameAction> = actionDescriptions.map { it.first }

    companion object {
        private fun getDescription(action: GameAction): String {
            return when (action) {
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
                is RerollOptionSelected -> action.option.source.rerollDescription
                Undo -> TODO()
            }
        }

        private fun create(
            title: String,
            message: String,
            actions: List<GameAction>,
        ): SingleChoiceInputDialog {
            return SingleChoiceInputDialog(null, title, message, actions.map { Pair(it, getDescription(it)) })
        }

        private fun createWithDescription(
            title: String,
            message: String,
            actions: List<Pair<GameAction, String>>,
        ): SingleChoiceInputDialog {
            return SingleChoiceInputDialog(null, title, message, actions)
        }

        fun createFanFactorDialog(
            team: Team,
            actions: List<GameAction>,
        ): SingleChoiceInputDialog =
            create(
                title = "Fan Factor Roll",
                message = "Roll D3 for ${team.name}",
                actions = actions,
            )

        fun createSelectKickoffCoinTossResultDialog(
            team: Team,
            actions: List<GameAction>,
        ) = create(
            title = "Call Coin Toss Outcome",
            message = "${team.name} must select a side of the coin",
            actions = actions,
        )

        fun createTossDialog(actions: List<GameAction>): SingleChoiceInputDialog =
            create(
                title = "Coin Toss",
                message = "Flip coin into the air",
                actions = actions,
            )

        fun createChooseToKickoffDialog(
            team: Team,
            actions: List<Pair<GameAction, String>>,
        ): SingleChoiceInputDialog =
            createWithDescription(
                title = "Kickoff?",
                message = "${team.name} must choose to kick-off or receive",
                actions = actions,
            )

        fun createInvalidSetupDialog(team: Team): SingleChoiceInputDialog =
            create(
                title = "Invalid Setup",
                message = "Invalid setup, please try again",
                actions = listOf(Confirm),
            )

        fun createCatchBallDialog(
            player: Player,
            actions: List<GameAction>,
        ): SingleChoiceInputDialog =
            create(
                title = "Catch Ball",
                message = "Roll D6 for ${player.name}",
                actions = actions,
            )

        fun createPickupBallDialog(
            player: Player,
            actions: List<GameAction>,
        ): SingleChoiceInputDialog =
            create(
                title = "Pickup Ball",
                message = "Roll D6 for ${player.name}",
                actions = actions,
            )

        fun createPickupRerollDialog(
            context: PickupRollResultContext,
            actions: List<GameAction>,
        ): SingleChoiceInputDialog {
            val message = "<Insert result of rolling D6>"
            return create(
                title = "Choose Reroll",
                message = message,
                actions = actions,
            )
        }

        fun createChooseBlockResultOrReroll(
            actions: List<GameAction>): SingleChoiceInputDialog {
            val message = "Choose result of block"
            return create(
                title = "Choose Reroll or Result",
                message = message,
                actions = actions,
            )
        }

        fun createBounceBallDialog(
            rules: Rules,
            actions: List<D8Result>,
        ): SingleChoiceInputDialog =
            createWithDescription(
                title = "Bounce Ball",
                message = "Roll D8 for the direction of the ball.",
                actions =
                    actions.map { roll: D8Result ->
                        val description =
                            when (val direction = rules.direction(roll)) {
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
                        Pair(roll, description)
                    },
            )

        fun createFollowUpDialog(player: Player): SingleChoiceInputDialog {
            return createWithDescription(
                title = "Follow-up",
                message = "Does ${player.name} want to follow up?",
                actions = listOf(Confirm to "Confirm", Cancel to "Cancel"),
            )
        }

        fun createUseApothecaryDialog(context: RiskingInjuryRollContext): SingleChoiceInputDialog {
            return createWithDescription(
                title = "Use Apothecary",
                message = "Do you want to use an apothecary to heal ${context.player.name} from a ${context.injuryResult}?",
                actions = listOf(Confirm to "Confirm", Cancel to "Cancel"),
            )
        }

        fun createUseSkillDialog(player: Player, skill: Skill): UserInput? {
            return createWithDescription(
                title = "Use ${skill.name}",
                message = "Does ${player.name} want to use ${skill.name}?",
                actions = listOf(Confirm to "Confirm", Cancel to "Cancel"),
            )
        }

    }
}

class UnknownInput(override val actions: List<GameAction>) : UserInput
