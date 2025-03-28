package com.jervisffb.ui.menu.intro

import cafe.adriel.voyager.navigator.Navigator
import com.jervisffb.ui.BuildConfig
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.DevScreen
import com.jervisffb.ui.menu.DevScreenViewModel
import com.jervisffb.ui.menu.JervisScreenModel
import com.jervisffb.ui.menu.StandAloneScreen
import com.jervisffb.ui.menu.StandAloneScreenModel
import com.jervisffb.ui.menu.fumbbl.FumbblScreen
import com.jervisffb.ui.menu.fumbbl.FumbblScreenModel
import com.jervisffb.utils.getBuildType
import com.jervisffb.utils.getPlatformDescription
import io.ktor.http.encodeURLParameter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime

/**
 * Display data for the Credit dialog
 */
data class CreditData(
    val title: String = "Jervis Fantasy Football",
    val mainDeveloper: String = "Ilios",
    val mainDeveloperDescription: String = "Pushing bits and trying to interpret the rulebook.",
    val clientVersion: String = BuildConfig.releaseVersion,
    val gitCommit: String = BuildConfig.gitHash,
    val fumbblDevelopers: List<String> = listOf(
        "SkiJunkie",
        "Christer",
        "Kalimar",
        "Candlejack",
        "BattleLore",
        "WhatBall",
        "Garion",
        "Lakrillo",
        "Java",
        "Tussock",
        "Cowhead",
        "F_alk",
        "FreeRange",
        "Harvestmouse",
        "Knut_Rockie",
        "MisterFurious",
        "Ryanfitz",
        "VocalVoodoo",
        "Minenbonnie",
        "Qaz",
        "ArrestedDevelopment"
    ),
    val fumbblDevelopersDescription: String = """
        This project is heavily inspired by the FUMBBL Client, and a lot of the graphics and 
        sound assets are copied from there. All credits go the respective creators.
    """.trimIndent(),
    val projectUrl: String = "https://github.com/cmelchior/jervis-ffb",
    val newIssueUrl: String = "https://github.com/cmelchior/jervis-ffb/issues/new"
)

data class NewsEntryData(
    val timestamp: String,
    val body: String,
)

/**
 * ViewModel class for the Main starting screen.
 */
class FrontpageScreenModel(private val menuViewModel: MenuViewModel) : JervisScreenModel {

    val news: List<NewsEntryData>
    private val _showCreditDialog = MutableStateFlow(false)
    val showCreditDialog: StateFlow<Boolean> = _showCreditDialog
    val creditData: CreditData

    init {
        // Prepare Git History so it can be displayed on the News section
        // It is formatted here because (for some reason) the BuildConfig plugin has problems
        // doing it in Gradle
        news = formatGitHistory()

        // Customize the create issue link, so it contains some basic information about the client
        val body = """
<Describe the issue>

-----
**Client Information (${getBuildType()})**
Jervis Client Version: ${BuildConfig.releaseVersion}
Git Commit: ${BuildConfig.gitHash}
${getPlatformDescription()}
        """.trimIndent().encodeURLParameter()
        creditData = CreditData(
            newIssueUrl = "https://github.com/cmelchior/jervis-ffb/issues/new?body=$body&labels=user"
        )
    }

    private fun formatGitHistory(): List<NewsEntryData> {
        val dateFormat = LocalDate.Format {
            monthName(MonthNames.ENGLISH_ABBREVIATED)
            char(' ')
            dayOfMonth()
            chars(", ")
            year()
        }

        return BuildConfig.gitHistory.lines().map {
            val epochTimestamp = it.substringBefore(":").toLong()
            val timestamp = Instant.fromEpochSeconds(epochTimestamp)
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date
                .format(dateFormat)
            val body = it.substringAfter(":").trim()
            NewsEntryData(timestamp, body)
        }
    }

    fun showCreditDialog(visible: Boolean) {
        _showCreditDialog.value = visible
    }

    fun gotoFumbblScreen(navigator: Navigator) {
        menuViewModel.navigatorContext.launch {
            val viewModel = FumbblScreenModel(menuViewModel)
            viewModel.initialize()
            navigator.push(FumbblScreen(menuViewModel, viewModel))
        }
    }

    fun gotoStandAloneScreen(navigator: Navigator) {
        menuViewModel.navigatorContext.launch {
            val viewModel = StandAloneScreenModel(menuViewModel)
            navigator.push(StandAloneScreen(menuViewModel, viewModel))
        }

    }

    fun gotoDevModeScreen(navigator: Navigator) {
        menuViewModel.navigatorContext.launch {
            val viewModel = DevScreenViewModel(menuViewModel)
            navigator.push(DevScreen(menuViewModel, viewModel))
        }
    }

    val clientVersion: String = BuildConfig.releaseVersion
}
