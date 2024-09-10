
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
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
import dk.ilios.jervis.actions.DBlockResult
import dk.ilios.jervis.actions.Dice
import dk.ilios.jervis.actions.DiceResults
import dk.ilios.jervis.actions.DieResult
import dk.ilios.jervis.ui.images.IconFactory
import dk.ilios.jervis.ui.viewmodel.DialogsViewModel
import dk.ilios.jervis.ui.viewmodel.DiceRollUserInputDialog
import dk.ilios.jervis.ui.viewmodel.SingleChoiceInputDialog

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun UserActionDialog(
    dialog: SingleChoiceInputDialog,
    vm: DialogsViewModel,
) {
    AlertDialog(
        modifier = Modifier.border(4.dp, when {
            dialog.owner?.isHomeTeam() == true -> Color.Red
            dialog.owner?.isAwayTeam() == true -> Color.Blue
            else -> Color.Green
        }, shape = RoundedCornerShape(4.dp)),
        onDismissRequest = {},
        title = { Text(text = dialog.title) },
        text = { Text(text = dialog.message) },
        confirmButton = {
            dialog.actionDescriptions.forEach { (action, description) ->
                Button(
                    onClick = { vm.buttonActionSelected(action) },
                ) {
                    Text(text = description)
                }
            }
        },
        properties =
            DialogProperties(
                usePlatformDefaultWidth = true,
                scrimColor = Color.Black.copy(alpha = 0.6f),
            ),
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MultipleSelectUserActionDialog(
    dialog: DiceRollUserInputDialog,
    vm: DialogsViewModel,
) {
    var showDialog by remember(dialog) { mutableStateOf(true) }
    if (showDialog) {
        val selectedRolls = remember(dialog) {
            mutableStateListOf<DieResult?>(*dialog.dice.map { vm.diceGenerator.rollDie(it.first) }.toTypedArray())
        }
        val result = DiceResults(selectedRolls.filterNotNull())
        val resultText = if (result.rolls.size < dialog.dice.size) null else dialog.result(result)
        AlertDialog(
            modifier = Modifier.border(4.dp, when {
                dialog.owner?.isHomeTeam() == true -> Color.Red
                dialog.owner?.isAwayTeam() == true -> Color.Blue
                else -> Color.Green
            }, shape = RoundedCornerShape(4.dp)),
            onDismissRequest = {
                showDialog = false
            },
            title = { Text(text = dialog.title) },
            text = {
                Column(modifier = Modifier.padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = dialog.message)
                    dialog.dice.forEachIndexed { i, el: Pair<Dice, List<DieResult>> ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            el.second.forEach { it: DieResult ->
                                val isSelected = remember(dialog) { derivedStateOf { selectedRolls[i] == it } }
                                val buttonColors =
                                    ButtonDefaults.buttonColors(
                                        backgroundColor = if (isSelected.value) MaterialTheme.colors.primary else MaterialTheme.colors.background,
                                    )

                                when(it) {
                                    is DBlockResult -> {
                                        val text = it.blockResult.name
                                        Button(
                                            modifier = Modifier.weight(1f).aspectRatio(1.0f),
                                            onClick = { selectedRolls[i] = it },
                                            colors = buttonColors,
                                        ) {
                                            Image(
                                                modifier = Modifier.fillMaxSize(),
                                                bitmap = IconFactory.getDiceIcon(it.blockResult),
                                                contentDescription = text,
                                                alignment = Alignment.Center,
                                                contentScale = ContentScale.Fit
                                            )
                                        }
                                    }
                                    else -> {
                                        Button(
                                            modifier = Modifier.weight(1f),
                                            onClick = { selectedRolls[i] = it },
                                            colors = buttonColors,
                                        ) {
                                            Text(
                                                text = it.value.toString(),
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
                        vm.buttonActionSelected(result)
                    },
                    enabled = (selectedRolls.size == dialog.dice.size) && !selectedRolls.contains(null),
                ) {
                    val suffix by derivedStateOf { if (resultText != null) " - $resultText" else "" }
                    Text("Confirm$suffix")
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
//    showDiceDialog(dialog, selectedRolls, vm, result, resultText)
//
//// TODO Figure out how to do recomposition correctly here?
//// TODO Looks like the default AlertDialog doesn't really support this very well. At least when it scales.
////  Looks like a custom implementation is needed. Also make it draggable
//@OptIn(ExperimentalComposeUiApi::class)
//@Composable
//private fun showDiceDialog(
//    dialog: DiceRollUserInputDialog,
//    selectedRolls: SnapshotStateList<DieResult?>,
//    vm: DialogsViewModel,
//    result: DiceResults,
//    resultText: String?,
//) {
//    var showDialog by remember { mutableStateOf(true) }
//    if (showDialog) {
//        AlertDialog(
//            onDismissRequest = {
//                showDialog = false
//            },
//            title = { Text(text = dialog.title) },
//            text = {
//                Column(modifier = Modifier.padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
//                    Text(text = dialog.message)
//                    dialog.dice.forEachIndexed { i, el: Pair<Dice, List<DieResult>> ->
//                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//                            el.second.forEach { it: DieResult ->
//                                val isSelected = remember { derivedStateOf { selectedRolls[i] == it } }
//                                val buttonColors =
//                                    ButtonDefaults.buttonColors(
//                                        backgroundColor = if (isSelected.value) MaterialTheme.colors.primary else MaterialTheme.colors.background,
//                                    )
//                                Button(
//                                    modifier = Modifier.weight(1f),
//                                    onClick = { selectedRolls[i] = it },
//                                    colors = buttonColors,
//                                ) {
//                                    Text(
//                                        text = it.value.toString(),
//                                        color = if (isSelected.value) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onBackground,
//                                    )
//                                }
//                            }
//                        }
//                    }
//                }
//            },
//            confirmButton = {
//                Button(
//                    onClick = {
//                        showDialog = false
//                        vm.buttonActionSelected(result)
//                    },
//                    enabled = (selectedRolls.size == dialog.dice.size) && !selectedRolls.contains(null),
//                ) {
//                    val suffix by derivedStateOf { if (resultText != null) " - $resultText" else "" }
//                    Text("Confirm$suffix")
//                }
//            },
//            properties =
//                DialogProperties(
//                    usePlatformDefaultWidth = true,
//                    scrimColor = Color.Black.copy(alpha = 0.6f),
//                ),
//        )
//    }
//}
