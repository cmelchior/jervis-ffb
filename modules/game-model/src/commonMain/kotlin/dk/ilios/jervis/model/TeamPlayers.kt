package dk.ilios.jervis.model

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class TeamPlayers(val team: Team) : Collection<Player> {

    private val noToPlayer = mutableMapOf<PlayerNo, Player>()

    fun add(player: Player) {
        player.team = this
        noToPlayer[player.number] = player
    }
    operator fun get(playerNo: PlayerNo): Player? = noToPlayer[playerNo]
    override val size: Int = noToPlayer.size
    override fun isEmpty(): Boolean = noToPlayer.isEmpty()
    override fun iterator(): Iterator<Player> = noToPlayer.values.iterator()
    override fun containsAll(elements: Collection<Player>): Boolean = noToPlayer.values.containsAll(elements)
    override fun contains(element: Player): Boolean = noToPlayer.containsValue(element)

    fun notifyDogoutChange() {
        val playersInDogout = noToPlayer.values.filter { it.location == DogOut }
        _dogoutState.tryEmit(playersInDogout)
    }
    private val _dogoutState = MutableSharedFlow<List<Player>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val dogoutFlow: SharedFlow<List<Player>> = _dogoutState
}