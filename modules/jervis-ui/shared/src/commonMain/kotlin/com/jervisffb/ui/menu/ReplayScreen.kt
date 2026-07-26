package com.jervisffb.ui.menu

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
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
import com.jervisffb.engine.bb2025.StandardBB2025Rules
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Pitch
import com.jervisffb.engine.rules.builder.DiceRollOwner
import com.jervisffb.engine.rules.builder.UndoActionBehavior
import com.jervisffb.engine.serialization.GameFileData
import com.jervisffb.ui.game.UiGameClientType
import com.jervisffb.ui.game.state.ReplayActionProvider
import com.jervisffb.ui.game.view.utils.JervisButton
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.components.setup.LoadFileComponent
import com.jervisffb.ui.menu.components.setup.LoadFileComponentModel
import kotlinx.coroutines.launch
import okio.Path
import okio.Path.Companion.toPath

/**
 * Screen for selecting a Jervis game file (`.jrg`) to replay.
 */
class ReplayScreenModel(private val menuViewModel: MenuViewModel) : ScreenModel {

    // Reuse the exact save-file picker used by the game setup wizard. The rules builder
    // is unused for replays (the real rules come from the loaded file).
    val loadFileModel = LoadFileComponentModel(StandardBB2025Rules().toBuilder(), menuViewModel)
    val isSetupValid = loadFileModel.isSetupValid

    fun startReplay(navigator: Navigator) {
        val file = loadFileModel.gameFile ?: return
        val path = loadFileModel.filePath.value.toPath()
        menuViewModel.navigatorContext.launch {
            val model = createReplayGameScreenModel(menuViewModel, file, path)
            navigator.push(GameScreen(menuViewModel, model))
        }
    }

    /**
     * Builds a [GameScreenModel] that starts the game at the very beginning (no actions
     * pre-applied) and hands the full recorded action list to a [ReplayActionProvider]
     * so it can be stepped through interactively. Mirrors `updateRules` but keeps
     * `initialActions` empty so we start at the beginning rather than the saved end state.
     */
    private fun createReplayGameScreenModel(menuViewModel: MenuViewModel, file: GameFileData, path: Path): GameScreenModel {
        val rules = file.game.rules.toBuilder().run {
            timers.timersEnabled = false
            diceRollsOwner = DiceRollOwner.ROLL_ON_SERVER // Server-roll so recorded dice show their roll animation during replay
            undoActionBehavior = UndoActionBehavior.ALLOWED // Required so backward stepping (Undo) works across dice rolls
            build()
        }
        val controller = GameEngineController(
            state = Game(
                rules = rules,
                homeTeam = file.game.state.homeTeam,
                awayTeam = file.game.state.awayTeam,
                pitch = Pitch.createForRuleset(rules),
            ),
            initialActions = emptyList(), // Start at the very beginning of the game
        )
        // Used by the replay's wrapped decoration provider to reproduce the normal-game UI effects.
        val gameSettings = GameSettings(gameRules = rules, isHotseatGame = true)
        return GameScreenModel(
            uiClientType = UiGameClientType.REPLAY,
            uiMode = TeamActionMode.ALL_TEAMS,
            gameController = controller,
            homeTeam = controller.state.homeTeam,
            awayTeam = controller.state.awayTeam,
            actionProvider = ReplayActionProvider(file.actions, controller, menuViewModel, gameSettings),
            mode = Replay(path),
            menuViewModel = menuViewModel,
        ).also {
            it.gameAcceptedByAllPlayers()
        }
    }
}

class ReplayScreen(private val menuViewModel: MenuViewModel, viewModel: ReplayScreenModel) : Screen {
    @Composable
    override fun Content() {
        JervisScreen(menuViewModel) {
            ReplayScreenContent(menuViewModel)
        }
    }
}

@Composable
fun Screen.ReplayScreenContent(menuViewModel: MenuViewModel) {
    val navigator = LocalNavigator.currentOrThrow
    val viewModel = rememberScreenModel { ReplayScreenModel(menuViewModel) }
    val isSetupValid by viewModel.isSetupValid.collectAsState()
    MenuScreenWithTitle(
        menuViewModel,
        title = "Replay",
        pageImage = { }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                LoadFileComponent(
                    viewModel = viewModel.loadFileModel,
                    title = "Select Game File",
                    hintText = "Game File",
                    iconDescription = "Find Game File",
                )
            }
            Column(
                modifier = Modifier.width(600.dp),
                horizontalAlignment = Alignment.End,
            ) {
                JervisButton(
                    text = "Start Replay",
                    onClick = { viewModel.startReplay(navigator) },
                    enabled = isSetupValid,
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
