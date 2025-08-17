package com.jervisffb.ui.game.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.jervisffb.engine.actions.DBlockResult
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.DicePool
import com.jervisffb.engine.actions.DicePoolChoice
import com.jervisffb.engine.actions.DicePoolResultsSelected
import com.jervisffb.engine.rules.bb2020.procedures.DieRoll
import com.jervisffb.ui.game.dialogs.DicePoolUserInputDialog
import com.jervisffb.ui.game.icons.IconFactory
import com.jervisffb.ui.game.viewmodel.DialogsViewModel
import kotlin.random.Random

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DicePoolSelectorDialog(
    dialog: DicePoolUserInputDialog,
    vm: DialogsViewModel,
) {
    val diceBackground = JervisTheme.diceBackground
    var showDialog by remember(dialog) { mutableStateOf(true) }
    if (showDialog) {
        // TODO Support multiple choices, right now the UI only support one dice pr pool
        val selectedRollIndex = remember(dialog) {
            mutableStateListOf<Int>(*dialog.dice.map { Random.nextInt(it.second.dice.size) }.toTypedArray())
        }
        AlertDialog(
            modifier = Modifier.border(4.dp, when {
                dialog.owner?.isHomeTeam() == true -> JervisTheme.homeTeamColor
                dialog.owner?.isAwayTeam() == true -> JervisTheme.awayTeamColor
                else -> Color.Green
            }, shape = RoundedCornerShape(4.dp)),
            onDismissRequest = {
                showDialog = false
            },
            title = { Text(text = dialog.dialogTitle) },
            text = {
                Column(modifier = Modifier.padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = dialog.message)
                    dialog.dice.forEachIndexed { poolIndex, el: Pair<Dice, DicePool<*, *>> ->
                        Divider(modifier = Modifier.height(1.dp).padding(top = 8.dp, bottom = 8.dp).background(color = Color.LightGray))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val dicePool = el.second
                            dicePool.dice.forEachIndexed { diceIndex, el: DieRoll<*> ->
                                val isSelected = remember(dialog) { derivedStateOf { selectedRollIndex[poolIndex] == diceIndex } }
                                when (val diceResult = el.result) {
                                    is DBlockResult -> {
                                        val buttonColors =
                                            ButtonDefaults.buttonColors(
                                                containerColor = if (isSelected.value) MaterialTheme.colorScheme.primary else diceBackground,
                                            )
                                        val text = diceResult.blockResult.name
                                        Button(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1.0f)
                                            ,
                                            onClick = { selectedRollIndex[poolIndex] = diceIndex },
                                            colors = buttonColors,
                                        ) {
                                            Image(
                                                modifier = Modifier.fillMaxSize(),
                                                bitmap = IconFactory.getDiceIcon(diceResult),
                                                contentDescription = text,
                                                alignment = Alignment.Center,
                                                contentScale = ContentScale.Fit
                                            )
                                        }
                                    }
                                    else -> {
                                        val buttonColors =
                                            ButtonDefaults.buttonColors(
                                                containerColor = if (isSelected.value) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background,
                                            )
                                        Button(
                                            modifier = Modifier.weight(1f),
                                            onClick = { selectedRollIndex[poolIndex] = diceIndex },
                                            colors = buttonColors,
                                        ) {
                                            Text(
                                                text = diceResult.value.toString(),
                                                color = if (isSelected.value) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground,
                                            )
                                        }                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false
                        val selectedResultsAction = DicePoolResultsSelected(
                            dialog.dice.mapIndexed { i, el: Pair<Dice, DicePool<*, *>> ->
                                val selectedRoll = el.second.dice[selectedRollIndex[i]]
                                DicePoolChoice(el.second.id, listOf(selectedRoll.result))
                            }
                        )
                        vm.userActionSelected(selectedResultsAction)
                    },
                    enabled = true // (selectedRolls.size == dialog.dice.size) && !selectedRolls.contains(null),
                ) {
                    Text("Confirm")
                }
            },
            properties =
                DialogProperties(
                    usePlatformDefaultWidth = true,
                    scrimColor = Color.Black.copy(alpha = 0.6f),
                ),
        )
    }
}
