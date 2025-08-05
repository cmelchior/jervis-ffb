@file:OptIn(
    InternalResourceApi::class,
    ExperimentalResourceApi::class,
)

package com.jervisffb.ui.menu

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
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
import com.jervisffb.engine.rules.BB72020Rules
import com.jervisffb.engine.rules.StandardBB2020Rules
import com.jervisffb.engine.rules.builder.DiceRollOwner
import com.jervisffb.engine.rules.builder.UndoActionBehavior
import com.jervisffb.engine.serialize.FILE_EXTENSION_GAME_FILE
import com.jervisffb.engine.serialize.GameFileData
import com.jervisffb.engine.serialize.JervisSerialization
import com.jervisffb.fumbbl.net.adapter.FumbblReplayAdapter
import com.jervisffb.jervis_ui.generated.resources.Res
import com.jervisffb.jervis_ui.generated.resources.jervis_frontpage_goblin
import com.jervisffb.ui.CacheManager
import com.jervisffb.ui.createDefaultAwayTeam
import com.jervisffb.ui.createDefaultBB7AwayTeam
import com.jervisffb.ui.createDefaultBB7HomeTeam
import com.jervisffb.ui.createDefaultHomeTeam
import com.jervisffb.ui.game.LocalActionProvider
import com.jervisffb.ui.game.state.ManualActionProvider
import com.jervisffb.ui.game.state.RandomActionProvider
import com.jervisffb.ui.game.state.ReplayActionProvider
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.view.utils.JervisButton
import com.jervisffb.ui.game.view.utils.paperBackground
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.utils.readFile
import com.jervisffb.utils.APPLICATION_DIRECTORY
import com.jervisffb.utils.jervisLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import okio.Path
import okio.Path.Companion.toPath
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.imageResource

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

    companion object {
        val LOG = jervisLogger()
    }

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

    private fun createDevHotseatBB7ScreenModel(menuViewModel: MenuViewModel, randomActions: Boolean = false): GameScreenModel {
        val rules = BB72020Rules().toBuilder().run {
            timers.timersEnabled = false
            diceRollsOwner = DiceRollOwner.ROLL_ON_CLIENT
            undoActionBehavior = UndoActionBehavior.ALLOWED
            build()
        }
        val homeTeam = createDefaultBB7HomeTeam(rules)
        val awayTeam = createDefaultBB7AwayTeam(rules)
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

    private fun createLoadedGameScreenModel(menuViewModel: MenuViewModel, file: GameFileData): GameScreenModel {
        val rules = file.game.rules.toBuilder().run {
            timers.timersEnabled = false
            diceRollsOwner = DiceRollOwner.ROLL_ON_CLIENT
            undoActionBehavior = UndoActionBehavior.ALLOWED
            build()
        }
        val homeTeam = file.homeTeam
        val awayTeam = file.awayTeam
        val game = file.game.state
        val gameController = file.game
        val gameSettings = GameSettings(
            gameRules = rules,
            isHotseatGame = true,
            initialActions = file.actions
        )
        val homeActionProvider = ManualActionProvider(
            gameController,
            menuViewModel,
            TeamActionMode.HOME_TEAM,
            gameSettings,
        )
        val awayActionProvider = ManualActionProvider(
            gameController,
            menuViewModel,
            TeamActionMode.AWAY_TEAM,
            gameSettings,
        )
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

    // Starts a Dev Hotseat BB7 game with pre-determined teams, no timer and client rolls enabled
    fun startManualBB7Game(navigator: Navigator) {
        menuViewModel.navigatorContext.launch {
            val viewModel = createDevHotseatBB7ScreenModel(menuViewModel)
            navigator.push(GameScreen(menuViewModel, viewModel))
        }
    }

    fun loadSaveFile(navigator: Navigator) {
        menuViewModel.navigatorContext.launch {
            readFile(
                extensionFilterDescription = "Jervis Game Files (.${FILE_EXTENSION_GAME_FILE})",
                extensionFilterFileType = FILE_EXTENSION_GAME_FILE
            ) { path, loadResult ->
                loadResult
                    .onSuccess { fileContent ->
                        // Just silently ignore `null` values as it indicates the dialog was closed
                        if (path != null) {
                            JervisSerialization.loadFromFileContent(fileContent)
                                .onSuccess { gameFile ->
                                    val viewModel = createLoadedGameScreenModel(menuViewModel, gameFile)
                                    navigator.push(GameScreen(menuViewModel, viewModel))
                                }
                                .onFailure { error ->
                                    // Ignore failure for now
                                    LOG.i { "Failed to load game file: ${error.message}" }
                                }
                        }
                    }
                    .onFailure { error ->
                        // Ignore failure for now
                        LOG.i { "Failed to load game file: ${error.message}" }
                    }
            }
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
        val viewModel = rememberScreenModel { DevScreenViewModel(menuViewModel) }
        JervisScreen(menuViewModel) {
            PageContent(viewModel, menuViewModel)
        }
    }

    @Composable
    fun PageContent(viewModel: DevScreenViewModel, menuViewModel: MenuViewModel) {
        val navigator = LocalNavigator.currentOrThrow
        val replayFiles by viewModel.availableReplayFiles.collectAsState(emptyList())
        val staticButtons = remember {
            listOf(
                "Start Standard game with manual actions" to { viewModel.startManualGame(navigator) },
                "Start Standard game with all random actions" to { viewModel.startRandomGame(navigator) },
                "Start BB7 game with all manual actions" to { viewModel.startManualBB7Game(navigator) },
                "Load save file" to { viewModel.loadSaveFile(navigator) }
            )
        }

        MenuScreenWithTitle(
            menuViewModel,
            title = "Developer Options",
            textBottomPadding = 12.dp,
            pageImage = {
                Image(
                    modifier = Modifier.align(Alignment.BottomEnd).fillMaxWidth(0.20f).offset(x = -50.dp, y = -20.dp).scale(1f,1f),
                    bitmap = imageResource(Res.drawable.jervis_frontpage_goblin),
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                )
            }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(horizontal = 16.dp)
                    ,
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().paperBackground(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        staticButtons.forEach { (title, onClick) ->
                            JervisButton(
                                text = title,
                                onClick = onClick,
                                textModifier = Modifier.padding(16.dp),
                                textUppercase = false,
                                buttonColor = JervisTheme.rulebookBlue
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        replayFiles.forEach { (name, file) ->
                            JervisButton(
                                text = "Start replay: ${file.name}",
                                onClick = { viewModel.startReplayGame(navigator, Replay(file)) },
                                textModifier = Modifier.padding(16.dp),
                                textUppercase = false,
                                buttonColor = JervisTheme.rulebookBlue,
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}
