package com.jervisffb.ui.menu

import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
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
import cafe.adriel.voyager.core.model.screenModelScope
import com.jervis.generated.SettingsKeys
import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.isOnAwayTeam
import com.jervisffb.engine.model.isOnHomeTeam
import com.jervisffb.engine.model.locations.DogOut
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.tables.Weather
import com.jervisffb.engine.utils.safeTryEmit
import com.jervisffb.fumbbl.net.adapter.FumbblReplayAdapter
import com.jervisffb.ui.SETTINGS_MANAGER
import com.jervisffb.ui.SoundManager
import com.jervisffb.ui.formatCurrency
import com.jervisffb.ui.game.UiGameController
import com.jervisffb.ui.game.icons.IconFactory
import com.jervisffb.ui.game.icons.LogoSize
import com.jervisffb.ui.game.model.UiPlayerCard
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
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
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
    // Indicates whether any Action Wheel menu is visible.
    val isActionWheelVisible: State<Boolean>
        field = mutableStateOf(false)

    val isPrimaryActionWheelVisible: State<Boolean>
        field = mutableStateOf(false)

    val isContextActionWheelVisible: State<Boolean>
        field = mutableStateOf(false)

    fun setPrimaryActionWheelVisibility(visible: Boolean) {
        isPrimaryActionWheelVisible.value = visible
        isActionWheelVisible.value = isPrimaryActionWheelVisible.value || isContextActionWheelVisible.value
    }

    fun setContextActionWheelVisibility(visible: Boolean) {
        isContextActionWheelVisible.value = visible
        isActionWheelVisible.value = isPrimaryActionWheelVisible.value || isContextActionWheelVisible.value
    }
}

