package com.jervisffb.ui.game.viewmodel

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.isOnAwayTeam
import com.jervisffb.engine.model.locations.DogOut
import com.jervisffb.engine.rules.bb2020.procedures.GameDrive
import com.jervisffb.engine.rules.builder.GameType
import com.jervisffb.engine.utils.safeTryEmit
import com.jervisffb.ui.game.UiGameController
import com.jervisffb.ui.game.UiGameSnapshot
import com.jervisffb.ui.game.model.UiPlayer
import com.jervisffb.ui.game.model.UiPlayerCard
import com.jervisffb.ui.game.state.ReplayActionProvider
import com.jervisffb.ui.menu.TeamActionMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn

data class ButtonData(
    val title: String,
    val onClick: () -> Unit
)

enum class SidebarView {
    RESERVES,
    INJURIES,
}

class SidebarViewModel(
    private val menuViewModel: MenuViewModel,
    private val uiState: UiGameController,
    val team: Team,
    private val hoverPlayerChannel: MutableSharedFlow<Player?>,
) {

    // The image is 145f/430f, but we need to stretch to make it fit the field image.
    val aspectRatio: Float = 145f/430f // 152.42f / 452f

    private val _view = MutableStateFlow(SidebarView.RESERVES)
    private val _buttons: Flow<List<ButtonData>> = uiState.uiStateFlow.map { uiSnapshot ->
        // TODO Find a better way to detect game mode
        if (uiState.actionProvider is ReplayActionProvider) return@map emptyList()

        val buttons = mutableListOf<ButtonData>()

        // Check if this team can "End Setup"
        if (team.isHomeTeam()) {
            buttons.addAll(uiSnapshot.homeTeamActions)
        } else if (team.isAwayTeam()) {
            buttons.addAll(uiSnapshot.awayTeamActions)
        }

        // Check if this team is during the setup phase. For now, we just hard-code a few examples
        // This is mostly for WASM, iOS as JVM has a proper menu bar. This should be reworked
        // once we add proper menu support on WASM/iOS.
        // Also, consider moving this logic into decorators somehow.
        val setupKickingTeam = uiSnapshot.stack.containsNode(GameDrive.SetupKickingTeam) && uiSnapshot.game.kickingTeam == team
        val setupReceivingTeam = uiSnapshot.stack.containsNode(GameDrive.SetupReceivingTeam) && uiSnapshot.game.receivingTeam == team
        val teamControlledByClient = when (uiState.uiMode) {
            TeamActionMode.HOME_TEAM -> team.isHomeTeam()
            TeamActionMode.AWAY_TEAM -> team.isAwayTeam()
            TeamActionMode.ALL_TEAMS -> true
        }
        if ((setupReceivingTeam || setupKickingTeam) && teamControlledByClient) {
            val availableSetups = when (uiState.rules.gameType) {
                GameType.STANDARD -> Setups.standardSetups
                GameType.BB7 -> Setups.sevensSetups
                GameType.DUNGEON_BOWL -> TODO("Dungeon Bowl setups not implemented yet.")
                GameType.GUTTER_BOWL -> TODO("Gutter Bowl setups not implemented yet.")
            }
            availableSetups.keys.forEach { setup ->
                buttons.add(ButtonData(setup, onClick = { menuViewModel.loadSetup(setup)}))
            }
        }
        buttons
    }

    // Expose Dogout information as a separate flow
    private val dogoutFlow: SharedFlow<List<Player>> = uiState.uiStateFlow.map {
        if (team.isHomeTeam()) {
            it.game.homeTeam.filter { player -> player.location == DogOut }
        } else {
            it.game.awayTeam.filter { player -> player.location == DogOut }
        }
    }.shareIn(menuViewModel.backgroundContext, SharingStarted.Lazily)

    // Player being hovered over.
    // All of these will be shown on the away team location, except when hovering over
    // the away team dugout, which should be shown in the home team
    fun hoverPlayer(): Flow<UiPlayerCard?> =
        hoverPlayerChannel.distinctUntilChanged { old, new ->
            old?.id == new?.id
        }
            .filter { player ->
                if (player == null) return@filter true
                when (team.isHomeTeam()) {
                    true -> player.isOnAwayTeam() && player.location is DogOut
                    false -> !(player.isOnAwayTeam() && player.location is DogOut)
                }
            }
            .distinctUntilChanged { old, new -> old?.id == new?.id }
            .map { player ->
                player?.let { UiPlayerCard(it) }
            }

    fun view(): StateFlow<SidebarView> = _view

    fun actionButtons(): Flow<List<ButtonData>> = _buttons

    fun reserveCount(): Flow<Int> = dogoutFlow.map {
        // Available players in the Dogout should only have this state
        it.count { player -> player.state == PlayerState.RESERVE }
    }

    fun reserves(): Flow<List<UiPlayer>> {
        return dogoutFlow
            .map { players: List<Player> ->
                players
                    .filter { it.state == PlayerState.RESERVE }
                    .sortedBy { it.number }
            }.combine(uiState.uiStateFlow) { players: List<Player>, uiState: UiGameSnapshot ->
                val dogoutActions = uiState.dogoutActions
                players.map {
                    val playerAction = dogoutActions[it.id]
                    UiPlayer(
                        it,
                        playerAction,
                        onHover = { hoverOver(it) },
                        onHoverExit = { hoverExit() }
                    )
                }
            }
    }

    fun knockedOut(): Flow<List<UiPlayer>> = mapTo(PlayerState.KNOCKED_OUT, dogoutFlow)

    fun badlyHurt(): Flow<List<UiPlayer>> = mapTo(PlayerState.BADLY_HURT, dogoutFlow)

    fun seriousInjuries(): Flow<List<UiPlayer>> = mapTo(listOf(PlayerState.SERIOUSLY_HURT, PlayerState.SERIOUS_INJURY, PlayerState.LASTING_INJURY), dogoutFlow)

    fun dead(): Flow<List<UiPlayer>> = mapTo(PlayerState.DEAD, dogoutFlow)

    fun banned(): Flow<List<UiPlayer>> = mapTo(PlayerState.BANNED, dogoutFlow)

    fun special(): Flow<List<UiPlayer>> = mapTo(PlayerState.FAINTED, dogoutFlow)

    fun injuriesCount(): Flow<Int> = dogoutFlow.map {
        // Available players should be in RESERVE, all others should be treated
        // as some kind of injury.
        it.count { player -> player.state != PlayerState.RESERVE}
    }

    fun hoverOver(player: Player) {
        hoverPlayerChannel.safeTryEmit(player)
    }

    fun hoverExit() {
        hoverPlayerChannel.safeTryEmit(null)
    }

    fun toggleToReserves() {
        _view.value = SidebarView.RESERVES
    }

    fun toggleInjuries() {
        _view.value = SidebarView.INJURIES
    }

    private fun mapTo(states: List<PlayerState>, dogoutFlow: SharedFlow<List<Player>>): Flow<List<UiPlayer>> {
        return dogoutFlow.map { players ->
            players.filter { states.contains(it.state) }
        }.map { players ->
            players.map { UiPlayer(it, selectAction = null, onHover = { hoverOver(it) }, onHoverExit = { hoverExit() }) }
        }
    }

    private fun mapTo(state: PlayerState, dogoutFlow: SharedFlow<List<Player>>): Flow<List<UiPlayer>> {
        return mapTo(listOf(state), dogoutFlow)
    }
}
