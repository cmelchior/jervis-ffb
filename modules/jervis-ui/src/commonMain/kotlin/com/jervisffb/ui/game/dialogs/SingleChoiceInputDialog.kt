package com.jervisffb.ui.game.dialogs

import androidx.compose.ui.text.font.FontVariation.width
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jervisffb.engine.actions.BlockTypeSelected
import com.jervisffb.engine.actions.CalculatedAction
import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CoinSideSelected
import com.jervisffb.engine.actions.CoinTossResult
import com.jervisffb.engine.actions.CompositeGameAction
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.D3Result
import com.jervisffb.engine.actions.D8Result
import com.jervisffb.engine.actions.DicePoolResultsSelected
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.DieResult
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.DogoutSelected
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.EndSetup
import com.jervisffb.engine.actions.EndTurn
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.InducementSelected
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerDeselected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.PlayerSubActionSelected
import com.jervisffb.engine.actions.RandomPlayersSelected
import com.jervisffb.engine.actions.RerollOptionSelected
import com.jervisffb.engine.actions.Revert
import com.jervisffb.engine.actions.SkillSelected
import com.jervisffb.engine.actions.Undo
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.reports.ReportStartingExtraTime.message
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.actions.foul.FoulContext
import com.jervisffb.engine.rules.bb2020.procedures.tables.injury.RiskingInjuryContext
import com.jervisffb.engine.rules.bb2020.skills.Skill

/**
 * Class wrapping the intent of choosing a single option between many
 */