/**
 * Top-level ViewModel foreverything on the Game Screen.
 */
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
            0f,
            IntOffset.Zero,
            gameController.rules.fieldWidth,
            gameController.rules.fieldHeight
        )
    )

    val sharedFieldData = LocalFieldDataWrapper()

    var fumbbl: FumbblReplayAdapter? = null
    val rules: Rules = gameController.rules
    // `false` until both teams have accepted the game
    val isReadyToStartGame = MutableStateFlow(false)
    val loadingMessages: StateFlow<String>
        field = MutableStateFlow<String>("")
    val isLoaded: StateFlow<Boolean>
        field = MutableStateFlow<Boolean>(false)
    val homeTeamIcon: MutableStateFlow<ImageBitmap?> = MutableStateFlow(null)
    val homeTeamData: LoadingTeamInfo
    val awayTeamIcon: MutableStateFlow<ImageBitmap?> = MutableStateFlow(null)
    val awayTeamData: LoadingTeamInfo
    private val _contextMenuFlow  = MutableSharedFlow<PlayerId?>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val contextMenuFlow: Flow<Player?> = _contextMenuFlow.map { it?.let { uiState.state.getPlayerById(it) } }
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

    val hoverPlayerFlow = MutableStateFlow<Player?>(null)
    val playerStatCardDismissed = MutableStateFlow(false)

    // Calculate which player to show on either the away or home side
    private fun computePlayerStatCard(fixedPlayer: Player?, hoveredPlayer: Player?, forHomeSide: Boolean): Player? {
        fun shouldShowFixedHere(p: Player, homeSide: Boolean): Boolean = when (homeSide) {
            true -> p.isOnHomeTeam()
            false -> p.isOnAwayTeam()
        }
        return when {
            // 1. If there is only an active player, it should be shown in the dogout of the team it belongs to.
            (fixedPlayer != null && hoveredPlayer == null) -> {
                if (shouldShowFixedHere(fixedPlayer, forHomeSide)) fixedPlayer else null
            }
            // 2. If there is no active player, but there is a hovered player, it should be shown in Away dogout (right side of the screen).
            // The only exception is if the player is in the Away dogout, in which case it should be shown in the Home dogout.
            (fixedPlayer == null && hoveredPlayer != null) -> {
                val isInAwayDogout = hoveredPlayer.isOnAwayTeam() && hoveredPlayer.location is DogOut
                when (forHomeSide) {
                    true -> if (isInAwayDogout) hoveredPlayer else null
                    false -> if (!isInAwayDogout) hoveredPlayer else null
                }
            }
            // 3. If there is both and active player and a hovered player, we show the active player in the team dogout
            // and the hovered player in the opposite team dogout. In the edge case, where you are selecting a player
            // in the dogout on the opposite side of the active player, the active player should be temporarily
            // hidden while we only show the hover player.
            (fixedPlayer != null && hoveredPlayer != null) -> {
                val showFixedPlayer = shouldShowFixedHere(fixedPlayer, forHomeSide)
                val isHoveredPlayerInHomeDogout = hoveredPlayer.isOnHomeTeam() && hoveredPlayer.location is DogOut
                val isHoveredPlayerInAwayDogout = hoveredPlayer.isOnAwayTeam() && hoveredPlayer.location is DogOut
                val isActivePlayerInHomeDogout = fixedPlayer.isOnHomeTeam()
                val isActivePlayerInAwayDogout = fixedPlayer.isOnAwayTeam()
                // Catch edge case where hovering over the other dogout while the active player is being shown
                // In that case, the hover player should override the active player.
                if (isActivePlayerInAwayDogout && isHoveredPlayerInHomeDogout) {
                    return if (forHomeSide) null else hoveredPlayer
                }
                if (isActivePlayerInHomeDogout && isHoveredPlayerInAwayDogout) {
                    return if (forHomeSide) hoveredPlayer else null
                }
                // Show either Fixed or Hover player, depending on the location.
                // If the hover and active player are the same, we only want to show the active player.
                when (showFixedPlayer) {
                    true -> fixedPlayer
                    false -> if (hoveredPlayer.id == fixedPlayer.id) null else hoveredPlayer
                }
            }
            else -> null // Otherwise do not show any player
        }
    }

    private val playerStatCardFlow: SharedFlow<Pair<Player?, Player?>> = combine(
        uiState.uiStateFlow,
        hoverPlayerFlow,
        playerStatCardDismissed
    ) { snapshot, hoveredPlayer, activePlayerDismissed ->
        val activePlayer = snapshot.game.activePlayer
        val fixedPlayer = activePlayer?.takeIf { !activePlayerDismissed || hoveredPlayer?.id == activePlayer.id }
        Pair(
            computePlayerStatCard(fixedPlayer, hoveredPlayer, forHomeSide = true),
            computePlayerStatCard(fixedPlayer, hoveredPlayer, forHomeSide = false)
        )
    }.shareIn(screenModelScope, SharingStarted.Eagerly, 1)

    fun playerStatCardFlowFor(team: Team): Flow<UiPlayerCard?> =
        playerStatCardFlow
            .map { (homeSidePlayer, awaySidePlayer) ->
                if (team.isHomeTeam()) homeSidePlayer else awaySidePlayer
            }
            .distinctUntilChanged { oldPlayer, newPlayer -> oldPlayer?.id == newPlayer?.id }
            .map { player -> player?.let(::UiPlayerCard) }

    fun dismissPlayerStatCard() {
        playerStatCardDismissed.value = true
    }

    val logsBackgroundColor: Flow<Color> = combine(
        fieldBackground,
        SETTINGS_MANAGER.observeBooleanKey(SettingsKeys.JERVIS_UI_USE_PITCH_WEATHER_AS_GAME_BACKGROUND_VALUE, false)
    ) { field, useFieldBackground ->
        if (useFieldBackground) field.logBackground else FieldDetails.NICE.logBackground
    }

    val selectedPlayersInUi = mutableListOf<PlayerId>()
    var isGameStatusBoxEnabled = mutableStateOf(false)
    var gameStatusTitle = mutableStateOf<String?>(null)
    var gameStatusBoxTitle = mutableStateOf("")

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
        screenModelScope.launch {
            uiState.uiStateFlow.collect {
                isGameStatusBoxEnabled.value = it.status.centerBadgeEnabled
                gameStatusBoxTitle.value = it.status.centerBadgeText
                gameStatusTitle.value = it.gameStatusText
                selectedPlayersInUi.clear()
            }
        }
        uiState.uiStateFlow
            .map { snapshot -> snapshot.game.activePlayer }
            .distinctUntilChanged()
            .onEach { playerStatCardDismissed.value = false }
            .launchIn(screenModelScope)
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
        loadingMessages.value = waitMessage
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
        loadingMessages.value = "Initializing icons"
        IconFactory.initialize(density, homeTeam, awayTeam)
        loadingMessages.value = "Initializing sounds"
        SoundManager.initialize()
        menuViewModel.uiState = uiState
        uiState.startGameEventLoop()
        onEngineInitialized()
        loadingMessages.value = "Starting game"
        isLoaded.value = true
    }

    fun stopGame() {
        onGameStopped()
    }

    fun updateFieldViewData(fieldLayoutCoordinates: LayoutCoordinates, borderSize: Float) {
        val offset = fieldLayoutCoordinates.localToWindow(Offset.Zero)
        val fieldPositionData = FieldViewData(
            JervisTheme.windowSizePx,
            fieldLayoutCoordinates.size,
            borderSize,
            IntOffset(offset.x.roundToInt(), offset.y.roundToInt()),
            gameController.rules.fieldWidth,
            gameController.rules.fieldHeight
        )
        fieldViewData.value = fieldPositionData
    }

    fun showPlayerContextMenu(player: PlayerId) {
        hoverPlayerFlow.safeTryEmit(null)
        _contextMenuFlow.tryEmit(player)
    }
    fun hidePlayerContextMenu() {
        _contextMenuFlow.tryEmit(null)
    }

    fun getSelectedPlayers(): List<PlayerId> {
        return selectedPlayersInUi.toList()
    }
}
