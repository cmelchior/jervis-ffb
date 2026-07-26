package com.jervisffb.ui.menu.components.setup

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jervisffb.shared.generated.resources.Res
import com.jervisffb.shared.generated.resources.jervis_icon_menu_folder
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.view.utils.TitleBorder
import org.jetbrains.compose.resources.painterResource

@Composable
fun LoadFileComponent(
    viewModel: LoadFileComponentModel,
    width: Dp = 600.dp,
    title: String = "Select Save File",
    hintText: String = "Save File",
    iconDescription: String = "Find Save File",
) {
    val filePath by viewModel.filePath.collectAsState()
    val loadError by viewModel.fileError.collectAsState()
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(modifier = Modifier.width(width).padding(bottom = 100.dp)) {
            LoadFileHeader(title)
            Spacer(modifier = Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = filePath,
                    onValueChange = { /* Do nothing */ },
                    readOnly = true,
                    singleLine = true,
                    label = { Text(hintText) },
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
                        painter = painterResource(Res.drawable.jervis_icon_menu_folder),
                        contentDescription = iconDescription,
                    )
                }
            }
            @Suppress("SENSELESS_COMPARISON")
            if (loadError != null) {
                Row(Modifier.fillMaxWidth().padding(top = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = loadError,
                        color = JervisTheme.rulebookRed,
                        fontSize = 14.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadFileHeader(header: String, color: Color = JervisTheme.rulebookRed) {
    TitleBorder(color)
    Box(
        modifier = Modifier.height(36.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            modifier = Modifier.padding(bottom = 2.dp),
            text = header,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = color
        )
    }
    TitleBorder(color)
}
