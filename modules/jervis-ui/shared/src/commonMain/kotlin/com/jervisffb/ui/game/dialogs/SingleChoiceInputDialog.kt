package com.jervisffb.ui.game.dialogs

import androidx.compose.ui.unit.Dp
import com.jervisffb.engine.actions.AdminGameAction
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
import com.jervisffb.engine.actions.ForegoActivationSelected
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionId
import com.jervisffb.engine.actions.InducementsSelected
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PassTypeSelected
import com.jervisffb.engine.actions.PitchSquareSelected
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerDeselected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.PlayersSelected
import com.jervisffb.engine.actions.RandomPlayersSelected
import com.jervisffb.engine.actions.RerollOptionSelected
import com.jervisffb.engine.actions.Revert
import com.jervisffb.engine.actions.SkillSelected
import com.jervisffb.engine.actions.Undo
import com.jervisffb.engine.common.context.FoulContext
import com.jervisffb.engine.common.context.RiskingInjuryContext
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.skills.Skill

/**
 * Class wrapping the intent of choosing a single option between many.
 * There is no "Confirm" button, you select the result directly.
 */
data class SingleChoiceInputDialog(
    val icon: Any? = null, // Replacement for javax.swing.Icon
    val title: String,
    val message: String,
    val width: Dp,
    val actionDescriptions: List<Pair<GameAction, String>>,
    val nextActionId: GameActionId,
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
                is PitchSquareSelected -> action.toString()
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
                is CompositeGameAction -> action.actionList.joinToString(prefix = "[", postfix = "]")
                is SkillSelected -> action.skill.toString()
                is InducementsSelected -> "Select Inducements"
                is CalculatedAction -> TODO("Should only be used in tests")
                is BlockTypeSelected -> action.type.name
                is DicePoolResultsSelected -> action.results.toString()
                is DirectionSelected -> action.direction.toString()
                is ForegoActivationSelected -> "Forego Activation: ${action.player}"
                is PlayersSelected -> "Select Players: $action"
                is PassTypeSelected -> action.type.name
                is AdminGameAction -> error("Not supported")
            }
        }

        private fun create(
            title: String,
            message: String,
            actionId: GameActionId,
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
                nextActionId = actionId,
                owner = owner,
                moveable = movable,
            )
        }

        private fun createWithDescription(
            title: String,
            message: String,
            actionId: GameActionId,
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
                nextActionId = actionId,
                owner = owner,
                moveable = movable
            )
        }

        fun createFanFactorDialog(actionId: GameActionId, team: Team): UserInputDialog = create(
            title = "Fan Factor Roll",
            message = "Roll D3 for ${team.name}",
            actions = D3Result.allOptions(),
            actionId = actionId,
            state = team.game,
            owner = team,
            width = DialogSize.SMALL,
            movable = false,
        )

        fun createSelectKickoffCoinTossResultDialog(
            team: Team,
            actionId: GameActionId,
            actions: List<GameAction>,
        ) = create(
            title = "Coin Toss",
            message = "Call the outcome of the coin toss.",
            actionId = actionId,
            actions = actions,
            state = team.game,
            owner = team,
            width = DialogSize.SMALL,
            movable = false,
        )

        fun createTossDialog(
            actionId: GameActionId,
            state: Game,
            actions: List<GameAction>)
        : SingleChoiceInputDialog =
            create(
                title = "Coin Toss",
                message = "Flip coin into the air.",
                actions = actions,
                actionId = actionId,
                state = state,
                owner = state.homeTeam,
                width = DialogSize.SMALL,
                movable = false
            )

        fun createChooseToKickoffDialog(
            actionId: GameActionId,
            team: Team,
            actions: List<Pair<GameAction, String>>,
        ): SingleChoiceInputDialog =
            createWithDescription(
                title = "Kickoff?",
                message = "${team.name} must choose to kick-off or receive",
                actions = actions,
                actionId = actionId,
                owner = team,
                width = DialogSize.SMALL,
                movable = false
            )

        fun createInvalidSetupDialog(actionId: GameActionId, team: Team): SingleChoiceInputDialog =
            create(
                title = "Invalid Setup",
                message = "Invalid setup, please try again",
                actions = listOf(Confirm),
                actionId = actionId,
                state = team.game,
                owner = team
            )

        fun createTouchdownScoredDialog(actionId: GameActionId, player: Player): SingleChoiceInputDialog =
            create(
                title = "TOUCHDOWN!",
                message = "A touchdown was scored by ${player.name}",
                actions = listOf(Confirm),
                actionId = actionId,
                state = player.team.game,
                owner = player.team
            )

        fun createCatchBallDialog(
            actionId: GameActionId,
            player: Player,
            actions: List<GameAction>,
        ): SingleChoiceInputDialog =
            create(
                title = "Catch Ball",
                message = "Roll D6 for ${player.name}",
                actions = actions,
                actionId = actionId,
                state = player.team.game,
                owner = player.team,
            )

        fun createPickupBallDialog(
            actionId: GameActionId,
            player: Player,
            actions: List<GameAction>,
        ): SingleChoiceInputDialog =
            create(
                title = "Pickup Ball",
                message = "Roll D6 for ${player.name}",
                actions = actions,
                actionId = actionId,
                state = player.team.game,
                owner = player.team,
            )

        fun createCatchRerollDialog(
            actionId: GameActionId,
            state: Game,
            actions: List<GameAction>,
            owner: Team
        ): SingleChoiceInputDialog {
            val message = "Reroll catching the ball?"
            return create(
                title = "Choose Reroll",
                message = message,
                actions = actions,
                actionId = actionId,
                state = state,
                owner = owner,
            )
        }

        fun createPickupRerollDialog(
            actionId: GameActionId,
            state: Game,
            actions: List<GameAction>,
        ): SingleChoiceInputDialog {
            val message = "<Insert result of rolling D6>"
            return create(
                title = "Choose Reroll",
                message = message,
                actions = actions,
                actionId = actionId,
                state = state,
                owner = state.activeTeam!!,
            )
        }

        fun createChooseBlockResultOrReroll(
            actionId: GameActionId,
            state: Game,
            actions: List<GameAction>,
            owner: Team
        ): SingleChoiceInputDialog {
            val message = "Choose result of block"
            return create(
                title = "Choose Reroll or Result",
                message = message,
                actions = actions,
                actionId = actionId,
                state = state,
                owner = owner
            )
        }

        @Deprecated("Use ActionWheelInputDialog.createBounceBallDialog() instead")
        fun createBounceBallDialog(
            actionId: GameActionId,
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
                owner = owner,
                actionId = actionId,
            )

        fun createFollowUpDialog(actionId: GameActionId, player: Player): SingleChoiceInputDialog {
            return createWithDescription(
                title = "Follow-up",
                message = "Does ${player.name} want to follow up?",
                actions = listOf(Confirm to "Follow Up", Cancel to "Stay In Place"),
                actionId = actionId,
                owner = player.team
            )
        }

        fun createUseApothecaryDialog(actionId: GameActionId, context: RiskingInjuryContext): SingleChoiceInputDialog {
            return createWithDescription(
                title = "Use Apothecary",
                message = "Do you want to use an apothecary to heal ${context.player.name} from a ${context.injuryResult}?",
                actions = listOf(Confirm to "Confirm", Cancel to "Cancel"),
                actionId = actionId,
                owner = context.player.team
            )
        }

        fun createUseSkillDialog(actionId: GameActionId, player: Player, skill: Skill<*>): UserInputDialog {
            return createWithDescription(
                title = "Use ${skill.name}?",
                message = "Does ${player.name} want to use ${skill.name}?",
                actions = listOf(Confirm to "Confirm", Cancel to "Cancel"),
                actionId = actionId,
                owner = player.team,
            )
        }

        fun createArgueTheCallDialog(actionId: GameActionId, context: FoulContext): UserInputDialog {
            return createWithDescription(
                title = "Argue the call",
                message = "${context.fouler.name} was caught by the ref. Argue the call?",
                actions = listOf(Confirm to "Argue", Cancel to "Stay silent"),
                actionId = actionId,
                owner = context.fouler.team
            )
        }

        fun createRushRerollDialog(
            actionId: GameActionId,
            state: Game,
            actions: List<GameAction>,
            owner: Team
        ): SingleChoiceInputDialog {
            val message = "Reroll Rush?"
            return create(
                title = "Choose Reroll",
                message = message,
                actions = actions,
                actionId = actionId,
                state = state,
                owner = owner
            )
        }

        fun createDodgeRerollDialog(
            actionId: GameActionId,
            state: Game,
            actions: List<GameAction>,
            owner: Team
        ): SingleChoiceInputDialog {
            val message = "Reroll Dodge?"
            return create(
                title = "Choose Reroll",
                message = message,
                actions = actions,
                actionId = actionId,
                state = state,
                owner = owner
            )
        }
    }
}
