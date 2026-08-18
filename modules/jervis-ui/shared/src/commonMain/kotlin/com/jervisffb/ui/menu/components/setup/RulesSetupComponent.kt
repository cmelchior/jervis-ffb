package com.jervisffb.ui.menu.components.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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

@Composable
fun SetupRulesComponent(viewModel: RulesSetupComponentModel) {
    val scrollState = rememberScrollState()
    val availableRuleBases by viewModel.availableRuleBases.collectAsState()
    val selectedRuleBase by viewModel.selectedRuleBase.collectAsState()

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
                        title = "Rules Base",
                        entries = availableRuleBases,
                        selectedEntry = selectedRuleBase,
                        enabled = true,
                    ) {
                        viewModel.updateRulesBase(it)
                    }
                }
            }
            Row(modifier = Modifier.padding(top = 16.dp)) {
                RulesSetupColumn.entries.forEachIndexed { index, column ->
                    if (index > 0) {
                        Spacer(modifier = Modifier.width(24.dp))
                    }
                    RulesSetupSectionsColumn(
                        sections = viewModel.sections.filter { it.column == column },
                        viewModel = viewModel,
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.RulesSetupSectionsColumn(
    sections: List<RulesSetupSection>,
    viewModel: RulesSetupComponentModel,
) {
    val nonEmptySections = sections.filter { it.controls.isNotEmpty() }
    Column(modifier = Modifier.weight(1f).wrapContentSize()) {
        nonEmptySections.forEachIndexed { index, section ->
            SmallHeader(
                title = section.title,
                topPadding = when (index) {
                    0 -> 0.dp
                    else -> smallHeaderTopPadding
                },
                bottomPadding = smallHeaderBottomPadding,
            )
            section.controls.forEach { control ->
                RulesSetupControlComponent(control, viewModel)
            }
        }
    }
}

@Composable
private fun RulesSetupControlComponent(
    control: RulesSetupControl,
    viewModel: RulesSetupComponentModel,
) {
    when (control) {
        RulesSetupControl.WEATHER_TABLE -> {
            val selectedEntry by viewModel.selectedWeatherTable.collectAsState()
            JervisDropdownMenuWithSections(
                title = "Weather Table",
                entries = viewModel.weatherTables,
                selectedEntry = selectedEntry,
            ) {
                viewModel.updateWeatherTable(it)
            }
        }
        RulesSetupControl.PRAYERS_TO_NUFFLE_TABLE -> {
            val selectedEntry by viewModel.selectedPrayersToNuffleTable.collectAsState()
            JervisDropdownMenuWithSections(
                title = "Prayers to Nuffle Table",
                entries = viewModel.prayersToNuffleTables,
                selectedEntry = selectedEntry,
            ) {
                viewModel.updatePrayersToNuffleTable(it)
            }
        }
        RulesSetupControl.KICK_OFF_TABLE -> {
            val selectedEntry by viewModel.selectedKickOffTable.collectAsState()
            JervisDropdownMenuWithSections(
                title = "Kick-off Table",
                entries = viewModel.kickOffTables,
                selectedEntry = selectedEntry,
            ) {
                viewModel.updateKickoffTable(it)
            }
        }
        RulesSetupControl.INJURY_TABLE -> {
            val selectedEntry by viewModel.selectedInjuryTable.collectAsState()
            JervisDropdownMenuWithSections(
                title = "Injury Table",
                entries = viewModel.injuryTables,
                selectedEntry = selectedEntry,
            ) {
                viewModel.updateInjuryTable(it)
            }
        }
        RulesSetupControl.STUNTY_INJURY_TABLE -> {
            val selectedEntry by viewModel.selectedStuntyInjuryTable.collectAsState()
            JervisDropdownMenuWithSections(
                title = "Stunty Injury Table",
                entries = viewModel.stuntyInjuryTables,
                selectedEntry = selectedEntry,
            ) {
                viewModel.updateStuntyInjuryTable(it)
            }
        }
        RulesSetupControl.CASUALTY_TABLE -> {
            val selectedEntry by viewModel.selectedCasualtyTable.collectAsState()
            JervisDropdownMenuWithSections(
                title = "Casualty Table",
                entries = viewModel.casualtyTables,
                selectedEntry = selectedEntry,
            ) {
                viewModel.updateCasualtyTable(it)
            }
        }
        RulesSetupControl.LASTING_INJURY_TABLE -> {
            val selectedEntry by viewModel.selectedLastingInjuryTable.collectAsState()
            JervisDropdownMenuWithSections(
                title = "Lasting Injury Table",
                entries = viewModel.lastingInjuryTables,
                selectedEntry = selectedEntry,
            ) {
                viewModel.updateLastingInjuryTable(it)
            }
        }
        RulesSetupControl.ARGUE_THE_CALL_TABLE -> {
            val selectedEntry by viewModel.selectedArgueTheCallTable.collectAsState()
            JervisDropdownMenuWithSections(
                title = "Argue the Call Table",
                entries = viewModel.argueTheCallTables,
                selectedEntry = selectedEntry,
            ) {
                viewModel.updateArgueTheCallTable(it)
            }
        }
        RulesSetupControl.PITCH -> {
            val selectedEntry by viewModel.selectedPitch.collectAsState()
            JervisDropdownMenuWithSections(
                title = "Pitch",
                entries = viewModel.pitches,
                selectedEntry = selectedEntry,
            ) {
                viewModel.updatePitch(it)
            }
        }
        RulesSetupControl.BALL -> {
            val selectedEntry by viewModel.selectedUnusualBall.collectAsState()
            JervisDropdownMenuWithSections(
                title = "Ball",
                entries = viewModel.unusualBallList,
                selectedEntry = selectedEntry,
            ) {
                viewModel.updateUnusualBall(it)
            }
        }
        RulesSetupControl.STADIUM -> {
            val selectedEntry by viewModel.selectedStadium.collectAsState()
            JervisDropdownMenuWithSections(
                title = "Stadia of the Old World",
                entries = viewModel.stadia,
                selectedEntry = selectedEntry,
            ) {
                viewModel.updateStadium(it)
            }
        }
        RulesSetupControl.PRAYERS_TO_NUFFLE_TOGGLE -> {
            val enabled by viewModel.prayersToNuffle.collectAsState()
            SimpleSwitch("Prayers To Nuffle", enabled) {
                viewModel.updatePrayersToNuffle(it)
            }
        }
        RulesSetupControl.MATCH_EVENTS_TOGGLE -> {
            val enabled by viewModel.matchEvents.collectAsState()
            SimpleSwitch("Match Events", enabled) {
                viewModel.updateMatchEvents(it)
            }
        }
        RulesSetupControl.EXTRA_TIME_TOGGLE -> {
            val enabled by viewModel.extraTime.collectAsState()
            SimpleSwitch("Extra Time", enabled) {
                viewModel.updateExtraTime(it)
            }
        }
    }
}
