import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import dk.ilios.jervis.ui.model.ActionSelectorViewModel
import dk.ilios.jervis.ui.model.UserInputDialog

@Composable
fun UserActionDialog(dialog: UserInputDialog, vm: ActionSelectorViewModel) {
    AlertDialog(
        title = { Text(text = dialog.title) },
        text = { Text(text = dialog.message) },
        onDismissRequest = { /* Do nothing */ },
        confirmButton = {
            dialog.actionDescriptions.forEach { (action, description) ->
                Button(
                    onClick = { vm.actionSelected(action) }
                ) {
                    Text(text = description)
                }
            }
        },
    )
}
