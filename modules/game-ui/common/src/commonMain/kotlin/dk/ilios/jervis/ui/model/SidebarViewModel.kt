package dk.ilios.jervis.ui.model

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.PlayerState
import dk.ilios.jervis.model.Team
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

enum class SidebarView {
    RESERVES, INJURIES
}

class SidebarViewModel(val team: Team) {
    // Image is 145f/430f, but we need to stretch to make it fit the field image.
    val aspectRatio: Float = 152.42f/452f

    private val _view = MutableStateFlow(SidebarView.RESERVES)
    private val _reserveCount = MutableStateFlow<Int?>(null)
    private val _injuriesCount = MutableStateFlow<Int?>(null)

    fun view(): StateFlow<SidebarView> = _view
    fun reserveCount(): StateFlow<Int?> = _reserveCount
    fun reserves(): Flow<List<Player>> {
        return team.players.dogoutFlow.map { players: List<Player> ->
            players
                .filter { it.state == PlayerState.STANDING }
                .sortedBy { it.number }
        }
    }
    fun knockedOut(): SnapshotStateList<UIPlayer> = mutableStateListOf()
    fun badlyHurt(): SnapshotStateList<UIPlayer> = mutableStateListOf()
    fun seriousInjuries(): SnapshotStateList<UIPlayer> = mutableStateListOf()
    fun dead(): SnapshotStateList<UIPlayer> = mutableStateListOf()
    fun injuriesCount(): StateFlow<Int?> = _injuriesCount

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