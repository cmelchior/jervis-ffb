package dk.ilios.jervis.model




/**
 * Wrapper for optionals with an API specifically tailored towards accessing context
 */
//@Serializable(with = OptionalSerializer::class)
class Optional<T>(var value: T?)
