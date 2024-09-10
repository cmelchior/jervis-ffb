package dk.ilios.jervis.model.context

import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.Team

/**
 * Marker interface for classes that is used to track state related to a specific [Procedure].
 *
 * There is no clear rule for when state should be put in a [ProcedureContext] rather than being
 * stored in the main [Game], [Team] or [Player] classes. But a general guideline is that any
 * state that is only relevant for a specific procedure probably belongs in a context class, while
 * state that is relevant to multiple procedures should be lifted to the global scope.
 *
 * @see [Game.getContext]
 * @see [Game.assertContext]
 * @see [Game.contexts]
 * @see [dk.ilios.jervis.commands.RemoveContext]
 */
interface ProcedureContext
