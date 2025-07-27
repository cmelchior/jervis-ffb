package com.jervisffb.ui.menu.components.about

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jervisffb.ui.game.dialogs.DialogSize
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.view.JervisTheme.buttonTextColor
import com.jervisffb.ui.game.view.utils.JervisButton
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.components.JervisDialog
import com.jervisffb.ui.menu.intro.CreditData
import com.jervisffb.ui.menu.utils.JervisLogo
import com.jervisffb.utils.openUrlInBrowser

/**
 * "About"-dialog shown when the version from the front page is pressed or the platform "About"
 * menu button is pressed on desktop apps. It also includes credits, version data and a link
 * to Github for creating issues.
 *
 * Should probably try to unify this and [com.jervisffb.ui.menu.fumbbl.FumbblLoginDialog]
 * into a shared dialog. Create a few more dialogs to get a feel for what needs to differ.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AboutDialogComponent(viewModel: MenuViewModel) {
    val creditData = viewModel.creditData
    val showDialog: Boolean by viewModel.isAboutDialogVisible.collectAsState()
    val dialogColor = JervisTheme.rulebookRed
    val textColor = JervisTheme.contentTextColor
    if (!showDialog) return
    JervisDialog(
        title = "About Jervis Fantasy Football",
        icon = { JervisLogo() },
        width = DialogSize.MEDIUM,
        content = { _, _ ->
            CreditDialogContent(
                dialogColor,
                textColor,
                creditData,
            )
        },
        buttons = {
            JervisButton(
                text = "Close",
                onClick = { viewModel.showAboutDialog(false) },
                buttonColor = JervisTheme.rulebookBlue,
                textColor = buttonTextColor
            )
            Spacer(modifier = Modifier.weight(1f))
            JervisButton(
                text = "FUMBBL Credits",
                onClick = { openUrlInBrowser(creditData.fumbblAttributionUrl) },
                buttonColor = JervisTheme.rulebookBlue,
                textColor = buttonTextColor,
            )
            Spacer(modifier = Modifier.width(16.dp))
            JervisButton(
                text = "Report Issue",
                onClick = { openUrlInBrowser(creditData.newIssueUrl) },
                buttonColor = JervisTheme.rulebookBlue,
                textColor = buttonTextColor,
            )
        },
        onDismissRequest = {
            viewModel.showAboutDialog(false)
        }
    )
}

@Composable
private fun ColumnScope.CreditDialogContent(
    dialogColor: Color,
    textColor: Color,
    data: CreditData,
) {
    Column(modifier = Modifier.weight(1f).padding(top = 8.dp)) {
        Row {
            CreditLabel("Version:", "", textColor)
            Spacer(modifier = Modifier.width(8.dp))
            CreditText("${data.clientVersion} (${data.gitCommit})", textColor)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            CreditLabel(
                "Creator:",
                data.mainDeveloperDescription,
                textColor
            )
            Spacer(modifier = Modifier.width(8.dp))
            CreditText(
                data.mainDeveloper,
                textColor
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            CreditLabel(
                "FUMBBL Credits:",
                data.fumbblDevelopersDescription.replace("\n" , ""),
                textColor
            )
            Spacer(modifier = Modifier.width(8.dp))
            CreditText(
                data.fumbblDevelopers.joinToString(", "),
                textColor
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            CreditLabel(
                "Disclaimer:",
                "",
                textColor
            )
            Spacer(modifier = Modifier.width(8.dp))
            CreditText(
                "Blood Bowl is a trademark of Games Workshop Limited, used without permission, used without intent to infringe, or in opposition to their copyright. This project is in no way official and is not endorsed by Games Workshop Limited.",
                textColor
            )
        }
    }
}

@Composable
private fun RowScope.CreditLabel(label: String, description: String, textColor: Color) {
    Column(modifier = Modifier.weight(1.5f)) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = label,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )
        if (description.isNotEmpty()) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = description,
                fontSize = 12.sp,
                color = textColor,
                fontWeight = FontWeight.Normal,
            )
        }
    }
}

@Composable
private fun RowScope.CreditText(text: String, textColor: Color) {
    Text(
        modifier = Modifier.weight(2f),
        text = text,
        fontSize = 14.sp,
        color = textColor,
        fontWeight = FontWeight.Normal,
    )
}
