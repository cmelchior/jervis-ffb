import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import dk.ilios.jervis.ui.viewmodel.UiActionFactory
import dk.ilios.jervis.ui.viewmodel.UserInputDialog

@Composable
fun UserActionDialog(dialog: UserInputDialog, vm: UiActionFactory) {
    AlertDialog(
        title = { Text(text = dialog.title) },
        text = { Text(text = dialog.message) },
        onDismissRequest = { /* Do nothing */ },
        confirmButton = {
            dialog.actionDescriptions.forEach { (action, description) ->
                Button(
                    onClick = { vm.userSelectedAction(action) }
                ) {
                    Text(text = description)
                }
            }
        },
    )
}
