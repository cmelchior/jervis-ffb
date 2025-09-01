package com.jervisffb.ui.game.dialogs

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CompositeGameAction
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.D3Result
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.D8Result
import com.jervisffb.engine.actions.DBlockResult
import com.jervisffb.engine.actions.DicePoolChoice
import com.jervisffb.engine.actions.DicePoolResultsSelected
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.DieResult
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.RerollOptionSelected
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.actions.SelectDicePoolResult
import com.jervisffb.engine.actions.SelectRerollOption
import com.jervisffb.engine.actions.Undo
import com.jervisffb.engine.model.DieId
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.DieRoll
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.BlockContext
import com.jervisffb.engine.rules.builder.UndoActionBehavior
import com.jervisffb.engine.rules.common.skills.Skill
import com.jervisffb.ui.game.dialogs.circle.ActionMenuItem
import com.jervisffb.ui.game.dialogs.circle.ActionWheelViewModel
import com.jervisffb.ui.game.dialogs.circle.DiceMenuItem
import com.jervisffb.ui.game.dialogs.circle.MenuExpandMode
import com.jervisffb.ui.game.icons.ActionIcon
import com.jervisffb.ui.game.state.UiActionProvider

/**
 * Class wrapping the intent to show an action wheel on the field. [viewModel]
 * contains all the menu states.
 */
