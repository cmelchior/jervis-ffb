package com.jervisffb.ui.menu.components.setup

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jervisffb.jervis_ui.generated.resources.Res
import com.jervisffb.jervis_ui.generated.resources.icon_menu_folder
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.view.utils.TitleBorder
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LoadFileComponent(viewModel: LoadFileComponentModel) {
    val filePath by viewModel.filePath.collectAsState()
    val loadErrror by viewModel.fileError.collectAsState()
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(modifier = Modifier.width(600.dp).padding(bottom = 100.dp)) {
            LoadFileHeader()
            Spacer(modifier = Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = filePath,
                    onValueChange = { /* Do nothing */ },
                    readOnly = true,
                    singleLine = true,
                    label = { Text("Save File") },
                )
                Box(
                    modifier = Modifier
                        .padding(start = 4.dp, top = 16.dp, bottom = 8.dp)
                        .size(48.dp)
                        .offset(x = 4.dp)
                        .clip(shape = RoundedCornerShape(4.dp))
                        .clickable { viewModel.openFileDialog() }
                    ,
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        modifier = Modifier.fillMaxSize(0.8f).aspectRatio(1f),
                        colorFilter = ColorFilter.tint(JervisTheme.rulebookRed) ,
                        painter = painterResource(Res.drawable.icon_menu_folder),
                        contentDescription = "Find Save File",
                    )
                }
            }
            if (loadErrror != null) {
                Row(Modifier.fillMaxWidth().padding(top = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = loadErrror,
                        color = JervisTheme.rulebookRed,
                        fontSize = 14.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadFileHeader(color: Color = JervisTheme.rulebookRed) {
    TitleBorder(color)
    Box(
        modifier = Modifier.height(36.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            modifier = Modifier.padding(bottom = 2.dp),
            text = "Select Save File",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = color
        )
    }
    TitleBorder(color)
}
