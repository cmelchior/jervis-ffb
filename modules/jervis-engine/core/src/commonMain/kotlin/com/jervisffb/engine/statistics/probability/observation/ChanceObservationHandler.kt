package com.jervisffb.engine.statistics.probability.observation

/**
 * Marker for procedures with an element of chance.
 *
 * By using this interface, classes promise they will create a
 * [AddChanceObservation] or [UpdateChanceObservation] command for any chance
 * event they control. This allows the engine to track the chance events and
 * their outcomes.
 *
 * Procedures with chance that do not do this end up creating a
 * [ChanceObservation.UnstructuredAction] which will short-circuit any
 * probability calculations done on a chain of events where it is included.
 */
interface ChanceObservationHandler
