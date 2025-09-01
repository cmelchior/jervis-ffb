package com.jervisffb.ui.game.viewmodel

import com.jervisffb.engine.model.CoachType
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.isOnAwayTeam
import com.jervisffb.engine.model.locations.DogOut
import com.jervisffb.engine.rules.bb2020.procedures.GameDrive
import com.jervisffb.engine.utils.safeTryEmit
import com.jervisffb.ui.game.UiGameController
import com.jervisffb.ui.game.UiGameSnapshot
import com.jervisffb.ui.game.model.UiPlayerCard
import com.jervisffb.ui.game.model.UiSidebarPlayer
import com.jervisffb.ui.game.state.ReplayActionProvider
import com.jervisffb.ui.menu.LocalFieldDataWrapper
import com.jervisffb.ui.menu.TeamActionMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn

data class ButtonData(
    val title: String,
    val onClick: () -> Unit
)

class SidebarViewModel(
    private val menuViewModel: MenuViewModel,
    private val uiState: UiGameController,
    val sharedFieldData: LocalFieldDataWrapper,
    val team: Team,
    // Channel specifically for handling the Player Stat Card being visible or not
    private val hoverPlayerChannel: MutableSharedFlow<Player?>,
) {

    // The original FUMBBL image is 145f/430f, but we need to stretch to make it fit the field image.
    val aspectRatio: Float = 410f/1030f // 145f/430f

    private val _buttons: Flow<List<ButtonData>> = uiState.uiStateFlow.map { uiSnapshot ->
        // TODO Find a better way to detect game mode
        if (uiState.actionProvider is ReplayActionProvider) return@map emptyList()
        val buttons = mutableListOf<ButtonData>()

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
        if ((setupReceivingTeam || setupKickingTeam) && teamControlledByClient && team.coach.type == CoachType.HUMAN) {
            val availableSetups = Setups.getSetups(uiState.rules.gameType)
            availableSetups.forEach { setup ->
                buttons.add(ButtonData(setup.name, onClick = { menuViewModel.loadSetup(setup)}))
            }
        }
        buttons
    }

    // Expose Dogout information as a separate flow
    private val dogoutFlow: SharedFlow<Pair<UiGameSnapshot, List<UiSidebarPlayer>>> = uiState.uiStateFlow.map { snapshot: UiGameSnapshot ->
        val list = if (team.isHomeTeam()) {
            snapshot.game.homeTeam.filter { player -> player.location == DogOut }
        } else {
            snapshot.game.awayTeam.filter { player -> player.location == DogOut }
        }
        val newList = list.map { player ->
            snapshot.players[player.id]?.let {
                UiSidebarPlayer(
                    it,
                    UiPlayerTransientData(onHover = { hoverOver(player) }, onHoverExit = { hoverExit() })
                )
            } ?: error("Cannot find player: $player.id}")
        }
        Pair(snapshot, newList)
    }.shareIn(menuViewModel.uiScope, SharingStarted.Eagerly, 1)

    fun dogoutAction(): Flow<(() -> Unit)?> = uiState.uiStateFlow.map {
        when (team.isHomeTeam()) {
            true -> it.homeDogoutOnClickAction
            false -> it.awayDogoutOnClickAction
        }
    }

    // Player being hovered over.
    // All of these will be shown on the away team location, except when hovering over
    // the away team dugout, which should be shown in the home team
    fun hoverPlayer(): Flow<UiPlayerCard?> =
        hoverPlayerChannel
            .distinctUntilChanged { old, new ->
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

    fun reserves(): Flow<List<UiSidebarPlayer>> {
        return dogoutFlow
            .map { (_, players) ->
                players
                    .filter { it.player.state == PlayerState.RESERVE }
                    .sortedBy { it.player.number }
            }
    }

    fun knockedOut(): Flow<List<UiSidebarPlayer>> = mapTo(PlayerState.KNOCKED_OUT, dogoutFlow)

    fun badlyHurt(): Flow<List<UiSidebarPlayer>> = mapTo(PlayerState.BADLY_HURT, dogoutFlow)

    fun seriousInjuries(): Flow<List<UiSidebarPlayer>> = mapTo(listOf(PlayerState.SERIOUSLY_HURT, PlayerState.SERIOUS_INJURY, PlayerState.LASTING_INJURY), dogoutFlow)

    fun dead(): Flow<List<UiSidebarPlayer>> = mapTo(PlayerState.DEAD, dogoutFlow)

    fun banned(): Flow<List<UiSidebarPlayer>> = mapTo(PlayerState.BANNED, dogoutFlow)

    fun special(): Flow<List<UiSidebarPlayer>> = mapTo(PlayerState.FAINTED, dogoutFlow)

    fun hoverOver(player: Player) {
        hoverPlayerChannel.safeTryEmit(player)
    }

    fun hoverExit() {
        hoverPlayerChannel.safeTryEmit(null)
    }

    private fun mapTo(states: List<PlayerState>, dogoutFlow: SharedFlow<Pair<UiGameSnapshot, List<UiSidebarPlayer>>>): Flow<List<UiSidebarPlayer>> {
        return dogoutFlow
            .map { it.second }
            .map { playerList ->
                playerList.filter { uiPlayer ->
                    states.contains(uiPlayer.state)
                }
            }
    }

    private fun mapTo(state: PlayerState, dogoutFlow: SharedFlow<Pair<UiGameSnapshot, List<UiSidebarPlayer>>>): Flow<List<UiSidebarPlayer>> {
        return mapTo(listOf(state), dogoutFlow)
    }
}
