package com.jervisffb.ui.game.viewmodel

import com.jervisffb.engine.common.procedures.StartOfDriveSequence
import com.jervisffb.engine.model.CoachType
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerDogoutState
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.locations.Dogout
import com.jervisffb.engine.utils.safeTryEmit
import com.jervisffb.ui.game.UiGameController
import com.jervisffb.ui.game.UiGameSnapshot
import com.jervisffb.ui.game.model.GuardedAction
import com.jervisffb.ui.game.model.UiAction
import com.jervisffb.ui.game.model.UiPlayerCard
import com.jervisffb.ui.game.model.UiSidebarPlayer
import com.jervisffb.ui.game.state.ReplayController
import com.jervisffb.ui.menu.GameScreenModel
import com.jervisffb.ui.menu.LocalPitchDataWrapper
import com.jervisffb.ui.menu.TeamActionMode
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn

data class ButtonData(
    val title: String,
    val onClick: () -> Unit
)

/**
 * A single titled group of players in the dogout, e.g. "Knocked Out" or
 * "Reserves".
 */
data class SidebarSection(
    val title: String,
    val players: Flow<UiSidebarData>,
)

/**
 * Class representing the sidebar players for a specific part of the dogout,
 * including their location.
 *
 * We support two modes:
 * - Compact: Players are laid out and placed next to each other based on how
 *   many players that are currently showing.
 * - Fixed: Players are given a fixed position from the start and will always
 *   be in the same position. Regardless of intermediate players being removed.
 *
 * TODO We might want to show injured players in the order it happened rather
 *  than by their player number. This will require extra metadata in the model.
 */
@ConsistentCopyVisibility
data class UiSidebarData private constructor(
    val players: ImmutableList<UiSidebarPlayer?>,
): ImmutableList<UiSidebarPlayer?> by players {
    constructor(players: List<UiSidebarPlayer?>): this(players.toImmutableList())
    companion object {

        fun compact(players: List<UiSidebarPlayer?>): UiSidebarData {
            return UiSidebarData(players.sortedBy { it?.number })
        }

        fun fixed(team: Team, players: List<UiSidebarPlayer>): UiSidebarData {

            // Create compact list using all players
            val sortedTeamPlayers = team
                .filter { !it.missNextGame }
                .sortedBy { it.number }

            // Make it easy to find the correct player
            val uiPlayers = players.associateBy { player -> player.number }

            // Create the fixed list. Empty spaces are marked with `null`
            val fixedListPlayers = Array<UiSidebarPlayer?>(sortedTeamPlayers.size) { index ->
                sortedTeamPlayers[index].let {
                    uiPlayers[it.number]
                }
            }

            return UiSidebarData(fixedListPlayers.toList())
        }

        val DEFAULT = UiSidebarData(
            players = emptyList(),
        )
    }
}

