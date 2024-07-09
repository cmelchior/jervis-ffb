
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import dk.ilios.jervis.actions.Dice
import dk.ilios.jervis.actions.DiceResults
import dk.ilios.jervis.actions.DieResult
import dk.ilios.jervis.ui.viewmodel.DialogsViewModel
import dk.ilios.jervis.ui.viewmodel.DiceRollUserInputDialog
import dk.ilios.jervis.ui.viewmodel.SingleChoiceInputDialog

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun UserActionDialog(dialog: SingleChoiceInputDialog, vm: DialogsViewModel) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text(text = dialog.title) },
        text = { Text(text = dialog.message) },
        confirmButton = {
            dialog.actionDescriptions.forEach { (action, description) ->
                Button(
                    onClick = { vm.buttonActionSelected(action) }
                ) {
                    Text(text = description)
                }
            }
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = true,
            scrimColor = Color.Black.copy(alpha = 0.6f)
        )
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MultipleSelectUserActionDialog(dialog: DiceRollUserInputDialog, vm: DialogsViewModel) {
    val selectedRolls = remember { mutableStateListOf<DieResult?>(*arrayOfNulls(dialog.dice.size)) }
    val result by derivedStateOf { DiceResults(selectedRolls.filterNotNull()) }
    val resultText by derivedStateOf { if (result.rolls.size < dialog.dice.size) null else dialog.result(result) }
    showDialog(dialog, selectedRolls, vm, result, resultText)
}

// TODO Figure out how to do recomposition correctly here?
@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun showDialog(
    dialog: DiceRollUserInputDialog,
    selectedRolls: SnapshotStateList<DieResult?>,
    vm: DialogsViewModel,
    result: DiceResults,
    resultText: String?
) {
    AlertDialog(
        onDismissRequest = {  },
        title = { Text(text = "Select Options") },
        text = {
            Column {
                dialog.dice.forEachIndexed { i, el: Pair<Dice, List<DieResult>> ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        el.second.forEach { it: DieResult ->
                            val isSelected = remember { derivedStateOf { selectedRolls[i] == it } }
                            val buttonColors = ButtonDefaults.buttonColors(
                                backgroundColor = if (isSelected.value) MaterialTheme.colors.primary else MaterialTheme.colors.background
                            )
                            Button(
                                modifier = Modifier.weight(1f),
                                onClick = { selectedRolls[i] = it },
                                colors = buttonColors
                            ) {
                                Text(
                                    text = it.result.toString(),
                                    color = if (isSelected.value) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onBackground
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                   // showDialog1 = false
                    vm.buttonActionSelected(result)

                },
                enabled = (selectedRolls.size == dialog.dice.size) && !selectedRolls.contains(null)
            ) {
                val suffix by derivedStateOf { if (resultText != null) " - [${result.rolls.sumOf { it.result }}] $resultText" else "" }
                Text("Confirm$suffix")
            }
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = true,
            scrimColor = Color.Black.copy(alpha = 0.6f)
        )
    )
}
