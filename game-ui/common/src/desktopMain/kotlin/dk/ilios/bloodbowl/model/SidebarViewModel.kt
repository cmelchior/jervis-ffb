package dk.ilios.bloodbowl.model

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateList
import dk.ilios.bloodbowl.ui.model.UIPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class SidebarView {
    RESERVES, INJURIES
}

class SidebarViewModel {
    private val _view = MutableStateFlow(SidebarView.RESERVES)
    private val _reserveCount = MutableStateFlow<Int?>(null)
    private val _injuriesCount = MutableStateFlow<Int?>(null)

    fun view(): StateFlow<SidebarView> = _view
    fun reserveCount(): StateFlow<Int?> = _reserveCount
    fun reserves(): SnapshotStateList<UIPlayer> = mutableStateListOf()
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