class SidebarViewModel(
    val gameViewModel: GameScreenModel,
    private val menuViewModel: MenuViewModel,
    private val uiState: UiGameController,
    val sharedPitchData: LocalPitchDataWrapper,
    val team: Team,
    // Channel specifically for handling the Player Stat Card being visible or not
    private val hoverPlayerChannel: MutableSharedFlow<Player?>,
) {

    // The original FUMBBL image is 145f/430f, but we need to stretch to make it fit the pitch image.
    val aspectRatio: Float = 410f/1030f // 145f/430f

    // TODO Right now Setup Buttons are in the Game Status Message bar. It would probably be slightly
    //  better to have them show in the dogout (in the bottom), as that area is more flexible.
    //  This code is first step towards doing that, but hasn't been wired up yet.
    //  See `SidebarButtons` composable in Sidebar.kt for the other half of this.
    private val _buttons: Flow<List<ButtonData>> = uiState.uiStateFlow.map { uiSnapshot ->
        // TODO Find a better way to detect game mode
        if (uiState.actionProvider is ReplayController) return@map emptyList()
        val buttons = mutableListOf<ButtonData>()

        // Check if this team is during the setup phase. For now, we just hard-code a few examples
        // This is mostly for WASM, iOS as JVM has a proper menu bar. This should be reworked
        // once we add proper menu support on WASM/iOS.
        // Also, consider moving this logic into decorators somehow.
        val setupKickingTeam = uiSnapshot.stack.containsNode(StartOfDriveSequence.SetupKickingTeam) && uiSnapshot.game.kickingTeam == team
        val setupReceivingTeam = uiSnapshot.stack.containsNode(StartOfDriveSequence.SetupReceivingTeam) && uiSnapshot.game.receivingTeam == team
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
    private val dogoutFlow: SharedFlow<Pair<UiGameSnapshot, List<UiSidebarPlayer>>> = combine(
        uiState.uiStateFlow,
        gameViewModel.isMovePlayersFreely,
    ) { snapshot: UiGameSnapshot, isMovePlayersFreelyMode: Boolean ->
        val list = if (team.isHomeTeam()) {
            snapshot.game.homeTeam.filter { player ->
                player.location == Dogout && snapshot.players[player.id]?.location == Dogout
            }
        } else {
            snapshot.game.awayTeam.filter { player ->
                player.location == Dogout && snapshot.players[player.id]?.location == Dogout
            }
        }
        val newList = list.map { player ->
            snapshot.players[player.id]?.let {
                UiSidebarPlayer(
                    when (isMovePlayersFreelyMode) {
                        true -> {
                            val playerRef = snapshot.game.getPlayerById(player.id)
                            it.copy(selectedAction = { model, selected -> model.beginMovePlayer(playerRef) })
                        }
                        false -> it
                    },
                    UiPlayerTransientData(
                        onHover = { hoverOver(player) },
                        onHoverExit = { hoverExit() },
                        onSecondaryClick = { gameViewModel.showPlayerContextMenu(player.id) }
                    )
                )
            } ?: error("Cannot find player: $player.id}")
        }
        Pair(snapshot, newList)
    }.shareIn(menuViewModel.uiScope, SharingStarted.Eagerly, 1)

    val dogoutAction: Flow<Pair<Boolean, GuardedAction?>> = uiState.uiStateFlow.map {
        when (team.isHomeTeam()) {
            true -> it.homeDogoutLooksSelectable to it.homeDogoutOnClickAction
            false -> it.awayDogoutLooksSelectable to it.awayDogoutOnClickAction
        }
    }

    val playerStatCardFlow: Flow<UiPlayerCard?> = gameViewModel.playerStatCardFlowFor(team)

    /** Standard Reserves keep their position when moving between the pitch and the dogout. */
    val reserves: Flow<UiSidebarData> = dogoutFlow
        .map { (_, players) ->
            val reservePlayers = players.filter { it.player.state == PlayerDogoutState.RESERVE }
            UiSidebarData.fixed(team, reservePlayers)
        }

    val knockedOut: Flow<UiSidebarData> = mapToCompactView(PlayerDogoutState.KNOCKED_OUT)
    val badlyHurt: Flow<UiSidebarData> = mapToCompactView(PlayerDogoutState.BADLY_HURT)
    val seriousInjuries: Flow<UiSidebarData> = mapToCompactView(
        PlayerDogoutState.SERIOUSLY_HURT,
        PlayerDogoutState.SERIOUS_INJURY,
        PlayerDogoutState.LASTING_INJURY,
    )
    val dead: Flow<UiSidebarData> = mapToCompactView(PlayerDogoutState.DEAD)
    val banned: Flow<UiSidebarData> = mapToCompactView(PlayerDogoutState.BANNED)
    val special: Flow<UiSidebarData> = mapToCompactView(
        PlayerDogoutState.FAINTED,
        PlayerDogoutState.DODGY_SNACK,
    )

    /** The injury sections of the dogout, in the order they should be rendered. */
    val injurySections: List<SidebarSection> = listOf(
        SidebarSection("Knocked Out", knockedOut),
        SidebarSection("Badly Hurt", badlyHurt),
        SidebarSection("Seriously Injured", seriousInjuries),
        SidebarSection("Dead", dead),
        SidebarSection("Banned", banned),
        SidebarSection("Special", special),
    )

    fun hoverOver(player: Player) {
        hoverPlayerChannel.safeTryEmit(player)
    }

    fun hoverExit() {
        hoverPlayerChannel.safeTryEmit(null)
    }

    fun dismissFixedCard() = gameViewModel.dismissPlayerStatCard()

    private fun mapToCompactView(vararg states: PlayerState): Flow<UiSidebarData> {
        return dogoutFlow
            .map { (_, players) ->
                val matchingPlayers = players.filter { states.contains(it.state) }
                UiSidebarData.compact(matchingPlayers)
            }
    }
}
