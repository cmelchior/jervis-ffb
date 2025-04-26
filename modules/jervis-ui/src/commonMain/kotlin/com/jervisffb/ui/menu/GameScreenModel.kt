package com.jervisffb.ui.menu

import androidx.compose.ui.graphics.ImageBitmap
import cafe.adriel.voyager.core.model.ScreenModel
import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.Rules
import com.jervisffb.fumbbl.net.adapter.FumbblReplayAdapter
import com.jervisffb.ui.SoundManager
import com.jervisffb.ui.formatCurrency
import com.jervisffb.ui.game.UiGameController
import com.jervisffb.ui.game.icons.IconFactory
import com.jervisffb.ui.game.icons.LogoSize
import com.jervisffb.ui.game.state.UiActionProvider
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Wrapper used to contain data needed to contain team specfic information
// for the loading screen. Team icons are treated seperately as they might
// be slower to load.
data class LoadingTeamInfo(
    val teamName: String,
    val coachName: String,
    val race: String,
    val teamValue: String
)

class GameScreenModel(
    private val uiMode: TeamActionMode,
    private val gameController: GameEngineController,
    val homeTeam: Team,
    var awayTeam: Team,
    val actionProvider: UiActionProvider,
    val mode: GameMode,
    val menuViewModel: MenuViewModel,
    private val actions: List<GameAction> = emptyList(),
    private val onEngineInitialized: () -> Unit = { },
    private val onGameStopped: () -> Unit = { }
) : ScreenModel {

    val hoverPlayerFlow = MutableSharedFlow<Player?>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    lateinit var uiState: UiGameController
    var fumbbl: FumbblReplayAdapter? = null
    val rules: Rules = gameController.rules
    // `false` until both teams have accepted the game
    val isReadyToStartGame = MutableStateFlow(false)
    val _loadingMessages = MutableStateFlow<String>("")
    val loadingMessages: StateFlow<String> = _loadingMessages
    val _isLoaded = MutableStateFlow<Boolean>(false)
    val isLoaded: StateFlow<Boolean> = _isLoaded
    val homeTeamIcon: MutableStateFlow<ImageBitmap?> = MutableStateFlow(null)
    val homeTeamData: LoadingTeamInfo
    val awayTeamIcon: MutableStateFlow<ImageBitmap?> = MutableStateFlow(null)
    val awayTeamData: LoadingTeamInfo

    init {
        menuViewModel.backgroundContext.launch {
            homeTeamIcon.value = IconFactory.loadRosterIcon(
                homeTeam.id,
                homeTeam.teamLogo ?: homeTeam.roster.logo,
                LogoSize.LARGE
            )
        }
        menuViewModel.backgroundContext.launch {
            awayTeamIcon.value = IconFactory.loadRosterIcon(
                awayTeam.id,
                awayTeam.teamLogo ?: awayTeam.roster.logo,
                LogoSize.LARGE
            )
        }
        homeTeamData = LoadingTeamInfo(
            homeTeam.name,
            homeTeam.coach.name,
            homeTeam.roster.name,
            formatCurrency(homeTeam.teamValue)
        )
        awayTeamData = LoadingTeamInfo(
            awayTeam.name,
            awayTeam.coach.name,
            awayTeam.roster.name,
            formatCurrency(awayTeam.teamValue)
        )
    }

    /**
     * If the loaading screen is used for a P2P Game, calling this method will displ
     */
    fun waitForOpponent() {
        val waitMessage = when (uiMode) {
            TeamActionMode.HOME_TEAM -> "Waiting for ${awayTeam.name}"
            TeamActionMode.AWAY_TEAM -> "Waiting for ${homeTeam.name}"
            TeamActionMode.ALL_TEAMS -> "" // Should not be called in Hotseat games
        }
        _loadingMessages.value = waitMessage
    }

    /**
     * Must be called when both players have accepted the game.
     * This will start loading game assets after which the game is started.
     */
    fun gameAcceptedByAllPlayers() {
        isReadyToStartGame.value = true
    }

    /**
     * Initialize game icons and other assets.
     */
    suspend fun initialize() {
        _loadingMessages.value = "Initializing icons"
        IconFactory.initialize(homeTeam, awayTeam)
        _loadingMessages.value = "Initializing sounds"
        SoundManager.initialize()
        uiState = UiGameController(
            uiMode,
            gameController,
            actionProvider,
            menuViewModel,
            actions
        )
        menuViewModel.uiState = uiState
        uiState.startGameEventLoop()
        onEngineInitialized()
        _loadingMessages.value = "Starting game"
        _isLoaded.value = true
    }

    fun stopGame() {
        onGameStopped()
    }
}