class ActionWheelInputDialog(
    override var owner: Team?,
    val viewModel: ActionWheelViewModel,
): UserInputDialog {

    companion object {

        fun createFanFactorDialog(provider: UiActionProvider, team: Team): UserInputDialog {
            val viewModel = ActionWheelViewModel(
                team = team,
                center = null,
                startHoverText = "Fan Factor Roll",
                fallbackToShowStartHoverText = false,
            ).also {
                it.topMenu.addDiceButton(
                    id = DieId("RollingDie-1"),
                    diceValue = D3Result.random(),
                    options = D3Result.allOptions(),
                    preferLtr = true,
                    expandable = true,
                    animatingFrom = D3Result.random(),
                )
                it.bottomMenu.addActionButton(
                    label = { " Roll Fan Factor" },
                    icon = ActionIcon.CONFIRM,
                    enabled = true,
                    onClick = { parent, button ->
                        val dice = it.topMenu.menuItems.first() as DiceMenuItem<*>
                        provider.userActionSelected(dice.value)
                        it.hideWheel(true)
                    }
                )
            }

            return ActionWheelInputDialog(
                owner = team,
                viewModel = viewModel,
            )
        }

        fun createDeviateDialog(
            provider: UiActionProvider,
            state: Game,
            request: ActionRequest,
            isKickOff: Boolean = true
        ): UserInputDialog {

            fun getResultLabel(d6: D6Result, d8: D8Result): String {
                val description =
                    when (val direction = state.rules.direction(d8)) {
                        Direction(-1, -1) -> "↖"
                        Direction(0, -1) -> "↑"
                        Direction(1, -1) -> "↗"
                        Direction(-1, 0) -> "←"
                        Direction(1, 0) -> "→"
                        Direction(-1, 1) -> "↙"
                        Direction(0, 1) -> "↓"
                        Direction(1, 1) -> "↘"
                        else -> TODO("Not supported: $direction")
                    }
                return "Deviate: ${d6.value}$description"
            }

            val team = request.team ?: state.homeTeam
            val viewModel = ActionWheelViewModel(
                team = team,
                center = state.singleBall().location,
                startHoverText = "Deviate Ball",
                fallbackToShowStartHoverText = false,
            ).also { wheelModel ->
                wheelModel.topMenu.let { menu ->
                    menu.addDiceButton(
                        id = DieId("Deviate-D6"),
                        diceValue = D6Result.random(),
                        options = D6Result.allOptions(),
                        preferLtr = true,
                        expandable = true,
                        animatingFrom = D6Result.random(),
                        onHover = { d6 ->
                            val d8 = wheelModel.topMenu.getDiceButton(1).value as D8Result
                            d6?.let { getResultLabel(it, d8) }
                        }
                    )
                    menu.addDiceButton(
                        id = DieId("Deviate-D8"),
                        diceValue = D8Result.random(),
                        options = D8Result.allOptions(),
                        preferLtr = false,
                        expandable = true,
                        animatingFrom = D8Result.random(),
                        onHover = { d8 ->
                            val d6 = wheelModel.topMenu.getDiceButton(0).value as D6Result
                            d8?.let { getResultLabel(d6, it) }
                        }
                    )
                }
                wheelModel.bottomMenu.addActionButton(
                    label = {
                        val d6 = wheelModel.topMenu.getDiceButton(0).value as D6Result
                        val d8 = wheelModel.topMenu.getDiceButton(1).value as D8Result
                        getResultLabel(d6, d8)
                    },
                    icon = ActionIcon.CONFIRM,
                    enabled = true,
                    onClick = { parent, button ->
                        val dice = DiceRollResults(wheelModel.topMenu.menuItems.reversed().filterIsInstance<DiceMenuItem<*>>().map { it.value })
                        provider.userActionSelected(dice)
                        wheelModel.hideWheel(true)
                    }
                )
            }
            return ActionWheelInputDialog(
                owner = team,
                viewModel = viewModel,
            )
        }

        fun createKickOffEventDialog(provider: UiActionProvider, rules: Rules, team: Team): UserInputDialog {

            fun getResultLabel(firstD6: D6Result, secondD6: D6Result): String {
                val rolls = listOf(firstD6, secondD6)
                val description: String =
                    rules.kickOffEventTable.roll(
                        rolls.first(),
                        rolls.last(),
                    ).description
                return "$description (${rolls.sumOf { it.value }})"
            }

            val currentBall = team.game.singleBall()
            val center = when (currentBall.location.isOnField(rules)) {
                true -> currentBall.location
                false -> currentBall.outOfBoundsAt!!
            }
            val viewModel = ActionWheelViewModel(
                team = team,
                center = center,
                startHoverText = "Kick-off Event",
                fallbackToShowStartHoverText = false,
            ).also { wheelModel ->
                val labelGenerator = {
                }
                wheelModel.topMenu.let { menu ->
                    menu.addDiceButton(
                        id = DieId("Kickoff-D6-1"),
                        diceValue = D6Result.random(),
                        options = D6Result.allOptions(),
                        preferLtr = true,
                        expandable = true,
                        animatingFrom = D6Result.random(),
                        onHover = { d6 ->
                            val dice2 = wheelModel.topMenu.getDiceButton(1).value as D6Result
                            d6?.let {getResultLabel(it, dice2) }
                        }
                    )
                    menu.addDiceButton(
                        id = DieId("Kickoff-D6-2"),
                        diceValue = D6Result.random(),
                        options = D6Result.allOptions(),
                        preferLtr = false,
                        expandable = true,
                        animatingFrom = D6Result.random(),
                        onHover = { d6 ->
                            val dice1 = wheelModel.topMenu.getDiceButton(0).value as D6Result
                            d6?.let {getResultLabel(dice1, it) }
                        }
                    )
                }
                wheelModel.bottomMenu.addActionButton(
                    label = {
                        val dice1 = wheelModel.topMenu.getDiceButton(0).value as D6Result
                        val dice2 = wheelModel.topMenu.getDiceButton(1).value as D6Result
                        getResultLabel(dice1, dice2)
                    },
                    icon = ActionIcon.CONFIRM,
                    enabled = true,
                    onClick = { parent, button ->
                        val dice = DiceRollResults(wheelModel.topMenu.menuItems.reversed().filterIsInstance<DiceMenuItem<*>>().map { it.value })
                        provider.userActionSelected(dice)
                        wheelModel.hideWheel(true)
                    }
                )
            }
            return ActionWheelInputDialog(
                owner = team,
                viewModel = viewModel,
            )
        }

        fun createBounceBallDialog(
            provider: UiActionProvider,
            state: Game,
            owner: Team,
        ): UserInputDialog {
            fun getResultLabel(d8: D8Result): String {
                val description =
                    when (val direction = state.rules.direction(d8)) {
                        Direction(-1, -1) -> "↖"
                        Direction(0, -1) -> "↑"
                        Direction(1, -1) -> "↗"
                        Direction(-1, 0) -> "←"
                        Direction(1, 0) -> "→"
                        Direction(-1, 1) -> "↙"
                        Direction(0, 1) -> "↓"
                        Direction(1, 1) -> "↘"
                        else -> TODO("Not supported: $direction")
                    }
                return "Bounce: ${d8.value}$description"
            }
            val viewModel = ActionWheelViewModel(
                team = owner,
                center = state.currentBall().location,
                startHoverText = "Bounce Ball",
                fallbackToShowStartHoverText = false,
            ).also { wheelModel ->
                wheelModel.topMenu.let { menu ->
                    menu.addDiceButton(
                        id = DieId("Bounce-D8"),
                        diceValue = D8Result.random(),
                        options = D8Result.allOptions(),
                        preferLtr = true,
                        expandable = true,
                        animatingFrom = D8Result.random(),
                        onHover = { d8 ->
                            d8?.let { getResultLabel(it) }
                        }
                    )
                }
                wheelModel.bottomMenu.addActionButton(
                    label = {
                        val d8 = wheelModel.topMenu.getDiceButton(0).value as D8Result
                        getResultLabel(d8)
                    },
                    icon = ActionIcon.CONFIRM,
                    enabled = true,
                    onClick = { parent, button ->
                        val dice = wheelModel.topMenu.getDiceResults()
                        provider.userActionSelected(dice)
                        wheelModel.hideWheel(true)
                    }
                )
            }
            return ActionWheelInputDialog(
                owner = owner,
                viewModel = viewModel,
            )
        }

        fun createBlockRollDialog(
            provider: UiActionProvider,
            request: ActionRequest,
            isBlitz: Boolean,
            isReroll: Boolean,
        ): UserInputDialog {
            val state = request.team!!.game
            val targetLocation = state.getContext<BlockContext>().defender.location as FieldCoordinate
            val viewModel = ActionWheelViewModel(
                team = request.team!!,
                center = targetLocation,
                startHoverText = when {
                    isBlitz && !isReroll -> "Roll Block Dice"
                    isBlitz && isReroll -> "Reroll Block Dice"
                    !isBlitz && !isReroll -> "Roll Block Dice"
                    !isBlitz && isReroll -> "Reroll Block Dice"
                    else -> error("Invalid state: $isBlitz, $isReroll")
                },
                fallbackToShowStartHoverText = true,
            ).also { wheelModel ->
                wheelModel.topMenu.let { menu ->
                    val diceCount = (request.actions.first() as RollDice).dice.size
                    repeat(diceCount) {
                        menu.addDiceButton(
                            id = DieId("Block-D6-${it + 1}"),
                            diceValue = DBlockResult.random(),
                            options = DBlockResult.allOptions(),
                            preferLtr = true,
                            expandable = true,
                            animatingFrom = DBlockResult.random(),
                            onHover = { dblock ->
                                dblock?.blockResult?.name
                            }
                        )
                    }
                }
                wheelModel.bottomMenu.addActionButton(
                    label = { "Lock Dice Roll" },
                    icon = ActionIcon.CONFIRM,
                    enabled = true,
                    onClick = { parent, button ->
                        val dice = DiceRollResults(wheelModel.topMenu.menuItems.filterIsInstance<DiceMenuItem<*>>().map { it.value })
                        provider.userActionSelected(dice)
                        wheelModel.hideWheel(true)
                    }
                )
            }
            return ActionWheelInputDialog(
                owner = request.team!!,
                viewModel = viewModel,
            )
        }

        fun createSelectBlockDie(
            provider: UiActionProvider,
            request: ActionRequest,
        ): UserInputDialog {
            val dicePool = request.actions.first() as SelectDicePoolResult
            val state = request.team!!.game
            val targetLocation = state.getContext<BlockContext>().defender.location as FieldCoordinate
            val viewModel = ActionWheelViewModel(
                team = request.team!!,
                center = targetLocation,
                startHoverText = "Select Block Result",
                fallbackToShowStartHoverText = false,
            ).also { wheelModel ->
                wheelModel.topMenu.let { menu ->
                    @Suppress("UNCHECKED_CAST")
                    val dice = dicePool.pools.first().dice as List<DieRoll<DBlockResult>>
                    dice.forEach {
                        menu.addDiceButton(
                            id = it.id,
                            diceValue = it.result,
                            options = DBlockResult.allOptions(),
                            preferLtr = true,
                            expandable = false,
                            onClick = { value: DieResult ->
                                val action = DicePoolResultsSelected.fromSingleDice(value)
                                provider.userActionSelected(action)
                            },
                            animatingFrom = DBlockResult.random(),
                            onHover = { dblock ->
                                dblock?.blockResult?.description
                            }
                        )
                    }
                }
//                wheelModel.bottomMenu.addActionButton(
//                    label = { "Lock Dice Roll" },
//                    icon = ActionIcon.CONFIRM,
//                    enabled = true,
//                    onClick = { parent, button ->
//                        val dice = DiceRollResults(wheelModel.topMenu.menuItems.filterIsInstance<DiceMenuItem<*>>().map { it.value })
//                        provider.userActionSelected(dice)
//                        wheelModel.hideWheel()
//                    }
//                )
            }
            return ActionWheelInputDialog(
                owner = request.team!!,
                viewModel = viewModel,
            )
        }

        fun createChooseBlockResultOrReroll(
            provider: UiActionProvider,
            state: Game,
            request: ActionRequest,
            chooseResultAfterReroll: Boolean = false,
        ): UserInputDialog {
            val targetLocation = state.getContext<BlockContext>().defender.location as FieldCoordinate
            val viewModel = ActionWheelViewModel(
                team = request.team!!,
                center = targetLocation,
                startHoverText = "Select Block Result",
                fallbackToShowStartHoverText = false,
            ).also { wheelModel ->
                var diceLocked = true
                wheelModel.topMenu.let { menu ->
                    val dice = state.getContext<BlockContext>().roll
                    dice.forEach { die ->
                        menu.addDiceButton(
                            id = die.id,
                            diceValue = die.result,
                            options = DBlockResult.allOptions(),
                            preferLtr = true,
                            expandable = !diceLocked,
                            onClick = { dieResult ->
                                val actions = mutableListOf<GameAction>()
                                var undo = false
                                if (dieResult != die.result) {
                                    undo = true
                                    actions.add(DiceRollResults(menu.menuItems.filterIsInstance<DiceMenuItem<*>>().map { it.value}))
                                }
                                if (chooseResultAfterReroll) {
                                    actions.add(DicePoolResultsSelected(listOf(DicePoolChoice(0, listOf(dieResult)))))
                                } else {
                                    actions.add(NoRerollSelected())
                                    actions.add(DicePoolResultsSelected(listOf(DicePoolChoice(0, listOf(dieResult)))))
                                }
                                if (undo) {
                                    provider.userMultipleActionsSelected(listOf(Undo, CompositeGameAction(actions)), false)
                                } else {
                                    provider.userActionSelected(CompositeGameAction(actions))
                                }
                            },
                            animatingFrom = DBlockResult.random(),
                            onHover = { dblock ->
                                dblock?.blockResult?.description
                            }
                        )
                    }
                }

                val rerollActionButtons = request.filterIsInstance<SelectRerollOption>().firstOrNull()?.let { rerollOption ->
                    rerollOption.options.map { option ->
                        val rerollSource = option.getRerollSource(state)
                        wheelModel.bottomMenu.addActionButton(
                            label = { rerollSource.rerollDescription },
                            icon = ActionIcon.TEAM_REROLL,
                            enabled = diceLocked,
                            onClick = { _, _ ->
                                provider.userActionSelected(RerollOptionSelected(option))
                            }
                        )
                    }
                }
                if (state.rules.undoActionBehavior == UndoActionBehavior.ALLOWED) {
                    wheelModel.bottomMenu.addActionButton(
                        label = { "Select Dice Values" },
                        icon = ActionIcon.CANCEL,
                        enabled = true,
                        onClick = { _, button ->
                            when (diceLocked) {
                                true -> {
                                    diceLocked = false
                                    rerollActionButtons?.forEach { it.enabled = false }
                                    wheelModel.topMenu.menuItems.forEach {
                                        if (it is DiceMenuItem<*>) {
                                            it.expandable = true
                                        }
                                    }
                                    (button as ActionMenuItem).apply {
                                        label = { "Lock Dice Roll" }
                                        icon = ActionIcon.CONFIRM
                                    }
                                }
                                false -> {
                                    diceLocked = true
                                    rerollActionButtons?.forEach { it.enabled = true }
                                    wheelModel.topMenu.menuItems.forEach {
                                        if (it is DiceMenuItem<*>) {
                                            it.expandable = false
                                        }
                                    }
                                    (button as ActionMenuItem).apply {
                                        label = { "Select Dice Values" }
                                        icon = ActionIcon.CANCEL
                                    }
                                }
                            }
                        }
                    )
                }
            }
            return ActionWheelInputDialog(
                owner = request.team!!,
                viewModel = viewModel,
            )
        }

        fun createFollowupDialog(
            provider: UiActionProvider,
            request: ActionRequest,
            player: Player,
        ): UserInputDialog {
            val viewModel = ActionWheelViewModel(
                team = request.team!!,
                center = player.coordinates,
                startHoverText = "Follow Up?",
                bottomExpandMode = MenuExpandMode.TWO_WAY,
                fallbackToShowStartHoverText = false,
            ).also { wheelModel ->
                wheelModel.bottomMenu.addActionButton(
                    label = { "Stay" },
                    icon = ActionIcon.STAY,
                    enabled = true,
                    onClick = { _, _ ->
                        provider.userActionSelected(Cancel)
                        wheelModel.hideWheel(actionSelected = true)
                    }
                )
                wheelModel.bottomMenu.addActionButton(
                    label = { "Follow Up" },
                    icon = ActionIcon.FOLLOW_UP,
                    enabled = true,
                    onClick = { _, _ ->
                        provider.userActionSelected(Confirm)
                        wheelModel.hideWheel(actionSelected = true)
                    }
                )
            }
            return ActionWheelInputDialog(
                owner = request.team!!,
                viewModel = viewModel,
            )
        }



        // -----  HELPERS -----

        // Create a Dialog for using a Skill. This is always a Yes/No answer
        fun createUseSkillDialog(
            provider: UiActionProvider,
            player: Player,
            skill: Skill,
        ): UserInputDialog {
            val message = "Use ${skill.name}?"
            val viewModel = ActionWheelViewModel(
                team = player.team,
                center = player.coordinates,
                startHoverText = null,
                bottomExpandMode = MenuExpandMode.TWO_WAY,
                fallbackToShowStartHoverText = false,
            ).also { wheelModel ->
                wheelModel.topMessage = message
                wheelModel.bottomMenu.addActionButton(
                    label = { "No" },
                    icon = ActionIcon.CANCEL,
                    enabled = true,
                    onClick = { _, _ ->
                        provider.userActionSelected(Cancel)
                        wheelModel.hideWheel(actionSelected = true)
                    }
                )
                wheelModel.bottomMenu.addActionButton(
                    label = { "Yes" },
                    icon = ActionIcon.CONFIRM,
                    enabled = true,
                    onClick = { _, _ ->
                        provider.userActionSelected(Confirm)
                        wheelModel.hideWheel(actionSelected = true)
                    }
                )
            }
            return ActionWheelInputDialog(
                owner = player.team,
                viewModel = viewModel,
            )
        }
    }
}
