package com.jervisffb.ui.menu.components.issues

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jervisffb.engine.GameEngineController
import com.jervisffb.ui.IssueTracker
import com.jervisffb.ui.game.dialogs.DialogSize
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.view.JervisTheme.buttonTextColor
import com.jervisffb.ui.game.view.utils.JervisButton
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.game.viewmodel.ReportIssueDialogData
import com.jervisffb.ui.menu.components.JervisDialog
import com.jervisffb.ui.menu.components.JervisOutlinedTextField
import com.jervisffb.ui.menu.components.SimpleSwitch
import com.jervisffb.utils.openUrlInBrowser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Handler for the user to create a bug report. It will use a proxy on the Jervis website
 * to handle the creation of the issue on GitHub and then show the final URL to the user
 * once created.
 *
 * Debug files are uploaded to the website
 */
@Composable
fun CreateIssueDialogComponent(viewModel: MenuViewModel) {
    val dialogData: ReportIssueDialogData by viewModel.isReportIssueDialogVisible.collectAsState()
    if (!dialogData.visible) return
    CreateIssueDialog(
        title = dialogData.title,
        message = dialogData.body,
        error = dialogData.error,
        game = dialogData.gameState,
        onIssueCreated = { url ->
            openUrlInBrowser(url)
            viewModel.hideReportIssueDialog()
        },
        onDismissRequest = {
            viewModel.hideReportIssueDialog()
        }
    )
}

/**
 * Dialog showing that something is still missing to be implemented
 */
@Composable
private fun CreateIssueDialog(
    title: String,
    message: String,
    error: Throwable? = null,
    game: GameEngineController? = null,
    onIssueCreated: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val (descriptionFocus) = FocusRequester.createRefs()
    var title by remember { mutableStateOf(title) }
    var message by remember { mutableStateOf(message) }
    var attachGameDump by remember { mutableStateOf(game != null) }
    var createInProgress by remember { mutableStateOf(false) }
    var createError by remember { mutableStateOf<Throwable?>(null) }
    var bodyScrollOffset by remember { mutableStateOf(0f) }

    LaunchedEffect(createInProgress) {
        if (createInProgress) {
            scope.launch(Dispatchers.Default) {
                IssueTracker.createNewIssue(title, message, error, if (attachGameDump) game else null)
                    .onSuccess { createdIssueUrl ->
                        onIssueCreated(createdIssueUrl)
                        createInProgress = false
                    }
                    .onFailure {
                        createError = it
                        createInProgress = false
                    }
            }
        }
    }

    JervisDialog(
        "Create new issue",
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
        width = DialogSize.LARGE,
        minHeight = JervisTheme.windowSizeDp.height * 0.8f,
        backgroundScrim = true,
        content = { _, textColor ->
            JervisOutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = title,
                onValueChange = {
                    if (it.endsWith("\t")) {
                        descriptionFocus.requestFocus()
                    } else {
                        title = it
                    }
                },
                label = "Title",
                placeholderText = "<Short problem summary>",
            )
            JervisOutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 8.dp)
                    .focusRequester(descriptionFocus)
                    .scrollable(
                        orientation = Orientation.Vertical,
                        state = rememberScrollableState { delta ->
                            bodyScrollOffset += delta
                            delta
                        }
                    )
                ,
                value = message,
                onValueChange = { message = it },
                label = "Description",
                placeholderText = "<Describe the problem>",
            )
            if (game != null) {
                Row {
                    SimpleSwitch(
                        label = "Include a copy of the game state (does not include chat)",
                        isSelected = attachGameDump,
                        isEnabled = true,
                        onSelected = { checked ->
                            attachGameDump = checked
                        }
                    )
                }
            }
            createError?.let { ex ->
                SelectionContainer {
                    Text(
                        text = if (ex.message?.isNotBlank() == true) {
                            "${ex::class.simpleName}: ${ex.message}"
                        } else {
                            "Unknown error: ${ex::class.simpleName}"
                        },
                        color = JervisTheme.rulebookRed,
                    )
                }
            }
        },
        buttons = {
            JervisButton(
                text = "Cancel",
                onClick = { onDismissRequest() },
                buttonColor = JervisTheme.rulebookBlue,
                textColor = buttonTextColor
            )
            Spacer(modifier = Modifier.weight(1f))
            JervisButton(
                text = if (createInProgress) "Creating..." else "Create Issue",
                enabled = !createInProgress && title.isNotBlank() && message.isNotBlank(),
                onClick = {
                    createInProgress = true
                },
                buttonColor = JervisTheme.rulebookBlue,
                textColor = buttonTextColor
            )
        },
        onDismissRequest = onDismissRequest,
    )
}
