package dk.ilios.jervis.actions

sealed interface ActionDescriptor
data object ContinueWhenReady: ActionDescriptor
data object EndTurnWhenReady: ActionDescriptor
data object RollD2: ActionDescriptor
data class SelectAvailableSpace(val x: UInt, val y: UInt): ActionDescriptor
