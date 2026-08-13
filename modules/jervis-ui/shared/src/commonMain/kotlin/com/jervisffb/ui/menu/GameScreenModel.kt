package com.jervisffb.ui.menu

import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import cafe.adriel.voyager.core.model.ScreenModel
import com.jervis.generated.SettingsKeys
import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.MovePlayer
import com.jervisffb.engine.bb2025.challenge.goal.BlockGoal
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.isOnAwayTeam
import com.jervisffb.engine.model.isOnHomeTeam
import com.jervisffb.engine.model.locations.Dogout
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.tables.Weather
import com.jervisffb.engine.utils.safeTryEmit
import com.jervisffb.fumbbl.net.adapter.FumbblReplayAdapter
import com.jervisffb.ui.SETTINGS_MANAGER
import com.jervisffb.ui.SoundManager
import com.jervisffb.ui.formatCurrency
import com.jervisffb.ui.game.UiFocus
import com.jervisffb.ui.game.UiFocusProvider
import com.jervisffb.ui.game.UiFocusStyle
import com.jervisffb.ui.game.UiGameClientType
import com.jervisffb.ui.game.UiGameController
import com.jervisffb.ui.game.icons.IconFactory
import com.jervisffb.ui.game.icons.LogoSize
import com.jervisffb.ui.game.model.ModelRef
import com.jervisffb.ui.game.model.UiPlayerCard
import com.jervisffb.ui.game.state.UiActionProviderGroup
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.view.pitch.PitchSizeData
import com.jervisffb.ui.game.view.pitch.PointerEventBus
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.game.viewmodel.PanelBackground
import com.jervisffb.ui.game.viewmodel.PitchDetails
import com.jervisffb.ui.game.viewmodel.PitchViewData
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
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
// It is stored inside a ComposeLocal for Compose to access when rendering the pitch
// The GameScreenModel must guarantee that one instance of this exists for the lifetime
// of the Game Screen.
@Stable
class LocalPitchDataWrapper(uiState: UiGameController) {
    // Information about the size of the Pitch, especially the width and height in pixels
    var size: PitchSizeData by mutableStateOf(PitchSizeData(0, IntSize.Zero, 0, 0))
    // Used to share mouse events across all pitch layers
    val pointerBus: PointerEventBus = PointerEventBus()
    // Indicates whether any Action Wheel menu is visible.
    val isActionWheelVisible: State<Boolean>
        field = mutableStateOf(false)

