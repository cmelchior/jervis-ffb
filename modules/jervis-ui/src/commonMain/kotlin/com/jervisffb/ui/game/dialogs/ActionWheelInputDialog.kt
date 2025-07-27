package com.jervisffb.ui.game.dialogs

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.actions.D3Result
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.D8Result
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.model.DieId
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.Rules
import com.jervisffb.ui.game.dialogs.circle.ActionWheelViewModel
import com.jervisffb.ui.game.dialogs.circle.DiceMenuItem
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
                        it.hideWheel()
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
                val labelGenerator = {

                }
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
                            wheelModel.hoverText.value = d6?.let { getResultLabel(it, d8) }
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
                            wheelModel.hoverText.value = d8?.let { getResultLabel(d6, it) }
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
                        wheelModel.hideWheel()
                    }
                )
            }
            return ActionWheelInputDialog(
                owner = team,
                viewModel = viewModel,
            )
        }

        fun createKickOffEventDialog(provider: UiActionProvider, rules: Rules, team: Team): UserInputDialog {
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
                    val dice1 = wheelModel.topMenu.getDiceButton(0).value
                    val dice2 = wheelModel.topMenu.getDiceButton(1).value
                    val rolls = listOf(dice1, dice2)
                    val description: String =
                        rules.kickOffEventTable.roll(
                            rolls.first() as D6Result,
                            rolls.last() as D6Result,
                        ).description
                    "$description (${rolls.sumOf { it.value }})"
                }
                wheelModel.topMenu.let { menu ->
                    menu.addDiceButton(
                        id = DieId("Kickoff-D6-1"),
                        diceValue = D6Result.random(),
                        options = D6Result.allOptions(),
                        preferLtr = true,
                        expandable = true,
                        animatingFrom = D6Result.random(),
                        onHover = { wheelModel.hoverText.value = labelGenerator() }
                    )
                    menu.addDiceButton(
                        id = DieId("Kickoff-D6-2"),
                        diceValue = D6Result.random(),
                        options = D6Result.allOptions(),
                        preferLtr = true,
                        expandable = true,
                        animatingFrom = D6Result.random(),
                        onHover = { wheelModel.hoverText.value = labelGenerator() }
                    )
                }
                wheelModel.bottomMenu.addActionButton(
                    label = labelGenerator,
                    icon = ActionIcon.CONFIRM,
                    enabled = true,
                    onClick = { parent, button ->
                        val dice = DiceRollResults(wheelModel.topMenu.menuItems.reversed().filterIsInstance<DiceMenuItem<*>>().map { it.value })
                        provider.userActionSelected(dice)
                        wheelModel.hideWheel()
                    }
                )
            }
            return ActionWheelInputDialog(
                owner = team,
                viewModel = viewModel,
            )
        }
    }
}
