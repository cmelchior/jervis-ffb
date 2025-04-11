@file:OptIn(
    InternalResourceApi::class,
    ExperimentalResourceApi::class,
)

package com.jervisffb.ui.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.GameSettings
import com.jervisffb.engine.model.Field
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.StandardBB2020Rules
import com.jervisffb.engine.rules.builder.DiceRollOwner
import com.jervisffb.engine.rules.builder.UndoActionBehavior
import com.jervisffb.fumbbl.net.adapter.FumbblReplayAdapter
import com.jervisffb.ui.CacheManager
import com.jervisffb.ui.createDefaultAwayTeam
import com.jervisffb.ui.createDefaultHomeTeam
import com.jervisffb.ui.game.LocalActionProvider
import com.jervisffb.ui.game.state.ManualActionProvider
import com.jervisffb.ui.game.state.RandomActionProvider
import com.jervisffb.ui.game.state.ReplayActionProvider
import com.jervisffb.ui.game.view.utils.paperBackground
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.utils.APPLICATION_DIRECTORY
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import okio.Path
import okio.Path.Companion.toPath
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.InternalResourceApi

// Which teams should be allowed to create actions on this client
enum class TeamActionMode {
    HOME_TEAM,
    AWAY_TEAM,
    ALL_TEAMS,
}

sealed interface GameMode
data object Random : GameMode
data class Manual(val actionMode: TeamActionMode) : GameMode
data class Replay(val file: Path) : GameMode

class DevScreenViewModel(private val menuViewModel: MenuViewModel) : ScreenModel {

    fun startReplayGame(navigator: Navigator, mode: Replay) {
        menuViewModel.navigatorContext.launch {
            val viewModel = createReplayScreenModel(menuViewModel, mode.file)
            navigator.push(GameScreen(menuViewModel, viewModel))
        }
    }

    private suspend fun createReplayScreenModel(menuViewModel: MenuViewModel, replayFile: Path): GameScreenModel {
        val fumbbl = FumbblReplayAdapter(replayFile, checkCommandsWhenLoading = false)
        fumbbl.loadCommands()
        val gameController = GameEngineController(fumbbl.getGame())
        return GameScreenModel(
            uiMode = TeamActionMode.ALL_TEAMS,
            gameController = gameController,
            homeTeam = gameController.state.homeTeam,
            awayTeam = gameController.state.awayTeam,
            actionProvider = ReplayActionProvider(menuViewModel, fumbbl),
            mode = Replay(replayFile),
            menuViewModel = menuViewModel,
        ).also {
            it.gameAcceptedByAllPlayers()
        }
    }

    private fun createDevHotseatScreenModel(menuViewModel: MenuViewModel, randomActions: Boolean = false): GameScreenModel {
        val rules = StandardBB2020Rules().toBuilder().run {
            timers.timersEnabled = false
            diceRollsOwner = DiceRollOwner.ROLL_ON_CLIENT
            undoActionBehavior = UndoActionBehavior.ALLOWED
            build()
        }
        val homeTeam = createDefaultHomeTeam(rules)
        val awayTeam = createDefaultAwayTeam(rules)
        val game = Game(rules, homeTeam, awayTeam, Field.Companion.createForRuleset(rules))
        val gameController = GameEngineController(game)
        val gameSettings = GameSettings(gameRules = rules, isHotseatGame = true)
        val homeActionProvider = when (randomActions) {
            false -> {
                ManualActionProvider(
                    gameController,
                    menuViewModel,
                    TeamActionMode.HOME_TEAM,
                    gameSettings,
                )
            }
            true -> RandomActionProvider(TeamActionMode.HOME_TEAM, gameController).also { it.startActionProvider() }
        }
        val awayActionProvider = when (randomActions) {
            false -> {
                ManualActionProvider(
                    gameController,
                    menuViewModel,
                    TeamActionMode.AWAY_TEAM,
                    gameSettings,
                )
            }
            true -> RandomActionProvider(TeamActionMode.AWAY_TEAM, gameController).also { it.startActionProvider() }
        }
        val actionProvider = LocalActionProvider(
            gameController,
            gameSettings,
            homeActionProvider,
            awayActionProvider
        )
        return GameScreenModel(
            TeamActionMode.ALL_TEAMS,
            gameController,
            gameController.state.homeTeam,
            gameController.state.awayTeam,
            actionProvider,
            mode = Manual(TeamActionMode.ALL_TEAMS),
            menuViewModel = menuViewModel,
            onEngineInitialized = {
                menuViewModel.controller = gameController
                menuViewModel.navigatorContext.launch {
                    // TODO Send to AI controller?
                    // controller.sendGameStarted()
                }
            }
        ).also {
            it.gameAcceptedByAllPlayers()
        }
    }

    // Starts a Dev Hotseat game with pre-determined teams, no timer and client rolls enabled
    fun startManualGame(navigator: Navigator) {
        menuViewModel.navigatorContext.launch {
            val viewModel = createDevHotseatScreenModel(menuViewModel)
            navigator.push(GameScreen(menuViewModel, viewModel))
        }

    }

    // Starts a Hotseat game with pre-determined teams, no timer and client rolls enabled and all actions are random
    fun startRandomGame(navigator: Navigator) {
        menuViewModel.navigatorContext.launch {
            val viewModel = createDevHotseatScreenModel(menuViewModel, randomActions = true)
            navigator.push(GameScreen(menuViewModel, viewModel))
        }
    }

    val availableReplayFiles: Flow<List<Pair<String, Path>>> = flow {
        // TODO For now, turn the path into an absolute path so we work with the FumbbleReplayAdapter
        //  This needs further refactoring
        val replayFiles = CacheManager.getFumbleReplayFiles()
        emit(replayFiles.map {
            val absolutePath = "$APPLICATION_DIRECTORY/$it".toPath()
            it.name to absolutePath
        })
    }
}

class DevScreen(private val menuViewModel: MenuViewModel, viewModel: DevScreenViewModel) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = rememberScreenModel { DevScreenViewModel(menuViewModel) }
        val replayFiles by viewModel.availableReplayFiles.collectAsState(emptyList())

        Column(
            modifier = Modifier.fillMaxSize().paperBackground(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(onClick = {
                viewModel.startManualGame(navigator)
            }) {
                Text(
                    text = "Start game with manual actions",
                    modifier = Modifier.padding(16.dp),
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                viewModel.startRandomGame(navigator)
            }) {
                Text(
                    text = "Start game with all random actions",
                    modifier = Modifier.padding(16.dp),
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            replayFiles.forEach { (name, file) ->
                Button(onClick = {
                    viewModel.startReplayGame(navigator, Replay(file))
                }) {
                    Text(
                        text = "Start replay: ${file.name}",
                        modifier = Modifier.padding(16.dp),
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