    val uiDecorations = uiState.uiDecorations

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
    private val uiClientType: UiGameClientType,
    private val uiMode: TeamActionMode,
    private val gameController: GameEngineController,
    val homeTeam: Team,
    var awayTeam: Team,
    val actionProvider: UiActionProviderGroup,
    val mode: GameMode,
    val menuViewModel: MenuViewModel,
    private val actions: List<GameAction> = emptyList(),
    private val onEngineInitialized: () -> Unit = { },
    private val onGameStopped: () -> Unit = { }
) : ScreenModel {

    private val focusProvider: UiFocusProvider? = when (val gameMode = mode) {
        is ChallengeGame -> UiFocusProvider { state ->
            val goal = gameMode.challenge.goal as? BlockGoal
            val targetIds = goal?.targetPlayerIds(state).orEmpty()
            UiFocus(
                players = targetIds.associateWith { UiFocusStyle.CHALLENGE_TARGET },
            )
        }
        else -> null
    }

    // Scope for everything that observes this game for as long as it exists.
    // Deliberately not Voyager's `screenModelScope`. GameScreenModels are always
    // constructed directly rather than through `rememberScreenModel`, and for an
    // unregistered model Voyager falls back to the key of whichever screen was
    // registered last. Every game in the process would share one scope, owned by
    // an unrelated screen and cancelled with none of them, which kept every game
    // ever built reachable. This is also why `onDispose must be called manually.
    private val modelScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main + CoroutineName("GameScreenModel")
    )

    val pitchViewData: MutableStateFlow<PitchViewData> = MutableStateFlow(
        PitchViewData(
            Size.Zero,
            IntSize.Zero,
            0f,
            IntOffset.Zero,
            gameController.rules.pitchWidth,
            gameController.rules.pitchHeight
        )
    )

    val sharedPitchData: LocalPitchDataWrapper

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
    val contextMenuFlow: Flow<ModelRef<Player>?> = _contextMenuFlow.map {
        it?.let { playerId ->
            val player = uiState.state.getPlayerById(playerId)
            ModelRef(playerId, player)
        }
    }

    // In Dev Mode, this is used to track a player we are currently moving
    val playerToMove: StateFlow<PlayerId?>
        field = MutableStateFlow<PlayerId?>(null)

    // In Dev Mode, this used to track a player that is being edited on the State Panel
    private var playerEditorOpenOnStatePlayerId: PlayerId? = null

    val uiState: UiGameController = UiGameController(
        uiClientType,
        uiMode,
        gameController,
        actionProvider,
        menuViewModel,
        actions,
        focusProvider,
    )
    val pitchBackground: Flow<PitchDetails> = uiState.uiStateFlow.map { uiSnapshot ->
        val weather = uiSnapshot.weather
        when (weather) {
            Weather.SWELTERING_HEAT -> PitchDetails.HEAT
            Weather.VERY_SUNNY -> PitchDetails.SUNNY
            Weather.PERFECT_CONDITIONS -> PitchDetails.NICE
            Weather.POURING_RAIN -> PitchDetails.RAIN
            Weather.BLIZZARD -> PitchDetails.BLIZZARD
        }
    }

    val hoverPlayerFlow = MutableStateFlow<Player?>(null)
    val playerStatCardDismissed = MutableStateFlow(false)

    init {
        sharedPitchData = LocalPitchDataWrapper(uiState)
    }

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
                val isInAwayDogout = hoveredPlayer.isOnAwayTeam() && hoveredPlayer.location is Dogout
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
                val isHoveredPlayerInHomeDogout = hoveredPlayer.isOnHomeTeam() && hoveredPlayer.location is Dogout
                val isHoveredPlayerInAwayDogout = hoveredPlayer.isOnAwayTeam() && hoveredPlayer.location is Dogout
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
    }.shareIn(modelScope, SharingStarted.Eagerly, 1)

    fun playerStatCardFlowFor(team: Team): Flow<UiPlayerCard?> =
        playerStatCardFlow
            .map { (homeSidePlayer, awaySidePlayer) ->
                if (team.isHomeTeam()) homeSidePlayer else awaySidePlayer
            }
            .distinctUntilChanged { oldPlayer, newPlayer -> oldPlayer?.id == newPlayer?.id }
            .map { player ->
                player?.let {
                    UiPlayerCard(player = ModelRef(it.id, it))
                }
            }

    fun dismissPlayerStatCard() {
        playerStatCardDismissed.value = true
    }

    fun beginMovePlayer(playerId: PlayerId) {
        hidePlayerContextMenu()
        playerToMove.value = playerId
    }

    fun movePlayerTo(coordinate: PitchCoordinate) {
        val playerId = playerToMove.value ?: return
        playerToMove.value = null
        actionProvider.currentProvider.userActionSelected(MovePlayer(playerId, coordinate))
        playerEditorOpenOnStatePlayerId = playerId
        showPlayerContextMenu(playerId)
    }

    // Returns `true` if we are moving a player as part of modifying their state.
    // In that case, we should open the player editor on the "State" panel again.
    fun shouldOpenPlayerEditorOnState(playerId: PlayerId): Boolean = (playerEditorOpenOnStatePlayerId == playerId)
    fun clearPlayerEditorOpenOnState(playerId: PlayerId) {
        if (playerEditorOpenOnStatePlayerId == playerId) {
            playerEditorOpenOnStatePlayerId = null
        }
    }

    val logsBackgroundColor: Flow<PanelBackground> = combine(
        pitchBackground,
        SETTINGS_MANAGER.observeBooleanKey(SettingsKeys.JERVIS_UI_USE_PITCH_WEATHER_AS_GAME_BACKGROUND_VALUE, false)
    ) { field, useFieldBackground ->
        when (useFieldBackground) {
            true -> field.panelBackground
            false -> PitchDetails.NICE.panelBackground
        }
    }

    val selectedPlayersInUi = mutableListOf<PlayerId>()
    var isGameStatusBoxEnabled = mutableStateOf(false)
    var gameStatusTitle = mutableStateOf<String?>(null)
    var gameStatusBoxTitle = mutableStateOf("")

    init {
        actionProvider.updateSharedData(sharedPitchData)
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
        modelScope.launch {
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
            .launchIn(modelScope)
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
     *
     * [attachToMenu] should only be `false` for a game that exists to be looked
     * at rather than played, such as the preview on the challenge details page.
     * Those must not become "the current game" as far as the menu is concerned,
     * or undo and the game menu keep pointing at them once they are discarded.
     */
    suspend fun initialize(density: Density, attachToMenu: Boolean = true) {
        loadingMessages.value = "Initializing icons"
        IconFactory.initialize(density, homeTeam, awayTeam)
        loadingMessages.value = "Initializing sounds"
        SoundManager.initialize()
        if (attachToMenu) {
            menuViewModel.uiState = uiState
        }
        // Also starts the rules engine, so the controller must not have been
        // started beforehand.
        uiState.startGameEventLoop()
        if (attachToMenu) {
            onEngineInitialized()
        }
        loadingMessages.value = "Starting game"
        isLoaded.value = true
    }

    /**
     * Releases the game and everything observing it.
     *
     * Voyager never calls this as the model is not registered with a `Screen`, so
     * whoever built the game has to.
     *
     * Safe to call more than once.
     */
    override fun onDispose() {
        onGameStopped()
        uiState.stopGameEventLoop()
        modelScope.cancel()
    }

    fun updateFieldViewData(fieldLayoutCoordinates: LayoutCoordinates, borderSize: Float) {
        val offset = fieldLayoutCoordinates.localToWindow(Offset.Zero)
        val fieldPositionData = PitchViewData(
            JervisTheme.windowSizePx,
            fieldLayoutCoordinates.size,
            borderSize,
            IntOffset(offset.x.roundToInt(), offset.y.roundToInt()),
            gameController.rules.pitchWidth,
            gameController.rules.pitchHeight
        )
        pitchViewData.value = fieldPositionData
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
