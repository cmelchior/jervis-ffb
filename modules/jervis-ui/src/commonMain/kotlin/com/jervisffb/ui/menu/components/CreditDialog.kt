package com.jervisffb.ui.menu.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.view.JervisTheme.buttonTextColor
import com.jervisffb.ui.game.view.utils.JervisButton
import com.jervisffb.ui.game.view.utils.TitleBorder
import com.jervisffb.ui.game.view.utils.bannerBackground
import com.jervisffb.ui.game.view.utils.paperBackground
import com.jervisffb.ui.menu.intro.CreditData
import com.jervisffb.ui.menu.intro.FrontpageScreenModel
import com.jervisffb.utils.openUrlInBrowser

/**
 * Credit/Version dialog shown when the version from the front page is pressed.
 *
 * Should probably try to unify this and [com.jervisffb.ui.menu.fumbbl.FumbblLoginDialog]
 * into a shared dialog. Create a few more dialogs to get a feel for what needs to differ.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CreditDialog(viewModel: FrontpageScreenModel, creditData: CreditData) {
    val showDialog: Boolean by viewModel.showCreditDialog.collectAsState()
    val dialogColor = JervisTheme.rulebookRed
    val textColor = JervisTheme.contentTextColor

    if (!showDialog) return

    Dialog(
        onDismissRequest = { viewModel.showCreditDialog(false) },
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        ),
    ) {
        Surface(
            elevation = 8.dp,
            modifier = Modifier
                .width(850.dp)
                .defaultMinSize(minHeight = 200.dp, minWidth = 850.dp)
                .paperBackground(color = JervisTheme.rulebookPaper)
            ,
            shape = MaterialTheme.shapes.medium,
            border = BorderStroke(8.dp, color = dialogColor),
            color = JervisTheme.rulebookPaper,
            contentColor = textColor,
        ) {
            Row(Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .paperBackground(JervisTheme.rulebookPaper)
            ) {
                Box(modifier = Modifier
                    .width(130.dp)
                    .fillMaxHeight()
                    .padding(start = 24.dp)
                    .bannerBackground()

                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        text = "J",
                        fontFamily = JervisTheme.fontFamily(),
                        color = JervisTheme.white,
                        textAlign = TextAlign.Center,
                        fontSize = 100.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Column(modifier = Modifier
                    .padding(start  = 24.dp, top = 16.dp, end = 24.dp, bottom = 16.dp)
                    .wrapContentHeight()
                ) {
                    CreditDialogContent(
                        dialogColor,
                        textColor,
                        creditData,
                        onCancel = {
                            viewModel.showCreditDialog(false)
                        },
                        reportIssue = { url ->
                            openUrlInBrowser(url)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.CreditDialogContent(
    dialogColor: Color,
    textColor: Color,
    data: CreditData,
    onCancel: () -> Unit = {},
    reportIssue: (String) -> Unit = {},
) {
    CreditDialogHeader(data.title, dialogColor)
    TitleBorder(dialogColor)
    Column(modifier = Modifier.weight(1f).padding(top = 8.dp)) {
        Row {
            CreditLabel("Version:", "", textColor)
            Spacer(modifier = Modifier.width(8.dp))
            CreditText("${data.clientVersion} (${data.gitCommit})", textColor)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            CreditLabel(
                "Main Developer:",
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
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        JervisButton(
            text = "Close",
            onClick = { onCancel() },
            buttonColor = JervisTheme.rulebookBlue,
            textColor = buttonTextColor
        )
        Spacer(modifier = Modifier.weight(1f))
        JervisButton(
            text = "Report Issue",
            onClick = { reportIssue(data.newIssueUrl) },
            buttonColor = JervisTheme.rulebookBlue,
            textColor = buttonTextColor,
        )
    }
}

@Composable
private fun ColumnScope.CreditDialogHeader(title: String, dialogColor: Color) {
    Box(
        modifier = Modifier.height(36.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            modifier = Modifier.padding(bottom = 2.dp),
            text = title.uppercase(),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = dialogColor
        )
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
