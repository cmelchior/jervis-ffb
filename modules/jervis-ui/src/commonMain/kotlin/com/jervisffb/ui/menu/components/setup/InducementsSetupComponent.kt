package com.jervisffb.ui.menu.components.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jervisffb.ui.formatCurrency
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.menu.components.JervisSwitch
import com.jervisffb.ui.menu.components.SmallHeader

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InducementsSetupComponent(viewModel: InducementsSetupComponentModel) {

    val rulebookInducements = viewModel.rulebookInducements
    val deathZoneInducements = viewModel.deathZoneInducements

    Box(
        modifier = Modifier.fillMaxSize().padding(top = 16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.width(750.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            SmallHeader("Rulebook", bottomPadding = 0.dp)
            Spacer(modifier = Modifier.height(8.dp))
            rulebookInducements.forEachIndexed { rowNo, inducement ->
                InducementRow(rowNo, inducement, { enabled ->
                    viewModel.updateStandardInducementEnabled(inducement.type, enabled)
                })
            }
            SmallHeader("Death Zone", topPadding = smallHeaderTopPadding, bottomPadding = 0.dp)
            Spacer(modifier = Modifier.height(8.dp))
            deathZoneInducements.forEachIndexed { rowNo, inducement ->
                InducementRow(rowNo, inducement) { enabled ->
                    viewModel.updateDeathZoneInducementEnabled(inducement.type, enabled)
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.InducementRow(rowNo: Int, inducement: InducementData, onCheckChanged: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = if (rowNo % 2 == 0) Color.Transparent else JervisTheme.rulebookPaperMediumDark)
            .padding(bottom = 8.dp, top = 4.dp, end = 8.dp)
        ,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(modifier = Modifier.width(70.dp), contentAlignment = Alignment.CenterStart) {
            JervisSwitch(
                enabled = true,
                checked = inducement.enabled,
                onCheckedChange = { onCheckChanged(it) }
            )
        }
        Text(modifier = Modifier.weight(1f).wrapContentHeight(align = Alignment.CenterVertically), text = inducement.name, textAlign = TextAlign.Start)
        OutlinedTextField(
            modifier = Modifier.width(70.dp).wrapContentHeight().align(Alignment.CenterVertically),
            value = if (inducement.max == Int.MAX_VALUE) "-" else inducement.max.toString(),
            onValueChange = {  },
            readOnly = true,
            enabled = false,
            maxLines = 1,
            label = { Text("Limit") }
        )
        OutlinedTextField(
            modifier = Modifier.width(125.dp),
            value = if (inducement.price == null) "Varies" else formatCurrency(inducement.price!!),
            onValueChange = {  },
            readOnly = true, // (it.price == null),
            enabled = false, // (it.price != null),
            maxLines = 1,
            label = { Text("Price") }
        )
        // Hide for now until we can figure out the UI
//        if (it.price == null) {
//            Box(Modifier.width(160.dp), contentAlignment = Alignment.Center) {
//                Box(modifier = Modifier.padding(end = 8.dp, top = 8.dp)) {
//                    JervisButton(text ="Settings", {})
//                }
//            }
//        }
    }
}
