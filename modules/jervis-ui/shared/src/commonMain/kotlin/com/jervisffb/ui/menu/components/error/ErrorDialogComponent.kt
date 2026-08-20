package com.jervisffb.ui.menu.components.error

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.view.JervisTheme.buttonTextColor
import com.jervisffb.ui.game.view.utils.JervisButton
import com.jervisffb.ui.game.viewmodel.ErrorDialog
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.components.JervisDialog

/**
 * Handles showing a system error to the user. This includes showing the error
 * as well as having an "Report Issue" button.
 */
@Composable
fun ErrorDialogComponent(viewModel: MenuViewModel) {
    val dialogData: ErrorDialog by viewModel.isErrorDialogVisible.collectAsState()
    if (!dialogData.visible) return
    val message = dialogData.message ?: dialogData.error?.message ?: "An unknown error has occurred."
    val dismiss = dialogData.onDismiss ?: { viewModel.hideErrorDialog() }
    ErrorDialog(
        title = dialogData.title,
        message = message,
        showReportIssue = (dialogData.error != null),
        onReportIssueRequest = {
            viewModel.showReportIssueDialog(
                title = dialogData.title,
                body = message,
                error = dialogData.error
            )
        },
        onDismissRequest = dismiss,
        onDialogDismissRequest = if (dialogData.onDismiss == null) dismiss else { {} },
        dismissButtonText = dialogData.dismissButtonText,
        secondaryAction = dialogData.secondaryAction,
        secondaryButtonText = dialogData.secondaryButtonText,
    )
}

/**
 * Dialog showing that something is still missing to be implemented
 */
@Composable
private fun ErrorDialog(
    title: String,
    message: String,
    showReportIssue: Boolean,
    onReportIssueRequest: () -> Unit,
    onDismissRequest: () -> Unit,
    onDialogDismissRequest: () -> Unit,
    dismissButtonText: String,
    secondaryAction: (() -> Unit)?,
    secondaryButtonText: String,
) {
    JervisDialog(
        title,
        icon = {
            Text(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                text = "!",
                fontFamily = JervisTheme.fontFamily(),
                color = JervisTheme.white,
                textAlign = TextAlign.Center,
                fontSize = 100.sp,
                fontWeight = FontWeight.Bold,
            )
        },
        width = 650.dp,
        backgroundScrim = true,
        content = { _, textColor ->
            SelectionContainer {
                Text(
                    text = message,
                    color = textColor
                )
            }
        },
        buttons = {
            Spacer(modifier = Modifier.weight(1f))
            if (secondaryAction != null) {
                JervisButton(
                    text = secondaryButtonText,
                    onClick = secondaryAction,
                    buttonColor = JervisTheme.rulebookBlue,
                    textColor = buttonTextColor
                )
                Spacer(modifier = Modifier.width(16.dp))
            }
            if (showReportIssue) {
                JervisButton(
                    text = "Report Issue",
                    onClick = { onReportIssueRequest() },
                    buttonColor = JervisTheme.rulebookBlue,
                    textColor = buttonTextColor
                )
                Spacer(modifier = Modifier.width(16.dp))
            }
            JervisButton(
                text = dismissButtonText,
                onClick = { onDismissRequest() },
                buttonColor = JervisTheme.rulebookBlue,
                textColor = buttonTextColor
            )
        },
        onDismissRequest = onDialogDismissRequest,
    )
}