data class SingleChoiceInputDialog(
    val icon: Any? = null, // Replacement for javax.swing.Icon
    val title: String,
    val message: String,
    val width: Dp,
    val actionDescriptions: List<Pair<GameAction, String>>,
    override var owner: Team? = null,
    val moveable: Boolean = true,
) : UserInputDialog {
    companion object {
        private fun getDescription(state: Game, action: GameAction): String {
            return when (action) {
                Confirm -> "Confirm"
                Continue -> "Continue"
                is DieResult -> action.value.toString()
                DogoutSelected -> "DogoutSelected"
                EndSetup -> "EndSetup"
                EndTurn -> "EndTurn"
                is FieldSquareSelected -> action.toString()
                is PlayerSelected -> "Player[${action.playerId}]"
                is DiceRollResults -> action.rolls.joinToString(prefix = "DiceRolls[", postfix = "]")
                is PlayerActionSelected -> "Action: ${action.action}"
                is PlayerDeselected -> "Deselect active player"
                EndAction -> "End Action"
                Cancel -> "Cancel"
                is CoinSideSelected -> action.side.name
                is CoinTossResult -> action.result.name
                is RandomPlayersSelected -> "Random players: $action"
                is NoRerollSelected -> "No reroll"
                is RerollOptionSelected -> action.option.getRerollSource(state).rerollDescription
                Undo -> TODO()
                Revert -> TODO()
                is MoveTypeSelected -> action.moveType.toString()
                is CompositeGameAction -> action.list.joinToString(prefix = "[", postfix = "]")
                is PlayerSubActionSelected -> action.name
                is SkillSelected -> action.skill.toString()
                is InducementSelected -> action.name
                is CalculatedAction -> TODO("Should only be used in tests")
                is BlockTypeSelected -> action.type.name
                is DicePoolResultsSelected -> action.results.toString()
                is DirectionSelected -> action.direction.toString()
            }
        }

        private fun create(
            title: String,
            message: String,
            actions: List<GameAction>,
            state: Game,
            owner: Team,
            width: Dp = DialogSize.MEDIUM,
            movable: Boolean = true,
        ): SingleChoiceInputDialog {
            return SingleChoiceInputDialog(
                icon = null,
                title = title,
                message = message,
                width = width,
                actionDescriptions = actions.map { Pair(it, getDescription(state, it))},
                owner = owner,
                moveable = movable,
            )
        }

        private fun createWithDescription(
            title: String,
            message: String,
            actions: List<Pair<GameAction, String>>,
            owner: Team,
            width: Dp = DialogSize.MEDIUM,
            movable: Boolean = true,
        ): SingleChoiceInputDialog {
            return SingleChoiceInputDialog(
                icon = null,
                title = title,
                message = message,
                width = width,
                actionDescriptions = actions,
                owner = owner,
                moveable = movable
            )
        }

        fun createFanFactorDialog(team: Team): UserInputDialog = create(
            title = "Fan Factor Roll",
            message = "Roll D3 for ${team.name}",
            actions = D3Result.allOptions(),
            state = team.game,
            owner = team,
            width = DialogSize.SMALL,
            movable = false,
        )

        fun createSelectKickoffCoinTossResultDialog(
            team: Team,
            actions: List<GameAction>,
        ) = create(
            title = "Coin Toss",
            message = "Call the outcome of the coin toss.",
            actions = actions,
            state = team.game,
            owner = team,
            width = DialogSize.SMALL,
            movable = false,
        )

        fun createTossDialog(
            state: Game,
            actions: List<GameAction>)
        : SingleChoiceInputDialog =
            create(
                title = "Coin Toss",
                message = "Flip coin into the air.",
                actions = actions,
                state = state,
                owner = state.homeTeam,
                width = DialogSize.SMALL,
                movable = false
            )

        fun createChooseToKickoffDialog(
            team: Team,
            actions: List<Pair<GameAction, String>>,
        ): SingleChoiceInputDialog =
            createWithDescription(
                title = "Kickoff?",
                message = "${team.name} must choose to kick-off or receive",
                actions = actions,
                owner = team,
                width = DialogSize.SMALL,
                movable = false
            )

        fun createInvalidSetupDialog(team: Team): SingleChoiceInputDialog =
            create(
                title = "Invalid Setup",
                message = "Invalid setup, please try again",
                actions = listOf(Confirm),
                state = team.game,
                owner = team
            )

        fun createCatchBallDialog(
            player: Player,
            actions: List<GameAction>,
        ): SingleChoiceInputDialog =
            create(
                title = "Catch Ball",
                message = "Roll D6 for ${player.name}",
                actions = actions,
                state = player.team.game,
                owner = player.team,
            )

        fun createPickupBallDialog(
            player: Player,
            actions: List<GameAction>,
        ): SingleChoiceInputDialog =
            create(
                title = "Pickup Ball",
                message = "Roll D6 for ${player.name}",
                actions = actions,
                state = player.team.game,
                owner = player.team,
            )

        fun createCatchRerollDialog(
            state: Game,
            actions: List<GameAction>,
            owner: Team
        ): SingleChoiceInputDialog {
            val message = "Reroll catching the ball?"
            return create(
                title = "Choose Reroll",
                message = message,
                actions = actions,
                state = state,
                owner = owner,
            )
        }

        fun createPickupRerollDialog(
            state: Game,
            actions: List<GameAction>,
        ): SingleChoiceInputDialog {
            val message = "<Insert result of rolling D6>"
            return create(
                title = "Choose Reroll",
                message = message,
                actions = actions,
                state = state,
                owner = state.activeTeam!!,
            )
        }

        fun createChooseBlockResultOrReroll(
            state: Game,
            actions: List<GameAction>,
            owner: Team
        ): SingleChoiceInputDialog {
            val message = "Choose result of block"
            return create(
                title = "Choose Reroll or Result",
                message = message,
                actions = actions,
                state = state,
                owner = owner
            )
        }

        fun createBounceBallDialog(
            rules: Rules,
            actions: List<D8Result>,
            owner: Team,
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
                owner = owner
            )

        fun createFollowUpDialog(player: Player): SingleChoiceInputDialog {
            return createWithDescription(
                title = "Follow-up",
                message = "Does ${player.name} want to follow up?",
                actions = listOf(Confirm to "Follow Up", Cancel to "Stay In Place"),
                owner = player.team
            )
        }

        fun createUseApothecaryDialog(context: RiskingInjuryContext): SingleChoiceInputDialog {
            return createWithDescription(
                title = "Use Apothecary",
                message = "Do you want to use an apothecary to heal ${context.player.name} from a ${context.injuryResult}?",
                actions = listOf(Confirm to "Confirm", Cancel to "Cancel"),
                owner = context.player.team
            )
        }

        fun createUseSkillDialog(player: Player, skill: Skill): UserInputDialog {
            return createWithDescription(
                title = "Use ${skill.name}?",
                message = "Does ${player.name} want to use ${skill.name}?",
                actions = listOf(Confirm to "Confirm", Cancel to "Cancel"),
                owner = player.team,
            )
        }

        fun createArgueTheCallDialog(context: FoulContext): UserInputDialog {
            return createWithDescription(
                title = "Argue the call",
                message = "${context.fouler.name} was caught by the ref. Argue the call?",
                actions = listOf(Confirm to "Argue", Cancel to "Stay silent"),
                owner = context.fouler.team
            )
        }

        fun createRushRerollDialog(
            state: Game,
            actions: List<GameAction>,
            owner: Team
        ): SingleChoiceInputDialog {
            val message = "Reroll Rush?"
            return create(
                title = "Choose Reroll",
                message = message,
                actions = actions,
                state = state,
                owner = owner
            )
        }

        fun createDodgeRerollDialog(
            state: Game,
            actions: List<GameAction>,
            owner: Team
        ): SingleChoiceInputDialog {
            val message = "Reroll Dodge?"
            return create(
                title = "Choose Reroll",
                message = message,
                actions = actions,
                state = state,
                owner = owner
            )
        }
    }
}
