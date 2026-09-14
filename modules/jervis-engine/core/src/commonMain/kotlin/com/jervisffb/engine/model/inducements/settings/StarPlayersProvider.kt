package com.jervisffb.engine.model.inducements.settings

import com.jervisffb.engine.rules.common.roster.StarPlayerPosition

/**
 * Interface for inducements that hire Star Players.
 *
 * A Star Player is not part of any team's roster, so anything that needs to
 * resolve one has to go through the ruleset's inducements, but since we want to
 * keep the `core` module as free of any inducement logic as possible, the
 * concept of "star player inducments" are restricted to `rules-common` and
 * friends. This interface bridges that gap so classes in the `core` module can
 * still access star players, which is needed to e.g. serialize teams.
 */
interface StarPlayersProvider {
    val players: List<StarPlayerPosition>
}
