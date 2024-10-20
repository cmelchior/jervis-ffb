package com.jervisffb.ui

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
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
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
import com.jervisffb.ui.images.IconFactory
import com.jervisffb.ui.viewmodel.DialogsViewModel
import com.jervisffb.ui.userinput.DicePoolUserInputDialog
import kotlin.random.Random

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DicePoolSelectorDialog(
    dialog: DicePoolUserInputDialog,
    vm: DialogsViewModel,
) {
    var showDialog by remember(dialog) { mutableStateOf(true) }
    if (showDialog) {
        // TODO Support multiple choices, right now the UI only support one dice pr pool
        val selectedRollIndex = remember(dialog) {
            mutableStateListOf<Int>(*dialog.dice.map { Random.nextInt(it.second.dice.size) }.toTypedArray())
        }
        AlertDialog(
            modifier = Modifier.border(4.dp, when {
                dialog.owner?.isHomeTeam() == true -> Theme.homeTeamColor
                dialog.owner?.isAwayTeam() == true -> Theme.awayTeamColor
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
                                val buttonColors =
                                    ButtonDefaults.buttonColors(
                                        backgroundColor = if (isSelected.value) MaterialTheme.colors.primary else MaterialTheme.colors.background,
                                    )

                                when(val diceResult = el.result) {
                                    is DBlockResult -> {
                                        val text = diceResult.blockResult.name
                                        Button(
                                            modifier = Modifier.weight(1f).aspectRatio(1.0f),
                                            onClick = { selectedRollIndex[poolIndex] = diceIndex },
                                            colors = buttonColors,
                                        ) {
                                            Image(
                                                modifier = Modifier.fillMaxSize(),
                                                bitmap = IconFactory.getDiceIcon(diceResult.blockResult),
                                                contentDescription = text,
                                                alignment = Alignment.Center,
                                                contentScale = ContentScale.Fit
                                            )
                                        }
                                    }
                                    else -> {
                                        Button(
                                            modifier = Modifier.weight(1f),
                                            onClick = { selectedRollIndex[poolIndex] = diceIndex },
                                            colors = buttonColors,
                                        ) {
                                            Text(
                                                text = diceResult.value.toString(),
                                                color = if (isSelected.value) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onBackground,
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
                        vm.buttonActionSelected(selectedResultsAction)
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
