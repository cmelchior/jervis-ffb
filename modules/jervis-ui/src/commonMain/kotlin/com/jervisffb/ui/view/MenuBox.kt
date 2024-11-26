package com.jervisffb.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RowScope.MenuBox(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(24.dp)
            .fillMaxHeight()
            .weight(1f,  false)
            .aspectRatio(1f)
            .background(color = JervisTheme.buttonColor)
            .clickable { onClick() }
        ,
        contentAlignment = Alignment.BottomEnd,

    ) {
        Text(
            modifier = Modifier.padding(16.dp),
            text = label,
            maxLines = 1,
            color = JervisTheme.buttonTextColor,
            fontSize = 48.sp,
        )
    }
}

