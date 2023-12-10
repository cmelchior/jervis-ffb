package dk.ilios.jervis.model

class TeamPlayers : Collection<Player> {

    private val noToPlayer = mutableMapOf<PlayerNo, Player>()

    fun add(player: Player) {
        noToPlayer[player.number]
    }

    operator fun get(playerNo: PlayerNo): Player? = noToPlayer[playerNo]
    override val size: Int = noToPlayer.size
    override fun isEmpty(): Boolean = noToPlayer.isEmpty()
    override fun iterator(): Iterator<Player> = noToPlayer.values.iterator()
    override fun containsAll(elements: Collection<Player>): Boolean = noToPlayer.values.containsAll(elements)
    override fun contains(element: Player): Boolean = noToPlayer.containsValue(element)
}