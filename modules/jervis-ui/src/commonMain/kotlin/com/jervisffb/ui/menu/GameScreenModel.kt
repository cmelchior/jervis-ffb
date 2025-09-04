package com.jervisffb.ui.menu

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import cafe.adriel.voyager.core.model.ScreenModel
import com.jervis.generated.SettingsKeys
import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.tables.Weather
import com.jervisffb.fumbbl.net.adapter.FumbblReplayAdapter
import com.jervisffb.ui.SETTINGS_MANAGER
import com.jervisffb.ui.SoundManager
import com.jervisffb.ui.formatCurrency
import com.jervisffb.ui.game.UiGameController
import com.jervisffb.ui.game.icons.IconFactory
import com.jervisffb.ui.game.icons.LogoSize
import com.jervisffb.ui.game.state.UiActionProvider
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.view.field.FieldSizeData
import com.jervisffb.ui.game.view.field.PointerEventBus
import com.jervisffb.ui.game.viewmodel.FieldDetails
import com.jervisffb.ui.game.viewmodel.FieldViewData
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// Wrapper used to contain data needed to contain team specfic information
// for the loading screen. Team icons are treated seperately as they might
// be slower to load.
data class LoadingTeamInfo(
    val teamName: String,
    val coachName: String,
    val race: String,
    val teamValue: String
)

// Act as a bridge between Compose and ViewModels.
// It is stored inside a ComposeLocal for Compose to access when rendering the field
// The GameScreenModel must guarantee that one instance of this exists for the lifetime
// of the Game Screen.
@Stable
class LocalFieldDataWrapper {
    // Information about the size of the Field, especially the width and height in pixels
    var size: FieldSizeData by mutableStateOf(FieldSizeData(0, IntSize.Zero, 0, 0))
    // Used to share mouse events across all field layers
    val pointerBus: PointerEventBus = PointerEventBus()
    // Indicates whether a context menu is visible
    var isContentMenuVisible by mutableStateOf(false)
}

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

    val fieldViewData: MutableStateFlow<FieldViewData> = MutableStateFlow(
        FieldViewData(
            Size.Zero,
            IntSize.Zero,
            IntOffset.Zero,
            gameController.rules.fieldWidth,
            gameController.rules.fieldHeight
        )
    )

    val hoverPlayerFlow = MutableSharedFlow<Player?>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val sharedFieldData = LocalFieldDataWrapper()

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
    val uiState: UiGameController = UiGameController(
        uiMode,
        gameController,
        actionProvider,
        menuViewModel,
        actions
    )
    val fieldBackground: Flow<FieldDetails> = uiState.uiStateFlow.map { uiSnapshot ->
        val weather = uiSnapshot.weather
        when (weather) {
            Weather.SWELTERING_HEAT -> FieldDetails.HEAT
            Weather.VERY_SUNNY -> FieldDetails.SUNNY
            Weather.PERFECT_CONDITIONS -> FieldDetails.NICE
            Weather.POURING_RAIN -> FieldDetails.RAIN
            Weather.BLIZZARD -> FieldDetails.BLIZZARD
        }
    }

    val logsBackgroundColor: Flow<Color> = combine(
        fieldBackground,
        SETTINGS_MANAGER.observeBooleanKey(SettingsKeys.JERVIS_UI_USE_PITCH_WEATHER_AS_GAME_BACKGROUND_VALUE, false)
    ) { field, useFieldBackground ->
        if (useFieldBackground) field.logBackground else FieldDetails.NICE.logBackground
    }

    init {
        actionProvider.updateSharedData(sharedFieldData)
        menuViewModel.backgroundContext.launch {
            // Use large icon for the loading screen
            homeTeamIcon.value = IconFactory.loadRosterIcon(
                homeTeam.id,
                homeTeam.teamLogo ?: homeTeam.roster.logo,
                LogoSize.LARGE
            )
            // Prepare small icon for the game view itself
            IconFactory.loadRosterIcon(
                homeTeam.id,
                homeTeam.teamLogo ?: homeTeam.roster.logo,
                LogoSize.SMALL
            )
        }
        menuViewModel.backgroundContext.launch {
            // Use large icon for the loading screen
            awayTeamIcon.value = IconFactory.loadRosterIcon(
                awayTeam.id,
                awayTeam.teamLogo ?: awayTeam.roster.logo,
                LogoSize.LARGE
            )
            // Prepare small icon for the game view itself
            IconFactory.loadRosterIcon(
                awayTeam.id,
                awayTeam.teamLogo ?: awayTeam.roster.logo,
                LogoSize.SMALL
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
     * If the loading screen is used for a P2P Game, calling this method will displ
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
    suspend fun initialize(density: Density) {
        _loadingMessages.value = "Initializing icons"
        IconFactory.initialize(density, homeTeam, awayTeam)
        _loadingMessages.value = "Initializing sounds"
        SoundManager.initialize()
        menuViewModel.uiState = uiState
        uiState.startGameEventLoop()
        onEngineInitialized()
        _loadingMessages.value = "Starting game"
        _isLoaded.value = true
    }

    fun stopGame() {
        onGameStopped()
    }

    fun updateFieldViewData(fieldLayoutCoordinates: LayoutCoordinates) {
        val offset = fieldLayoutCoordinates.localToWindow(Offset.Zero)
        val fieldPositionData = FieldViewData(
            JervisTheme.windowSizePx,
            fieldLayoutCoordinates.size,
            IntOffset(offset.x.roundToInt(), offset.y.roundToInt()),
            gameController.rules.fieldWidth,
            gameController.rules.fieldHeight
        )
        fieldViewData.value = fieldPositionData
    }
}
