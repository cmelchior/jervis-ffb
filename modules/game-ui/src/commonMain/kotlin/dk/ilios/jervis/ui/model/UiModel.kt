package dk.ilios.jervis.ui.model

/**
 * Base interface for all UI classes that grant access to some underlying model information
 */
interface UiModel<T : Any> {
    // Normally do not use this. Only provide it here for now
    // Changes here will not trigger recomposition because the wrapper object is the same ;/
    val model: T
}
