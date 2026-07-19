package com.jervisffb.engine.rules.common.tables

/**
 * A result on the Kick-Off Table.
 *
 * This is the `core`-resident abstraction for a kick-off event. The concrete
 * catalog of events (and the procedures that resolve them) lives in the
 * `rules-common` module as [KickOffEventResult], so that those procedures do
 * not have to reside in `core`.
 *
 * [KickOffEventResult]: com.jervisffb.engine.rules.common.tables.KickOffEventResult
 */
interface KickOffEvent : TableResult
