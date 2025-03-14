package com.jervisffb.ui.menu.components.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jervisffb.ui.menu.components.JervisDropDownMenu
import com.jervisffb.ui.menu.components.JervisDropdownMenuWithSections
import com.jervisffb.ui.menu.components.SimpleSwitch
import com.jervisffb.ui.menu.components.SmallHeader

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SetupRulesComponent(viewModel: RulesSetupComponentModel) {

    val scrollState = rememberScrollState()

    val selectedWeatherTable by viewModel.selectedWeatherTable.collectAsState()
    val selectedKickOffTable by viewModel.selectedKickOffTable.collectAsState()

    val selectedPitchEntry by viewModel.selectedPitch.collectAsState()
    val selectedUnusualBallEntry by viewModel.selectedUnusualBall.collectAsState()

    val selectedStadia by viewModel.selectedStadium.collectAsState()

    val prayersToNuffleEnabled by viewModel.prayersToNuffle.collectAsState()
    val matchEventsEnabled by viewModel.matchEvents.collectAsState()
    val extraTimeEnabled by viewModel.extraTime.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize().padding(top = 16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.width(750.dp).padding(top = 16.dp).verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Box(modifier = Modifier, contentAlignment = Alignment.CenterEnd) {
                    JervisDropDownMenu(
                        title = "Rules Preset",
                        entries = presets,
                        enabled = true,
                    ) {
                        // viewModel.updatePreset(it)
                    }
                }
            }
            Row(
                modifier = Modifier.padding(top = 16.dp),
            ) {
                Column(
                    modifier = Modifier.weight(1f).wrapContentSize()
                ) {
                    SmallHeader("Tables", bottomPadding = smallHeaderBottomPadding)
                    // BoxHeader("Tables", bottomPadding = 16.dp)
                    JervisDropdownMenuWithSections(
                        title = "Weather Table",
                        entries = viewModel.weatherTables,
                        selectedEntry = selectedWeatherTable
                    ) {
                        viewModel.updateWeatherTable(it)
                    }
                    JervisDropdownMenuWithSections(
                        title = "Kick-off Table",
                        entries = viewModel.kickOffTables,
                        selectedEntry = selectedKickOffTable
                    ) {
                        viewModel.updateKickoffTable(it)
                    }

                    SmallHeader("Stadia", topPadding = smallHeaderTopPadding, bottomPadding = smallHeaderBottomPadding)
                    JervisDropdownMenuWithSections(
                        title = "Stadia of the Old World",
                        entries = viewModel.stadia,
                        selectedEntry = selectedStadia
                    ) {
                        viewModel.updateStadium(it)
                    }
                }
                Spacer(modifier = Modifier.width(24.dp))
                Column(
                    modifier = Modifier.weight(1f).wrapContentSize()
                ) {
                    SmallHeader("Pitch and Ball", bottomPadding = smallHeaderBottomPadding)
                    JervisDropdownMenuWithSections(
                        title = "Pitch",
                        entries = viewModel.pitches,
                        selectedEntry = selectedPitchEntry,
                    ) {
                        viewModel.updatePitch(it)
                    }
                    JervisDropdownMenuWithSections(
                        title = "Ball",
                        entries = viewModel.unusualBallList,
                        selectedEntry = selectedUnusualBallEntry,
                    ) {
                        viewModel.updateUnusualBall(it)
                    }

                    SmallHeader("Events", topPadding = smallHeaderTopPadding, bottomPadding = smallHeaderBottomPadding)
                    SimpleSwitch("Prayers To Nuffle", prayersToNuffleEnabled) {
                        viewModel.updatePrayersToNuffle(it)
                    }
                    SimpleSwitch("Match Events", matchEventsEnabled) {
                        viewModel.updateMatchEvents(it)
                    }
                    SimpleSwitch("Extra Time", extraTimeEnabled) {
                        viewModel.updateExtraTime(it)
                    }
                }
            }
        }
    }
}

