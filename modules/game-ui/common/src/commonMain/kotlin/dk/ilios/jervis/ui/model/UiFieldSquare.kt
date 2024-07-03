package dk.ilios.jervis.ui.model

import dk.ilios.jervis.model.FieldSquare

class UiFieldSquare(
    override val model: FieldSquare,
    val ball: UiBall? = null,
    val player: UiPlayer? = null,
    val onSelected: (() -> Unit)? = null,
): UiModel<FieldSquare>
