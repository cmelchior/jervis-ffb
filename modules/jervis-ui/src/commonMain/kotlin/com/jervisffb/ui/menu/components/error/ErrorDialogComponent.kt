package com.jervisffb.ui.menu.components.error

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.jervisffb.ui.IssueTracker
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.view.JervisTheme.buttonTextColor
import com.jervisffb.ui.game.view.utils.JervisButton
import com.jervisffb.ui.game.viewmodel.ErrorDialog
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.components.JervisDialog
import com.jervisffb.utils.openUrlInBrowser

/**
 * Handles showing a system error to the user. This includes showing the error
 * as well as having an "Report Issue" button.
 */
@Composable
fun ErrorDialogComponent(viewModel: MenuViewModel) {
    val dialogData: ErrorDialog by viewModel.isErrorDialogVisible.collectAsState()
    if (!dialogData.visible) return
    ErrorDialog(
        title = dialogData.title,
        message = dialogData.error?.message ?: "An unknown error has occurred.",
        showReportIssue = (dialogData.error != null),
        onReportIssueRequest = {
            val reportUrl = createGithubUrl(dialogData)
            openUrlInBrowser(reportUrl)
        },
        onDismissRequest = {
            viewModel.hideErrorDialog()
        }
    )
}

private fun createGithubUrl(dialogData: ErrorDialog): String {
    return if (dialogData.error != null) {
        IssueTracker.createIssueUrlFromException(
            dialogData.title,
            "",
            dialogData.error
        )
    } else {
        IssueTracker.createIssueUrl(
            dialogData.title,
            "",
            IssueTracker.Label.USER
        )
    }
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
    onDismissRequest: () -> Unit
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
            if (!showReportIssue) {
                Spacer(modifier = Modifier.weight(1f))
            }
            JervisButton(
                text = "Close",
                onClick = { onDismissRequest() },
                buttonColor = JervisTheme.rulebookBlue,
                textColor = buttonTextColor
            )
            if (showReportIssue) {
                Spacer(modifier = Modifier.weight(1f))
                JervisButton(
                    text = "Report Issue",
                    onClick = { onReportIssueRequest() },
                    buttonColor = JervisTheme.rulebookBlue,
                    textColor = buttonTextColor
                )
            }
        },
        onDismissRequest = onDismissRequest,
    )
}
