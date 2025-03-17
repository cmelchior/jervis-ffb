package com.jervisffb.ui.game.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jervisffb.engine.actions.BlockTypeSelected
import com.jervisffb.engine.actions.CalculatedAction
import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CoinSideSelected
import com.jervisffb.engine.actions.CoinTossResult
import com.jervisffb.engine.actions.CompositeGameAction
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.Continue
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
import com.jervisffb.ui.game.viewmodel.ActionSelectorViewModel

@Composable
fun ActionSelector(
    vm: ActionSelectorViewModel,
    modifier: Modifier,
) {
    val inputs: List<GameAction> by vm.availableActions.collectAsState(emptyList())
    Column(modifier = modifier.padding(8.dp)) {
        Column(
            modifier =
                modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            inputs.forEach { action ->
                Button(
                    modifier = Modifier.padding(0.dp),
                    contentPadding = PaddingValues(2.dp),
                    onClick = { vm.actionSelected(action) },
                ) {
                    val text =
                        when (action) {
                            Confirm -> "Confirm"
                            Continue -> "Continue"
                            is DieResult -> action.toString()
                            DogoutSelected -> "DogoutSelected"
                            EndSetup -> "EndSetup"
                            EndTurn -> "EndTurn"
                            is FieldSquareSelected -> action.toString()
                            is PlayerSelected -> "Player[${action.playerId}]"
                            is DiceRollResults -> action.rolls.joinToString(prefix = "DiceRolls[", postfix = "]")
                            is PlayerActionSelected -> "Action: $action"
                            is PlayerDeselected -> "Deselect active player"
                            EndAction -> "End Action"
                            Cancel -> "Cancel"
                            is CoinSideSelected -> "Selected: ${action.side}"
                            is CoinTossResult -> "Coin flip: ${action.result}"
                            is RandomPlayersSelected -> "Random players: $action"
                            is NoRerollSelected -> "No reroll"
                            is RerollOptionSelected -> action.option.toString()
                            is MoveTypeSelected -> action.moveType.toString()
                            Undo -> TODO()
                            Revert -> TODO()
                            is CompositeGameAction -> action.list.joinToString(prefix = "[", postfix = "]")
                            is PlayerSubActionSelected -> action.action.toString()
                            is SkillSelected -> action.skill.toString()
                            is InducementSelected -> action.name
                            is BlockTypeSelected -> action.type.toString()
                            is CalculatedAction -> TODO("Should only be used in tests")
                            is DicePoolResultsSelected -> "Dice pool: $action"
                            is DirectionSelected -> "Direction: ${action.direction}"
                        }
                    Text(text, fontSize = 10.sp)
                }
            }
        }
    }
}
