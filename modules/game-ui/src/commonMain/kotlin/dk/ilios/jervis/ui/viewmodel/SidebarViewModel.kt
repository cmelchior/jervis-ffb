package dk.ilios.jervis.ui.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import dk.ilios.jervis.actions.PlayerSelected
import dk.ilios.jervis.model.DogOut
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.PlayerState
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.model.isOnAwayTeam
import dk.ilios.jervis.ui.model.UiPlayer
import dk.ilios.jervis.ui.model.UiPlayerCard
import dk.ilios.jervis.utils.safeTryEmit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

enum class SidebarView {
    RESERVES,
    INJURIES,
}

class SidebarViewModel(
    private val uiActionFactory: UiActionFactory,
    private val team: Team,
    private val hoverPlayerChannel: MutableSharedFlow<Player?>,
) {
    // Image is 145f/430f, but we need to stretch to make it fit the field image.
    val aspectRatio: Float = 152.42f / 452f

    private val _view = MutableStateFlow(SidebarView.RESERVES)
    private val _reserveCount = MutableStateFlow<Int?>(null)
    private val _injuriesCount = MutableStateFlow<Int?>(null)

    init {
        // How to handle both user input and
        //
    }

    // Player being hovered over.
    // All of these will be shown on the away team location, except when hovering over
    // the away team dugout, which should be shown in the home team
    fun hoverPlayer(): Flow<UiPlayerCard?> =
        hoverPlayerChannel.distinctUntilChanged { old, new ->
            old?.id == new?.id
        }.filter { player ->
            if (player == null) return@filter true
            when (team.isHomeTeam()) {
                true -> player.isOnAwayTeam() && player.location is DogOut
                false -> !(player.isOnAwayTeam() && player.location is DogOut)
            }
        }
            .distinctUntilChanged()
            .map { player ->
                player?.let { UiPlayerCard(it) }
            }

    fun view(): StateFlow<SidebarView> = _view

    fun reserveCount(): StateFlow<Int?> = _reserveCount

    fun reserves(): Flow<List<UiPlayer>> {
        return team.dogoutFlow.map { players: List<Player> ->
            players
                .filter { it.state == PlayerState.STANDING }
                .sortedBy { it.number }
        }.combine(uiActionFactory.fieldActions) { e1: List<Player>, e2: UserInput ->
            val userInput = e2 as? SelectPlayerInput
            val selectablePlayers = userInput?.actions?.associateBy { (it as PlayerSelected).getPlayer(team.game) } ?: emptyMap()
            e1.map {
                val selectAction =
                    selectablePlayers[it]?.let {
                        { uiActionFactory.userSelectedAction(it) }
                    }
                UiPlayer(it, selectAction, onHover = { hoverOver(it) })
            }
        }
    }

    fun knockedOut(): SnapshotStateList<UiPlayer> = mutableStateListOf()

    fun badlyHurt(): SnapshotStateList<UiPlayer> = mutableStateListOf()

    fun seriousInjuries(): SnapshotStateList<UiPlayer> = mutableStateListOf()

    fun dead(): SnapshotStateList<UiPlayer> = mutableStateListOf()

    fun injuriesCount(): StateFlow<Int?> = _injuriesCount

    fun hoverOver(player: Player) {
        hoverPlayerChannel.safeTryEmit(player)
    }

    init {
        _reserveCount.value = 12
        _injuriesCount.value = 0
    }

    fun toggleReserves() {
        _view.value = SidebarView.RESERVES
    }

    fun toggleInjuries() {
        _view.value = SidebarView.INJURIES
    }
}
