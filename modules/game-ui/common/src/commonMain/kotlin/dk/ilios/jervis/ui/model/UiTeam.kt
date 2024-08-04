package dk.ilios.jervis.ui.model

import dk.ilios.jervis.model.Team

class UiTeam(override val model: Team) : Collection<UiPlayer>, UiModel<Team> {
    override val size: Int = model.size

    override fun isEmpty(): Boolean = model.isEmpty()

    override fun iterator(): Iterator<UiPlayer> = this.iterator()

    override fun containsAll(elements: Collection<UiPlayer>): Boolean = this.containsAll(elements)

    override fun contains(element: UiPlayer): Boolean = this.contains(element)
}
